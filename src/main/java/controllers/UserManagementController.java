package controllers;

import entities.User;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.UserService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class UserManagementController {

    @FXML
    private Label totalUsersLabel;
    @FXML
    private Label activeUsersLabel;
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
    private HBox paginationContainer; // Replace Pagination with an HBox
    private int totalUserCount = 0;
    private int currentPage = 0;
    private final int USERS_PER_PAGE = 10; // Adjust as needed
    private List<User> allUsers; // Keep a local copy of all users


    private final UserService userService = new UserService();
    private final ObservableList<User> usersOnPage = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        setupActionColumn();
        loadAllUsers(); // Load all users
        updatePagination(); // Initial pagination setup
        usersTable.setItems(usersOnPage);

    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        isBlockedColumn.setCellValueFactory(new PropertyValueFactory<>("blocked"));
        rolesColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                String.join(", ", cellData.getValue().getRoles())
        ));
    }

    private void setupActionColumn() {
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final Button toggleBlockButton = new Button();
            private final HBox buttons = new HBox(10, editButton, deleteButton, toggleBlockButton);

            {
                // Apply styles (consider moving to CSS)
                editButton.setStyle("-fx-background-color: #00897b; -fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-font-family: 'Segoe UI', sans-serif; -fx-background-radius: 8; -fx-padding: 8 15 8 15; -fx-cursor: hand;");
                deleteButton.setStyle("-fx-background-color: #e57373; -fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-font-family: 'Segoe UI', sans-serif; -fx-background-radius: 8; -fx-padding: 8 15 8 15; -fx-cursor: hand;");
                toggleBlockButton.setStyle("-fx-background-color: #ffb300; -fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-font-family: 'Segoe UI', sans-serif; -fx-background-radius: 8; -fx-padding: 8 15 8 15; -fx-cursor: hand;");
                buttons.setStyle("-fx-alignment: CENTER;");

                editButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    if (user != null) editUser(user);
                });

                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    if (user != null) deleteUser(user);
                });

                toggleBlockButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    if (user != null) toggleBlockUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    if (user != null) {
                        toggleBlockButton.setText(user.isBlocked() ? "Unblock" : "Block");
                        setGraphic(buttons);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    void loadAllUsers() {
        try {
            allUsers = userService.recuperer();
            totalUserCount = allUsers.size();
            loadStats(); // Update stats after loading users
            showUsersOnPage(currentPage);

        } catch (SQLException e) {
            showError("Failed to load users: " + e.getMessage());
        }
    }

    private void loadStats() {
        try {
            long activeUsers = allUsers.stream().filter(user -> !user.isBlocked()).count();
            totalUsersLabel.setText("Total Users: " + totalUserCount);
            activeUsersLabel.setText("Active Users: " + activeUsers);
        } catch (Exception e) {
            showError("Failed to load stats: " + e.getMessage());
            totalUsersLabel.setText("Total Users: N/A");
            activeUsersLabel.setText("Active Users: N/A");
        }
    }

    private void updatePagination() {
        paginationContainer.getChildren().clear(); // Clear old buttons
        int pageCount = (int) Math.ceil((double) totalUserCount / USERS_PER_PAGE);
        if (pageCount <= 1) return; // No pagination needed

        // First Button
        Button firstButton = new Button("First");
        firstButton.setOnAction(event -> {
            currentPage = 0;
            showUsersOnPage(currentPage);
            updatePagination();
        });
        paginationContainer.getChildren().add(firstButton);

        // Previous Button
        Button prevButton = new Button("Previous");
        prevButton.setOnAction(event -> {
            if (currentPage > 0) {
                currentPage--;
                showUsersOnPage(currentPage);
                updatePagination();
            }
        });
        paginationContainer.getChildren().add(prevButton);

        // Page Number Buttons
        int maxPagesToShow = 5;
        int startPage = Math.max(0, currentPage - maxPagesToShow / 2);
        int endPage = Math.min(pageCount - 1, currentPage + maxPagesToShow / 2);

        if (endPage - startPage < maxPagesToShow - 1) {
            if (startPage == 0) {
                endPage = Math.min(pageCount - 1, maxPagesToShow - 1);
            } else if (endPage == pageCount - 1) {
                startPage = Math.max(0, pageCount - maxPagesToShow);
            }
        }

        for (int i = startPage; i <= endPage; i++) {
            final int page = i;
            Button pageButton = new Button(String.valueOf(page + 1));
            if (page == currentPage) {
                pageButton.setStyle("-fx-font-weight: bold; -fx-background-color: #b0bec5;"); // Highlight current page
            }
            pageButton.setOnAction(event -> {
                currentPage = page;
                showUsersOnPage(currentPage);
                updatePagination();
            });
            paginationContainer.getChildren().add(pageButton);
        }

        // Next Button
        Button nextButton = new Button("Next");
        nextButton.setOnAction(event -> {
            if (currentPage < pageCount - 1) {
                currentPage++;
                showUsersOnPage(currentPage);
                updatePagination();
            }
        });
        paginationContainer.getChildren().add(nextButton);

        // Last Button
        Button lastButton = new Button("Last");
        lastButton.setOnAction(event -> {
            currentPage = pageCount - 1;
            showUsersOnPage(currentPage);
            updatePagination();
        });
        paginationContainer.getChildren().add(lastButton);

    }
    private void showUsersOnPage(int page) {
        usersOnPage.clear();
        int start = page * USERS_PER_PAGE;
        int end = Math.min(start + USERS_PER_PAGE, allUsers.size());
        if (start <= allUsers.size()) {
            usersOnPage.addAll(allUsers.subList(start, end));
        }
    }


    @FXML
    public void addUser(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddUserForm.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add New User");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait(); // Wait for the form to close

            loadAllUsers(); // Refresh the list after the modal closes
            updatePagination();

        } catch (IOException e) {
            showError("Failed to load add user form: " + e.getMessage());
        }
    }

    public void editUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditUserForm.fxml"));
            Parent root = loader.load();
            EditUserFormController editUserController = loader.getController();
            // Pass data needed for editing
            editUserController.initData(user);
            // No need to pass this controller if refresh is handled below

            Stage stage = new Stage();
            stage.setTitle("Edit User");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait(); // Wait for the form to close

            loadAllUsers(); // Refresh the list after the modal closes
            updatePagination();

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
                loadAllUsers(); // Refresh the table AND update stats
                updatePagination();
                showAlert(Alert.AlertType.INFORMATION, "Success", "User deleted successfully.");
            } catch (SQLException e) {
                showError("Could not delete user: " + e.getMessage());
            }
        }
    }

    public void toggleBlockUser(User user) {
        boolean newBlockedStatus = !user.isBlocked();
        try {
            userService.updateBlockStatus(user.getId(), newBlockedStatus);
            loadAllUsers(); // Reload users which implicitly refreshes table and stats
            updatePagination();
            showAlert(Alert.AlertType.INFORMATION, "User Block Status", "User " + user.getEmail() + " is now " + (newBlockedStatus ? "blocked" : "unblocked") + ".");
        } catch (SQLException e) {
            showError("Failed to update block status: " + e.getMessage());
        }
    }

    // --- Utility Methods ---
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }

    private void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Error", message);
    }
}

