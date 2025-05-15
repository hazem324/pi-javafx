package controllers.marketplace;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import models.Cart;
import services.CartService;
import utils.EmailWithAttachment;
import utils.PdfGenerator;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class ConfirmationCommandeController {

    private int orderId;
    private String nomClient;
    private String emailClient;

    @FXML
    private Button btnVoirCommandes;
    @FXML
    private Button btnRetourHome;

    public void setCommandeData(int orderId, String nomClient, String emailClient) {
        this.orderId = orderId;
        this.nomClient = nomClient;
        this.emailClient = emailClient;
    }

    @FXML
    private void onRetourHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketPlace/CartView.fxml"));

            Parent root = loader.load();

            // Récupérer le contrôleur de la page d'accueil
            controllers.HomePageController controller = loader.getController();

            Stage stage = (Stage) btnRetourHome.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Accueil");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du retour à l'accueil : " + e.getMessage());
        }
    }


    @FXML
    private void onVoirCommandes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplace/TrackingView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnVoirCommandes.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mes commandes");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors de l'affichage des commandes : " + e.getMessage());
        }
    }

    @FXML
    private void handleGenererPDF() {
        try {
            LocalDate date = LocalDate.now();
            List<Cart> produits = new CartService().getByOrderId(orderId);
            double total = produits.stream().mapToDouble(Cart::getTotal).sum();

            String pdfPath = PdfGenerator.generateInvoice(orderId, nomClient, emailClient, produits, total, date);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Facture générée");
            alert.setContentText("PDF créé : " + pdfPath);
            alert.showAndWait();

            java.awt.Desktop.getDesktop().open(new File(pdfPath));
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de la génération du PDF : " + e.getMessage());
        }
    }

    @FXML
    private void onEnvoyerEmail() {
        try {
            List<Cart> produits = new CartService().getByOrderId(orderId);
            double total = produits.stream().mapToDouble(Cart::getTotal).sum();
            String filePath = PdfGenerator.generateInvoice(orderId, nomClient, emailClient, produits, total, LocalDate.now());

            File pdf = new File(filePath);
            if (!pdf.exists()) {
                throw new RuntimeException("Le fichier PDF n’a pas été généré.");
            }

            String sujet = "Votre facture CultureSpace";
            String corps = "Bonjour " + nomClient + ",\n\nVeuillez trouver ci-joint votre facture.";
            EmailWithAttachment.sendEmailWithAttachment(emailClient, sujet, corps, pdf);

            new Alert(Alert.AlertType.INFORMATION, "📧 Facture envoyée à " + emailClient).show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de l’envoi de l’e-mail : " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.show();
    }
}