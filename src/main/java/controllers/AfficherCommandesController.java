package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Cart;
import models.Order;
import services.CartService;
import services.OrderService;
import services.ProductService;
import tests.MainFX;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherCommandesController {

    @FXML private TableView<Order> orderTable;
    @FXML private TableColumn<Order, Integer> colId;
    @FXML private TableColumn<Order, String> colDate;
    @FXML private TableColumn<Order, String> colStatut;
    @FXML private TableColumn<Order, Double> colTotal;
    @FXML private TableColumn<Order, Void> colActions;
    @FXML private TableColumn<Order, Void> colDetails;

    private final ObservableList<Order> orders = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        colStatut.setCellFactory(column -> new TableCell<>() {
            private final ComboBox<String> comboBox = new ComboBox<>(
                    FXCollections.observableArrayList("en attente", "en cours", "livrée")
            );

            {
                comboBox.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    String nouveauStatut = comboBox.getValue();
                    try {
                        new OrderService().modifierstatus(order.getId(), nouveauStatut);
                        order.setStatus(nouveauStatut);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });
            }

            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setGraphic(null);
                } else {
                    comboBox.setValue(statut);
                    setGraphic(comboBox);
                }
            }
        });
        colStatut.setCellValueFactory(new PropertyValueFactory<>("status"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        addSupprimerButton();
        addVoirDetailsButton();

        chargerCommandes();
    }

    private void chargerCommandes() {
        orders.clear();
        try {
            orders.addAll(new OrderService().recuperer());
            orderTable.setItems(orders);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addSupprimerButton() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Supprimer");

            {
                btn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                btn.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    try {
                        new OrderService().supprimer(order);
                        orders.remove(order);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void addVoirDetailsButton() {
        colDetails.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Voir détails");

            {
                btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                btn.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    StringBuilder details = new StringBuilder();

                    details.append("Date : ").append(order.getCreationDate()).append("\n");
                    details.append("Statut : ").append(order.getStatus()).append("\n");
                    details.append("Total : ").append(order.getTotalPrice()).append(" TND\n\n");

                    details.append("Produits :\n");

                    try {
                        CartService cartService = new CartService();
                        ProductService productService = new ProductService();
                        List<Cart> produits = cartService.getByOrderId(order.getId());

                        for (Cart c : produits) {
                            String nomProduit = productService.getProductById(c.getProductId()).getProductName();
                            details.append("- ").append(nomProduit).append(" (x").append(c.getProductQuantity()).append(")\n");
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                        details.append("[Erreur lors du chargement des produits]");
                    }

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Détails Commande");
                    alert.setHeaderText("Commande #" + order.getId());
                    alert.setContentText(details.toString());
                    alert.showAndWait();
                });

            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    @FXML
    private void onRetourPanier() {
        try {
            MainFX.chargerVue("/CartView.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
