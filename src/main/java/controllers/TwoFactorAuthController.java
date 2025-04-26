package controllers;

import entities.User;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import services.EmailService;
import services.UserService;
import utils.SessionManager;

import java.io.IOException;
import java.time.LocalDateTime;
import jakarta.mail.MessagingException;
import javafx.util.Duration;

public class TwoFactorAuthController {
    @FXML
    private TextField tokenTF;
    @FXML
    private Label timerLabel;

    private User user;
    private final UserService userService = new UserService();
    private final EmailService emailService = new EmailService();
    private Timeline timeline;
    private int secondsLeft = 120;

    public void setUser(User user) {
        this.user = user;
        startTimer();
    }

    @FXML
    private void verifyToken(ActionEvent actionEvent) {
        String token = tokenTF.getText().trim();

        // Reset field style
        tokenTF.getStyleClass().remove("error");
        tokenTF.setStyle(""); // Clear inline styles

        // Validation
        if (token.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Oops!", "Please enter the verification code!");
            tokenTF.getStyleClass().add("error");
            return;
        }

        try {
            if (userService.validateTwoFactorToken(user.getId(), token)) {
                // Stop timer
                if (timeline != null) {
                    timeline.stop();
                }
                // Clear 2FA token from SessionManager
                SessionManager.getInstance().clearTwoFactorToken(user.getId());
                SessionManager.getInstance().setCurrentUser(user);

                showAlert(Alert.AlertType.INFORMATION, "Success!", "Welcome back, Administrator " + user.getFirstName() + "!");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminDashboard.fxml"));
                Parent root = loader.load();
                tokenTF.getScene().setRoot(root);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid or expired verification code. Please try again or request a new code.");
                tokenTF.getStyleClass().add("error");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error during verification: " + e.getMessage());
            tokenTF.getStyleClass().add("error");
        }
    }

    @FXML
    private void resendToken(ActionEvent actionEvent) {
        try {
            // Stop existing timer
            if (timeline != null) {
                timeline.stop();
            }
            // Generate and send new token
            String token = userService.generateTwoFactorToken();
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(2);
            userService.updateTwoFactorToken(user.getId(), token, expiry);
            emailService.sendTwoFactorEmail(user.getEmail(), token);
            showAlert(Alert.AlertType.INFORMATION, "Success", "A new verification code has been sent to your email.");
            tokenTF.clear();
            tokenTF.getStyleClass().remove("error");
            tokenTF.setStyle("");
            // Restart timer
            secondsLeft = 120;
            startTimer();
        } catch (MessagingException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to resend verification code: " + e.getMessage());
        }
    }

    private void startTimer() {
        timerLabel.setText("Code expires in: 2:00");
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsLeft--;
            int minutes = secondsLeft / 60;
            int seconds = secondsLeft % 60;
            timerLabel.setText(String.format("Code expires in: %d:%02d", minutes, seconds));
            if (secondsLeft <= 0) {
                timeline.stop();
                showAlert(Alert.AlertType.WARNING, "Expired", "The verification code has expired. Please request a new code.");
                tokenTF.clear();
                tokenTF.getStyleClass().remove("error");
                tokenTF.setStyle("");
            }
        }));
        timeline.setCycleCount(120);
        timeline.play();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        alert.getDialogPane().setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; -fx-font-size: 14px;");
        if (type == Alert.AlertType.INFORMATION) {
            alert.getDialogPane().lookupButton(alert.getButtonTypes().get(0)).setStyle("-fx-background-color: #28a745; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-background-radius: 8;");
        } else if (type == Alert.AlertType.WARNING) {
            alert.getDialogPane().lookupButton(alert.getButtonTypes().get(0)).setStyle("-fx-background-color: #ff9500; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-background-radius: 8;");
        } else if (type == Alert.AlertType.ERROR) {
            alert.getDialogPane().lookupButton(alert.getButtonTypes().get(0)).setStyle("-fx-background-color: #dc3545; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-background-radius: 8;");
        }

        alert.showAndWait();
    }
}