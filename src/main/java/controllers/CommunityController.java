package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.AnchorPane;
import models.Community;
import services.CommunityService;

import java.io.IOException;
import java.util.List;

public class CommunityController {

    @FXML
    private GridPane communityGrid;

    private final CommunityService communityService = new CommunityService();
    @FXML
    private AnchorPane centerContent;

    @FXML
    public void initialize() {
        loadCommunityContainersFromDB();
    }

    private void loadCommunityContainersFromDB() {
        communityGrid.getChildren().clear();

        List<Community> communityList = communityService.recuperer();

        int column = 0;
        int row = 0;

        for (Community community : communityList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/community-container.fxml"));
                AnchorPane pane = loader.load();

                CommunityContainerController controller = loader.getController();
                controller.setData(community);

                communityGrid.add(pane, column, row);

                column++;
                if (column == 2) { // 2 colonnes par ligne
                    column = 0;
                    row++;
                }
            } catch (IOException e) {
                e.printStackTrace(); // Remplace ça par un logger si besoin
            }
        }
    }
}
