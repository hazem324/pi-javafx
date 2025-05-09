package controllers.statistique;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.chart.PieChart;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import services.PostStatsService;
import models.UserPostStats;
import models.statique.CommunityPostStats;

import java.sql.SQLException;
import java.util.List;

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
    private PostStatsService postStatsService;

    @FXML
    public void initialize() {
        // Initialize PostStatsService
        postStatsService = new PostStatsService();

        // Initialize tooltip label
        tooltipLabel = new Label();
        tooltipLabel.setStyle("-fx-background-color: #333333; -fx-text-fill: #ffffff; -fx-font-size: 12; -fx-padding: 5; -fx-background-radius: 5; -fx-opacity: 0.9;");
        tooltipLabel.setVisible(false);

        try {
            // Fetch total communities and posts
            int totalCommunitiesCount = postStatsService.getTotalCommunities();
            int totalPostsCount = postStatsService.getTotalPosts();

            // Update labels
            totalCommunities.setText(String.valueOf(totalCommunitiesCount));
            totalPosts.setText(String.valueOf(totalPostsCount));

            // Fetch user post stats
            List<UserPostStats> userStats = postStatsService.getUserPostStats();

            // Populate user pie chart
            for (UserPostStats stat : userStats) {
                PieChart.Data data = new PieChart.Data(stat.getFullName(), stat.getPostCount());
                userPieChart.getData().add(data);
            }
            userPieChart.setPrefSize(500, 500);
            userPieChart.setMinSize(500, 500);

            // Note: Community pie chart data is commented out in the service.
            // If you uncomment and use getCommunityPostStats(), you can populate communityPieChart similarly:
            
            List<CommunityPostStats> communityStats = postStatsService.getCommunityPostStats();
            for (CommunityPostStats stat : communityStats) {
                PieChart.Data data = new PieChart.Data(stat.getCommunityName(), stat.getPostCount());
                communityPieChart.getData().add(data);
            }
            communityPieChart.setPrefSize(500, 500);
            communityPieChart.setMinSize(500, 500);
            

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

            // Add hover effects for community pie chart (if used)
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

            // Add tooltip label to the scene
            if (communityPieChart.getParent() instanceof StackPane) {
                ((StackPane) communityPieChart.getParent()).getChildren().add(tooltipLabel);
            }
            if (userPieChart.getParent() instanceof StackPane) {
                ((StackPane) userPieChart.getParent()).getChildren().add(tooltipLabel);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Optionally, show an alert to the user
            // Example: Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to load statistics: " + e.getMessage());
            // alert.showAndWait();
        }
    }
}