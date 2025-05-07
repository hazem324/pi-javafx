package controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import entities.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TotpSetupController {
    @FXML private ImageView qrCodeImage;
    private User user;
    private String secretKey;

    public void setUser(User user) {
        this.user = user;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
        displayQrCode();
    }

    private void displayQrCode() {
        String totpUri = String.format("otpauth://totp/CultureSpace:%s?secret=%s&issuer=YourApp", user.getEmail(), secretKey);
        Image qrImage = generateQrCode(totpUri, 200, 200);
        if (qrImage != null) {
            qrCodeImage.setImage(qrImage);
        }
    }

    @FXML
    private void onSetupComplete() {
        try {
            // Verify the resource exists
            java.net.URL resourceUrl = getClass().getResource("/TotpVerification.fxml");
            if (resourceUrl == null) {
                throw new IllegalStateException("Resource not found: /TotpVerification.fxml");
            }

            // Load the TOTP verification view
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();
            TotpVerificationController controller = loader.getController();
            controller.setUser(user);
            qrCodeImage.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load TOTP verification: " + e.getMessage());
        } catch (IllegalStateException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to find FXML file: /TotpVerification.fxml\nCheck if the file exists in the resources directory.\n" + e.getMessage());
        }
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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
