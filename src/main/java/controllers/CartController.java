package controllers;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import models.Cart;
import models.Order;
import models.Product;
import models.User;
import services.*;
import tests.MainFX;
import utils.CartStorage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CartController {

    @FXML private TableView<Cart> cartTable;
    @FXML private TableColumn<Cart, String> colProduct;
    @FXML private TableColumn<Cart, Double> colPrice;
    @FXML private TableColumn<Cart, Integer> colQuantity;
    @FXML private TableColumn<Cart, Double> colTotal;
    @FXML private TableColumn<Cart, Void> colActions;
    @FXML private Label totalLabel;

    @FXML
    private AnchorPane couponBoxContainer;

    private final CartService cartService = new CartService();
    private final ProductService productService = new ProductService();
    private String appliedCouponCode = null;

    private double total;
    private double initialTotal;
    @FXML
    private Button validerBtn;

    @FXML
    public void initialize() {
        try {
            FXMLLoader loader = new FXMLLoader(MainFX.class.getResource("/coupon_box.fxml"));
            Parent couponBox = loader.load();
            CouponController couponController = loader.getController();
            couponController.setCartController(this);
            couponBoxContainer.getChildren().setAll(couponBox);
        } catch (IOException e) {
            e.printStackTrace();
        }

        setupTable();
        loadCartItems();
    }

    public void applyDiscount(double percent, String code) {
        total = initialTotal * (1 - percent / 100);
        totalLabel.setText("Total avec réduction : " + String.format("%.2f", total) + " TND");
        appliedCouponCode = code;
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
            initialTotal = panier.stream().mapToDouble(Cart::getTotal).sum();
            total = initialTotal;
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

            // ✅ Récupérer l'utilisateur connecté (ex. ID=1 temporaire)
            User utilisateur = new UserService().getUserById(1);

            double total = CartStorage.panier.stream().mapToDouble(Cart::getTotal).sum();

            // ✅ Création de la commande
            OrderService orderService = new OrderService();
            CartService cartService = new CartService();

            Order order = new Order();
            order.setUserId(utilisateur.getId());
            order.setCreationDate(LocalDateTime.now().toString());
            order.setStatus("en attente");
            order.setTotalPrice(total);

            int orderId = orderService.ajouterEtRetournerId(order);

            // Copier les produits du panier avant clear()
            List<Cart> copiePanier = new ArrayList<>();
            for (Cart c : CartStorage.panier) {
                Cart copie = new Cart();
                copie.setProductId(c.getProductId());
                copie.setProductQuantity(c.getProductQuantity());
                copie.setPrice(c.getPrice());
                copie.setTotal(c.getTotal());
                copie.setUserId(utilisateur.getId());
                copie.setOrderId(orderId);
                cartService.ajouter(copie);
                copiePanier.add(copie);
            }
            CartStorage.panier.clear();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ConfirmationCommande.fxml"));
            Parent root = loader.load();

            ConfirmationCommandeController controller = loader.getController();
            String nomComplet = utilisateur.getFirstName() + " " + utilisateur.getLastName();
            controller.setCommandeData(orderId, nomComplet, utilisateur.getEmail());

            Stage stage = (Stage) cartTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Confirmation de commande");

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
            MainFX.chargerVue("/Product_List.fxml");
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

    public void cancelDiscount() {
        total = initialTotal;
        totalLabel.setText(String.format("%.2f TND", total));
    }

    @FXML
    private void onSuivreCommande() {
        try {
            MainFX.chargerVue("/TrackingView.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
