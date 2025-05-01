package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import services.UserService;
import utils.SecurityConfig;
import utils.SessionManager;

import java.sql.SQLException;

public class SecuritySettingsController {
    @FXML private CheckBox email2FACheckBox;
    @FXML private CheckBox totp2FACheckBox;

    private AdminDashboardController dashboardController;
    private final UserService userService = new UserService();

    public void setDashboardController(AdminDashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML
    public void initialize() {
        SecurityConfig securityConfig = SecurityConfig.getInstance();
        // Reflect the current state of SecurityConfig (set by LoginController based on is_verified)
        email2FACheckBox.setSelected(securityConfig.isEmail2FAEnabled());
        totp2FACheckBox.setSelected(securityConfig.isTotp2FAEnabled());

        email2FACheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            securityConfig.setEmail2FAEnabled(newVal);
            // If enabling email 2FA, disable TOTP to ensure only one method is active
            if (newVal) {
                securityConfig.setTotp2FAEnabled(false);
                totp2FACheckBox.setSelected(false);
                try {
                    int userId = SessionManager.getInstance().getCurrentUser().getId();
                    userService.updateVerificationStatus(userId, false); // Set is_verified to 0
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update 2FA preference: " + e.getMessage());
                }
            }
            showAlert(Alert.AlertType.INFORMATION, "Success", "Email 2FA " + (newVal ? "enabled" : "disabled") + ".");
        });

        totp2FACheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            securityConfig.setTotp2FAEnabled(newVal);
            // If enabling TOTP, disable email 2FA to ensure only one method is active
            if (newVal) {
                securityConfig.setEmail2FAEnabled(false);
                email2FACheckBox.setSelected(false);
            }
            // Update is_verified for the current user to reflect their preference
            try {
                int userId = SessionManager.getInstance().getCurrentUser().getId();
                userService.updateVerificationStatus(userId, newVal); // Set is_verified to 1 if TOTP is enabled, 0 if disabled
                showAlert(Alert.AlertType.INFORMATION, "Success", "TOTP 2FA " + (newVal ? "enabled" : "disabled") + ".");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update TOTP preference: " + e.getMessage());
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}
