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
import services.UserService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class SignupController {
    @FXML
    private TextField firstNameTF;

    @FXML
    private TextField lastNameTF;

    @FXML
    private TextField emailTF;

    @FXML
    private PasswordField passwordTF;

    @FXML
    private void signup(ActionEvent actionEvent) {
        String firstName = firstNameTF.getText().trim();
        String lastName = lastNameTF.getText().trim();
        String email = emailTF.getText().trim();
        String password = passwordTF.getText();

        // Reset field styles
        firstNameTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
        lastNameTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
        emailTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
        passwordTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");

        // Validation
        if (firstName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Oops!", "Please enter your first name!");
            firstNameTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #ff6b6b; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
            return;
        }

        if (!isValidName(firstName)) {
            showAlert(Alert.AlertType.WARNING, "Oops!", "First name should contain only letters!");
            firstNameTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #ff6b6b; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
            return;
        }

        if (lastName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Oops!", "Please enter your last name!");
            lastNameTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #ff6b6b; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
            return;
        }

        if (!isValidName(lastName)) {
            showAlert(Alert.AlertType.WARNING, "Oops!", "Last name should contain only letters!");
            lastNameTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #ff6b6b; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
            return;
        }

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

        // Check if email already exists
        UserService us = new UserService();
        try {
            us.findByEmail(email);
            showAlert(Alert.AlertType.WARNING, "Oops!", "Email already registered! Please use a different email.");
            emailTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #ff6b6b; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
            return;
        } catch (SQLException e) {
            // Email not found, proceed with signup
        }

        if (password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Oops!", "Please enter your password!");
            passwordTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #ff6b6b; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
            return;
        }

        if (password.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Oops!", "Password must be at least 6 characters long!");
            passwordTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #ff6b6b; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
            return;
        }

        // Proceed with signup
        User user = new User(firstName, lastName, email, password, false, false, null, List.of("ROLE_USER"));
        try {
            us.ajouter(user);
            showAlert(Alert.AlertType.INFORMATION, "Success!", "Account created successfully, " + firstName + "! Please log in.");
            switchToLogin(actionEvent);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Failed to create account: " + e.getMessage());
            emailTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #ff6b6b; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
        }
    }

    @FXML
    private void switchToLogin(ActionEvent actionEvent) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
        try {
            Parent root = loader.load();
            firstNameTF.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Failed to load login page: " + e.getMessage());
        }
    }

    private boolean isValidName(String name) {
        String nameRegex = "^[A-Za-z]+$";
        Pattern pattern = Pattern.compile(nameRegex);
        return pattern.matcher(name).matches();
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