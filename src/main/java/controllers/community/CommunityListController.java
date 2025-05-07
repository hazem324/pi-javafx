package controllers.community;

import controllers.sideBar.MainSidebar;
import entities.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import models.Community;
import utils.SessionManager;
import services.CommunityService;
import services.JoinRequestService;
import javafx.scene.image.Image;
import javafx.scene.text.Font;

//import services.UserService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class CommunityListController {

    private User userLogin;
    

    @FXML
    private GridPane communitiesGrid;
    @FXML
    private Text userName;
    @FXML
    private ImageView userAvatar;
    private  MainSidebar mainSidebar;
    @FXML

    private final CommunityService communityService = new CommunityService();
    private final JoinRequestService joinRequestService = new JoinRequestService();
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

   userLogin = SessionManager.getCurrentUser();

     userLogin.getId();
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Community Details");
        dialog.setResizable(true);

        // Custom dialog pane content
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_CENTER);
        content.setPrefWidth(500);
        content.setPrefHeight(500);

        // Image view (banner)
        ImageView imageView = new ImageView();
        imageView.setFitWidth(400);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        if (community.getBanner() != null && !community.getBanner().isEmpty()) {
            try {
                imageView.setImage(new Image(community.getBanner(), true));
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
            }
        }

        // Name label
        Label nameLabel = new Label(community.getName());
        nameLabel.setFont(Font.font("Cambria", 26));
        nameLabel.getStyleClass().add("alert-title");

        // Description
        Label descriptionLabel = new Label("Description:\n" + community.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setFont(Font.font("System", 14));
        descriptionLabel.getStyleClass().add("alert-description");

        // Category
        Label categoryLabel = new Label("Category: " + community.getCategory().getValue());
        categoryLabel.setFont(Font.font("System", 14));
        categoryLabel.getStyleClass().add("alert-description");

        // Join button
        Button joinButton = new Button("🚀 Request to Join");
        joinButton.getStyleClass().add("join-button");
        joinButton.setOnAction(event -> {
            if (userLogin.getId() < 0) {
                showAlert(Alert.AlertType.ERROR, "Error", "You must be logged in to join a community.");
                return;
            }

            // Call sendJoinRequest with userId and communityId
            String result;
            try {
                result = joinRequestService.sendJoinRequest(userLogin.getId(),community.getId());
                showAlert(Alert.AlertType.INFORMATION, "Join Request", result);
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            dialog.close();
        });

        content.getChildren().addAll(imageView, nameLabel, descriptionLabel, categoryLabel, joinButton);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Load custom CSS
        try {
            String cssPath = getClass().getResource("/assets/style/CommunityList.css").toExternalForm();
            dialog.getDialogPane().getStylesheets().add(cssPath);
        } catch (NullPointerException e) {
            System.err.println("CSS file not found: CommunityList.css");
        }

        dialog.showAndWait();
    }

    // Helper method to show alerts
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
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
