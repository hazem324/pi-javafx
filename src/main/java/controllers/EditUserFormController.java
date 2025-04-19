package controllers;

import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.UserService;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditUserFormController {

    @FXML
    private TextField idField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private CheckBox blockedCheckBox;

    private UserService userService = new UserService();
    private UserManagementController userManagementController;
    private User currentUser;

    public void setUserManagementController(UserManagementController controller) {
        this.userManagementController = controller;
    }

    public void initData(User user) {
        currentUser = user;
        idField.setText(String.valueOf(user.getId()));
        firstNameField.setText(user.getFirstName());
        lastNameField.setText(user.getLastName());
        emailField.setText(user.getEmail());
        blockedCheckBox.setSelected(user.isBlocked());
    }

    @FXML
    public void handleSave(ActionEvent event) {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No user data to save.");
            return;
        }

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        boolean isBlocked = blockedCheckBox.isSelected();

        if (!isValidInput(firstName, lastName, email)) {
            return;
        }

        // Create a new User object with the updated information
        User updatedUser = new User(currentUser.getId(), firstName, lastName, email, currentUser.getPassword(), currentUser.isVerified(), isBlocked, currentUser.getProfileIMG(), currentUser.getRoles());

        try {
            // Check if the email has been changed and if the new email already exists
            if (!email.equals(currentUser.getEmail())) {
                try {
                    User existingUserWithEmail = userService.findByEmail(email);
                    if (existingUserWithEmail != null) {
                        showAlert(Alert.AlertType.ERROR, "Error", "This email address is already in use.");
                        return;
                    }
                } catch (SQLException e) {
                    // If findByEmail throws an exception, it might indicate a database issue or user not found,
                    // but in the context of checking for an existing email, we should handle the non-existence
                    // by not triggering the error.
                }
            }

            userService.modifier(updatedUser);
            showAlert(Alert.AlertType.INFORMATION, "Success", "User information updated successfully.");
            // Refresh the user table in the Admin Dashboard
            if (userManagementController != null) {
                userManagementController.loadAllUsers();
            }
            // Close the modal
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not update user: " + e.getMessage());
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
        // Close the modal
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