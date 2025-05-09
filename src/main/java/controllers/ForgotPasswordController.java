package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import utils.ResetPasswordUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ForgotPasswordController {

    @FXML
    private TextField emailTF;

    private Connection connection;

    @FXML
    public void initialize() {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/culturespacedb?useSSL=false&enabledTLSProtocols=TLSv1.2",
                    "root",
                    ""
            );
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to connect to the database.");
        }
    }

    @FXML
    private void submitEmail() {
        String email = emailTF.getText().trim();
        if (email.isEmpty() || !email.matches("^[\\w-\\.]+@([\\w-]+)(\\.[\\w-]{2,4})?$")) {
            showAlert("Invalid Email", "Please enter a valid email address.");
            return;
        }

        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM user WHERE email = ?")) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                ResetPasswordUtil.setEmail(email);
                // Navigate to OTP entry screen
                Stage stage = (Stage) emailTF.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/OtpEntry.fxml"));
                stage.setScene(new Scene(loader.load()));
            } else {
                showAlert("Email Not Found", "No user found with this email address.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while validating the email.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
