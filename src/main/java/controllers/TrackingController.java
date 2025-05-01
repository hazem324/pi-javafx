package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import models.TrackingEvent;
import services.TrackingService;
import tests.MainFX;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class TrackingController {

    @FXML private TextField orderIdField;
    @FXML private ProgressIndicator pi1, pi2, pi3, pi4, pi5;
    @FXML private TableView<TrackingEvent> eventTable;
    @FXML private TableColumn<TrackingEvent, LocalDateTime> colDate;
    @FXML private TableColumn<TrackingEvent, String> colStatut;

    private final TrackingService trackingService = new TrackingService();

    @FXML
    public void afficherTracking() {
        resetProgress(); // tout à 0%

        try {
            int orderId = Integer.parseInt(orderIdField.getText().trim());
            List<String> statusList = trackingService.getStatutsByOrderId(orderId);

            // Liste ordonnée des étapes
            List<String> orderedStages = List.of("en_attente", "preparation", "expedition", "en_route", "livree");

            if (statusList.isEmpty()) {
                // 🟢 Aucune étape enregistrée → cocher uniquement "en_attente"
                setProgressFor("en_attente", 1);
                return;
            }

            // 🟢 On récupère la dernière étape atteinte
            String lastStatus = statusList.get(statusList.size() - 1);

            for (String stage : orderedStages) {
                setProgressFor(stage, 1); // cocher les étapes précédentes

                if (stage.equals(lastStatus)) break; // s'arrêter à la dernière atteinte
            }
            List<TrackingEvent> events = trackingService.getEventsByOrderId(orderId);
            eventTable.getItems().setAll(events);

        } catch (NumberFormatException e) {
            System.out.println("ID invalide");
        }
    }


    private void setProgressFor(String status, double value) {
        switch (status) {
            case "en_attente":   pi1.setProgress(value); break;
            case "preparation":  pi2.setProgress(value); break;
            case "expedition":   pi3.setProgress(value); break;
            case "en_route":     pi4.setProgress(value); break;
            case "livree":       pi5.setProgress(value); break;
        }
    }


    private void resetProgress() {
        pi1.setProgress(0);
        pi2.setProgress(0);
        pi3.setProgress(0);
        pi4.setProgress(0);
        pi5.setProgress(0);
    }

    @FXML
    private void onRetourPanier() {
        try {
            MainFX.chargerVue("/CartView.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("status"));
    }



}
