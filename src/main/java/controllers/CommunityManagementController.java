package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Community;
import services.CommunityService;
import enums.CategoryGrp;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CommunityManagementController {
    @FXML private TableView<Community> communityTable;
    @FXML private TableColumn<Community, String> nameColumn;
    @FXML private TableColumn<Community, String> descriptionColumn;
    @FXML private TableColumn<Community, String> categoryColumn;
    @FXML private TableColumn<Community, String> bannerColumn;
    @FXML private TableColumn<Community, String> creationDateColumn;
    @FXML private TableColumn<Community, Void> actionColumn;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField bannerField;
    @FXML private Button addButton;

    private final CommunityService communityService = new CommunityService();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        // Initialize ComboBox with CategoryGrp values
        categoryComboBox.getItems().addAll(CategoryGrp.getAllValues());
        categoryComboBox.setPromptText("Select Category");

        // Set up table columns
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
        categoryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory().getValue()));
        bannerColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBanner() != null ? cellData.getValue().getBanner() : ""));
        creationDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCreationDate() != null
                        ? cellData.getValue().getCreationDate().format(dateFormatter)
                        : ""));

        // Set up action column with Edit and Delete buttons
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox actionBox = new HBox(10, editButton, deleteButton);

            {
                editButton.getStyleClass().add("action-button");
                deleteButton.getStyleClass().add("delete-button");
                editButton.setOnAction(event -> {
                    Community community = getTableView().getItems().get(getIndex());
                    editCommunity(community);
                });
                deleteButton.setOnAction(event -> {
                    Community community = getTableView().getItems().get(getIndex());
                    deleteCommunity(community);
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

        // Load communities
        loadCommunities();
    }

    private void loadCommunities() {
        List<Community> communities = communityService.recuperer();
        communityTable.getItems().setAll(communities);
    }

    @FXML
    private void addCommunity() {
        String name = nameField.getText().trim();
        String description = descriptionField.getText().trim();
        String categoryValue = categoryComboBox.getSelectionModel().getSelectedItem();
        String banner = bannerField.getText().trim();

        if (name.isEmpty() || description.isEmpty() || categoryValue == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Name, description, and category are required.");
            return;
        }

        try {
            CategoryGrp category = CategoryGrp.fromValue(categoryValue);
            Community community = new Community();
            community.setName(name);
            community.setDescription(description);
            community.setCategory(category);
            community.setBanner(banner.isEmpty() ? null : banner);
            community.setCreationDate(LocalDateTime.now());

            communityService.ajouter(community);
            loadCommunities();
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Community added successfully!");
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid category: " + e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add community: " + e.getMessage());
        }
    }

    private void editCommunity(Community community) {
        Dialog<Community> dialog = new Dialog<>();
        dialog.setTitle("Edit Community");
        dialog.setHeaderText("Edit Community Details");

        // Set up dialog fields
        TextField nameInput = new TextField(community.getName());
        nameInput.getStyleClass().add("text-input");
        TextArea descriptionInput = new TextArea(community.getDescription());
        descriptionInput.getStyleClass().add("text-input");
        descriptionInput.setPrefHeight(100);
        ComboBox<String> categoryInput = new ComboBox<>();
        categoryInput.getStyleClass().add("combo-box");
        categoryInput.getItems().addAll(CategoryGrp.getAllValues());
        categoryInput.setValue(community.getCategory().getValue());
        TextField bannerInput = new TextField(community.getBanner() != null ? community.getBanner() : "");
        bannerInput.getStyleClass().add("text-input");

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Name:"), nameInput,
                new Label("Description:"), descriptionInput,
                new Label("Category:"), categoryInput,
                new Label("Banner URL:"), bannerInput
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        dialog.getDialogPane().setStyle("-fx-background-color: linear-gradient(to bottom right, #ffffff, #e0f2f1); -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 4, 4); -fx-padding: 20;");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String name = nameInput.getText().trim();
                String description = descriptionInput.getText().trim();
                String categoryValue = categoryInput.getValue();
                String banner = bannerInput.getText().trim();

                if (name.isEmpty() || description.isEmpty() || categoryValue == null) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Name, description, and category are required.");
                    return null;
                }

                try {
                    Community updatedCommunity = new Community();
                    updatedCommunity.setId(community.getId());
                    updatedCommunity.setName(name);
                    updatedCommunity.setDescription(description);
                    updatedCommunity.setCategory(CategoryGrp.fromValue(categoryValue));
                    updatedCommunity.setBanner(banner.isEmpty() ? null : banner);
                    updatedCommunity.setCreationDate(community.getCreationDate());
                    return updatedCommunity;
                } catch (IllegalArgumentException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Invalid category: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedCommunity -> {
            try {
                communityService.modifier(updatedCommunity);
                loadCommunities();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Community updated successfully!");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to edit community: " + e.getMessage());
            }
        });
    }

    private void deleteCommunity(Community community) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete " + community.getName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    communityService.supprimer(community.getId());
                    loadCommunities();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Community deleted successfully!");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete community: " + e.getMessage());
                }
            }
        });
    }

    private void clearFields() {
        nameField.clear();
        descriptionField.clear();
        categoryComboBox.getSelectionModel().clearSelection();
        bannerField.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}