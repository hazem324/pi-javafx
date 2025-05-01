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

    @FXML
    private TextArea postContent;
    @FXML
    private ImageView postImagePreview;
    @FXML
    private VBox postsContainer;

    private final CommunityService communityService = new CommunityService();
    private final PostService postService = new PostService();
    private final LikesService likesService = new LikesService();
    private Community currentCommunity;
    private String imageUrl;
    private int communityId;
    private User currentUser;

    @FXML
    public void initialize() {
        // Get the logged-in user
        currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            System.out.println("No user logged in!");
            showAlert("Error", "Please log in to view community posts.");
        }
    }

    // Setter for communityId
    public void setCommunityId(int communityId) {
        this.communityId = communityId;
    }

    // Method to start the controller after communityId is set
    public void start() throws SQLException {
        if (currentUser == null) {
            return; // Already handled in initialize
        }
        loadCommunityInfo();
        if (currentCommunity != null) { // Only load posts if community is valid
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
        String avatarPath = "/assets/img_avatar.png";
        java.net.URL avatarUrl = getClass().getResource(avatarPath);
        if (avatarUrl != null) {
            authorImage.setImage(new Image(avatarUrl.toExternalForm()));
        } else {
            authorImage.setImage(new Image("https://via.placeholder.com/50"));
            System.err.println("Comment author avatar not found: " + avatarPath);
        }

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

        String imagePath = imageUrl;
        try {
            postService.addPostToCommunity(content, imagePath, communityId, currentUser.getId());
            postContent.clear();
            postImagePreview.setVisible(false);
            imageUrl = null;
            VBox newPostNode = createPostNode(postService.getPostById(postService.getLatestPostId()));
            postsContainer.getChildren().add(0, newPostNode); // Add new post at the top
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
        String avatarPath = "/assets/img_avatar.png";
        java.net.URL authorImageUrl = getClass().getResource(avatarPath);
        if (authorImageUrl != null) {
            authorImage.setImage(new Image(authorImageUrl.toExternalForm()));
        } else {
            authorImage.setImage(new Image("https://via.placeholder.com/50"));
            System.err.println("Post author avatar not found: " + avatarPath);
        }

        Text authorName = new Text(post.getUser().getFirstName());
        authorName.getStyleClass().add("post-author-name");

        Text postDate = new Text(post.getCreationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        postDate.getStyleClass().add("post-date");

        header.getChildren().addAll(authorImage, authorName, postDate);

        Text content = new Text(post.getContent());
        content.getStyleClass().add("post-content");
        content.setWrappingWidth(600);

        ImageView postImage = null;
        if (post.getPostImg() != null && !post.getPostImg().isEmpty()) {
            try {
                String postImgPath = post.getPostImg();
                Image image;
                if (postImgPath.startsWith("http://") || postImgPath.startsWith("https://")) {
                    image = new Image(postImgPath, true);
                    if (image.isError()) {
                        throw new IllegalArgumentException("Failed to load image: " + image.getException().getMessage());
                    }
                } else {
                    java.net.URL resourceUrl = getClass().getResource(postImgPath.startsWith("/") ? postImgPath : "/" + postImgPath);
                    if (resourceUrl != null) {
                        image = new Image(resourceUrl.toExternalForm());
                    } else {
                        image = new Image("https://via.placeholder.com/300x200");
                        System.err.println("Post image resource not found: " + postImgPath);
                    }
                }
                postImage = new ImageView(image);
                postImage.getStyleClass().add("post-image");
            } catch (Exception e) {
                System.err.println("Error loading post image: " + e.getMessage());
            }
        }

        HBox actionsBar = new HBox(15);
        actionsBar.getStyleClass().add("post-actions-bar");

        // Check if the current user has liked the post
        boolean hasLiked = false;
        try {
            hasLiked = likesService.hasLiked(post.getId(), String.valueOf(currentUser.getId()));
        } catch (SQLException e) {
            showAlert("Error", "Failed to check like status: " + e.getMessage());
        }

        Button likeButton = new Button("Like (" + post.getLikes() + ")");
        likeButton.getStyleClass().add("action-button");
        // Set the button color based on like status
        if (hasLiked) {
            likeButton.setStyle("-fx-background-color: #E6F9E6;"); // Light green if liked
        } else {
            likeButton.setStyle("-fx-background-color: #E6F9E6;"); // Default light green
        }
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

    private void handleLike(Post post, Button likeButton, VBox postNode) throws SQLException {
        boolean hasLiked = likesService.hasLiked(post.getId(), String.valueOf(currentUser.getId()));
        if (!hasLiked) {
            // User hasn't liked the post yet, so add a like
            Likes like = new Likes(post.getId(), String.valueOf(currentUser.getId()));
            likesService.ajouter(like);
            likeButton.setText("Like (" + (post.getLikes() + 1) + ")");
        } else {
            // User has already liked the post, so remove the like
            likesService.removeLike(post.getId(), String.valueOf(currentUser.getId()));
            likeButton.setText("Like (" + (post.getLikes() - 1) + ")");
        }
        // Refresh only this post node
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
        submitButton.setOnAction(event -> {
            String commentContent = commentInput.getText();
            if (!commentContent.isEmpty()) {
                PostComment comment = new PostComment();
                comment.setPcommentContent(commentContent);
                comment.setPost(post);
                comment.setUser(currentUser);

                if (postService.addComment(comment)) {
                    // Add the new comment node and refresh the comments section
                    HBox newCommentNode = createCommentNode(comment);
                    VBox commentsSection = (VBox) postNode.getChildren().get(postNode.getChildren().size() - 1);
                    commentsSection.getChildren().add(newCommentNode);
                    commentInput.clear();
                }
            }
        });

        HBox commentInputContainer = new HBox(10);
        commentInputContainer.getStyleClass().add("comment-input-container");
        commentInputContainer.getChildren().addAll(commentInput, submitButton);

        VBox commentsSection = (VBox) postNode.getChildren().get(postNode.getChildren().size() - 1);
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