package controllers.community;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import models.Community;

public class CommunityContainerController {

    @FXML
    private Label nameLabel;

    @FXML
    private Label communitycategory;

    @FXML
    private ImageView imageView;

    public void setData(Community community) {
        nameLabel.setText(community.getName());
        communitycategory.setText(community.getCategory().toString());

        // Load image from URL (like Cloudinary)
        if (community.getBanner() != null && !community.getBanner().isEmpty()) {
            try {
                Image image = new Image(community.getBanner(), true); // true = background loading
                imageView.setImage(image);
            } catch (Exception e) {
                System.err.println("Failed to load image: " + e.getMessage());
            }
        }
    }
}
