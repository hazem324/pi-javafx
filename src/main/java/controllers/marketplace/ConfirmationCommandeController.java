package controllers.marketplace;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import test.MainFX;

import java.io.IOException;

public class ConfirmationCommandeController {
    @FXML
    private BorderPane bp;

    @FXML
    private void onRetourProduits() {
        try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplace/ProductList.fxml"));
        Parent root = loader.load();
        bp.setCenter(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onVoirCommandes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplace/AfficherCommandes.fxml"));
            Parent root = loader.load();
            bp.setCenter(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}