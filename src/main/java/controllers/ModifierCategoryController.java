package controllers;

import entities.Category;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import services.CategoryService;

import java.io.IOException;
import java.sql.SQLException;

public class ModifierCategoryController {

    @FXML
    private TextField nameField;

    @FXML
    private TextArea descField;

    @FXML
    private AfficherCategoryController parentController;

    private HomePageController homePageController;

    private Category selectedCategory;

    private CategoryService categoryService;

    // Méthode pour initialiser la catégorie sélectionnée et le contrôleur parent
    public void setCategoryAndParent(Category category, AfficherCategoryController parentController) {
        System.out.println("Setting category and parent controller...");
        this.selectedCategory = category;
        this.parentController = parentController;
        this.categoryService = new CategoryService();

        // Pré-remplir les champs avec les données de la catégorie
        System.out.println("Pre-filling fields: Name = " + category.getName() + ", Description = " + category.getDescription());
        nameField.setText(category.getName());
        descField.setText(category.getDescription());
    }

    // Method to set the HomePageController
    public void setParentController(HomePageController homePageController) {
        this.homePageController = homePageController;
    }

    @FXML
    public void update(ActionEvent actionEvent) {
        System.out.println("Update button clicked!");
        String name = nameField.getText().trim();
        String description = descField.getText().trim();

        // Contrôle de saisie : champs vides
        if (name.isEmpty() || description.isEmpty()) {
            System.out.println("Validation failed: Empty fields");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Champs manquants");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez remplir tous les champs !");
            alert.showAndWait();
            return;
        }

        // Contrôle de saisie : description d'au moins 10 caractères
        if (description.length() < 10) {
            System.out.println("Validation failed: Description too short (" + description.length() + " characters)");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de validation");
            alert.setHeaderText(null);
            alert.setContentText("La description doit contenir au moins 10 caractères !");
            alert.showAndWait();
            return;
        }

        try {
            // Vérifier si le nom existe déjà (sauf si c'est le même nom que la catégorie actuelle)
            if (!name.equals(selectedCategory.getName()) && categoryService.categoryExists(name)) {
                System.out.println("Validation failed: Category name already exists");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Category already exists!");
                alert.showAndWait();
                return;
            }

            // Mettre à jour la catégorie
            System.out.println("Updating category: " + selectedCategory.getId());
            selectedCategory.setName(name);
            selectedCategory.setDescription(description);
            categoryService.modifier(selectedCategory);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Catégorie mise à jour avec succès !");
            alert.showAndWait();

            // Rafraîchir la table dans AfficherCategoryController
            System.out.println("Refreshing table in parent controller...");
            parentController.refreshTable();

            // Revenir à la page AfficherCategory
            cancel(actionEvent);

        } catch (SQLException e) {
            System.out.println("SQLException during update: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Une erreur est survenue");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void cancel(ActionEvent actionEvent) {
        System.out.println("Cancel button clicked!");
        homePageController.goToAfficherCategory(actionEvent);
    }
}