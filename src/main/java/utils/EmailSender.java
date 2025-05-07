package utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;
import jakarta.mail.Session;

public class EmailSender {
    private static final String FROM = "hazemhadda123@gmail.com";
    private static final String USERNAME = "hazemhadda123@gmail.com";
    private static final String PASSWORD = "yutn cjwp nsts btkb"; // mot de passe d'application

    public static void sendAlertEmail(String to, String subject, String body) {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(properties,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(USERNAME, PASSWORD);
                    }
                });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            // Set the email content as HTML
            message.setContent(body, "text/html; charset=UTF-8");

            Transport.send(message);
            System.out.println("✅ Email envoyé avec succès à " + to);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("❌ Erreur lors de l'envoi de l'email: " + e.getMessage());
        }
    }
}