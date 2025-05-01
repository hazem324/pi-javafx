package controllers.sideBar;

import controllers.CommunityListController;
import controllers.EventDetailsController;
import controllers.EventsController;
import controllers.RegistrationDetailsController;
import entities.Event; // Added import
import entities.EventRegistration;
import entities.User;
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

    private User loggedInUser;

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        System.out.println("Logged-in user set in MainSidebar: " + (loggedInUser != null ? loggedInUser.toString() : "null"));
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

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

    public void AccederAuEvents(MouseEvent mouseEvent) {
        loadPage("/events");
    }

    public void handleLogout(ActionEvent actionEvent) {
        loggedInUser = null;
        System.out.println("User logged out in MainSidebar.");
        // Add navigation to login page if needed
    }

    public void loadPage(String page) {
        try {
            String fxmlPath = page + ".fxml";
            System.out.println("Attempting to load FXML: " + fxmlPath);
            java.net.URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("Error: FXML file not found at " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            if (loader.getController() instanceof CommunityListController) {
                CommunityListController communityListController = loader.getController();
                communityListController.setMainSidebarController(this);
            } else if (loader.getController() instanceof EventsController) {
                EventsController eventsController = loader.getController();
                eventsController.setMainSidebarController(this);
            } else if (loader.getController() instanceof EventDetailsController) {
                EventDetailsController eventDetailsController = loader.getController();
                eventDetailsController.setMainSidebarController(this);
                eventDetailsController.setLoggedInUser(loggedInUser);
                System.out.println("Passing logged-in user to EventDetailsController: " + (loggedInUser != null ? loggedInUser.toString() : "null"));
            }

            bp.setCenter(root);
        } catch (IOException e) {
            System.err.println("Failed to load page: " + page);
            e.printStackTrace();
        }
    }

    public void loadPageWithRoot(Parent root) {
        bp.setCenter(root);
    }

    public void loadRegistrationDetails(EventRegistration registration) {
        try {
            String fxmlPath = "/registrationDetails.fxml";
            System.out.println("Attempting to load FXML: " + fxmlPath);
            java.net.URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("Error: FXML file not found at " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            RegistrationDetailsController controller = loader.getController();
            controller.setMainSidebarController(this);
            controller.setRegistration(registration);

            bp.setCenter(root);
        } catch (IOException e) {
            System.err.println("Failed to load registration details page");
            e.printStackTrace();
        }
    }

    // Added method to load event details page with a specific event
    public void loadEventDetails(Event event) {
        try {
            String fxmlPath = "/eventDetails.fxml";
            System.out.println("Attempting to load FXML: " + fxmlPath);
            java.net.URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("Error: FXML file not found at " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            EventDetailsController controller = loader.getController();
            controller.setMainSidebarController(this);
            controller.setEvent(event);
            controller.setLoggedInUser(loggedInUser);
            System.out.println("Passing logged-in user to EventDetailsController: " + (loggedInUser != null ? loggedInUser.toString() : "null"));

            bp.setCenter(root);
        } catch (IOException e) {
            System.err.println("Failed to load event details page");
            e.printStackTrace();
        }
    }
}