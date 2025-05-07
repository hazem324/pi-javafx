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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.stage.Stage;
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
    @FXML private ImageView logoView;
    @FXML private Hyperlink forgotPasswordLink;

    private final UserService userService = new UserService();
    private final EmailService emailService = new EmailService();
    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    @FXML
    public void initialize() {
        System.out.println("LoginController initialized");
        // Add hover event handlers for the logo
        if (logoView != null) {
            logoView.setOnMouseEntered(event -> {
                logoView.setScaleX(1.1);
                logoView.setScaleY(1.1);
            });
            logoView.setOnMouseExited(event -> {
                logoView.setScaleX(1.0);
                logoView.setScaleY(1.0);
            });
        }
        // Add hover event handlers for the forgot password link
        if (forgotPasswordLink != null) {
            forgotPasswordLink.setOnMouseEntered(event -> {
                forgotPasswordLink.setStyle("-fx-font-size: 16px; -fx-text-fill: #ffffff; -fx-font-family: 'Roboto'; -fx-font-weight: bold; -fx-underline: true; -fx-effect: dropshadow(gaussian, #ffffff, 8, 0, 0, 0);");
            });
            forgotPasswordLink.setOnMouseExited(event -> {
                forgotPasswordLink.setStyle("-fx-font-size: 16px; -fx-text-fill: #ffffff; -fx-font-family: 'Roboto'; -fx-font-weight: bold; -fx-border-width: 0; -fx-effect: dropshadow(gaussian, #ffffff, 5, 0, 0, 0);");
            });
        }
    }

    @FXML
    private void login(ActionEvent actionEvent) {
        String email = emailTF.getText().trim();
        String password = passwordTF.getText();

        // Reset field styles
        resetFieldStyle(emailTF);
        resetFieldStyle(passwordTF);

        // Validation
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Oops!", "Please enter your email!");
            setErrorStyle(emailTF);
            return;
        }

        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.WARNING, "Oops!", "Please enter a valid email (e.g., user@domain.com)!");
            setErrorStyle(emailTF);
            return;
        }

        if (password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Oops!", "Please enter your password!");
            setErrorStyle(passwordTF);
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

                String hashedPassword = user.getPassword();
                System.out.println("Stored hashed password for user " + email + ": " + hashedPassword);
                if (hashedPassword != null && hashedPassword.startsWith("$2a$")) {
                    if (BCrypt.checkpw(password, hashedPassword)) {
                        // Store user in SessionManager
                        SessionManager.getInstance().setCurrentUser(user);

                        // Initialize SecurityConfig based on user's is_verified status
                        SecurityConfig securityConfig = SecurityConfig.getInstance();
                        securityConfig.initializeBasedOnUserPreference(user.isVerified());

                        if (user.getRoles().contains("ROLE_ADMIN")) {
                            boolean email2FAEnabled = securityConfig.isEmail2FAEnabled();
                            boolean totp2FAEnabled = securityConfig.isTotp2FAEnabled();

                            if (!email2FAEnabled && !totp2FAEnabled) {
                                navigateToDashboard(user);
                                return;
                            }

                            boolean hasTotpSetup = user.isVerified() && TotpSecretStorage.getSecret(user.getId()) != null;

                            if (user.isVerified() && totp2FAEnabled) {
                                if (!hasTotpSetup) {
                                    navigateToTotpSetup(user);
                                } else {
                                    navigateToTotpVerification(user);
                                }
                            } else if (email2FAEnabled) {
                                String token = userService.generateTwoFactorToken();
                                LocalDateTime expiry = LocalDateTime.now().plusMinutes(2);
                                userService.updateTwoFactorToken(user.getId(), token, expiry);
                                try {
                                    emailService.sendTwoFactorEmail(user.getEmail(), token);
                                    navigateToEmail2FA(user);
                                } catch (MessagingException e) {
                                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to send 2FA email: " + e.getMessage());
                                    return;
                                }
                            } else {
                                navigateToDashboard(user);
                            }
                        } else if (user.getRoles().contains("ROLE_STUDENT")) {
                            navigateToDashboard(user);
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Access Denied", "You do not have the necessary permissions to access this application.");
                        }
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Invalid password. Please try again!");
                        setErrorStyle(passwordTF);
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Stored password format is invalid. Please reset your password.");
                    setErrorStyle(emailTF);
                    setErrorStyle(passwordTF);
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "User not found. Please check your email or sign up!");
                setErrorStyle(emailTF);
            }
        } catch (SQLException | IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error during login: " + e.getMessage());
            setErrorStyle(emailTF);
        }
    }

    @FXML
    private void forgotPassword() throws Exception {
        Stage stage = (Stage) emailTF.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ForgotPassword.fxml"));
        stage.setScene(new Scene(loader.load()));
    }

    @FXML
    private void switchToSignup(ActionEvent actionEvent) {
        try {
            Stage stage = (Stage) emailTF.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Signup.fxml"));
            stage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load signup page: " + e.getMessage());
        }
    }

    private void navigateToTotpSetup(User user) throws IOException, SQLException {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        String secretKey = key.getKey();
        TotpSecretStorage.saveSecret(user.getId(), secretKey);
        userService.updateVerificationStatus(user.getId(), true);

        Stage stage = (Stage) emailTF.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/TotpSetup.fxml"));
        Scene scene = new Scene(loader.load());
        TotpSetupController controller = loader.getController();
        controller.setUser(user);
        controller.setSecretKey(secretKey);
        stage.setScene(scene);
    }

    private void navigateToTotpVerification(User user) throws IOException {
        Stage stage = (Stage) emailTF.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/TotpVerification.fxml"));
        Scene scene = new Scene(loader.load());
        TotpVerificationController controller = loader.getController();
        controller.setUser(user);
        stage.setScene(scene);
    }

    private void navigateToEmail2FA(User user) throws IOException {
        Stage stage = (Stage) emailTF.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/TwoFactorAuth.fxml"));
        Scene scene = new Scene(loader.load());
        TwoFactorAuthController controller = loader.getController();
        controller.setUser(user);
        stage.setScene(scene);
    }

    private void navigateToDashboard(User user) throws IOException {
        Stage stage = (Stage) emailTF.getScene().getWindow();
        FXMLLoader loader;
        if (user.getRoles().contains("ROLE_ADMIN")) {
            loader = new FXMLLoader(getClass().getResource("/AdminDashboard.fxml"));
        } else if (user.getRoles().contains("ROLE_STUDENT")) {
            loader = new FXMLLoader(getClass().getResource("/sideBar/main.fxml"));
        } else {
            showAlert(Alert.AlertType.ERROR, "Access Denied", "Invalid role for dashboard access.");
            return;
        }
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    private void resetFieldStyle(TextField field) {
        field.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
    }

    private void setErrorStyle(TextField field) {
        field.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #ff6b6b; -fx-border-radius: 5; -fx-background-radius: 5; -fx-prompt-text-fill: #888888; -fx-font-style: italic;");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setStyle("-fx-font-family: 'System'; -fx-font-size: 14px;");
        Node okButton = alert.getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setStyle(switch (type) {
                case INFORMATION -> "-fx-background-color: #90EE90; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-background-radius: 5;";
                case WARNING -> "-fx-background-color: #ffa500; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-background-radius: 5;";
                case ERROR -> "-fx-background-color: #ff6b6b; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-background-radius: 5;";
                default -> "";
            });
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