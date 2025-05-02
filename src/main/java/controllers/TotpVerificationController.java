package controllers;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import utils.TotpSecretStorage;

import java.io.IOException;

public class TotpVerificationController {
    @FXML private TextField totpCodeTF;
    private User user;
    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    public void setUser(User user) {
        this.user = user;
    }

    @FXML
    private void verifyTotpCode(ActionEvent event) {
        String totpCode = totpCodeTF.getText().trim();
        if (totpCode.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Oops!", "Please enter the TOTP code!");
            return;
        }

        try {
            // Check if user has TOTP set up
            if (!user.isVerified()) {
                showAlert(Alert.AlertType.ERROR, "Error", "TOTP not set up for this user.");
                return;
            }

            // Retrieve TOTP secret from local storage
            String totpSecret = TotpSecretStorage.getSecret(user.getId());
            if (totpSecret == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "TOTP secret not found for this user.");
                return;
            }

            boolean isCodeValid = gAuth.authorize(totpSecret, Integer.parseInt(totpCode));
            if (isCodeValid) {
                // Redirect based on user role
                if (user.getRoles().contains("ROLE_ADMIN")) {
                    // Navigate to admin dashboard
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminDashboard.fxml"));
                    Parent root = loader.load();
                    totpCodeTF.getScene().setRoot(root);
                } else if (user.getRoles().contains("ROLE_STUDENT")) {
                    // Navigate to student dashboard
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/sideBar/main.fxml"));
                    Parent root = loader.load();
                    totpCodeTF.getScene().setRoot(root);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Access Denied", "You do not have the necessary permissions to access this application.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid TOTP code. Please try again.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "TOTP code must be a number.");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load dashboard: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}