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
        primaryStage = stage;
        chargerVue("/CartView.fxml");
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
