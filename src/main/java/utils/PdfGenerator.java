package utils;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import models.Cart;

import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfGenerator {

    public static String generateInvoice(int orderId, String clientName, String email, List<Cart> produits, double total, LocalDate date) {
        String fileName = "facture_" + orderId + ".pdf";

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            Font bold = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font normal = new Font(Font.HELVETICA, 12, Font.NORMAL);

            // Titre
            document.add(new Paragraph("CultureSpace - Facture", bold));
            document.add(new Paragraph("Date : " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), normal));
            document.add(new Paragraph("Client : " + clientName, normal));
            document.add(new Paragraph("Email  : " + email, normal));
            document.add(new Paragraph("Commande n°" + orderId, normal));
            document.add(new Paragraph(" "));

            // Tableau
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4, 2, 2, 2});

            // En-têtes
            table.addCell(createHeaderCell("Produit"));
            table.addCell(createHeaderCell("Quantité"));
            table.addCell(createHeaderCell("Prix (DT)"));
            table.addCell(createHeaderCell("Total (DT)"));

            // Contenu
            for (Cart c : produits) {
                table.addCell(new Phrase(c.getProductName(), normal));
                table.addCell(new Phrase(String.valueOf(c.getProductQuantity()), normal));
                table.addCell(new Phrase(String.format("%.2f", c.getPrice()), normal));
                table.addCell(new Phrase(String.format("%.2f", c.getTotal()), normal));
            }

            // Ligne total
            PdfPCell cell = new PdfPCell(new Phrase("TOTAL"));
            cell.setColspan(3);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cell);
            table.addCell(new Phrase(String.format("%.2f", total), bold));

            document.add(table);
            document.close();

            return fileName;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static PdfPCell createHeaderCell(String text) {
        Font font = new Font(Font.HELVETICA, 12, Font.BOLD);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }
}