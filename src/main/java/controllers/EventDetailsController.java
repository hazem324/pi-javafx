package controllers;

import entities.Event;
import entities.EventRegistration;
import entities.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import controllers.sideBar.MainSidebar;
import services.EventRegistrationService;
import utils.SessionManager;

import java.io.File;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class EventDetailsController {

    @FXML
    private ImageView eventImage;

    @FXML
    private Text titleLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label endDateLabel;

    @FXML
    private Label locationLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label categoryLabel;

    @FXML
    private Label placesLabel;

    private Event event;
    private MainSidebar mainSidebarController;
    private User loggedInUser; // This field is now optional

    public void setEvent(Event event) {
        this.event = event;
        System.out.println("Setting event in EventDetailsController: " + (event != null ? event.toString() : "null"));
        populateEventDetails();
    }

    public void setMainSidebarController(MainSidebar mainSidebarController) {
        this.mainSidebarController = mainSidebarController;
        System.out.println("MainSidebarController set in EventDetailsController: " + mainSidebarController);
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        System.out.println("Logged-in user set in EventDetailsController: " + (loggedInUser != null ? user.toString() : "null"));
    }

    private void populateEventDetails() {
        if (event == null) {
            System.err.println("Event is null in populateEventDetails");
            return;
        }

        String imagePath = System.getProperty("user.home") + "/esprit-event/images/" + event.getImageFilename();
        System.out.println("Loading event image from: " + imagePath);
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            Image image = new Image(imageFile.toURI().toString());
            eventImage.setImage(image);
        } else {
            try {
                Image placeholderImage = new Image("/assets/images/placeholder.jpg");
                eventImage.setImage(placeholderImage);
            } catch (IllegalArgumentException e) {
                System.err.println("Placeholder image not found: /assets/images/placeholder.jpg");
            }
        }

        System.out.println("Setting title: " + event.getTitle());
        titleLabel.setText(event.getTitle() != null ? event.getTitle() : "N/A");
        System.out.println("Setting description: " + event.getEventDescription());
        descriptionLabel.setText("Description: " + (event.getEventDescription() != null ? event.getEventDescription() : "N/A"));
        System.out.println("Setting start date: " + event.getEventDate());
        dateLabel.setText("Start Date: " + (event.getEventDate() != null ? event.getEventDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A"));
        if (event.getEndDate() != null) {
            System.out.println("Setting end date: " + event.getEndDate());
            endDateLabel.setText("End Date: " + event.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            endDateLabel.setVisible(true);
        }
        System.out.println("Setting location: " + event.getEventLocation());
        locationLabel.setText("Location: " + (event.getEventLocation() != null ? event.getEventLocation() : "N/A"));
        System.out.println("Setting status: " + event.getStatus());
        statusLabel.setText("Status: " + (event.getStatus() != null ? event.getStatus() : "N/A"));
        System.out.println("Setting category: " + (event.getCategory() != null ? event.getCategory().getName() : "N/A"));
        categoryLabel.setText("Category: " + (event.getCategory() != null ? event.getCategory().getName() : "N/A"));
        System.out.println("Setting places: " + event.getNumberOfPlaces());
        placesLabel.setText("Places Left: " + event.getNumberOfPlaces());
    }

    @FXML
    public void goBack() {
        mainSidebarController.loadPage("/events");
    }

    @FXML
    public void registerForEvent() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        System.out.println("Attempting to register for event. Logged-in user from SessionManager: " + (currentUser != null ? currentUser.toString() : "null"));

        if (currentUser == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "You must be logged in to register for an event.");
            alert.showAndWait();
            return;
        }

        if (event.getNumberOfPlaces() <= 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No places left for this event.");
            alert.showAndWait();
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to register for this event?");
        confirmation.setTitle("Confirm Registration");
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                EventRegistrationService registrationService = new EventRegistrationService();
                EventRegistration registration = registrationService.registerUserForEvent(event, currentUser);

                // Update the UI with the new number of places (already decremented in EventRegistrationService)
                populateEventDetails();

                // Show success alert
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Registration successful! A QR code and PDF have been generated for your event.");
                successAlert.setTitle("Success");
                successAlert.showAndWait();

                // Navigate to the registration details page
                mainSidebarController.loadRegistrationDetails(registration);
            } catch (SQLException e) {
                Alert alert;
                if (e.getMessage().equals("User is already registered for this event.")) {
                    alert = new Alert(Alert.AlertType.WARNING, "You are already registered for this event.");
                } else if (e.getMessage().equals("No places left for this event.")) {
                    alert = new Alert(Alert.AlertType.ERROR, "No places left for this event.");
                } else {
                    alert = new Alert(Alert.AlertType.ERROR, "Failed to register: " + e.getMessage());
                }
                alert.showAndWait();
            }
        }
    }
}