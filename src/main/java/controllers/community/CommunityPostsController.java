package controllers.community;

import entities.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import models.Community;
import models.Post;
import models.PostComment;
import models.Likes;
import services.CommunityService;
import services.PostService;
import services.LikesService;
import utils.SessionManager;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class CommunityPostsController {

    @FXML private TextArea postContent;
    @FXML private ImageView postImagePreview;
    @FXML private VBox postsContainer;

    private final CommunityService communityService = new CommunityService();
    private final PostService postService = new PostService();
    private final LikesService likesService = new LikesService();
    private Community currentCommunity;
    private String imageUrl;
    private int communityId;
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            System.out.println("No user logged in!");
            showAlert("Error", "Please log in to view community posts.");
        }
        debugImageLoading("/assets/images/placeholder.jpg");
    }

    public void setCommunityId(int communityId) {
        this.communityId = communityId;
    }

    public void start() throws SQLException {
        if (currentUser == null) return;
        loadCommunityInfo();
        if (currentCommunity != null) {
            loadPosts();
        }
    }

    private void loadCommunityInfo() {
        if (communityId <= 0) {
            showAlert("Error", "Invalid community ID: " + communityId);
            return;
        }
        currentCommunity = communityService.getCommunityById(communityId);
        if (currentCommunity == null) {
            showAlert("Warning", "Community not found for ID: " + communityId);
        }
    }

    private void loadPosts() throws SQLException {
        List<Post> posts = postService.getPostsByCommunityId(communityId);
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
        authorImage.setFitWidth(50);
        authorImage.setFitHeight(50);
        String avatarPath = "/assets/images/placeholder.jpg";
        loadImage(authorImage, avatarPath, "https://via.placeholder.com/50x50");

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
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Upload Image URL");
        dialog.setHeaderText("Enter the URL of the image");
        dialog.setContentText("Image URL:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(url -> {
            try {
                Image image = new Image(url, true);
                if (image.isError()) {
                    showAlert("Error", "Failed to load image from URL: " + image.getException().getMessage());
                    return;
                }
                postImagePreview.setImage(image);
                postImagePreview.setFitWidth(400);
                postImagePreview.setPreserveRatio(true);
                postImagePreview.setVisible(true);
                this.imageUrl = url;
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

        try {
            postService.addPostToCommunity(content, imageUrl, communityId, currentUser.getId());
            postContent.clear();
            postImagePreview.setVisible(false);
            imageUrl = null;
            loadPosts(); // Refresh the entire posts list
        } catch (SQLException e) {
            showAlert("Error", "Failed to create post: " + e.getMessage());
        }
    }

    private VBox createPostNode(Post post) {
        VBox postNode = new VBox(15);
        postNode.getStyleClass().add("timeline-post");
        postNode.setId("post-" + post.getId());

        HBox header = new HBox(10);
        header.getStyleClass().add("post-header");

        ImageView authorImage = new ImageView();
        authorImage.getStyleClass().add("post-author-image");
        authorImage.setFitWidth(50);
        authorImage.setFitHeight(50);
        String avatarPath = "/images/img_avatar.png";
        loadImage(authorImage, avatarPath, "https://via.placeholder.com/50x50");

        Text authorName = new Text(post.getUser().getFirstName());
        authorName.getStyleClass().add("post-author-name");

        Text postDate = new Text(post.getCreationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        postDate.getStyleClass().add("post-date");

        header.getChildren().addAll(authorImage, authorName, postDate);

        Text content = new Text(post.getContent());
        content.getStyleClass().add("post-content");
        content.setWrappingWidth(600);

        // Handle post image
        ImageView postImage = null;
        if (post.getPostImg() != null && !post.getPostImg().isEmpty()) {
            postImage = new ImageView();
            postImage.getStyleClass().add("post-image");
            postImage.setPreserveRatio(true);
            postImage.setFitWidth(400);

            try {
                if (post.getPostImg().startsWith("http")) {
                    postImage.setImage(new Image(post.getPostImg(), true));
                } else {
                    String localPath = post.getPostImg().startsWith("/") ?
                            post.getPostImg() : "/" + post.getPostImg();
                    java.net.URL resourceUrl = getClass().getResource(localPath);
                    if (resourceUrl != null) {
                        postImage.setImage(new Image(resourceUrl.toExternalForm()));
                    } else {
                        loadImage(postImage, "/images/default-img.jpg", "https://via.placeholder.com/400x300");
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading post image: " + e.getMessage());
                loadImage(postImage, "/images/default-img.jpg", "https://via.placeholder.com/400x300");
            }
        }

        HBox actionsBar = new HBox(15);
        actionsBar.getStyleClass().add("post-actions-bar");

        boolean hasLiked = false;
        try {
            hasLiked = likesService.hasLiked(post.getId(), String.valueOf(currentUser.getId()));
        } catch (SQLException e) {
            showAlert("Error", "Failed to check like status: " + e.getMessage());
        }

        Button likeButton = new Button("Like (" + post.getLikes() + ")");
        likeButton.getStyleClass().add("action-button");
        likeButton.setStyle(hasLiked ?
                "-fx-background-color: #4CAF50; -fx-text-fill: white;" :
                "-fx-background-color: #E6F9E6; -fx-text-fill: black;");

        likeButton.setOnAction(event -> {
            try {
                handleLike(post, likeButton, postNode);
            } catch (SQLException e) {
                showAlert("Error", "Failed to like/unlike post: " + e.getMessage());
            }
        });

        Button commentButton = new Button("Comment");
        commentButton.getStyleClass().add("action-button");
        commentButton.setOnAction(event -> showCommentInput(post, postNode));

        actionsBar.getChildren().addAll(likeButton, commentButton);

        VBox commentsSection = new VBox(10);
        commentsSection.getStyleClass().add("comments-section");

        for (PostComment comment : post.getPostComments()) {
            HBox commentNode = createCommentNode(comment);
            commentsSection.getChildren().add(commentNode);
        }

        postNode.getChildren().add(header);
        postNode.getChildren().add(content);
        if (postImage != null) {
            postNode.getChildren().add(postImage);
        }
        postNode.getChildren().add(actionsBar);
        postNode.getChildren().add(commentsSection);

        return postNode;
    }

    private void loadImage(ImageView imageView, String resourcePath, String fallbackUrl) {
        try {
            java.net.URL resourceUrl = getClass().getResource(resourcePath);
            if (resourceUrl != null) {
                imageView.setImage(new Image(resourceUrl.toExternalForm()));
            } else {
                imageView.setImage(new Image(fallbackUrl));
                System.err.println("Image not found at: " + resourcePath);
            }
        } catch (Exception e) {
            imageView.setImage(new Image(fallbackUrl));
            System.err.println("Error loading image: " + e.getMessage());
        }
    }

    private void debugImageLoading(String path) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url == null) {
                System.err.println("Image not found at: " + path);
                System.err.println("ClassLoader resources: " +
                        getClass().getClassLoader().getResource(path));
            } else {
                System.out.println("Successfully loaded image from: " + url.toExternalForm());
                Image testImage = new Image(url.toExternalForm());
                if (testImage.isError()) {
                    System.err.println("Image loading error: " + testImage.getException().getMessage());
                } else {
                    System.out.println("Image dimensions: " + testImage.getWidth() + "x" + testImage.getHeight());
                }
            }
        } catch (Exception e) {
            System.err.println("Debug error: " + e.getMessage());
        }
    }

    private void handleLike(Post post, Button likeButton, VBox postNode) throws SQLException {
        boolean hasLiked = likesService.hasLiked(post.getId(), String.valueOf(currentUser.getId()));
        if (!hasLiked) {
            Likes like = new Likes(post.getId(), String.valueOf(currentUser.getId()));
            likesService.ajouter(like);
            likeButton.setText("Like (" + (post.getLikes() + 1) + ")");
            likeButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        } else {
            likesService.removeLike(post.getId(), String.valueOf(currentUser.getId()));
            likeButton.setText("Like (" + (post.getLikes() - 1) + ")");
            likeButton.setStyle("-fx-background-color: #E6F9E6; -fx-text-fill: black;");
        }
        int index = postsContainer.getChildren().indexOf(postNode);
        if (index >= 0) {
            postsContainer.getChildren().set(index, createPostNode(postService.getPostById(post.getId())));
        }
    }

    private void showCommentInput(Post post, VBox postNode) {
        TextArea commentInput = new TextArea();
        commentInput.getStyleClass().add("comment-input");
        commentInput.setPromptText("Write a comment...");

        Button submitButton = new Button("Comment");
        submitButton.getStyleClass().add("comment-submit-button");

        // Get the comments section first
        VBox commentsSection = (VBox) postNode.getChildren().get(postNode.getChildren().size() - 1);

        submitButton.setOnAction(event -> {
            String commentContent = commentInput.getText().trim();
            if (!commentContent.isEmpty()) {
                PostComment comment = new PostComment();
                comment.setPcommentContent(commentContent);
                comment.setPost(post);
                comment.setUser(currentUser);

                if (postService.addComment(comment)) {
                    // Add new comment to view
                    commentsSection.getChildren().add(createCommentNode(comment));
                    commentInput.clear();
                }
            }
        });

        HBox commentInputContainer = new HBox(10);
        commentInputContainer.getStyleClass().add("comment-input-container");
        commentInputContainer.getChildren().addAll(commentInput, submitButton);

        // Add to the comments section
        commentsSection.getChildren().add(commentInputContainer);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}