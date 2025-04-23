package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import java.net.URL;

import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.sql.SQLException;
import models.Community;
import models.Post;
import models.PostComment;
import models.User;
import services.CommunityService;
import services.PostService;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class CommunityPostsController {
    @FXML
    private Text communityName;
    @FXML
    private Text userName;
    @FXML
    private ImageView userAvatar;
    @FXML
    private TextArea postContent;
    @FXML
    private ImageView postImagePreview;
    @FXML
    private VBox postsContainer;

    private final CommunityService communityService = new CommunityService();
    private final PostService postService = new PostService();
    private Community currentCommunity;
    private File selectedImageFile;
    private String imageUrl;
    private final int USER_ID = 1; // Hardcoded user ID
    private final int COMMUNITY_ID = 1; // Hardcoded community ID

    @FXML
    public void initialize() throws SQLException {
        loadUserInfo();
        loadCommunityInfo();
        loadPosts();
    }

    private void loadUserInfo() {
        // Hardcode user info for user ID 1
        userName.setText("User One"); // Replace with actual user name or fetch from DB if needed
        // Optionally load user avatar (hardcoded or from DB)
        userAvatar.setImage(new Image("file:src/main/resources/images/default-avatar.png")); // Adjust path
    }

    private void loadCommunityInfo() {
        currentCommunity = communityService.getCommunityById(COMMUNITY_ID);
        if (currentCommunity != null) {
            communityName.setText("Publications de la Communauté - " + currentCommunity.getName());
        } else {
            communityName.setText("Publications de la Communauté - Unknown");
        }
    }

    private void loadPosts() throws SQLException {
        List<Post> posts = postService.getPostsByCommunityId(COMMUNITY_ID);
        postsContainer.getChildren().clear();

        for (Post post : posts) {
            VBox postNode = createPostNode(post);
            postsContainer.getChildren().add(postNode);
        }
    }


    private HBox createCommentNode(PostComment comment) {
        HBox commentNode = new HBox(10);
        commentNode.getStyleClass().add("comment");

        ImageView authorImage = new ImageView();
        authorImage.getStyleClass().add("comment-author-image");
        authorImage.setImage(new Image("file:src/main/resources/images/default-avatar.png")); // Adjust path

        VBox commentContent = new VBox(5);
        Text authorName = new Text(comment.getUser().getFirstName());
        authorName.getStyleClass().add("comment-author-name");

        Text content = new Text(comment.getPcommentContent());
        content.getStyleClass().add("comment-content");
        content.setWrappingWidth(550);

        commentContent.getChildren().addAll(authorName, content);
        commentNode.getChildren().addAll(authorImage, commentContent);

        return commentNode;
    }

    @FXML
    private void handleImageUpload() {
        // Create a TextInputDialog to prompt the user for an image URL
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Upload Image URL");
        dialog.setHeaderText("Enter the URL of the image");
        dialog.setContentText("Image URL:");

        // Show the dialog and wait for the user's input
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(url -> {
            try {
                // Validate the URL by attempting to load the image
                Image image = new Image(url, true); // true means load asynchronously
                // Check if the image loaded successfully
                if (image.isError()) {
                    showAlert("Error", "Failed to load image from URL: " + image.getException().getMessage());
                    return;
                }
                // Set the image in the preview
                postImagePreview.setImage(image);
                postImagePreview.setVisible(true);
                // Store the URL instead of a File object
                selectedImageFile = null; // Clear any previous file reference
                // Update the post submission logic to use the URL directly
                this.imageUrl = url; // Add a new field to store the URL
            } catch (Exception e) {
                showAlert("Error", "Invalid image URL: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handlePostSubmit() {
        String content = postContent.getText();
        if (content.isEmpty()) {
            showAlert("Error", "Post content cannot be empty");
            return;
        }

        String imagePath = imageUrl; // Use the URL directly
        try {
            postService.addPostToCommunity(content, imagePath, COMMUNITY_ID, USER_ID);
            postContent.clear();
            postImagePreview.setVisible(false);
            imageUrl = null; // Clear the URL after submission
            loadPosts();
        } catch (SQLException e) {
            showAlert("Error", "Failed to create post: " + e.getMessage());
        }
    }

    private VBox createPostNode(Post post) {
        VBox postNode = new VBox(15);
        postNode.getStyleClass().add("timeline-post");
        postNode.setId("post-" + post.getId()); // Set ID for lookup

        // Post Header
        HBox header = new HBox(10);
        header.getStyleClass().add("post-header");

        ImageView authorImage = new ImageView();
        authorImage.getStyleClass().add("post-author-image");

        // Load author image safely
        String authorImagePath = "/assets/logo.jpg";
        java.net.URL authorImageUrl = getClass().getResource(authorImagePath);
        if (authorImageUrl != null) {
            Image image = new Image(authorImageUrl.toExternalForm());
            authorImage.setImage(image);
        } else {
            System.err.println("Author image resource not found: " + authorImagePath);
            Image defaultImage = new Image("https://via.placeholder.com/50");
            authorImage.setImage(defaultImage);
        }

        Text authorName = new Text(post.getUser().getFirstName());
        authorName.getStyleClass().add("post-author-name");

        Text postDate = new Text(post.getCreationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        postDate.getStyleClass().add("post-date");

        header.getChildren().addAll(authorImage, authorName, postDate);

        // Post Content
        Text content = new Text(post.getContent());
        content.getStyleClass().add("post-content");
        content.setWrappingWidth(600);

        // Post Image if exists (safe loading)
        ImageView postImage = null;
        if (post.getPostImg() != null && !post.getPostImg().isEmpty()) {
            try {
                String postImgPath = post.getPostImg();
                Image image;

                if (postImgPath.startsWith("http://") || postImgPath.startsWith("https://")) {
                    // Load from external URL
                    image = new Image(postImgPath);
                } else {
                    // Load from local resource
                    URL resourceUrl = getClass().getResource(postImgPath.startsWith("/") ? postImgPath : "/" + postImgPath);
                    if (resourceUrl != null) {
                        image = new Image(resourceUrl.toExternalForm());
                    } else {
                        System.err.println("Post image not found: " + postImgPath);
                        image = new Image("https://via.placeholder.com/300x200");
                    }
                }

                postImage = new ImageView(image);
                postImage.getStyleClass().add("post-image");
            } catch (Exception e) {
                System.err.println("Error loading post image: " + e.getMessage());
            }
        }

        // Post Actions
        HBox actionsBar = new HBox(15);
        actionsBar.getStyleClass().add("post-actions-bar");

        Button likeButton = new Button("Like (" + post.getLikes() + ")");
        likeButton.getStyleClass().add("action-button");
        likeButton.setOnAction(event -> {
            try {
                handleLike(post);
            } catch (SQLException e) {
                showAlert("Error", "Failed to like post: " + e.getMessage());
            }
        });

        Button commentButton = new Button("Comment");
        commentButton.getStyleClass().add("action-button");
        commentButton.setOnAction(event -> showCommentInput(post));

        actionsBar.getChildren().addAll(likeButton, commentButton);

        // Comments Section
        VBox commentsSection = new VBox(10);
        commentsSection.getStyleClass().add("comments-section");

        for (PostComment comment : post.getPostComments()) {
            HBox commentNode = createCommentNode(comment);
            commentsSection.getChildren().add(commentNode);
        }

        // Assemble everything
        postNode.getChildren().add(header);
        postNode.getChildren().add(content);
        if (postImage != null) {
            postNode.getChildren().add(postImage);
        }
        postNode.getChildren().add(actionsBar);
        postNode.getChildren().add(commentsSection);

        return postNode;
    }


    private void handleLike(Post post) throws SQLException {
        if (postService.likePost(post.getId())) {
            loadPosts();
        }
    }

    private void showCommentInput(Post post) {
        TextArea commentInput = new TextArea();
        commentInput.getStyleClass().add("comment-input");
        commentInput.setPromptText("Write a comment...");

        Button submitButton = new Button("Comment");
        submitButton.getStyleClass().add("comment-submit-button");
        submitButton.setOnAction(event -> {
            String commentContent = commentInput.getText();
            if (!commentContent.isEmpty()) {
                PostComment comment = new PostComment();
                comment.setPcommentContent(commentContent);
                comment.setPost(post);
                comment.setUser(new User()); // Set minimal user info
                comment.getUser().setId(USER_ID);
                comment.getUser().setFirstName("User One"); // Adjust as needed

                if (postService.addComment(comment)) {
                    try {
                        loadPosts();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        HBox commentInputContainer = new HBox(10);
        commentInputContainer.getStyleClass().add("comment-input-container");
        commentInputContainer.getChildren().addAll(commentInput, submitButton);

        VBox postNode = (VBox) postsContainer.lookup("#post-" + post.getId());
        if (postNode != null) {
            VBox commentsSection = (VBox) postNode.getChildren().get(postNode.getChildren().size() - 1);
            commentsSection.getChildren().add(commentInputContainer);
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void goToProfile() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/profile/Profile.fxml"));
            Stage stage = (Stage) postsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToHome() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home/HomePage.fxml"));
            Stage stage = (Stage) postsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showAllCommunities() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/community/Community-Liste-Page.fxml"));
            Stage stage = (Stage) postsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showMyCommunities() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/community/MyCommunities.fxml"));
            Stage stage = (Stage) postsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/auth/Login.fxml"));
            Stage stage = (Stage) postsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}