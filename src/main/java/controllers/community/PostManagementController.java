package controllers.community;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Community;
import models.Post;
import models.User;
import services.CommunityService;
import services.PostService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PostManagementController {
    @FXML private TableView<Post> postTable;
    @FXML private TableColumn<Post, String> contentColumn;
    @FXML private TableColumn<Post, String> imageColumn;
    @FXML private TableColumn<Post, String> communityColumn;
    @FXML private TableColumn<Post, String> userColumn;
    @FXML private TableColumn<Post, String> creationDateColumn;
    @FXML private TableColumn<Post, Integer> likesColumn;
    @FXML private TableColumn<Post, Void> actionColumn;
    @FXML private TextArea contentField;
    @FXML private TextField imageField;
    @FXML private ComboBox<Community> communityComboBox;
    @FXML private Button addButton;

    private final PostService postService = new PostService();
    private final CommunityService communityService = new CommunityService();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final int ADMIN_USER_ID = 1; // Hardcoded admin user ID, adjust as needed

    @FXML
    public void initialize() {
        // Initialize ComboBox with communities
        List<Community> communities = communityService.recuperer();
        communityComboBox.getItems().addAll(communities);
        communityComboBox.setCellFactory(lv -> new ListCell<Community>() {
            @Override
            protected void updateItem(Community item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        communityComboBox.setButtonCell(new ListCell<Community>() {
            @Override
            protected void updateItem(Community item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        communityComboBox.setPromptText("Select Community");

        // Set up table columns
        contentColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getContent()));
        imageColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPostImg() != null ? cellData.getValue().getPostImg() : ""));
        communityColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCommunity() != null ? cellData.getValue().getCommunity().getName() : "N/A"));
        userColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getUser() != null ? cellData.getValue().getUser().getFirstName() : "N/A"));
        creationDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCreationDate() != null
                        ? cellData.getValue().getCreationDate().format(dateFormatter)
                        : ""));
        likesColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(
                cellData.getValue().getLikes() != null ? cellData.getValue().getLikes() : 0).asObject());

        // Set up action column with Edit and Delete buttons
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox actionBox = new HBox(10, editButton, deleteButton);

            {
                editButton.getStyleClass().add("action-button");
                deleteButton.getStyleClass().add("delete-button");
                editButton.setOnAction(event -> {
                    Post post = getTableView().getItems().get(getIndex());
                    editPost(post);
                });
                deleteButton.setOnAction(event -> {
                    Post post = getTableView().getItems().get(getIndex());
                    deletePost(post);
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

        // Load posts
        loadPosts();
    }

    private void loadPosts() {
        try {
            List<Post> posts = postService.recuperer();
            postTable.getItems().setAll(posts);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load posts: " + e.getMessage());
        }
    }

    @FXML
    private void addPost() {
        String content = contentField.getText().trim();
        String imageUrl = imageField.getText().trim();
        Community selectedCommunity = communityComboBox.getSelectionModel().getSelectedItem();

        if (content.isEmpty() || selectedCommunity == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Post content and community are required.");
            return;
        }

        try {
            Post post = new Post();
            post.setContent(content);
            post.setPostImg(imageUrl.isEmpty() ? null : imageUrl);
            post.setCreationDate(LocalDateTime.now());
            post.setModificationDate(LocalDateTime.now());
            post.setLikes(0);
            post.setCommunity(selectedCommunity);
            User adminUser = new User();
            adminUser.setId(ADMIN_USER_ID);
            adminUser.setFirstName("Admin"); // Adjust as needed
            post.setUser(adminUser);

            postService.ajouter(post);
            loadPosts();
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Post added successfully!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add post: " + e.getMessage());
        }
    }

    private void editPost(Post post) {
        Dialog<Post> dialog = new Dialog<>();
        dialog.setTitle("Edit Post");
        dialog.setHeaderText("Edit Post Details");

        // Set up dialog fields
        TextArea contentInput = new TextArea(post.getContent());
        contentInput.getStyleClass().add("text-input");
        contentInput.setPrefHeight(100);
        TextField imageInput = new TextField(post.getPostImg() != null ? post.getPostImg() : "");
        imageInput.getStyleClass().add("text-input");
        ComboBox<Community> communityInput = new ComboBox<>();
        communityInput.getStyleClass().add("combo-box");
        communityInput.getItems().addAll(communityService.recuperer());
        communityInput.setCellFactory(lv -> new ListCell<Community>() {
            @Override
            protected void updateItem(Community item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        communityInput.setButtonCell(new ListCell<Community>() {
            @Override
            protected void updateItem(Community item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        communityInput.setValue(post.getCommunity());

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Content:"), contentInput,
                new Label("Image URL:"), imageInput,
                new Label("Community:"), communityInput
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        dialog.getDialogPane().setStyle("-fx-background-color: linear-gradient(to bottom right, #ffffff, #e0f2f1); -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 4, 4); -fx-padding: 20;");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String contentText = contentInput.getText().trim();
                String imageUrl = imageInput.getText().trim();
                Community selectedCommunity = communityInput.getValue();

                if (contentText.isEmpty() || selectedCommunity == null) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Content and community are required.");
                    return null;
                }

                Post updatedPost = new Post();
                updatedPost.setId(post.getId());
                updatedPost.setContent(contentText);
                updatedPost.setPostImg(imageUrl.isEmpty() ? null : imageUrl);
                updatedPost.setCreationDate(post.getCreationDate());
                updatedPost.setModificationDate(LocalDateTime.now());
                updatedPost.setLikes(post.getLikes());
                updatedPost.setCommunity(selectedCommunity);
                updatedPost.setUser(post.getUser());
                updatedPost.setPostComments(post.getPostComments());
                return updatedPost;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedPost -> {
            try {
                postService.modifier(updatedPost);
                loadPosts();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Post updated successfully!");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to edit post: " + e.getMessage());
            }
        });
    }

    private void deletePost(Post post) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this post?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    postService.supprimer(post.getId());
                    loadPosts();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Post deleted successfully!");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete post: " + e.getMessage());
                }
            }
        });
    }

    private void clearFields() {
        contentField.clear();
        imageField.clear();
        communityComboBox.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}