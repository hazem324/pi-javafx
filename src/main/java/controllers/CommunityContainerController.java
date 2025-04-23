package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
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

    }


}
