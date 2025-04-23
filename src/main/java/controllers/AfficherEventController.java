package controllers;

import entities.Event;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import services.EventService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AfficherEventController implements ParentAwareController {

    @FXML
    private TableView<Event> eventTable;

    @FXML
    private TableColumn<Event, String> imageColumn;

    @FXML
    private TableColumn<Event, String> titleColumn;

    @FXML
    private TableColumn<Event, String> descriptionColumn;

    @FXML
    private TableColumn<Event, String> startDateColumn;

    @FXML
    private TableColumn<Event, String> endDateColumn;

    @FXML
    private TableColumn<Event, String> locationColumn;

    @FXML
    private TableColumn<Event, String> statusColumn;

    @FXML
    private TableColumn<Event, String> categoryColumn;

    @FXML
    private TableColumn<Event, String> placesColumn;

    @FXML
    private Button updateButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button backButton;

    private EventService eventService;

    private HomePageController parentController;
    private AdminDashboardController adminDashboardController; // Add this

    // Setter for AdminDashboardController
    public void setAdminDashboardController(AdminDashboardController adminDashboardController) {
        this.adminDashboardController = adminDashboardController;
    }

    @Override
    public void setParentController(HomePageController parentController) {
        this.parentController = parentController;
    }

    @FXML
    public void initialize() {
        try {
            // Initialize the service
            eventService = new EventService();

            // Configure the image column to display images
            imageColumn.setCellFactory(column -> new TableCell<Event, String>() {
                private final ImageView imageView = new ImageView();

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setGraphic(null);
                    } else {
                        Event event = getTableRow().getItem();
                        String imageFilename = event.getImageFilename();
                        if (imageFilename != null && !imageFilename.isEmpty()) {
                            try {
                                // Load the image from the classpath (resources/images/)
                                String imagePath = "/images/" + imageFilename;
                                System.out.println("Attempting to load image from classpath: " + imagePath); // Debug log
                                java.net.URL imageUrl = getClass().getResource(imagePath);
                                if (imageUrl != null) {
                                    Image image = new Image(imageUrl.toString(), 50, 50, true, true);
                                    if (!image.isError()) {
                                        imageView.setImage(image);
                                        setGraphic(imageView);
                                    } else {
                                        System.err.println("Error: Image failed to load for event: " + event.getTitle());
                                        setGraphic(null);
                                    }
                                } else {
                                    System.err.println("Error: Image file not found in classpath at: " + imagePath + " for event: " + event.getTitle());
                                    setGraphic(null);
                                }
                            } catch (Exception e) {
                                System.err.println("Error loading image for event: " + event.getTitle() + " - " + e.getMessage());
                                setGraphic(null);
                            }
                        } else {
                            System.out.println("No image filename for event: " + event.getTitle());
                            setGraphic(null);
                        }
                    }
                }
            });

            // Bind the columns to the properties of the Event class
            titleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
            descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEventDescription()));
            startDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEventDate().toString()));
            endDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                    cellData.getValue().getEndDate() != null ? cellData.getValue().getEndDate().toString() : "N/A"
            ));
            locationColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEventLocation()));
            statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
            categoryColumn.setCellValueFactory(cellData -> {
                Event event = cellData.getValue();
                String categoryName = (event.getCategory() != null && event.getCategory().getName() != null)
                        ? event.getCategory().getName()
                        : "N/A";
                return new SimpleStringProperty(categoryName);
            });
            placesColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getNumberOfPlaces()).asObject().asString());

            // Load events from the database
            loadEvents();
        } catch (Exception e) {
            System.err.println("Error during initialization of AfficherEventController: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize AfficherEventController", e);
        }
    }

    // Method to load events
    private void loadEvents() {
        try {
            List<Event> events = eventService.recuperer();
            System.out.println("Number of events loaded: " + events.size());
            for (Event event : events) {
                System.out.println("Event: " + event);
            }
            eventTable.getItems().setAll(events);
        } catch (Exception e) {
            System.err.println("Error loading events: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors du chargement des événements");
            alert.setContentText("Impossible de charger les événements : " + e.getMessage());
            alert.show();
        }
    }

    // Method to refresh the table (called by ModifierEventController)
    public void refreshTable() {
        loadEvents();
    }

    @FXML
    public void update(ActionEvent actionEvent) {
        // Check if an event is selected
        Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un événement à modifier !");
            alert.show();
            return;
        }

        try {
            // Load ModifierEvent.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierEvent.fxml"));
            Parent pageContent = loader.load();

            // Get the controller and pass the selected event and this controller
            ModifierEventController controller = loader.getController();
            controller.setParentController(parentController);
            controller.setAfficherEventController(this);
            controller.setSelectedEvent(selectedEvent);

            // Set the content directly in the parent controller's content area
            BorderPane contentArea = parentController.getContentArea();
            if (contentArea != null) {
                contentArea.setCenter(pageContent);
            } else {
                throw new IllegalStateException("Content area is not initialized in HomePageController");
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors du chargement de la page de modification");
            alert.setContentText("Impossible de charger la page : " + e.getMessage());
            alert.show();
        } catch (IllegalStateException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de la navigation");
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    @FXML
    public void delete(ActionEvent actionEvent) {
        // Check if an event is selected
        Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un événement à supprimer !");
            alert.show();
            return;
        }

        // Show a confirmation dialog
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to delete this event?");
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete the event using the correct method name from EventService
                eventService.supprimer(selectedEvent);

                // Refresh the table
                loadEvents();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Événement supprimé avec succès !");
                alert.show();
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Erreur lors de la suppression");
                alert.setContentText(e.getMessage());
                alert.show();
            }
        }
    }

//    @FXML
//    public void back(ActionEvent actionEvent) {
//        // Check if the parent controller is the AdminDashboardController
//        if (adminDashboardController != null) {
//            adminDashboardController.showEvents(); // Navigate back to the events list in the dashboard
//        } else if (parentController != null) {
//            parentController.goToAjouterEvent(actionEvent); // Fallback if used in a different context
//        } else {
//            System.err.println("Parent controller (AdminDashboardController or HomePageController) is not set.");
//            // Optionally, show an error message to the user
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setTitle("Erreur de navigation");
//            alert.setHeaderText(null);
//            alert.setContentText("Impossible de revenir à la page précédente.");
//            alert.show();
//        }
//    }
}