package controllers.sideBar;

import controllers.CommunityListController;
import controllers.marketplace.ProductController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;

import java.io.IOException;

public class MainSidebar {

    @FXML
    private BorderPane bp;
    @FXML
    private AnchorPane ap;
    @FXML
    private VBox userMenu;

    public void AccederAuProfil(ActionEvent actionEvent) {
    }

    public void AccederAuHome(ActionEvent actionEvent) {
    }

    public void AccederAuAllCommunities(MouseEvent mouseEvent) {
        loadPage("/community/Community-Liste-Page");
    }

    public void AccederAuMyCommunities(ActionEvent actionEvent) {
    }

    public void AccederAuMarketPlace(MouseEvent mouseEvent) {
        loadPage("/marketPlace/product_list");
    }

    public void AccederAuPanier(MouseEvent mouseEvent) {
        loadPage("/marketPlace/CartView");
    }

    public void handleLogout(ActionEvent actionEvent) {
    }

    public void loadPage(String page) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(page + ".fxml"));
            Parent root = loader.load();

            // Injection for CommunityListController
            if (loader.getController() instanceof CommunityListController) {
                CommunityListController communityListController = loader.getController();
                communityListController.setMainSidebarController(this);
            }

            // Injection for ProductController
            if (loader.getController() instanceof ProductController) {
                ProductController productController = loader.getController();
                productController.setBorderPane(bp); // Pass the MainSidebar's BorderPane to ProductController
            }

            bp.setCenter(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }}