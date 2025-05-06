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
import services.EmailService;
import services.UserService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class LoginController {
    @FXML
    private TextField emailTF;

    @FXML
    private PasswordField passwordTF;

    private final UserService userService = new UserService();
    private final EmailService emailService = new EmailService();

    @FXML
    // private void login(ActionEvent actionEvent) {
    //     String email = emailTF.getText().trim();
    //     String password = passwordTF.getText();

    //     // Reset field styles
    //     emailTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
    //     passwordTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");

    //     // Validation
    //     if (email.isEmpty()) {
    //         showAlert(Alert.AlertType.WARNING, "Oops!", "Please enter your email!");
    //         emailTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #ff6b6b; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
    //         return;
    //     }

    //     if (!isValidEmail(email)) {
    //         showAlert(Alert.AlertType.WARNING, "Oops!", "Please enter a valid email (e.g., user@domain.com)!");
    //         emailTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #ff6b6b; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
    //         return;
    //     }

    //     if (password.isEmpty()) {
    //         showAlert(Alert.AlertType.WARNING, "Oops!", "Please enter your password!");
    //         passwordTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #ff6b6b; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
    //         return;
    //     }

    //     // Proceed with login
    //     try {
    //         User user = userService.findByEmail(email);
    //         if (user != null) {
    //             if (user.isBlocked()) {
    //                 showAlert(Alert.AlertType.ERROR, "Blocked", "Your account has been blocked by the administrators. Please contact support for more information.");
    //                 return;
    //             }
    //             if (BCrypt.checkpw(password, user.getPassword())) {
    //                 // Store user in SessionManager
    //                 SessionManager.getInstance().setCurrentUser(user);

    //                 if (user.getRoles().contains("ROLE_ADMIN")) {
    //                     // Generate and send 2FA token for admin
    //                     String token = userService.generateTwoFactorToken();
    //                     LocalDateTime expiry = LocalDateTime.now().plusMinutes(2);
    //                     userService.updateTwoFactorToken(user.getId(), token, expiry);
    //                     try {
    //                         emailService.sendTwoFactorEmail(user.getEmail(), token);
    //                     } catch (MessagingException e) {
    //                         showAlert(Alert.AlertType.ERROR, "Error", "Failed to send 2FA email: " + e.getMessage());
    //                         return;
    //                     }

    //                     // Navigate to 2FA view for admin
    //                     FXMLLoader loader = new FXMLLoader(getClass().getResource("/TwoFactorAuth.fxml"));
    //                     Parent root = loader.load();
    //                     TwoFactorAuthController controller = loader.getController();
    //                     controller.setUser(user);
    //                     emailTF.getScene().setRoot(root);
    //                 } else if (user.getRoles().contains("ROLE_STUDENT")) {
    //                     // Navigate to student dashboard
    //                     FXMLLoader loader = new FXMLLoader(getClass().getResource("/sideBar/main.fxml"));
    //                     Parent root = loader.load();
    //                     emailTF.getScene().setRoot(root);
    //                 } else {
    //                     showAlert(Alert.AlertType.ERROR, "Access Denied", "You do not have the necessary permissions to access this application.");
    //                 }
    //             } else {
    //                 showAlert(Alert.AlertType.ERROR, "Error", "Invalid password. Please try again!");
    //                 passwordTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #ff6b6b; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
    //             }
    //         } else {
    //             showAlert(Alert.AlertType.ERROR, "Error", "User not found. Please check your email or sign up!");
    //             emailTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #ff6b6b; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
    //         }
    //     } catch (SQLException | IOException e) {
    //         showAlert(Alert.AlertType.ERROR, "Error", "Error during login: " + e.getMessage());
    //         emailTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #ff6b6b; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
    //     }
    // }

    private void login(ActionEvent actionEvent) {
        String email = emailTF.getText().trim();
        String password = passwordTF.getText();
    
        // Réinitialiser le style des champs
        emailTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
        passwordTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
    
        // Validation des champs
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
    
        // Traitement de la connexion
        try {
            User user = userService.findByEmail(email);
            if (user != null) {
                if (user.isBlocked()) {
                    showAlert(Alert.AlertType.ERROR, "Blocked", "Your account has been blocked by the administrators. Please contact support for more information.");
                    return;
                }
    
                if (BCrypt.checkpw(password, user.getPassword())) {
                    SessionManager.getInstance().setCurrentUser(user);
    
                    if (user.getRoles().contains("ROLE_ADMIN")) {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/adminDashboard.fxml"));
                        Parent root = loader.load();
                        emailTF.getScene().setRoot(root);
                    } else if (user.getRoles().contains("ROLE_STUDENT")) {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/sideBar/main.fxml"));
                        Parent root = loader.load();
                        emailTF.getScene().setRoot(root);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Access Denied", "You do not have the necessary permissions to access this application.");
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Invalid password. Please try again!");
                    passwordTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #ff6b6b; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "User not found. Please check your email or sign up!");
                emailTF.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #ff6b6b; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
            }
        } catch (SQLException | IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error during login: " + e.getMessage());
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
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load signup page: " + e.getMessage());
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