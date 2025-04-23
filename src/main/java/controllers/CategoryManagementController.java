package controllers;

import entities.Category;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import services.CategoryService;

import java.sql.SQLException;
import java.util.List;

public class CategoryManagementController {
    @FXML private TableView<Category> categoryTable;
    @FXML private TableColumn<Category, String> nameColumn;
    @FXML private TableColumn<Category, String> descriptionColumn;
    @FXML private TableColumn<Category, Void> actionColumn;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private Button addButton;

    private final CategoryService categoryService = new CategoryService();

    @FXML
    public void initialize() {
        // Set up table columns
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));

        // Set up action column
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox actionBox = new HBox(10, editButton, deleteButton);

            {
                editButton.getStyleClass().add("action-button");
                deleteButton.getStyleClass().add("delete-button");
                editButton.setOnAction(event -> {
                    Category category = getTableView().getItems().get(getIndex());
                    editCategory(category);
                });
                deleteButton.setOnAction(event -> {
                    Category category = getTableView().getItems().get(getIndex());
                    deleteCategory(category);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(actionBox);
                }
            }
        });

        // Load categories
        loadCategories();
    }

    private void loadCategories() {
        try {
            List<Category> categories = categoryService.recuperer();
            categoryTable.getItems().setAll(categories);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load categories: " + e.getMessage());
        }
    }

    @FXML
    private void addCategory() {
        String name = nameField.getText().trim();
        String description = descriptionField.getText().trim();

        if (name.isEmpty() || description.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Name and description are required.");
            return;
        }

        try {
            if (categoryService.categoryExists(name)) {
                showAlert(Alert.AlertType.ERROR, "Error", "Category with this name already exists.");
                return;
            }

            Category category = new Category();
            category.setName(name);
            category.setDescription(description);

            categoryService.ajouter(category);
            loadCategories();
            clearForm();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Category added successfully!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add category: " + e.getMessage());
        }
    }

    private void editCategory(Category category) {
        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle("Edit Category");
        dialog.setHeaderText("Edit Category Details");

        TextField nameInput = new TextField(category.getName());
        TextArea descriptionInput = new TextArea(category.getDescription());
        descriptionInput.setPrefHeight(100);

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Name:"), nameInput,
                new Label("Description:"), descriptionInput
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String name = nameInput.getText().trim();
                String description = descriptionInput.getText().trim();

                if (name.isEmpty() || description.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Name and description are required.");
                    return null;
                }
                try {
                    if (!name.equals(category.getName()) && categoryService.categoryExists(name)) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Category with this name already exists.");
                        return null;
                    }
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to validate category: " + e.getMessage());
                    return null;
                }

                Category updatedCategory = new Category();
                updatedCategory.setId(category.getId());
                updatedCategory.setName(name);
                updatedCategory.setDescription(description);
                return updatedCategory;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedCategory -> {
            try {
                categoryService.modifier(updatedCategory);
                loadCategories();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Category updated successfully!");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update category: " + e.getMessage());
            }
        });
    }

    private void deleteCategory(Category category) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this category?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    categoryService.supprimer(category);
                    loadCategories();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Category deleted successfully!");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete category: " + e.getMessage());
                }
            }
        });
    }

    private void clearForm() {
        nameField.clear();
        descriptionField.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}