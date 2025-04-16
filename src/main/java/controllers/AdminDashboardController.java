package controllers;

import entities.User;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.UserService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

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
    private HBox welcomeCard;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label totalUsersLabel;

    @FXML
    private Label activeUsersLabel;

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

    // Removed Pagination
    // @FXML
    // private Pagination pagination;

    private UserService userService = new UserService();
    private ObservableList<User> allUsers = FXCollections.observableArrayList();
    // Removed usersPerPage
    // private final int usersPerPage = 10;

    @FXML
    public void initialize() {
        // Configure TableView columns using PropertyValueFactory
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        isBlockedColumn.setCellValueFactory(new PropertyValueFactory<>("blocked"));
        rolesColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.join(", ", cellData.getValue().getRoles())));

        // Configure Actions column with Edit, Delete, and Block/Unblock buttons
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final Button toggleBlockButton = new Button();
            private final HBox buttons = new HBox(10, editButton, deleteButton, toggleBlockButton);

            {
                editButton.setStyle("-fx-background-color: #00897b; -fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-font-family: 'Segoe UI', sans-serif; -fx-background-radius: 8; -fx-padding: 8 15 8 15; -fx-cursor: hand;");
                deleteButton.setStyle("-fx-background-color: #e57373; -fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-font-family: 'Segoe UI', sans-serif; -fx-background-radius: 8; -fx-padding: 8 15 8 15; -fx-cursor: hand;");
                toggleBlockButton.setStyle("-fx-background-color: #ffb300; -fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-font-family: 'Segoe UI', sans-serif; -fx-background-radius: 8; -fx-padding: 8 15 8 15; -fx-cursor: hand;");
                buttons.setStyle("-fx-alignment: CENTER;");
                editButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    editUser(user);
                });
                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    deleteUser(user);
                });
                toggleBlockButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    toggleBlockUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView().getItems().get(getIndex()) == null) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    toggleBlockButton.setText(user.isBlocked() ? "Unblock" : "Block");
                    setGraphic(buttons);
                }
            }
        });

        // Load all users and stats on startup
        loadAllUsers();
        loadStats();

        // Directly set all users to the table
        usersTable.setItems(allUsers);
    }

    void loadAllUsers() {
        try {
            List<User> users = userService.recuperer();
            allUsers.setAll(users);
        } catch (SQLException e) {
            showError("Failed to load users: " + e.getMessage());
        }
    }

    private void loadStats() {
        try {
            long totalUsers = allUsers.size();
            long activeUsers = allUsers.stream().filter(user -> !user.isBlocked()).count();
            totalUsersLabel.setText("Total Users: " + totalUsers);
            activeUsersLabel.setText("Active Users: " + activeUsers);
        } catch (Exception e) {
            showError("Failed to load stats: " + e.getMessage());
            totalUsersLabel.setText("Total Users: N/A");
            activeUsersLabel.setText("Active Users: N/A");
        }
    }

    @FXML
    public void showUsers(ActionEvent actionEvent) {
        usersPane.setVisible(true);
    }

    @FXML
    public void addUser(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddUserForm.fxml"));
            Parent root = loader.load();

            AddUserFormController addUserFormController = loader.getController();
            addUserFormController.setAdminDashboardController(this); // Pass the current controller

            Stage stage = new Stage();
            stage.setTitle("Add New User");
            stage.initModality(Modality.APPLICATION_MODAL); // Block interaction with the main window
            stage.setScene(new Scene(root));
            stage.showAndWait(); // Show the modal and wait for it to be closed
        } catch (IOException e) {
            showError("Failed to load add user form: " + e.getMessage());
        }
    }

    @FXML
    public void editUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditUserForm.fxml"));
            Parent root = loader.load();

            EditUserFormController editUserController = loader.getController();
            editUserController.setAdminDashboardController(this); // Pass the current controller
            editUserController.initData(user); // Pass the user data to the edit form

            Stage stage = new Stage();
            stage.setTitle("Edit User");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            showError("Failed to load edit user form: " + e.getMessage());
        }
    }

    public void deleteUser(User userToDelete) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete User");
        alert.setHeaderText("Confirm Delete");
        alert.setContentText("Are you sure you want to delete user: " + userToDelete.getFirstName() + " " + userToDelete.getLastName() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                userService.supprimer(userToDelete);
                loadAllUsers(); // Refresh the table
                showAlert(Alert.AlertType.INFORMATION, "Success", "User deleted successfully.");
            } catch (SQLException e) {
                showError("Could not delete user: " + e.getMessage());
            }
        }
    }

    @FXML
    public void toggleBlockUser(User user) {
        boolean newBlockedStatus = !user.isBlocked();
        try {
            userService.updateBlockStatus(user.getId(), newBlockedStatus);
            allUsers.stream()
                    .filter(u -> u.getId() == user.getId())
                    .findFirst()
                    .ifPresent(u -> u.setBlocked(newBlockedStatus));
            usersTable.refresh();
            loadStats();
            showAlert(Alert.AlertType.INFORMATION, "User Block Status", "User " + user.getEmail() + " is now " + (newBlockedStatus ? "blocked" : "unblocked") + ".");
        } catch (SQLException e) {
            showError("Failed to update block status: " + e.getMessage());
        }
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