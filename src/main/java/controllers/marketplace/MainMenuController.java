package controllers.marketplace;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainMenuController {
     @FXML
    private BorderPane bp;

    @FXML
    private void openCategoryUI() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplace/product_category_list.fxml"));
        Parent root = loader.load();
        bp.setCenter(root);
        
    }

    @FXML
    private void openProductUI() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplace/product_list.fxml"));
        Parent root = loader.load();
        bp.setCenter(root);
       
    }

    private void loadUI(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}