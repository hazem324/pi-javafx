package controllers.statistique;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.chart.PieChart;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;

public class PostStaticController {

    @FXML
    private Label totalCommunities;

    @FXML
    private Label totalPosts;

    @FXML
    private PieChart communityPieChart;

    @FXML
    private PieChart userPieChart;

    @FXML
    private StackPane communityChartContainer;

    @FXML
    private StackPane userChartContainer;

    private Label tooltipLabel;

    @FXML
    public void initialize() {
        // Initialize tooltip label
        tooltipLabel = new Label();
        tooltipLabel.setStyle("-fx-background-color: #333333; -fx-text-fill: #ffffff; -fx-font-size: 12; -fx-padding: 5; -fx-background-radius: 5; -fx-opacity: 0.9;");
        tooltipLabel.setVisible(false);

        // Static data for testing
        int totalCommunitiesCount = 10;
        int totalPostsCount = 100;

        // Static data for community pie chart
        PieChart.Data[] communityData = {
            new PieChart.Data("Community A", 30),
            new PieChart.Data("Community B", 20),
            new PieChart.Data("Community C", 15),
            new PieChart.Data("Community D", 10)
        };

        // Static data for user pie chart
        PieChart.Data[] userData = {
            new PieChart.Data("John Doe", 25),
            new PieChart.Data("Jane Smith", 20),
            new PieChart.Data("Alice Johnson", 15),
            new PieChart.Data("Bob Brown", 10)
        };

        // Update UI
        totalCommunities.setText(String.valueOf(totalCommunitiesCount));
        totalPosts.setText(String.valueOf(totalPostsCount));

        // Populate community pie chart
        communityPieChart.getData().addAll(communityData);
        communityPieChart.setPrefSize(500, 500); // Larger chart size
        communityPieChart.setMinSize(500, 500); // Ensure minimum size

        // Populate user pie chart
        userPieChart.getData().addAll(userData);
        userPieChart.setPrefSize(500, 500); // Larger chart size
        userPieChart.setMinSize(500, 500); // Ensure minimum size

        // Add hover effects for community pie chart
        for (PieChart.Data data : communityPieChart.getData()) {
            Node node = data.getNode();
            node.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
                tooltipLabel.setText(data.getName() + ": " + (int)data.getPieValue() + " posts");
                tooltipLabel.setVisible(true);
                tooltipLabel.setTranslateX(event.getX());
                tooltipLabel.setTranslateY(event.getY() - 20);
            });
            node.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
                tooltipLabel.setVisible(false);
            });
        }

        // Add hover effects for user pie chart
        for (PieChart.Data data : userPieChart.getData()) {
            Node node = data.getNode();
            node.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
                tooltipLabel.setText(data.getName() + ": " + (int)data.getPieValue() + " posts");
                tooltipLabel.setVisible(true);
                tooltipLabel.setTranslateX(event.getX());
                tooltipLabel.setTranslateY(event.getY() - 20);
            });
            node.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
                tooltipLabel.setVisible(false);
            });
        }

        // Add tooltip label to the scene (assuming charts are in StackPanes)
        if (communityPieChart.getParent() instanceof StackPane) {
            ((StackPane) communityPieChart.getParent()).getChildren().add(tooltipLabel);
        }
        if (userPieChart.getParent() instanceof StackPane) {
            ((StackPane) userPieChart.getParent()).getChildren().add(tooltipLabel);
        }
    }
}