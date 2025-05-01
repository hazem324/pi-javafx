package tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainFX extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/CartView.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.setTitle("Test Panier avec Code Promo");
        stage.show();
    }

    public static void chargerVue(String fxmlPath) throws IOException, IOException {
        FXMLLoader loader = new FXMLLoader(MainFX.class.getResource(fxmlPath));
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("CultureSpace");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
