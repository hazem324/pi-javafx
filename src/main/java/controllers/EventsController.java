package controllers;

import entities.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import services.EventService;
import controllers.sideBar.MainSidebar;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventsController {

    @FXML
    private GridPane eventsGrid;

    private MainSidebar mainSidebarController;
    private EventService eventService;

    public void setMainSidebarController(MainSidebar mainSidebarController) {
        this.mainSidebarController = mainSidebarController;
    }

    @FXML
    public void initialize() {
        try {
            eventService = new EventService();
            List<Event> events = eventService.recuperer();
            displayEvents(events);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayEvents(List<Event> events) {
        int column = 0;
        int row = 0;

        for (Event event : events) {
            VBox card = createEventCard(event);
            eventsGrid.add(card, column, row);

            column++;
            if (column == 3) {
                column = 0;
                row++;
            }
        }
    }

    private VBox createEventCard(Event event) {
        VBox card = new VBox(10);
        card.setPrefWidth(200);
        card.setPrefHeight(300);
        card.getStyleClass().add("event-card");

        // Event Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(120);
        imageView.getStyleClass().add("event-image");
        String imagePath = System.getProperty("user.home") + "/esprit-event/images/" + event.getImageFilename();
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            Image image = new Image(imageFile.toURI().toString());
            imageView.setImage(image);
        } else {
            // Attempt to load placeholder image, but handle failure gracefully
            try {
                Image placeholderImage = new Image("/assets/images/placeholder.jpg");
                imageView.setImage(placeholderImage);
            } catch (IllegalArgumentException e) {
                System.err.println("Placeholder image not found: /assets/images/placeholder.jpg");
            }
        }

        // Event Title
        Label titleLabel = new Label(event.getTitle());
        titleLabel.getStyleClass().add("event-title");
        titleLabel.setWrapText(true);

        // Event Date
        Label dateLabel = new Label("Date: " + event.getEventDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dateLabel.getStyleClass().add("event-info");

        // Number of Places
        Label placesLabel = new Label("Places Left: " + event.getNumberOfPlaces());
        placesLabel.getStyleClass().add("event-info");
        // Style the label in red if places are 0
        if (event.getNumberOfPlaces() == 0) {
            placesLabel.setStyle("-fx-text-fill: red;");
        }

        // "Sorry, no places left" Label (visible only if places are 0)
        Label noPlacesLabel = new Label("Sorry, no places left");
        noPlacesLabel.getStyleClass().add("event-info");
        noPlacesLabel.setStyle("-fx-text-fill: red;");
        noPlacesLabel.setVisible(event.getNumberOfPlaces() == 0);

        // Buttons
        Button moreInfoButton = new Button("More Information");
        moreInfoButton.getStyleClass().add("button-info");
        moreInfoButton.setOnAction(e -> showEventDetails(event));

        Button registerButton = new Button("Register");
        registerButton.getStyleClass().add("button-register");
        registerButton.setOnAction(e -> {
            if (event.getNumberOfPlaces() > 0) {
                showEventDetails(event);
            } else {
                System.out.println("Cannot register: No places left for event: " + event.getTitle());
            }
        });

        HBox buttonBox = new HBox(10, moreInfoButton, registerButton);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);

        card.getChildren().addAll(imageView, titleLabel, dateLabel, placesLabel, noPlacesLabel, buttonBox);
        return card;
    }

    private void showEventDetails(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/eventDetails.fxml"));
            Parent root = loader.load();

            EventDetailsController detailsController = loader.getController();
            detailsController.setEvent(event);
            detailsController.setMainSidebarController(mainSidebarController);

            mainSidebarController.loadPageWithRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}