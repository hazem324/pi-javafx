package controllers.sideBar;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;

import java.io.IOException;

import controllers.community.CommunityListController;

public class MainSidebar {



    @FXML
    private BorderPane bp;
    @FXML
    private AnchorPane ap;
    @FXML private VBox userMenu;
    public void AccederAuProfil(ActionEvent actionEvent) {
    }


    public void AccederAuHome(ActionEvent actionEvent) {
    }

    public void AccederAuAllCommunities(javafx.scene.input.MouseEvent mouseEvent) {

        loadPage("/community/Community-Liste-Page");
    }

    public void AccederAuMyCommunities(javafx.scene.input.MouseEvent mouseEvent) {
        loadPage("/community/MyCommunitys");
    }

    public void AccederAuMarketPlace(javafx.scene.input.MouseEvent mouseEvent) {
        loadPage("/marketPlace/product_list");
    }

    public void AccederAuPanier(javafx.scene.input.MouseEvent mouseEvent) {
        loadPage("/marketPlace/CartView");
    }

    public void handleLogout(ActionEvent actionEvent) {
    }
    public void loadPage(String page) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(page + ".fxml"));
            Parent root = loader.load();

            // ⚡ Injection de MainController dans ProfileController
            if (loader.getController() instanceof CommunityListController) {
                CommunityListController communityListController = loader.getController();
                communityListController.setMainSidebarController(this);
            }

            bp.setCenter(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
