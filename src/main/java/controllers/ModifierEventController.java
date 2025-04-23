package controllers;

import entities.Category;
import entities.Event;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import services.CategoryService;
import services.EventService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class ModifierEventController implements ParentAwareController {

    @FXML
    private TextField titleField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private TextField locationField;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private ComboBox<Category> categoryComboBox;

    @FXML
    private TextField placesField;

    @FXML
    private TextField imageField;

    @FXML
    private Button uploadButton;

    @FXML
    private Button updateButton;

    @FXML
    private Button backButton;

    private HomePageController parentController;
    private AfficherEventController afficherEventController;
    private Event selectedEvent;
    private EventService eventService;
    private CategoryService categoryService;

    @Override
    public void setParentController(HomePageController parentController) {
        this.parentController = parentController;
    }

    public void setAfficherEventController(AfficherEventController afficherEventController) {
        this.afficherEventController = afficherEventController;
    }

    public void setSelectedEvent(Event event) {
        this.selectedEvent = event;
        populateForm();
    }

    @FXML
    public void initialize() {
        try {
            eventService = new EventService();
            categoryService = new CategoryService();

            // Populate status ComboBox
            statusComboBox.getItems().addAll("active", "inactive", "canceled");

            // Populate category ComboBox
            List<Category> categories = categoryService.recuperer();
            categoryComboBox.getItems().addAll(categories);
            categoryComboBox.setCellFactory(param -> new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getName());
                    }
                }
            });
            categoryComboBox.setButtonCell(new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getName());
                    }
                }
            });
        } catch (SQLException e) {
            showAlert("Error", "Failed to initialize form: " + e.getMessage());
        }
    }

    private void populateForm() {
        if (selectedEvent == null) return;

        titleField.setText(selectedEvent.getTitle());
        descriptionField.setText(selectedEvent.getEventDescription());
        startDatePicker.setValue(selectedEvent.getEventDate().toLocalDate());
        endDatePicker.setValue(selectedEvent.getEndDate());
        locationField.setText(selectedEvent.getEventLocation());
        statusComboBox.setValue(selectedEvent.getStatus());
        categoryComboBox.setValue(selectedEvent.getCategory());
        placesField.setText(String.valueOf(selectedEvent.getNumberOfPlaces()));
        imageField.setText(selectedEvent.getImageFilename() != null ? selectedEvent.getImageFilename() : "");
    }

    @FXML
    public void uploadImage(ActionEvent event) {
        try {
            // Create a FileChooser to let the user select an image
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Event Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );

            // Show the file chooser dialog
            File selectedFile = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());
            if (selectedFile != null) {
                // Define the destination folder for images (e.g., src/main/resources/images)
                String destinationDir = "src/main/resources/images";
                File dir = new File(destinationDir);
                if (!dir.exists()) {
                    dir.mkdirs(); // Create the directory if it doesn't exist
                }

                // Create a unique file name to avoid overwriting existing files
                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path destinationPath = Paths.get(destinationDir, fileName);

                // Copy the selected file to the destination folder
                Files.copy(selectedFile.toPath(), destinationPath);

                // Update the imageField with the file name
                imageField.setText(fileName);
                showAlert("Success", "Image uploaded successfully: " + fileName);
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to upload image: " + e.getMessage());
        }
    }

    @FXML
    public void updateEvent(ActionEvent event) {
        try {
            // Validate inputs
            String title = titleField.getText();
            String description = descriptionField.getText();
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            String location = locationField.getText();
            String status = statusComboBox.getValue();
            Category category = categoryComboBox.getValue();
            String placesText = placesField.getText();
            String imageFilename = imageField.getText();

            // Required field validations
            if (title == null || title.trim().isEmpty()) {
                showAlert("Validation Error", "Title is required.");
                return;
            }
            if (description == null || description.trim().isEmpty()) {
                showAlert("Validation Error", "Description is required.");
                return;
            }
            if (startDate == null) {
                showAlert("Validation Error", "Start date is required.");
                return;
            }
            if (location == null || location.trim().isEmpty()) {
                showAlert("Validation Error", "Location is required.");
                return;
            }
            if (status == null) {
                showAlert("Validation Error", "Status is required.");
                return;
            }
            if (category == null) {
                showAlert("Validation Error", "Category is required.");
                return;
            }
            if (placesText == null || placesText.trim().isEmpty()) {
                showAlert("Validation Error", "Number of places is required.");
                return;
            }

            // Date validations
            LocalDate today = LocalDate.now();
            if (startDate.isBefore(today)) {
                showAlert("Validation Error", "Start date must be today or later.");
                return;
            }
            if (endDate != null && endDate.isBefore(startDate)) {
                showAlert("Validation Error", "End date must be after start date.");
                return;
            }

            // Number of places validation
            int numberOfPlaces;
            try {
                numberOfPlaces = Integer.parseInt(placesText);
                if (numberOfPlaces <= 0) {
                    showAlert("Validation Error", "Number of places must be a positive integer.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Validation Error", "Number of places must be a valid integer.");
                return;
            }

            // Check if title already exists (excluding the current event)
            if (eventService.eventExists(title) && !title.equals(selectedEvent.getTitle())) {
                showAlert("Validation Error", "An event with this title already exists.");
                return;
            }

            // Update the event
            selectedEvent.setTitle(title);
            selectedEvent.setEventDescription(description);
            selectedEvent.setEventDate(LocalDateTime.of(startDate, LocalTime.of(0, 0)));
            selectedEvent.setEndDate(endDate);
            selectedEvent.setEventLocation(location);
            selectedEvent.setStatus(status);
            selectedEvent.setCategory(category);
            selectedEvent.setNumberOfPlaces(numberOfPlaces);
            selectedEvent.setImageFilename(imageFilename.isEmpty() ? null : imageFilename);

            eventService.modifier(selectedEvent);

            // Refresh the table in AfficherEventController
            if (afficherEventController != null) {
                afficherEventController.refreshTable();
            }

            showAlert("Success", "Event updated successfully!");

            // Navigate back to AfficherEvent
            parentController.goToAfficherEvent(event);

        } catch (SQLException e) {
            showAlert("Error", "Failed to update event: " + e.getMessage());
        }
    }

    @FXML
    public void goBack(ActionEvent event) {
        parentController.goToAfficherEvent(event);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}