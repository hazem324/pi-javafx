package controllers.community;

import entities.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Community;
import services.CommunityService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class MyCommunityController {

    @FXML
    private BorderPane bp;

    @FXML
    private GridPane MYcommunityGrid;

    private User userLogin;
    private CommunityService communityService;

    @FXML
    public void initialize() {
        communityService = new CommunityService();
        userLogin = SessionManager.getCurrentUser();

        if (userLogin == null) {
            System.out.println("No user logged in!");
            showAlert("Error", "Please log in to view your communities.");
            return;
        }

        loadUserCommunities();
    }

    private void loadUserCommunities() {
        List<Community> communities = communityService.getUserCommunity(userLogin.getId());
        System.out.println("User ID: " + userLogin.getId() + ", Communities found: " + communities.size());

        MYcommunityGrid.getChildren().clear();

        if (communities.isEmpty()) {
            Label noCommunitiesLabel = new Label("You are not a member of any communities yet.");
            noCommunitiesLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
            MYcommunityGrid.add(noCommunitiesLabel, 0, 0);
        } else {
            int column = 0;
            int row = 0;
            for (Community community : communities) {
                System.out.println("Community ID: " + community.getId() + ", Name: " + community.getName());
                VBox card = createCommunityCard(community);
                MYcommunityGrid.add(card, column, row);
                column++;
                if (column == 3) { // 3 cards per row
                    column = 0;
                    row++;
                }
            }
        }
    }

    private VBox createCommunityCard(Community community) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(10));
        card.getStyleClass().add("community-card");
        card.setPrefWidth(200);
        card.setPrefHeight(250);

        // Banner image
        ImageView bannerView = new ImageView();
        loadCommunityBanner(bannerView, community.getBanner());

        // Community name
        Label nameLabel = new Label(community.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
        nameLabel.setWrapText(true);

        // Community category
        Label categoryLabel = new Label("Category: " + community.getCategory().toString());
        categoryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        card.getChildren().addAll(bannerView, nameLabel, categoryLabel);
        card.setOnMouseClicked(event -> handleCommunityCardClick(event, community));

        return card;
    }

    private void loadCommunityBanner(ImageView imageView, String bannerUrl) {
        try {
            Image image;
            if (bannerUrl != null && !bannerUrl.trim().isEmpty()) {
                if (bannerUrl.startsWith("http://") || bannerUrl.startsWith("https://")) {
                    image = new Image(bannerUrl, true);
                } else {
                    // Handle local resources
                    String resourcePath = bannerUrl.startsWith("/") ? bannerUrl : "/" + bannerUrl;
                    image = new Image(getClass().getResourceAsStream(resourcePath));
                }
            } else {
                throw new IllegalArgumentException("No banner URL provided");
            }

            imageView.setImage(image);
            imageView.setFitWidth(180);
            imageView.setFitHeight(120);
            imageView.setPreserveRatio(true);
            imageView.setStyle("-fx-background-radius: 5;");
        } catch (Exception e) {
            System.err.println("Failed to load banner: " + e.getMessage());
            imageView.setImage(new Image("https://via.placeholder.com/180x120"));
            imageView.setFitWidth(50);
            imageView.setFitHeight(50);
        }
    }

    private void handleCommunityCardClick(MouseEvent event, Community community) {
        if (community.getId() <= 0) {
            showAlert("Error", "Invalid community ID: " + community.getId());
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/community/Community-Posts.fxml"));
            Parent root = loader.load();

            // Get the controller and set the community ID
            CommunityPostsController controller = loader.getController();
            controller.setCommunityId(community.getId());
            controller.start();

            // Update the current view instead of creating new stage
            bp.setCenter(root);

        } catch (IOException e) {
            System.err.println("Failed to load Community-Posts.fxml: " + e.getMessage());
            showAlert("Error", "Unable to load community posts page");
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            showAlert("Error", "A database error occurred");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}