package controllers;

import javafx.fxml.FXML;
import test.MainFX;

import java.io.IOException;

public class ConfirmationCommandeController {

    @FXML
    private void onRetourProduits() {
        try {
            MainFX.chargerVue("/ProductList.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onVoirCommandes() {
        try {
            MainFX.chargerVue("/AfficherCommandes.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
