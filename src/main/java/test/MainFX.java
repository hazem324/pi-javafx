package test;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainFX extends Application {


    public static Stage primaryStage;

    public static void main(String[] args) {
       
        System.out.println("Launching application...");
        launch(args);
    }

    @Override
    public void start(Stage stage) {

       try {
            // Test getUserCommunity(1)  
            System.out.println("Loading FXML...");
            FXMLLoader loader = new FXMLLoader();
            String initialFxml = "/login.fxml"; // Initial view
            java.net.URL fxmlUrl = getClass().getResource(initialFxml);
            if (fxmlUrl == null) {
                System.err.println("FXML file not found at " + initialFxml);
                Platform.exit();
                return;
            }
            loader.setLocation(fxmlUrl);
            Parent root = loader.load();

            System.out.println("Creating scene...");
            Scene scene = new Scene(root, 1000, 600);

            System.out.println("Setting up stage...");
            primaryStage = stage; // Store the stage
            primaryStage.setTitle("CultureSpace");
            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest(e -> Platform.exit());

            System.out.println("Showing stage...");
            primaryStage.show();

            System.out.println("Application started successfully");
        } catch (IOException e) {
            System.err.println("Failed to load FXML: " + e.getMessage());
            e.printStackTrace();
            Platform.exit();
        }
    }


    public static void chargerVue(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        java.net.URL fxmlUrl = MainFX.class.getResource(fxmlPath);
        if (fxmlUrl == null) {
            throw new IOException("FXML file not found at " + fxmlPath);
        }
        loader.setLocation(fxmlUrl);
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("CultureSpace");
        primaryStage.show();
    }
}