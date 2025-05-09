package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import utils.MyDatabase;
import utils.ResetPasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

public class ResetPasswordController {

    @FXML
    private TextField resetCodeTF;

    @FXML
    private PasswordField newPasswordPF;

    private Connection connection;

    @FXML
    public void initialize() {
        connection = MyDatabase.getInstance().getCnx();
        if (connection == null) {
            showAlert("Error", "Failed to connect to the database.");
        } else {
            System.out.println("Database connection established successfully in ResetPasswordController.");
        }
    }

    @FXML
    private void resetPassword() {
        String enteredCode = resetCodeTF.getText().trim();
        String newPassword = newPasswordPF.getText().trim();

        System.out.println("Verifying OTP for email: " + ResetPasswordUtil.getEmail());
        System.out.println("Entered OTP: " + enteredCode);
        System.out.println("Is entered OTP empty? " + enteredCode.isEmpty());

        // Validate the OTP input
        if (enteredCode.isEmpty()) {
            showAlert("Invalid OTP", "Please enter the OTP.");
            return;
        }

        // Verify the OTP
        String storedOtp = ResetPasswordUtil.getOtp();
        if (storedOtp == null || !storedOtp.equals(enteredCode)) {
            showAlert("Invalid OTP", "The OTP is incorrect. Please try again or request a new one.");
            return;
        }

        if (newPassword.isEmpty() || newPassword.length() < 8) {
            showAlert("Invalid Password", "The new password must be at least 8 characters long.");
            return;
        }

        try {
            // Hash the new password using jBCrypt
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            System.out.println("New hashed password: " + hashedPassword);

            // Validate the hash format
            if (!hashedPassword.startsWith("$2a$")) {
                showAlert("Error", "Failed to generate a valid password hash. Please try again.");
                return;
            }

            // Update the password in the database
            try (PreparedStatement stmt = connection.prepareStatement("UPDATE user SET password = ? WHERE email = ?")) {
                stmt.setString(1, hashedPassword); // Store the hashed password
                stmt.setString(2, ResetPasswordUtil.getEmail());
                int rowsUpdated = stmt.executeUpdate();
                System.out.println("Rows affected by password update: " + rowsUpdated);
                if (rowsUpdated > 0) {
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Password reset successfully!");
                    successAlert.showAndWait();

                    // Navigate back to login screen
                    try {
                        Stage stage = (Stage) resetCodeTF.getScene().getWindow();
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
                        Scene loginScene = new Scene(loader.load());
                        stage.setScene(loginScene);
                        stage.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert("Error", "Failed to load the login screen. Please restart the application.");
                    }

                    ResetPasswordUtil.clear();
                } else {
                    showAlert("Error", "Failed to reset password. User not found.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while resetting the password: " + e.getMessage());
        }
    }

    @FXML
    private void goBack() {
        try {
            Stage stage = (Stage) resetCodeTF.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OtpEntry.fxml"));
            Scene otpEntryScene = new Scene(loader.load());
            stage.setScene(otpEntryScene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to go back to the OTP entry screen: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}