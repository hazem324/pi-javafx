package controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import models.Cart;
import models.Product;
import services.ProductService;
import tests.MainFX;
import utils.CartStorage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ProductListController {

    @FXML
    private VBox productContainer;

    private final ProductService productService = new ProductService();

    @FXML
    public void initialize() {
        try {
            List<Product> products = productService.getAllProducts();
            for (Product p : products) {
                productContainer.getChildren().add(createProductCard(p));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dcdcdc; -fx-border-radius: 8; -fx-background-radius: 8;");
        card.setPrefWidth(300);

        Label name = new Label(product.getProductName());
        name.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label desc = new Label(product.getProductDescription());
        desc.setWrapText(true);

        Label price = new Label("Prix : " + product.getProductPrice() + " TND");

        ImageView image = new ImageView();
        try {
            image.setImage(new Image(product.getImageUrl(), 120, 120, true, true));
        } catch (Exception e) {
            image.setImage(new Image("https://via.placeholder.com/120")); // fallback
        }

        HBox bottom = new HBox(10);
        Spinner<Integer> quantitySpinner = new Spinner<>(1, product.getProductStock(), 1);
        Button btnAdd = new Button("Ajouter au panier");

        btnAdd.setOnAction(evt -> {
            int qty = quantitySpinner.getValue();
            boolean produitExiste = false;

            for (Cart c : CartStorage.panier) {
                if (c.getProductId() == product.getId()) {
                    c.setProductQuantity(c.getProductQuantity() + qty);
                    c.setTotal(c.getProductQuantity() * c.getPrice());
                    produitExiste = true;
                    break;
                }
            }

            if (!produitExiste) {
                Cart item = new Cart();
                item.setProductId(product.getId());
                item.setProductQuantity(qty);
                item.setPrice(product.getProductPrice());
                item.setTotal(product.getProductPrice() * qty);
                item.setUserId(1); // à adapter
                item.setOrderId(0);
                CartStorage.panier.add(item);
            }

            try {
                MainFX.chargerVue("/CartView.fxml");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });



        bottom.getChildren().addAll(quantitySpinner, btnAdd);
        card.getChildren().addAll(image, name, desc, price, bottom);
        return card;
    }

    @FXML
    private void onVoirPanier() {
        try {
            MainFX.chargerVue("/CartView.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
