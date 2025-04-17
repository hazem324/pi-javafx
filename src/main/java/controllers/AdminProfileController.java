package controllers;

import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.UserService;
import utils.SessionManager;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminProfileController {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField passwordField;

    @FXML
    private TextField confirmPasswordField;

    @FXML
    private Button cancelButton; // Declare the cancelButton field

    private AdminDashboardController adminDashboardController;
    private UserService userService = new UserService();
    private User loggedInAdmin;

    public void setAdminDashboardController(AdminDashboardController controller) {
        this.adminDashboardController = controller;
        loadAdminDetails();
    }

    public void loadAdminDetails() {
        loggedInAdmin = SessionManager.getCurrentUser();
        if (loggedInAdmin != null) {
            try {
                User fullAdminDetails = userService.getAdminDetails(loggedInAdmin.getId());
                if (fullAdminDetails != null) {
                    firstNameField.setText(fullAdminDetails.getFirstName());
                    lastNameField.setText(fullAdminDetails.getLastName());
                    emailField.setText(fullAdminDetails.getEmail());
                    // Password fields are intentionally left empty for security
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Could not load admin details.");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to fetch admin details: " + e.getMessage());
            }
        }
    }

    @FXML
    public void saveProfileChanges(ActionEvent event) {
        if (loggedInAdmin == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No admin data to save.");
            return;
        }

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String newPassword = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (!isValidInput(firstName, lastName, email)) {
            return;
        }

        String passwordToUpdate = loggedInAdmin.getPassword();

        if (!newPassword.isEmpty()) {
            if (!newPassword.equals(confirmPassword)) {
                showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match.");
                return;
            }
            passwordToUpdate = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        }

        User updatedAdmin = new User(loggedInAdmin.getId(), firstName, lastName, email, passwordToUpdate, loggedInAdmin.isVerified(), loggedInAdmin.isBlocked(), loggedInAdmin.getProfileIMG(), loggedInAdmin.getRoles());

        try {
            // Check for duplicate email
            if (!email.equals(loggedInAdmin.getEmail())) {
                User existingUserWithEmail = userService.findByEmail(email);
                if (existingUserWithEmail != null && existingUserWithEmail.getId() != loggedInAdmin.getId()) {
                    showAlert(Alert.AlertType.ERROR, "Error", "This email address is already in use.");
                    return;
                }
            }

            userService.modifier(updatedAdmin);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully.");

            // Redirect to the main dashboard page
            loadAdminDashboard(event);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not update profile: " + e.getMessage());
        }
    }

    private boolean isValidInput(String firstName, String lastName, String email) {
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
        return true;
    }

    private boolean isValidName(String name) {
        String nameRegex = "^[A-Za-z\\s]+$";
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
    public void cancelChanges(ActionEvent event) {
        // Redirect back to the main dashboard page
        loadAdminDashboard(event);
    }

    private void loadAdminDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminDashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load dashboard.");
            e.printStackTrace(); // Print the stack trace for debugging
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