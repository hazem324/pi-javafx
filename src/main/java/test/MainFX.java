package test;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.io.IOException;

import entities.User;


public class MainFX extends Application {

    
    // Étape 1 : Déclarer un stage statique
    public static Stage primaryStage;

    public static void main(String[] args) {
       
        System.out.println("Launching application...");
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            System.out.println("Loading FXML...");
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sideBar/main.fxml"));
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));

            Parent root = loader.load();

            System.out.println("Creating scene...");
            Scene scene = new Scene(root, 1000, 600);

            System.out.println("Setting up stage...");
            primaryStage = stage; // Étape 2 : Stocker le stage dans la variable statique
            primaryStage.setTitle("Community Manager");
            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest(e -> Platform.exit());

            System.out.println("Showing stage...");
            primaryStage.show();

            System.out.println("Application started successfully");
        } catch (IOException e) {
            System.err.println("Failed to load FXML:");
            e.printStackTrace();
            Platform.exit();
        }
    }

    // Étape 3 : Ajouter la méthode chargerVue
    public static void chargerVue(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainFX.class.getResource(fxmlPath));
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("CultureSpace");
        primaryStage.show();
    }
}
