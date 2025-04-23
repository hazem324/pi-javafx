package controllers;

import entities.Category;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import services.CategoryService;

import java.sql.SQLException;

public class AjouterCategoryController {

    @FXML
    private TextField nameField;

    @FXML
    private TextArea descField;

    private HomePageController parentController;

    // Method to set the parent controller
    public void setParentController(HomePageController parentController) {
        this.parentController = parentController;
    }

    @FXML
    public void submit(ActionEvent actionEvent) {
        String name = nameField.getText().trim();
        String description = descField.getText().trim();

        if (name.isEmpty() || description.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Champs manquants");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez remplir tous les champs !");
            alert.show();
            return;
        }

        CategoryService cs = new CategoryService();

        try {
            // Vérifier si la catégorie existe déjà
            if (cs.categoryExists(name)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Category already exists!");
                alert.show();
                return;
            }

            Category c = new Category(name, description);
            cs.ajouter(c);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Catégorie ajoutée avec succès !");
            alert.show();

            // Réinitialiser les champs
            nameField.clear();
            descField.clear();

        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Une erreur est survenue");
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    @FXML
    public void showall(ActionEvent actionEvent) {
        parentController.goToAfficherCategory(actionEvent);
    }

    public void showCommunities(ActionEvent actionEvent) {
    }

    public void showAddCommunity(ActionEvent actionEvent) {
    }

    public void showPosts(ActionEvent actionEvent) {
    }

    public void showEventCategory(ActionEvent actionEvent) {
    }

    public void showAddCategory(ActionEvent actionEvent) {
    }

    public void showEvent(ActionEvent actionEvent) {
    }

    public void showAddEvent(ActionEvent actionEvent) {

    }
}