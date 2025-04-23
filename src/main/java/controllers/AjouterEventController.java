package controllers;

import entities.Category;
import entities.Event;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import services.CategoryService;
import services.EventService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AjouterEventController implements ParentAwareController {

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
    private ChoiceBox<String> statusChoiceBox;

    @FXML
    private ChoiceBox<Category> categoryChoiceBox;

    @FXML
    private TextField placesField;

    @FXML
    private TextField imageField;

    @FXML
    private Button chooseImageButton;

    @FXML
    private Button addEventButton;

    @FXML
    private Button backButton;

    private EventService eventService;
    private CategoryService categoryService;
    private HomePageController parentController;
    private String selectedImageFilename;

    @Override
    public void setParentController(HomePageController parentController) {
        this.parentController = parentController;
    }

    @FXML
    public void initialize() {
        try {
            eventService = new EventService();
            categoryService = new CategoryService();

            // Populate status choices with the correct values from Event.java
            statusChoiceBox.getItems().addAll(Event.STATUS_ACTIVE, Event.STATUS_CANCELLED, Event.STATUS_COMPLETED);
            statusChoiceBox.setValue(Event.STATUS_ACTIVE);

            // Populate category choices from the database
            List<Category> categories = categoryService.recuperer();
            if (categories.isEmpty()) {
                showAlert("Warning", "No categories found in the database. Please add some categories first.");
            } else {
                categoryChoiceBox.getItems().addAll(categories);
                categoryChoiceBox.setValue(categories.get(0)); // Set default to first category

                // Set a StringConverter to display only the category name
                categoryChoiceBox.setConverter(new StringConverter<Category>() {
                    @Override
                    public String toString(Category category) {
                        return category != null ? category.getName() : "";
                    }

                    @Override
                    public Category fromString(String string) {
                        // Not needed for ChoiceBox, but required to implement
                        return categoryChoiceBox.getItems().stream()
                                .filter(category -> category.getName().equals(string))
                                .findFirst()
                                .orElse(null);
                    }
                });
            }
        } catch (SQLException e) {
            System.err.println("Error during initialization of AjouterEventController: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to initialize Add Event form: " + e.getMessage());
        }
    }

    @FXML
    public void chooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Event Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                // Define the destination directory in the user's home directory
                String userHome = System.getProperty("user.home");
                String destinationDir = userHome + "/esprit-event/images/";
                File destinationDirFile = new File(destinationDir);

                // Create the directory if it doesn't exist
                if (!destinationDirFile.exists()) {
                    boolean dirsCreated = destinationDirFile.mkdirs();
                    if (!dirsCreated) {
                        System.err.println("Failed to create directory: " + destinationDir);
                        showAlert("Error", "Failed to create image storage directory.");
                        return;
                    }
                }

                // Define the destination path for the image
                String destinationPath = destinationDir + selectedFile.getName();
                File destinationFile = new File(destinationPath);

                // Copy the image to the destination
                Files.copy(selectedFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Image copied to: " + destinationPath); // Debug log

                // Update the image field with the filename
                selectedImageFilename = selectedFile.getName();
                imageField.setText(selectedImageFilename);
            } catch (IOException e) {
                System.err.println("Error copying image: " + e.getMessage());
                showAlert("Error", "Failed to upload image: " + e.getMessage());
            }
        }
    }

    @FXML
    public void addEvent(ActionEvent event) {
        try {
            // Validate inputs
            String title = titleField.getText();
            if (title == null || title.trim().isEmpty()) {
                showAlert("Validation Error", "Title cannot be empty.");
                return;
            }

            String description = descriptionField.getText();
            if (description == null || description.trim().isEmpty()) {
                showAlert("Validation Error", "Description cannot be empty.");
                return;
            }

            LocalDate startDate = startDatePicker.getValue();
            if (startDate == null) {
                showAlert("Validation Error", "Start date must be selected.");
                return;
            }

            LocalDate endDate = endDatePicker.getValue();
            if (endDate != null && endDate.isBefore(startDate)) {
                showAlert("Validation Error", "End date cannot be before start date.");
                return;
            }

            String location = locationField.getText();
            if (location == null || location.trim().isEmpty()) {
                showAlert("Validation Error", "Location cannot be empty.");
                return;
            }

            String status = statusChoiceBox.getValue();
            if (status == null) {
                showAlert("Validation Error", "Status must be selected.");
                return;
            }

            Category category = categoryChoiceBox.getValue();
            if (category == null) {
                showAlert("Validation Error", "Category must be selected.");
                return;
            }

            String placesText = placesField.getText();
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

            // Create the event
            Event newEvent = new Event();
            newEvent.setTitle(title);
            newEvent.setEventDescription(description);
            newEvent.setEventDate(LocalDateTime.of(startDate, java.time.LocalTime.of(0, 0)));
            newEvent.setEndDate(endDate);
            newEvent.setEventLocation(location);
            newEvent.setStatus(status);
            newEvent.setCategory(category);
            newEvent.setNumberOfPlaces(numberOfPlaces);
            newEvent.setImageFilename(selectedImageFilename);

            // Add the event to the database
            eventService.ajouter(newEvent);

            // Show success message
            showAlert("Success", "Event added successfully!");

            // Clear the form
            clearForm();

            // Navigate back to the event list
            parentController.goToAfficherEvent(event);
        } catch (SQLException e) {
            System.err.println("Error adding event: " + e.getMessage());
            showAlert("Error", "Failed to add event: " + e.getMessage());
        }
    }

    @FXML
    public void back(ActionEvent event) {
        parentController.goToAfficherEvent(event);
    }

    private void clearForm() {
        titleField.clear();
        descriptionField.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        locationField.clear();
        statusChoiceBox.setValue(null);
        categoryChoiceBox.setValue(null);
        placesField.clear();
        imageField.clear();
        selectedImageFilename = null;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(title.equals("Success") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showCommunities(ActionEvent actionEvent) {
    }

    public void showAddCommunity(ActionEvent actionEvent) {
    }

    public void showPosts(ActionEvent actionEvent) {
    }

    public void showEventCategory(ActionEvent actionEvent) {
    }

    public void showAddCategory(ActionEvent actionEvent) {
    }

    public void showEvent(ActionEvent actionEvent) {
    }

    public void showAddEvent(ActionEvent actionEvent) {
    }

    public void cancel(ActionEvent actionEvent) {
    }

    public void saveEvent(ActionEvent actionEvent) {
    }
}