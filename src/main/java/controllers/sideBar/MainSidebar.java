package controllers.sideBar;

import controllers.EventDetailsController;
import controllers.EventsController;
import controllers.RegistrationDetailsController;
import entities.Event;
import entities.EventRegistration;
import entities.User;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import controllers.community.CommunityListController;
import javafx.stage.Stage;
import java.io.IOException;

public class MainSidebar {

    @FXML private Button logoutButton;
    @FXML private BorderPane bp;
    @FXML private AnchorPane ap;
    @FXML private VBox userMenu;

    private User loggedInUser;

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        System.out.println("Logged-in user set in MainSidebar: " + (loggedInUser != null ? loggedInUser.toString() : "null"));
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    @FXML
    public void AccederAuProfil(ActionEvent actionEvent) {
        System.out.println("Navigating to Profile");
        loadPage("/profile/Profile");
    }

    @FXML
    public void AccederAuHome(ActionEvent actionEvent) {
        System.out.println("Navigating to Home");
        loadPage("/home/HomePage");
    }

    @FXML
    public void AccederAuAllCommunities(MouseEvent mouseEvent) {
        System.out.println("Navigating to All Communities");
        loadPage("/community/Community-Liste-Page");
    }

    @FXML
    public void AccederAuMyCommunities(MouseEvent mouseEvent) {
        System.out.println("Navigating to My Communities");
        loadPage("/community/MyCommunitys");
    }

    @FXML
    public void AccederAuMarketPlace(MouseEvent mouseEvent) {
        System.out.println("Navigating to Marketplace");
        loadPage("/marketPlace/product_list");
    }

    @FXML
    public void AccederAuPanier(MouseEvent mouseEvent) {
        System.out.println("Navigating to Cart");
        loadPage("/marketPlace/CartView");
    }

    @FXML
    public void AccederAuEvents(MouseEvent mouseEvent) {
        System.out.println("Navigating to Events");
        loadPage("/events");
    }

    @FXML
    public void logout(ActionEvent actionEvent) {
        try {
            System.out.println("Logging out user: " + (loggedInUser != null ? loggedInUser.toString() : "null"));
            if (logoutButton == null) {
                showError("Logout button is not initialized. Please check FXML configuration.");
                System.err.println("Error: logoutButton is null in MainSidebar.logout");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            if (stage == null) {
                showError("Cannot access stage for logout. Scene or window is null.");
                System.err.println("Error: Stage is null in MainSidebar.logout");
                return;
            }
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            showError("Failed to logout: " + e.getMessage());
            System.err.println("IOException in MainSidebar.logout: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalStateException e) {
            showError("Failed to find FXML file: /Login.fxml \n" + e.getMessage());
            System.err.println("IllegalStateException in MainSidebar.logout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadPage(String page) {
        try {
            String fxmlPath = page + ".fxml";
            System.out.println("Attempting to load FXML: " + fxmlPath);
            java.net.URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("Error: FXML file not found at " + fxmlPath);
                showError("Page not found: " + page);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            if (bp == null) {
                showError("BorderPane is not initialized. Please check FXML configuration.");
                System.err.println("Error: bp is null in MainSidebar.loadPage");
                return;
            }

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
            showError("Failed to load page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadPageWithRoot(Parent root) {
        if (bp == null) {
            showError("BorderPane is not initialized. Please check FXML configuration.");
            System.err.println("Error: bp is null in MainSidebar.loadPageWithRoot");
            return;
        }
        bp.setCenter(root);
    }

    public void loadRegistrationDetails(EventRegistration registration) {
        try {
            String fxmlPath = "/registrationDetails.fxml";
            System.out.println("Attempting to load FXML: " + fxmlPath);
            java.net.URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("Error: FXML file not found at " + fxmlPath);
                showError("Registration details page not found");
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
            showError("Failed to load registration details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadEventDetails(Event event) {
        try {
            String fxmlPath = "/eventDetails.fxml";
            System.out.println("Attempting to load FXML: " + fxmlPath);
            java.net.URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("Error: FXML file not found at " + fxmlPath);
                showError("Event details page not found");
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
            showError("Failed to load event details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Error", message);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}