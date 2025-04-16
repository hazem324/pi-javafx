package controllers;

import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.mindrot.jbcrypt.BCrypt;
import services.UserService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class LoginController {
    @FXML
    private TextField emailTF;

    @FXML
    private PasswordField passwordTF;

    @FXML
    private void login(ActionEvent actionEvent) {
        String email = emailTF.getText().trim();
        String password = passwordTF.getText();

        // Reset field styles
        emailTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
        passwordTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");

        // Validation
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Oops!", "Please enter your email!");
            emailTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #ff6b6b; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
            return;
        }

        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.WARNING, "Oops!", "Please enter a valid email (e.g., user@domain.com)!");
            emailTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #ff6b6b; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
            return;
        }

        if (password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Oops!", "Please enter your password!");
            passwordTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #ff6b6b; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
            return;
        }

        // Proceed with login
        UserService us = new UserService();
        try {
            User user = us.findByEmail(email);
            if (user != null) {
                if (user.isBlocked()) {
                    showAlert(Alert.AlertType.ERROR, "Blocked", "Your account has been blocked by the administrators. Please contact support for more information.");
                    return; // Prevent login for blocked users
                }
                if (BCrypt.checkpw(password, user.getPassword())) {
                    if (user.getRoles().contains("ROLE_ADMIN")) {
                        showAlert(Alert.AlertType.INFORMATION, "Success!", "Welcome back, Administrator " + user.getFirstName() + "!");
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminDashboard.fxml"));
                        try {
                            Parent root = loader.load();
                            emailTF.getScene().setRoot(root);
                        } catch (IOException e) {
                            showAlert(Alert.AlertType.ERROR, "Erreur", "Failed to load dashboard: " + e.getMessage());
                        }
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Access Denied", "You do not have the necessary permissions to access the admin dashboard.");
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Invalid password. Please try again!");
                    passwordTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #ff6b6b; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "User not found. Please check your email or sign up!");
                emailTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #ff6b6b; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Database error during login: " + e.getMessage());
            emailTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #ff6b6b; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
        }
    }

    @FXML
    private void switchToSignup(ActionEvent actionEvent) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Signup.fxml"));
        try {
            Parent root = loader.load();
            emailTF.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Failed to load signup page: " + e.getMessage());
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        // Style the alert based on type
        alert.getDialogPane().setStyle("-fx-font-family: 'System'; -fx-font-size: 14px;");
        if (type == Alert.AlertType.INFORMATION) {
            alert.getDialogPane().lookupButton(ButtonType.OK).setStyle("-fx-background-color: #90EE90; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-background-radius: 5;");
        } else if (type == Alert.AlertType.WARNING) {
            alert.getDialogPane().lookupButton(ButtonType.OK).setStyle("-fx-background-color: #ffa500; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-background-radius: 5;");
        } else if (type == Alert.AlertType.ERROR) {
            alert.getDialogPane().lookupButton(ButtonType.OK).setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-background-radius: 5;");
        }

        alert.showAndWait();
    }
}