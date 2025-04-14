package controllers;

import entities.User;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import services.UserService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AdminDashboardController {
    @FXML
    private VBox sidebar;

    @FXML
    private Button usersButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Label adminLabel;

    @FXML
    private VBox usersPane;

    @FXML
    private Button addUserButton;

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, Number> idColumn;

    @FXML
    private TableColumn<User, String> firstNameColumn;

    @FXML
    private TableColumn<User, String> lastNameColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, Boolean> isBlockedColumn;

    @FXML
    private TableColumn<User, String> rolesColumn;

    @FXML
    private TableColumn<User, Void> actionsColumn;

    @FXML
    public void initialize() {
        // Configure TableView columns
        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()));
        firstNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFirstName()));
        lastNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLastName()));
        emailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        isBlockedColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isBlocked()));
        rolesColumn.setCellValueFactory(cellData -> {
            List<String> roles = cellData.getValue().getRoles();
            return new SimpleStringProperty(roles != null ? String.join(", ", roles) : "");
        });

        // Configure Actions column with Edit and Delete buttons
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttons = new HBox(10, editButton, deleteButton);

            {
                editButton.setStyle("-fx-background-color: #1e90ff; -fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-background-radius: 5; -fx-padding: 5 15 5 15;");
                deleteButton.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-background-radius: 5; -fx-padding: 5 15 5 15;");
                buttons.setStyle("-fx-alignment: CENTER;");
                editButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    editUser(user);
                });
                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    deleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });

        // Load users on startup
        showUsers(null);
    }

    @FXML
    public void showUsers(ActionEvent actionEvent) {
        UserService us = new UserService();
        try {
            List<User> users = us.recuperer();
            usersTable.setItems(FXCollections.observableArrayList(users));
            usersPane.setVisible(true);
        } catch (SQLException e) {
            showError("Failed to load users: " + e.getMessage());
        }
    }

    @FXML
    public void addUser(ActionEvent actionEvent) {
        // TODO: Implement Create operation
        showAlert(Alert.AlertType.INFORMATION, "Add User", "Create user functionality to be implemented.");
    }

    @FXML
    public void editUser(User user) {
        // TODO: Implement Update operation
        showAlert(Alert.AlertType.INFORMATION, "Edit User", "Update user functionality to be implemented for user: " + user.getEmail());
    }

    @FXML
    public void deleteUser(User user) {
        // TODO: Implement Delete operation
        showAlert(Alert.AlertType.INFORMATION, "Delete User", "Delete user functionality to be implemented for user: " + user.getEmail());
    }

    @FXML
    public void logout(ActionEvent actionEvent) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
        try {
            Parent root = loader.load();
            logoutButton.getScene().setRoot(root);
        } catch (IOException e) {
            showError("Failed to logout: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }

    private void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Erreur", message);
    }
}