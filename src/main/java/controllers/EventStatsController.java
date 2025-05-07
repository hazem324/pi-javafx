package controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import services.EventRegistrationService;
import services.EventService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventStatsController {

    @FXML
    private BarChart<String, Number> registrationChart;

    @FXML
    private PieChart categoryChart;

    private AdminDashboardController dashboardController;

    public void setDashboardController(AdminDashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML
    public void initialize() {
        try {
            loadRegistrationChart();
            loadCategoryChart();
        } catch (SQLException e) {
            e.printStackTrace();
            // Optionally, show an alert to the user
        }
    }

    private void loadRegistrationChart() throws SQLException {
        EventRegistrationService registrationService = new EventRegistrationService();
        Map<String, Integer> registrationsPerEvent = registrationService.getRegistrationsPerEvent();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Registrations");

        for (Map.Entry<String, Integer> entry : registrationsPerEvent.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        registrationChart.getData().add(series);
    }

    private void loadCategoryChart() throws SQLException {
        EventService eventService = new EventService();
        Map<String, Integer> eventsByCategory = eventService.getEventsByCategory();

        for (Map.Entry<String, Integer> entry : eventsByCategory.entrySet()) {
            categoryChart.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
    }
}