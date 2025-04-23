package controllers;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import models.Cart;
import models.Order;
import models.Product;
import services.CartService;
import services.OrderService;
import services.ProductService;
import test.MainFX;
import utils.CartStorage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class CartController {

    @FXML private TableView<Cart> cartTable;
    @FXML private TableColumn<Cart, String> colProduct;
    @FXML private TableColumn<Cart, Double> colPrice;
    @FXML private TableColumn<Cart, Integer> colQuantity;
    @FXML private TableColumn<Cart, Double> colTotal;
    @FXML private TableColumn<Cart, Void> colActions;
    @FXML private Label totalLabel;

    private final CartService cartService = new CartService();
    private final ProductService productService = new ProductService();

    @FXML
    public void initialize() {
        setupTable();
        loadCartItems();
    }

    private void setupTable() {
        colProduct.setCellValueFactory(data -> {
            try {
                Product p = productService.getProductById(data.getValue().getProductId());
                return new SimpleStringProperty(p.getProductName());
            } catch (Exception e) {
                return new SimpleStringProperty("Produit inconnu");
            }
        });
        colPrice.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrice()).asObject());
        colQuantity.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getProductQuantity()).asObject());
        colQuantity.setCellFactory(param -> new TableCell<>() {
            private final Spinner<Integer> quantitySpinner = new Spinner<>();
            private final Button modifierBtn = new Button("Modifier");

            {
                quantitySpinner.setPrefWidth(60);
                quantitySpinner.setEditable(true);
                modifierBtn.setPrefWidth(70);
                modifierBtn.setStyle("-fx-background-color: #0AAE61; -fx-text-fill: white;");
                modifierBtn.setOnAction(event -> {
                    Cart cart = getTableView().getItems().get(getIndex());
                    try {
                        int newQty = quantitySpinner.getValue();

                        if (newQty < 1) {
                            showAlert("La quantité doit être au minimum 1 !");
                            return;
                        }
                        Product p = new ProductService().getProductById(cart.getProductId());
                        if (newQty > p.getProductStock()) {
                            showAlert("Quantité demandée dépasse le stock disponible !");
                            return;
                        }

                        cart.setProductQuantity(newQty);
                        cart.setTotal(newQty * cart.getPrice());
                        new CartService().modifier(cart);
                        loadCartItems();
                    } catch (NumberFormatException e) {
                        showAlert("Veuillez entrer une quantité valide !");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            protected void updateItem(Integer qty, boolean empty) {
                super.updateItem(qty, empty);
                if (empty || qty == null) {
                    setGraphic(null);
                } else {
                    try {
                        Cart cart = getTableView().getItems().get(getIndex());
                        Product produit = productService.getProductById(cart.getProductId());
                        int stockMax = produit.getProductStock();

                        SpinnerValueFactory<Integer> valueFactory =
                                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, stockMax, qty);
                        quantitySpinner.setValueFactory(valueFactory);
                        quantitySpinner.setEditable(false);

                        setGraphic(new HBox(10, quantitySpinner, modifierBtn));
                    } catch (Exception e) {
                        e.printStackTrace();
                        setGraphic(null);
                    }
                }
            }
        });

        colTotal.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getTotal()).asObject());
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button supprimerBtn = new Button("Supprimer");

            {
                supprimerBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white;");
                supprimerBtn.setOnAction(event -> {
                    Cart cart = getTableView().getItems().get(getIndex());
                    try {
                        CartStorage.panier.removeIf(c -> c.getProductId() == cart.getProductId());
                        loadCartItems();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : supprimerBtn);
            }
        });

    }

    private void loadCartItems() {
        try {
            List<Cart> panier = CartStorage.panier;
            cartTable.getItems().setAll(panier);
            double total = panier.stream().mapToDouble(Cart::getTotal).sum();
            totalLabel.setText(String.format("%.2f TND", total));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onValider() {
        try {
            if (CartStorage.panier.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Votre panier est vide !").show();
                return;
            }
            OrderService orderService = new OrderService();
            CartService cartService = new CartService();

            Order order = new Order();
            order.setUserId(1);
            order.setCreationDate(java.time.LocalDateTime.now().toString());
            order.setStatus("en attente");
            order.setTotalPrice(CartStorage.panier.stream().mapToDouble(Cart::getTotal).sum());

            int orderId = orderService.ajouterEtRetournerId(order);

            for (Cart c : CartStorage.panier) {
                c.setOrderId(orderId);
                cartService.ajouter(c);
            }

            CartStorage.panier.clear();
            MainFX.chargerVue("/ConfirmationCommande.fxml");

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur lors de la validation !").show();
        }
    }

    @FXML
    private void onVider() {
        CartStorage.panier.clear();
        loadCartItems();
    }

    @FXML
    private void onAfficherCommandes() {
        try {
            MainFX.chargerVue("/AfficherCommandes.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onContinuer() {
        try {
            MainFX.chargerVue("/ProductList.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Quantité invalide");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
