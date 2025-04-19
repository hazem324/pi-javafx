package controllers;

import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.UserService;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AddUserFormController {

    private static final Logger LOGGER = Logger.getLogger(AddUserFormController.class.getName());

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField passwordField;

    private UserService userService = new UserService();
    private UserManagementController userManagementController;

    public void setUserManagementController(UserManagementController controller) {
        this.userManagementController = controller;
    }


    @FXML
    public void handleSave(ActionEvent event) {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (!isValidInput(firstName, lastName, email, password)) {
            return; // Don't proceed if input is invalid
        }

        User newUser = new User(firstName, lastName, email, password, false, false, null, Arrays.asList("ROLE_STUDENT"));

        try {
            // Check if email already exists
            try {
                userService.findByEmail(email);
                showAlert(Alert.AlertType.ERROR, "Error", "Email address already exists.");
                return;
            } catch (SQLException e) {
                // Email not found, proceed with adding the new user
                userService.ajouter(newUser);
                showAlert(Alert.AlertType.INFORMATION, "Success", "New user added successfully.");
                if (userManagementController != null) {
                    userManagementController.loadAllUsers(); // Call with page 0
                }
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.close();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQLException during add user:", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Could not add user: " + e.getMessage());
        }
    }

    private boolean isValidInput(String firstName, String lastName, String email, String password) {
        if (firstName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "First name cannot be empty.");
            return false;
        }
        if (!isValidName(firstName)) {
            showAlert(Alert.AlertType.ERROR, "Error", "First name should contain only letters and spaces.");
            return false;
        }
        if (lastName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Last name cannot be empty.");
            return false;
        }
        if (!isValidName(lastName)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Last name should contain only letters and spaces.");
            return false;
        }
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Email cannot be empty.");
            return false;
        }
        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid email address.");
            return false;
        }
        if (password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Password cannot be empty.");
            return false;
        }
        if (password.length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Error", "Password must be at least 6 characters long.");
            return false;
        }
        return true;
    }

    private boolean isValidName(String name) {
        String nameRegex = "^[A-Za-z\\s]+$"; // Added \\s to allow whitespace
        Pattern pattern = Pattern.compile(nameRegex);
        Matcher matcher = pattern.matcher(name);
        return matcher.matches();
    }

    private boolean isValidEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}