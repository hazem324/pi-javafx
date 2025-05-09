package controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.io.image.ImageDataFactory;
import entities.EventRegistration;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import controllers.sideBar.MainSidebar;
import javafx.stage.FileChooser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;

public class RegistrationDetailsController {

    @FXML
    private Text titleLabel;

    @FXML
    private Label eventLabel;

    @FXML
    private Label userLabel;

    @FXML
    private Label registrationDateLabel;

    @FXML
    private Label ticketNumberLabel;

    @FXML
    private ImageView qrCodeImage;

    private EventRegistration registration;
    private MainSidebar mainSidebarController;

    public void setRegistration(EventRegistration registration) {
        this.registration = registration;
        populateRegistrationDetails();
        generateQRCode();
    }

    public void setMainSidebarController(MainSidebar mainSidebarController) {
        this.mainSidebarController = mainSidebarController;
    }

    private void populateRegistrationDetails() {
        if (registration == null) {
            System.err.println("Registration is null in populateRegistrationDetails");
            return;
        }

        titleLabel.setText("Registration Confirmation");
        eventLabel.setText("Event: " + (registration.getEvent() != null ? registration.getEvent().getTitle() : "N/A"));
        userLabel.setText("User: " + (registration.getUser() != null ? registration.getUser().getUsername() : "N/A"));
        registrationDateLabel.setText("Registration Date: " + (registration.getRegistrationDate() != null ? registration.getRegistrationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A"));
        ticketNumberLabel.setText("Ticket Number: " + (registration.getTicketNumber() != null ? registration.getTicketNumber() : "N/A"));
    }

    private void generateQRCode() {
        try {
            // Generate QR code content (e.g., a URL to verify the ticket)
            String qrContent = "https://yourapp.com/verify-ticket/" + registration.getTicketNumber();
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 200, 200);

            // Convert BitMatrix to BufferedImage
            BufferedImage bufferedImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < 200; x++) {
                for (int y = 0; y < 200; y++) {
                    bufferedImage.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            // Convert BufferedImage to JavaFX Image and display
            javafx.scene.image.Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
            qrCodeImage.setImage(fxImage);

            // Save the QR code image to a file
            File qrFile = new File(registration.getQrCode());
            qrFile.getParentFile().mkdirs(); // Create the directory if it doesn't exist
            javax.imageio.ImageIO.write(bufferedImage, "PNG", qrFile);
        } catch (WriterException | java.io.IOException e) {
            System.err.println("Failed to generate QR code: " + e.getMessage());
        }
    }

    @FXML
    public void goBack() {
        mainSidebarController.loadPage("/events");
    }

    @FXML
    public void downloadTicket() {
        try {
            // Use FileChooser to let the user select where to save the PDF
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Ticket PDF");
            fileChooser.setInitialFileName(registration.getTicketNumber() + ".pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showSaveDialog(null);

            if (file != null) {
                // Create PDF using iText
                PdfWriter writer = new PdfWriter(new FileOutputStream(file));
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                // Add ticket details
                document.add(new Paragraph("Event Registration Ticket").setFontSize(20).setBold());
                document.add(new Paragraph("Event: " + registration.getEvent().getTitle()));
                document.add(new Paragraph("User: " + registration.getUser().getUsername()));
                document.add(new Paragraph("Registration Date: " + registration.getRegistrationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
                document.add(new Paragraph("Ticket Number: " + registration.getTicketNumber()));

                // Add QR code image
                Image qrImage = new Image(ImageDataFactory.create(registration.getQrCode()));
                qrImage.setWidth(100);
                qrImage.setHeight(100);
                document.add(qrImage);

                document.close();

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Ticket downloaded successfully!");
                alert.showAndWait();
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to download ticket: " + e.getMessage());
            alert.showAndWait();
        }
    }
}