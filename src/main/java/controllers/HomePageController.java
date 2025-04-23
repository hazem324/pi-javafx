package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class HomePageController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private Button ajouterCategoryButton;

    @FXML
    private Button afficherCategoryButton;

    @FXML
    private Button modifierCategoryButton;

    @FXML
    private Button ajouterEventButton;

    @FXML
    private Button afficherEventButton;

    @FXML
    public void goToAjouterCategory(ActionEvent actionEvent) {
        loadPage("/ajoutercategory.fxml", "Add Category Page");
    }

    @FXML
    public void goToAfficherCategory(ActionEvent actionEvent) {
        loadPage("/affichercategory.fxml", "Show Categories Page");
    }

    @FXML
    public void goToModifierCategory(ActionEvent actionEvent) {
        loadPage("/modifiercategory.fxml", "Update Category Page");
    }

    @FXML
    public void goToAjouterEvent(ActionEvent actionEvent) {
        loadPage("/ajouterevent.fxml", "Add Event Page");
    }

    @FXML
    public void goToAfficherEvent(ActionEvent actionEvent) {
        loadPage("/afficherevent.fxml", "Show Events Page");
    }

    // Helper method to load a page into the center area
    public void loadPage(String fxmlPath, String pageName) {
        try {
            // Validate inputs
            if (fxmlPath == null || fxmlPath.trim().isEmpty()) {
                throw new IllegalArgumentException("FXML path cannot be null or empty");
            }
            if (rootPane == null) {
                throw new IllegalStateException("Root pane is not initialized");
            }

            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (loader.getLocation() == null) {
                throw new IOException("Cannot find FXML file at path: " + fxmlPath);
            }
            Parent pageContent = loader.load();

            // Set the parent controller if the loaded controller implements ParentAwareController
            Object controller = loader.getController();
            if (controller instanceof ParentAwareController) {
                ((ParentAwareController) controller).setParentController(this);
            } else if (controller != null) {
                System.out.println("Controller for " + pageName + " does not implement ParentAwareController: " + controller.getClass().getName());
            }

            // Load the content into the center of the BorderPane
            rootPane.setCenter(pageContent);
        } catch (Exception e) {
            String errorMessage = "Unable to load the page: " + e.getMessage() + "\n" + getStackTraceAsString(e);
            System.err.println("Error loading page " + pageName + ": " + errorMessage);
            e.printStackTrace();
            showErrorAlert("Error navigating to " + pageName, errorMessage);
        }
    }

    // Getter for the content area (returns the BorderPane)
    public BorderPane getContentArea() {
        return rootPane;
    }

    // Helper method to show an error alert
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Helper method to convert stack trace to string
    private String getStackTraceAsString(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.toString()).append("\n");
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}