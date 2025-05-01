package controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import org.mindrot.jbcrypt.BCrypt;
import services.EmailService;
import services.UserService;
import utils.SecurityConfig;
import utils.SessionManager;
import utils.TotpSecretStorage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.regex.Pattern;
import jakarta.mail.MessagingException;

public class LoginController {
    @FXML private TextField emailTF;
    @FXML private PasswordField passwordTF;

    private final UserService userService = new UserService();
    private final EmailService emailService = new EmailService();
    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

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
        try {
            User user = userService.findByEmail(email);
            if (user != null) {
                if (user.isBlocked()) {
                    showAlert(Alert.AlertType.ERROR, "Blocked", "Your account has been blocked by the administrators. Please contact support for more information.");
                    return;
                }
                if (BCrypt.checkpw(password, user.getPassword())) {
                    // Store user in SessionManager
                    SessionManager.getInstance().setCurrentUser(user);

                    // Initialize SecurityConfig based on user's is_verified status
                    SecurityConfig securityConfig = SecurityConfig.getInstance();
                    securityConfig.initializeBasedOnUserPreference(user.isVerified());

                    if (user.getRoles().contains("ROLE_ADMIN")) {
                        boolean email2FAEnabled = securityConfig.isEmail2FAEnabled();
                        boolean totp2FAEnabled = securityConfig.isTotp2FAEnabled();

                        // If both 2FA methods are disabled, skip to dashboard
                        if (!email2FAEnabled && !totp2FAEnabled) {
                            navigateToDashboard(user);
                            return;
                        }

                        // Check if user has TOTP set up using is_verified
                        boolean hasTotpSetup = user.isVerified() && TotpSecretStorage.getSecret(user.getId()) != null;

                        // Use is_verified to determine preferred security method
                        if (user.isVerified() && totp2FAEnabled) {
                            // User has chosen TOTP (is_verified = 1) and TOTP is enabled
                            if (!hasTotpSetup) {
                                // Navigate to TOTP setup
                                navigateToTotpSetup(user);
                            } else {
                                // Navigate to TOTP verification
                                navigateToTotpVerification(user);
                            }
                        } else if (email2FAEnabled) {
                            // Either user hasn't chosen TOTP (is_verified = 0) or TOTP is disabled, use email 2FA if enabled
                            // Generate and send 2FA token for admin
                            String token = userService.generateTwoFactorToken();
                            LocalDateTime expiry = LocalDateTime.now().plusMinutes(2);
                            userService.updateTwoFactorToken(user.getId(), token, expiry);
                            try {
                                emailService.sendTwoFactorEmail(user.getEmail(), token);
                            } catch (MessagingException e) {
                                showAlert(Alert.AlertType.ERROR, "Error", "Failed to send 2FA email: " + e.getMessage());
                                return;
                            }

                            // Navigate to 2FA view for admin
                            navigateToEmail2FA(user);
                        } else {
                            // Neither TOTP nor email 2FA is enabled or applicable, go to dashboard
                            navigateToDashboard(user);
                        }
                    } else if (user.getRoles().contains("ROLE_STUDENT")) {
                        // Navigate to student dashboard
                        navigateToDashboard(user);
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Signup.fxml"));
            Parent root = loader.load();
            emailTF.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load signup page: " + e.getMessage());
        }
    }

    private void navigateToTotpSetup(User user) throws IOException, SQLException {
        // Generate TOTP secret
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        String secretKey = key.getKey();

        // Save secret to local storage
        TotpSecretStorage.saveSecret(user.getId(), secretKey);

        // Set is_verified to true (1)
        userService.updateVerificationStatus(user.getId(), true);

        // Navigate to TOTP setup view
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/TotpSetup.fxml"));
        Parent root = loader.load();
        TotpSetupController controller = loader.getController();
        controller.setUser(user);
        controller.setSecretKey(secretKey);
        emailTF.getScene().setRoot(root);
    }

    private void navigateToTotpVerification(User user) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/TotpVerification.fxml"));
        Parent root = loader.load();
        TotpVerificationController controller = loader.getController();
        controller.setUser(user);
        emailTF.getScene().setRoot(root);
    }

    private void navigateToEmail2FA(User user) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/TwoFactorAuth.fxml"));
        Parent root = loader.load();
        TwoFactorAuthController controller = loader.getController();
        controller.setUser(user);
        emailTF.getScene().setRoot(root);
    }

    private void navigateToDashboard(User user) throws IOException {
        if (user.getRoles().contains("ROLE_ADMIN")) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminDashboard.fxml"));
            Parent root = loader.load();
            emailTF.getScene().setRoot(root);
        } else if (user.getRoles().contains("ROLE_STUDENT")) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sideBar/main.fxml"));
            Parent root = loader.load();
            emailTF.getScene().setRoot(root);
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

    private Image generateQrCode(String data, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return new Image(new ByteArrayInputStream(pngOutputStream.toByteArray()));
        } catch (WriterException | IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to generate QR code: " + e.getMessage());
            return null;
        }
    }
}
