package controllers;

import controllers.marketplace.ProductCategoryManagementController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminDashboardController {
    @FXML private BorderPane dashboardPane;
    @FXML private VBox sidebar;
    @FXML private Button usersButton;
    @FXML private Button profileButton;
    @FXML private Button communitiesButton;
    @FXML private Button requestButton;
    @FXML private Button communitiesStatButton;
    @FXML private Button postsButton;
    @FXML private Button eventsButton;
    @FXML private Button categoriesButton;
    @FXML private Button productCategoriesButton;
    @FXML private Button logoutButton;
    @FXML private Label adminLabel;
    @FXML private VBox center;

    public void setCenterContent(javafx.scene.Node node) {
        System.out.println("Setting center content: " + node);
        dashboardPane.setCenter(node);
    }

    @FXML
    public void initialize() {
        adminLabel.setText("Admin");
        loadView("/UserManagementView.fxml");
    }

    @FXML
    public void showUsers(ActionEvent actionEvent) {
        loadView("/UserManagementView.fxml");
    }

    @FXML
    public void showProfile(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminProfile.fxml"));
            Parent root = loader.load();
            AdminProfileController profileController = loader.getController();
            profileController.setAdminDashboardController(this);
            profileController.loadAdminDetails();
            dashboardPane.setCenter(root);
        } catch (IOException e) {
            showError("Failed to load admin profile: " + e.getMessage());
        }
    }

    @FXML
    public void showCommunities(ActionEvent actionEvent) {
        loadView("/community/CommunityManagementView.fxml");
    }

    @FXML
    public void showCommunitiesStat(ActionEvent actionEvent){
         loadView("/static/post_static.fxml");
    }

    @FXML
    public void showRequest(ActionEvent actionEvent){

    }

    @FXML
    public void showPosts(ActionEvent actionEvent) {
        loadView("/community/PostManagementView.fxml");
    }

    @FXML
    public void showEvents(ActionEvent actionEvent) {
        loadView("/event/EventManagementView.fxml");
    }

    @FXML
    public void showCategories(ActionEvent actionEvent) {
        loadView("/event-category/CategoryManagementView.fxml");
    }
    @FXML
    public void showProductCategories(ActionEvent actionEvent) {
        System.out.println("showProductCategories called");
        try {
            java.net.URL resourceUrl = getClass().getResource("/marketPlace/ProductCategoryManagementView.fxml");
            if (resourceUrl == null) {
                throw new IllegalStateException("Resource not found: /marketPlace/ProductCategoryManagementView.fxml");
            }
            System.out.println("Loading resource: " + resourceUrl);
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();
            ProductCategoryManagementController categoryController = loader.getController();
            System.out.println("Setting dashboardController for categoryController: this = " + this);
            categoryController.setDashboardController(this);
            dashboardPane.setCenter(root);
            System.out.println("Product category view set to dashboardPane. Scene: " + (dashboardPane.getScene() != null ? "Attached" : "Not attached"));
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
            showError("Failed to load product category management view: " + e.getMessage());
        }
    }
    @FXML
    public void logout(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            showError("Failed to logout: " + e.getMessage());
        } catch (IllegalStateException e) {
            e.printStackTrace();
            showError("Failed to find FXML file: /Login.fxml \n" + e.getMessage());
        }
    }

    private void loadView(String fxmlPath) {
        try {
            java.net.URL resourceUrl = getClass().getResource(fxmlPath);
            if (resourceUrl == null) {
                throw new IllegalStateException("Resource not found: " + fxmlPath);
            }
            System.out.println("Loading resource: " + resourceUrl);
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent viewRoot = loader.load();
            dashboardPane.setCenter(viewRoot);
            System.out.println("View " + fxmlPath + " set to dashboardPane. Scene: " + (dashboardPane.getScene() != null ? "Attached" : "Not attached"));
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load view: " + fxmlPath + "\n" + e.getMessage());
        } catch (IllegalStateException e) {
            e.printStackTrace();
            showError("Failed to find FXML file: " + fxmlPath + "\nCheck if the file exists in the resources directory.\n" + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }

    private void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Error", message);
    }
}