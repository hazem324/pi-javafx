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
        // Initialize CommunityService
        communityService = new CommunityService();

        // Get the logged-in user
        userLogin = SessionManager.getCurrentUser();

        // Check if user is logged in
        if (userLogin == null) {
            System.out.println("No user logged in!");
            showAlert("Error", "Please log in to view your communities.");
            return;
        }

        // Fetch communities for the user
        List<Community> communities = communityService.getUserCommunity(userLogin.getId());
        System.out.println("User ID: " + userLogin.getId() + ", Communities found: " + communities.size());

        // Populate the GridPane with community cards
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
        String bannerUrl = community.getBanner();
        try {
            if (bannerUrl != null && !bannerUrl.trim().isEmpty() && 
                (bannerUrl.startsWith("http://") || bannerUrl.startsWith("https://"))) {
                Image bannerImage = new Image(bannerUrl, true);
                if (bannerImage.isError()) {
                    throw new IllegalArgumentException("Failed to load image: " + bannerImage.getException().getMessage());
                }
                bannerView.setImage(bannerImage);
            } else {
                throw new IllegalArgumentException("Invalid or empty banner URL");
            }
            bannerView.setFitWidth(180);
            bannerView.setFitHeight(120);
            bannerView.setPreserveRatio(true);
            bannerView.setStyle("-fx-background-radius: 5;");
        } catch (Exception e) {
            // Fallback to placeholder URL
            bannerView.setImage(new Image("https://via.placeholder.com/180x120"));
            bannerView.setFitWidth(180);
            bannerView.setFitHeight(120);
            System.err.println("Failed to load banner for community " + community.getName() + ": " + e.getMessage());
        }

        // Community name
        Label nameLabel = new Label(community.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
        nameLabel.setWrapText(true);

        // Community category
        Label categoryLabel = new Label("Category: " + community.getCategory().toString());
        categoryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        // Add components to card
        card.getChildren().addAll(bannerView, nameLabel, categoryLabel);

        // Add click event handler to navigate to community posts
        card.setOnMouseClicked((MouseEvent event) -> {
            if (community.getId() <= 0) {
                showAlert("Error", "Invalid community ID: " + community.getId());
                return;
            }
            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/community/Community-Posts.fxml"));
                    Parent root = loader.load();
                    bp.setCenter(root);
                if (loader.getLocation() == null) {
                    throw new IOException("FXML file not found at /community/Community-Posts.fxml");
                }
               
                // Get the CommunityPostsController and set the community ID
                CommunityPostsController controller = loader.getController();
                controller.setCommunityId(community.getId());
                controller.start(); // Call start to initialize after setting communityId

                Stage stage = (Stage) MYcommunityGrid.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException | SQLException e) {
                System.err.println("Failed to load Community-Posts.fxml: " + e.getMessage());
                showAlert("Error", "Unable to load community posts page: " + e.getMessage());
            }
        });

        return card;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}