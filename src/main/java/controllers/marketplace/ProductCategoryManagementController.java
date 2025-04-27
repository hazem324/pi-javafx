package controllers.marketplace;

import controllers.AdminDashboardController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import java.io.IOException;

public class ProductCategoryManagementController {
    @FXML private BorderPane root;
    private AdminDashboardController dashboardController;

    @FXML
    public void initialize() {
        System.out.println("ProductCategoryManagementController.initialize: dashboardController = " + (dashboardController != null ? "Not null" : "Null"));
        try {
            java.net.URL listUrl = getClass().getResource("/marketPlace/product_category_list.fxml");
            if (listUrl == null) {
                throw new IllegalStateException("Resource not found: /marketPlace/product_category_list.fxml");
            }
            System.out.println("Loading list view: " + listUrl);
            FXMLLoader loader = new FXMLLoader(listUrl);
            loader.setControllerFactory(clazz -> {
                ProductCategoryController controller = new ProductCategoryController();
                controller.setDashboardController(dashboardController);
                return controller;
            });
            Parent listView = loader.load();
            System.out.println("List view loaded");
            if (root.getScene() == null) {
                System.out.println("Warning: BorderPane root has no scene. Delaying setCenter.");
                root.sceneProperty().addListener((obs, oldScene, newScene) -> {
                    if (newScene != null) {
                        root.setCenter(listView);
                        System.out.println("List view set to BorderPane center after scene attachment.");
                    }
                });
            } else {
                root.setCenter(listView);
                System.out.println("List view set to BorderPane center. Scene: Attached");
            }
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
            showError("Failed to load product category list: " + e.getMessage());
        }
    }

    public void setDashboardController(AdminDashboardController dashboardController) {
        this.dashboardController = dashboardController;
        System.out.println("setDashboardController in ProductCategoryManagementController: dashboardController = " + (dashboardController != null ? "Not null" : "Null"));
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}