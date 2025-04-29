package controllers.community;

import controllers.sideBar.MainSidebar;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import models.Community;
import services.CommunityService;
import javafx.scene.image.Image;
import javafx.scene.text.Font;

//import services.UserService;

import java.io.IOException;
import java.util.List;

public class CommunityListController {
    @FXML
    private GridPane communitiesGrid;
    @FXML
    private Text userName;
    @FXML
    private ImageView userAvatar;
    private  MainSidebar mainSidebar;
    @FXML

    private final CommunityService communityService = new CommunityService();
  //  private final UserService userService = new UserService();

    @FXML
    public void initialize() {
      //  loadUserInfo();
        loadCommunities();
    }

    /*private void loadUserInfo() {
        // Load current user info
        String currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            userName.setText(currentUser);
        }
    }*/

    private void loadCommunities() {
        List<Community> communities = communityService.recuperer();
        int column = 0;
        int row = 0;
        int maxColumns = 3; // Number of columns in the grid

        for (Community community : communities) {
            VBox communityCard = createCommunityCard(community);
            communitiesGrid.add(communityCard, column, row);
            
            column++;
            if (column >= maxColumns) {
                column = 0;
                row++;
            }
        }
    }

    private VBox createCommunityCard(Community community) {
    VBox card = new VBox(10);
    card.getStyleClass().add("community-card");
    card.setPadding(new Insets(10));
    card.setAlignment(Pos.TOP_CENTER);
    card.setPrefWidth(220);
    card.setPrefHeight(220);

    // Image (Banner)
    ImageView imageView = new ImageView();
    imageView.setFitWidth(180);
    imageView.setFitHeight(115);
    imageView.setPreserveRatio(true);
    imageView.setPickOnBounds(true);

    if (community.getBanner() != null && !community.getBanner().isEmpty()) {
        try {
            Image image = new Image(community.getBanner(), true);
            imageView.setImage(image);
        } catch (Exception e) {
            System.err.println("Failed to load image: " + e.getMessage());
        }
    }

    // Name Label
    Label nameLabel = new Label(community.getName());
    nameLabel.getStyleClass().add("community-name");
    nameLabel.setFont(Font.font("Cambria", 24));

    // Category Label
    Label categoryLabel = new Label(community.getCategory().getValue());
    categoryLabel.getStyleClass().add("community-category");
    categoryLabel.setFont(Font.font("Cambria Bold", 18));

    card.getChildren().addAll(nameLabel, imageView, categoryLabel);
    card.setOnMouseClicked(event -> showCommunityDetails(community));

    return card;
}


    private void showCommunityDetails(Community community) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Community Details");
        alert.setHeaderText(community.getName());
        
        VBox content = new VBox(10);
        content.getStyleClass().add("alert-dialog");
        
        Label descriptionLabel = new Label("Description: " + community.getDescription());
        descriptionLabel.getStyleClass().add("alert-description");
        
        Label categoryLabel = new Label("Category: " + community.getCategory());
        categoryLabel.getStyleClass().add("alert-description");
        
        Button joinButton = new Button("Request to Join");
        joinButton.getStyleClass().add("join-button");
        joinButton.setOnAction(event -> {
            // Implement join request logic here
            alert.close();
        });
        
        content.getChildren().addAll(descriptionLabel, categoryLabel, joinButton);
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }

    @FXML
    private void goToProfile() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/profile/Profile.fxml"));
            Stage stage = (Stage) communitiesGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void setMainSidebarController(MainSidebar mainSidebar) {
        this.mainSidebar = mainSidebar;
    }
    @FXML
    private void goToHome() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home/HomePage.fxml"));
            Stage stage = (Stage) communitiesGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showAllCommunities() {
        // Already on the communities list page
    }

    @FXML
    private void showMyCommunities() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/community/MyCommunities.fxml"));
            Stage stage = (Stage) communitiesGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
       /* userService.logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/auth/Login.fxml"));
            Stage stage = (Stage) communitiesGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}
