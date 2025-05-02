package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import utils.ResetPasswordUtil;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.Random;

public class OtpEntryController {

    @FXML
    private Button proceedButton; // Inject the Proceed button

    // Store the generated OTP for verification in the next step
    private String generatedOtp;
    private boolean otpSent = false; // Flag to track if OTP has been sent

    @FXML
    private void sendResetCode() {
        // If OTP has already been sent, do nothing or show a message
        if (otpSent) {
            showAlert("Info", "OTP has already been sent. Please proceed to enter the code.");
            return;
        }

        // Generate a 6-digit OTP
        generatedOtp = generateOtp();
        System.out.println("Generated OTP: " + generatedOtp);

        // Get the email from ResetPasswordUtil
        String email = ResetPasswordUtil.getEmail();
        if (email == null || email.isEmpty()) {
            showAlert("Error", "Email not found. Please start the process again.");
            return;
        }

        // Send the OTP to the email
        try {
            sendOtpEmail(email, generatedOtp);
            System.out.println("OTP sent to email: " + email);

            // Store the OTP for verification in the next step
            ResetPasswordUtil.setOtp(generatedOtp);
            otpSent = true; // Mark OTP as sent

            // Disable the Proceed button to prevent multiple clicks
            if (proceedButton != null) {
                proceedButton.setDisable(true);
            } else {
                System.out.println("proceedButton is null; cannot disable button");
            }

            // Navigate to reset password screen
            if (proceedButton == null) {
                showAlert("Error", "Cannot navigate: Proceed button is null.");
                return;
            }
            Stage stage = (Stage) proceedButton.getScene().getWindow();
            if (stage == null) {
                showAlert("Error", "Cannot navigate: Stage is null.");
                return;
            }
            System.out.println("Loading ResetPassword.fxml...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ResetPassword.fxml"));
            if (loader.getLocation() == null) {
                showAlert("Error", "Cannot find ResetPassword.fxml. Ensure the file exists in the resources directory.");
                return;
            }
            Scene scene = new Scene(loader.load());
            System.out.println("Navigating to ResetPassword screen...");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to send OTP email or navigate: " + e.getMessage());
        }
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Generate a 6-digit OTP
        return String.valueOf(otp);
    }

    private void sendOtpEmail(String toEmail, String otp) throws MessagingException {
        // Email server settings (using Mailtrap)
        String host = "sandbox.smtp.mailtrap.io";
        String port = "2525";
        String username = "821a4ea22fe070";
        String password = "2747895b201008";

        // Set properties for the email session
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);

        // Create a session with authentication
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        // Create the email message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("from@example.com")); // Mailtrap doesn't check the 'from' address
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Password Reset OTP");
        message.setText("Your OTP for password reset is: " + otp + "\n\nThis OTP is valid for 10 minutes.");

        // Send the email
        Transport.send(message);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}