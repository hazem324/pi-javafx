package controllers.marketplace;

import controllers.AdminDashboardController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import models.ProductCategory;
import services.ProductCategoryService;

import java.io.IOException;

public class ProductCategoryController {
    // List view components
    @FXML private TableView<ProductCategory> categoryTable;
    @FXML private TableColumn<ProductCategory, Number> idColumn;
    @FXML private TableColumn<ProductCategory, String> nameColumn;
    @FXML private TableColumn<ProductCategory, String> descriptionColumn;
    @FXML private TableColumn<ProductCategory, Void> actionsColumn;
    @FXML private Button addButton;

    // Form view components
    @FXML private Label formTitle;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final ProductCategoryService service = new ProductCategoryService();
    private final ObservableList<ProductCategory> categoryList = FXCollections.observableArrayList();
    private ProductCategory editingCategory;
    private AdminDashboardController dashboardController;

    public ProductCategoryController() {
        System.out.println("ProductCategoryController instantiated");
    }

    public void setDashboardController(AdminDashboardController dashboardController) {
        this.dashboardController = dashboardController;
        System.out.println("setDashboardController called: dashboardController = " + (dashboardController != null ? "Not null" : "Null"));
    }

    @FXML
    public void initialize() {
        System.out.println("Initializing ProductCategoryController: dashboardController = " + (dashboardController != null ? "Not null" : "Null"));
        if (categoryTable != null) {
            System.out.println("Initializing TableView for product categories");
            try {
                idColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()));
                nameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
                descriptionColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDescription()));
                actionsColumn.setCellFactory(param -> new TableCell<>() {
                    private final Button editButton = new Button("Edit");
                    private final Button deleteButton = new Button("Delete");
                    {
                        editButton.setStyle("-fx-background-color: #f6c107; -fx-text-fill: white; -fx-background-radius: 6;");
                        deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 6;");
                        editButton.setOnAction(e -> {
                            ProductCategory category = getTableView().getItems().get(getIndex());
                            showEditForm(category);
                        });
                        deleteButton.setOnAction(e -> {
                            ProductCategory category = getTableView().getItems().get(getIndex());
                            handleDelete(category);
                        });
                    }
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox buttons = new HBox(10, editButton, deleteButton);
                            buttons.setStyle("-fx-alignment: center;");
                            setGraphic(buttons);
                        }
                    }
                });
                refreshTable();
                System.out.println("TableView initialized with " + categoryList.size() + " categories");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to initialize table: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
        if (nameField != null && categoryTable == null) {
            System.out.println("Initializing form view");
        }
    }

    @FXML
    private void showAddForm() {
        System.out.println("showAddForm called: dashboardController = " + (dashboardController != null ? "Not null" : "Null"));
        try {
            java.net.URL formUrl = getClass().getResource("/marketPlace/product_category_form.fxml");
            if (formUrl == null) {
                throw new IllegalStateException("Resource not found: /marketPlace/product_category_form.fxml");
            }
            System.out.println("Loading add form: " + formUrl);
            FXMLLoader loader = new FXMLLoader(formUrl);
            loader.setControllerFactory(clazz -> {
                ProductCategoryController controller = new ProductCategoryController();
                controller.setDashboardController(dashboardController);
                return controller;
            });
            Parent formRoot = loader.load();
            ProductCategoryController formController = loader.getController();
            formController.formTitle.setText("Add Category");
            formController.saveButton.setText("Save");
            if (dashboardController != null) {
                dashboardController.setCenterContent(formRoot);
                System.out.println("Add form loaded and set to dashboardPane");
            } else {
                System.err.println("Warning: dashboardController is null in showAddForm, using fallback Stage");
                showAlert("Warning", "Dashboard controller not initialized. Opening form in new window.", Alert.AlertType.WARNING);
                Stage stage = new Stage();
                stage.setScene(new Scene(formRoot));
                stage.setTitle("Add Category");
                stage.show();
            }
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void showEditForm(ProductCategory category) {
        System.out.println("showEditForm called: dashboardController = " + (dashboardController != null ? "Not null" : "Null"));
        try {
            java.net.URL formUrl = getClass().getResource("/marketPlace/product_category_form.fxml");
            if (formUrl == null) {
                throw new IllegalStateException("Resource not found: /marketPlace/product_category_form.fxml");
            }
            System.out.println("Loading edit form: " + formUrl);
            FXMLLoader loader = new FXMLLoader(formUrl);
            loader.setControllerFactory(clazz -> {
                ProductCategoryController controller = new ProductCategoryController();
                controller.setDashboardController(dashboardController);
                return controller;
            });
            Parent formRoot = loader.load();
            ProductCategoryController formController = loader.getController();
            formController.setEditingCategory(category);
            formController.fillForm(category);
            formController.formTitle.setText("Edit Category");
            formController.saveButton.setText("Update");
            if (dashboardController != null) {
                dashboardController.setCenterContent(formRoot);
                System.out.println("Edit form loaded and set to dashboardPane");
            } else {
                System.err.println("Warning: dashboardController is null in showEditForm, using fallback Stage");
                showAlert("Warning", "Dashboard controller not initialized. Opening form in new window.", Alert.AlertType.WARNING);
                Stage stage = new Stage();
                stage.setScene(new Scene(formRoot));
                stage.setTitle("Edit Category");
                stage.show();
            }
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void showListView() {
        System.out.println("showListView called: dashboardController = " + (dashboardController != null ? "Not null" : "Null"));
        try {
            java.net.URL listUrl = getClass().getResource("/marketPlace/product_category_list.fxml");
            if (listUrl == null) {
                throw new IllegalStateException("Resource not found: /marketPlace/product_category_list.fxml");
            }
            System.out.println("Loading list view: " + listUrl);
            FXMLLoader loader = new FXMLLoader(listUrl);
            loader.setControllerFactory(clazz -> {
                ProductCategoryController controller = new ProductCategoryController();
                controller.setDashboardController(dashboardController);
                return controller;
            });
            Parent listRoot = loader.load();
            if (dashboardController != null) {
                dashboardController.setCenterContent(listRoot);
                System.out.println("List view loaded and set to dashboardPane");
            } else {
                System.err.println("Warning: dashboardController is null in showListView, using fallback Stage");
                showAlert("Warning", "Dashboard controller not initialized. Opening list in new window.", Alert.AlertType.WARNING);
                Stage stage = new Stage();
                stage.setScene(new Scene(listRoot));
                stage.setTitle("Category List");
                stage.show();
            }
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open category list: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }
        ProductCategory category = editingCategory != null ? editingCategory : new ProductCategory();
        category.setName(nameField.getText());
        category.setDescription(descriptionField.getText());
        try {
            if (editingCategory == null) {
                service.addCategory(category);
                showAlert("Success", "Category created successfully!", Alert.AlertType.INFORMATION);
            } else {
                service.updateCategory(category);
                showAlert("Success", "Category updated successfully!", Alert.AlertType.INFORMATION);
            }
            showListView();
        } catch (Exception e) {
            showAlert("Error", "Failed to save category: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleDelete(ProductCategory category) {
        try {
            service.deleteCategory(category.getId());
            showAlert("Success", "Category deleted successfully!", Alert.AlertType.INFORMATION);
            refreshTable();
        } catch (Exception e) {
            showAlert("Error", "Failed to delete category: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setEditingCategory(ProductCategory category) {
        this.editingCategory = category;
    }

    private void fillForm(ProductCategory category) {
        nameField.setText(category.getName());
        descriptionField.setText(category.getDescription());
    }

    private void refreshTable() {
        try {
            categoryList.setAll(service.getAllCategories());
            categoryTable.setItems(categoryList);
            System.out.println("Refreshed table with " + categoryList.size() + " categories");
        } catch (Exception e) {
            System.err.println("Error refreshing table: " + e.getMessage());
            throw e;
        }
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        String name = nameField.getText();
        if (name == null || name.trim().isEmpty()) {
            errors.append("Name cannot be blank.\n");
        } else if (name.length() < 4) {
            errors.append("Name must be at least 4 characters long.\n");
        }
        String description = descriptionField.getText();
        if (description == null || description.trim().isEmpty()) {
            errors.append("Description cannot be blank.\n");
        } else if (description.length() < 10) {
            errors.append("Description must be at least 10 characters long.\n");
        }
        if (errors.length() > 0) {
            showAlert("Validation Error", errors.toString(), Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}