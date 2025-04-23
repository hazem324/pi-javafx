package controllers;

import entities.Category;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import services.CategoryService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AfficherCategoryController{

    @FXML
    private TableView<Category> categoryTable;

    @FXML
    private TableColumn<Category, String> nameColumn;

    @FXML
    private TableColumn<Category, String> descColumn;

    @FXML
    private Button updateButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button backButton;

    private CategoryService categoryService;

    private HomePageController parentController;

    public void setParentController(HomePageController parentController) {
        this.parentController = parentController;
        System.out.println("ParentController set: " + (parentController != null));
    }

    @FXML
    public void initialize() {
        categoryService = new CategoryService();
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        descColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
        loadCategories();
    }

    private void loadCategories() {
        try {
            List<Category> categories = categoryService.recuperer();
            categoryTable.getItems().setAll(categories);
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors du chargement des catégories");
            alert.setContentText("Impossible de charger les catégories : " + e.getMessage());
            alert.show();
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    public void refreshTable() {
        loadCategories();
    }

    @FXML
    public void update(ActionEvent actionEvent) {
        Category selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner une catégorie à modifier !");
            alert.show();
            return;
        }

        if (parentController == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de configuration");
            alert.setHeaderText(null);
            alert.setContentText("Le contrôleur parent n'est pas défini.");
            alert.show();
            System.out.println("Error: parentController is null in update");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifiercategory.fxml"));
            Parent pageContent = loader.load();

            ModifierCategoryController controller = loader.getController();
            controller.setCategoryAndParent(selectedCategory, this);
            controller.setParentController(parentController);

            parentController.loadPage("/modifiercategory.fxml", "Update Category Page");
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de navigation");
            alert.setHeaderText(null);
            alert.setContentText("Impossible d'ouvrir la page de modification : " + e.getMessage());
            alert.show();
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    @FXML
    public void delete(ActionEvent actionEvent) {
        Category selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner une catégorie à supprimer !");
            alert.show();
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to delete this category?");
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                categoryService.supprimer(selectedCategory);
                loadCategories();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Catégorie supprimée avec succès !");
                alert.show();
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Erreur lors de la suppression");
                alert.setContentText(e.getMessage());
                alert.show();
            }
        }
    }

    @FXML
    public void back(ActionEvent actionEvent) {
        if (parentController == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de configuration");
            alert.setHeaderText(null);
            alert.setContentText("Le contrôleur parent n'est pas défini.");
            alert.show();
            System.out.println("Error: parentController is null in back");
            return;
        }
        parentController.goToAjouterCategory(actionEvent);
    }

}