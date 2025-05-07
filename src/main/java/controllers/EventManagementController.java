package controllers;

import entities.Category;
import entities.Event;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

public class EventManagementController {
    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> titleColumn;
    @FXML private TableColumn<Event, String> descriptionColumn;
    @FXML private TableColumn<Event, String> startDateColumn;
    @FXML private TableColumn<Event, String> endDateColumn;
    @FXML private TableColumn<Event, String> locationColumn;
    @FXML private TableColumn<Event, String> statusColumn;
    @FXML private TableColumn<Event, String> categoryColumn;
    @FXML private TableColumn<Event, Integer> placesColumn;
    @FXML private TableColumn<Event, String> imageColumn;
    @FXML private TableColumn<Event, Void> actionColumn;
    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField locationField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private TextField placesField;
    @FXML private TextField imageField;
    @FXML private Button chooseImageButton;
    @FXML private Button addButton;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;

    private final EventService eventService = new EventService();
    private final CategoryService categoryService = new CategoryService();
    private String selectedImageFilename;

    @FXML
    public void initialize() {
        // Populate status ComboBox for adding events
        statusComboBox.getItems().addAll("active", "canceled", "completed");
        statusComboBox.setValue("active");

        // Populate filter ComboBox
        statusFilterComboBox.getItems().addAll("All", "active", "canceled", "completed");
        statusFilterComboBox.setValue("All");

        // Populate category ComboBox
        try {
            List<Category> categories = categoryService.recuperer();
            categoryComboBox.getItems().addAll(categories);
            categoryComboBox.setCellFactory(lv -> new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getName());
                }
            });
            categoryComboBox.setButtonCell(new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getName());
                }
            });
            if (!categories.isEmpty()) {
                categoryComboBox.setValue(categories.get(0));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load categories: " + e.getMessage());
        }

        // Set up table columns
        titleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEventDescription()));
        startDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEventDate().toString()));
        endDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getEndDate() != null ? cellData.getValue().getEndDate().toString() : "N/A"));
        locationColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEventLocation()));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        categoryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCategory() != null ? cellData.getValue().getCategory().getName() : "N/A"));
        placesColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getNumberOfPlaces()).asObject());
        imageColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getImageFilename() != null ? cellData.getValue().getImageFilename() : ""));

        // Set up action column
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox actionBox = new HBox(10, editButton, deleteButton);

            {
                editButton.getStyleClass().add("action-button");
                deleteButton.getStyleClass().add("delete-button");
                editButton.setOnAction(event -> {
                    Event eventItem = getTableView().getItems().get(getIndex());
                    editEvent(eventItem);
                });
                deleteButton.setOnAction(event -> {
                    Event eventItem = getTableView().getItems().get(getIndex());
                    deleteEvent(eventItem);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(actionBox);
                }
            }
        });

        // Load events
        loadEvents();
    }

    private void loadEvents() {
        try {
            String searchTitle = searchField != null ? searchField.getText() : null;
            String statusFilter = statusFilterComboBox != null ? statusFilterComboBox.getValue() : "All";
            LocalDate fromDate = fromDatePicker != null ? fromDatePicker.getValue() : null;
            LocalDate toDate = toDatePicker != null ? toDatePicker.getValue() : null;

            // Validate date range
            if (fromDate != null && toDate != null && toDate.isBefore(fromDate)) {
                showAlert(Alert.AlertType.ERROR, "Error", "To Date must be after From Date.");
                toDatePicker.setValue(null); // Reset invalid To Date
                toDate = null;
            }

            List<Event> events = eventService.getEvents(searchTitle, statusFilter, fromDate, toDate);
            eventTable.getItems().setAll(events);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load events: " + e.getMessage());
        }
    }

    @FXML
    private void searchEvents() {
        loadEvents();
    }

    @FXML
    private void filterEvents() {
        loadEvents();
    }

    @FXML
    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Event Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(chooseImageButton.getScene().getWindow());
        if (selectedFile != null) {
            try {
                String destinationDir = "src/main/resources/images";
                File dir = new File(destinationDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path destinationPath = Paths.get(destinationDir, fileName);
                Files.copy(selectedFile.toPath(), destinationPath);
                selectedImageFilename = fileName;
                imageField.setText(fileName);
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to upload image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void addEvent() {
        try {
            String title = titleField.getText().trim();
            String description = descriptionField.getText().trim();
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            String location = locationField.getText().trim();
            String status = statusComboBox.getValue();
            Category category = categoryComboBox.getValue();
            String placesText = placesField.getText().trim();

            // Validation
            if (title.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Title is required.");
                return;
            }
            if (description.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Description is required.");
                return;
            }
            if (startDate == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Start date is required.");
                return;
            }
            if (endDate != null && endDate.isBefore(startDate)) {
                showAlert(Alert.AlertType.ERROR, "Error", "End date must be after start date.");
                return;
            }
            if (location.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Location is required.");
                return;
            }
            if (status == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Status is required.");
                return;
            }
            if (category == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Category is required.");
                return;
            }
            int numberOfPlaces;
            try {
                numberOfPlaces = Integer.parseInt(placesText);
                if (numberOfPlaces <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Number of places must be positive.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Number of places must be a valid integer.");
                return;
            }
            if (eventService.eventExists(title)) {
                showAlert(Alert.AlertType.ERROR, "Error", "An event with this title already exists.");
                return;
            }

            // Create and add event
            Event newEvent = new Event();
            newEvent.setTitle(title);
            newEvent.setEventDescription(description);
            newEvent.setEventDate(LocalDateTime.of(startDate, LocalTime.of(0, 0)));
            newEvent.setEndDate(endDate);
            newEvent.setEventLocation(location);
            newEvent.setStatus(status);
            newEvent.setCategory(category);
            newEvent.setNumberOfPlaces(numberOfPlaces);
            newEvent.setImageFilename(selectedImageFilename);

            eventService.ajouter(newEvent);
            loadEvents();
            clearForm();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Event added successfully!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add event: " + e.getMessage());
        }
    }

    private void editEvent(Event event) {
        Dialog<Event> dialog = new Dialog<>();
        dialog.setTitle("Edit Event");
        dialog.setHeaderText("Edit Event Details");

        // Form fields
        TextField titleInput = new TextField(event.getTitle());
        TextArea descriptionInput = new TextArea(event.getEventDescription());
        descriptionInput.setPrefHeight(100);
        DatePicker startDateInput = new DatePicker(event.getEventDate().toLocalDate());
        DatePicker endDateInput = new DatePicker(event.getEndDate());
        TextField locationInput = new TextField(event.getEventLocation());
        ComboBox<String> statusInput = new ComboBox<>();
        statusInput.getItems().addAll("active", "canceled", "completed");
        statusInput.setValue(event.getStatus());
        ComboBox<Category> categoryInput = new ComboBox<>();
        try {
            categoryInput.getItems().addAll(categoryService.recuperer());
            categoryInput.setCellFactory(lv -> new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getName());
                }
            });
            categoryInput.setButtonCell(new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getName());
                }
            });
            categoryInput.setValue(event.getCategory());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load categories: " + e.getMessage());
            return;
        }
        TextField placesInput = new TextField(String.valueOf(event.getNumberOfPlaces()));
        TextField imageInput = new TextField(event.getImageFilename() != null ? event.getImageFilename() : "");
        Button chooseImageButton = new Button("Choose Image");
        String[] editImageFilename = {event.getImageFilename()};
        chooseImageButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Event Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            File selectedFile = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (selectedFile != null) {
                try {
                    String destinationDir = "src/main/resources/images";
                    File dir = new File(destinationDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                    Path destinationPath = Paths.get(destinationDir, fileName);
                    Files.copy(selectedFile.toPath(), destinationPath);
                    editImageFilename[0] = fileName;
                    imageInput.setText(fileName);
                } catch (IOException ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to upload image: " + ex.getMessage());
                }
            }
        });

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Title:"), titleInput,
                new Label("Description:"), descriptionInput,
                new Label("Start Date:"), startDateInput,
                new Label("End Date:"), endDateInput,
                new Label("Location:"), locationInput,
                new Label("Status:"), statusInput,
                new Label("Category:"), categoryInput,
                new Label("Number of Places:"), placesInput,
                new Label("Image:"), imageInput, chooseImageButton
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    String title = titleInput.getText().trim();
                    String description = descriptionInput.getText().trim();
                    LocalDate startDate = startDateInput.getValue();
                    LocalDate endDate = endDateInput.getValue();
                    String location = locationInput.getText().trim();
                    String status = statusInput.getValue();
                    Category category = categoryInput.getValue();
                    String placesText = placesInput.getText().trim();
                    String imageFilename = imageInput.getText().trim();

                    if (title.isEmpty() || description.isEmpty() || startDate == null || location.isEmpty() ||
                            status == null || category == null || placesText.isEmpty()) {
                        showAlert(Alert.AlertType.ERROR, "Error", "All fields except image and end date are required.");
                        return null;
                    }
                    if (endDate != null && endDate.isBefore(startDate)) {
                        showAlert(Alert.AlertType.ERROR, "Error", "End date must be after start date.");
                        return null;
                    }
                    int numberOfPlaces;
                    try {
                        numberOfPlaces = Integer.parseInt(placesText);
                        if (numberOfPlaces <= 0) {
                            showAlert(Alert.AlertType.ERROR, "Error", "Number of places must be positive.");
                            return null;
                        }
                    } catch (NumberFormatException ex) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Number of places must be a valid integer.");
                        return null;
                    }
                    if (!title.equals(event.getTitle()) && eventService.eventExists(title)) {
                        showAlert(Alert.AlertType.ERROR, "Error", "An event with this title already exists.");
                        return null;
                    }

                    Event updatedEvent = new Event();
                    updatedEvent.setId(event.getId());
                    updatedEvent.setTitle(title);
                    updatedEvent.setEventDescription(description);
                    updatedEvent.setEventDate(LocalDateTime.of(startDate, LocalTime.of(0, 0)));
                    updatedEvent.setEndDate(endDate);
                    updatedEvent.setEventLocation(location);
                    updatedEvent.setStatus(status);
                    updatedEvent.setCategory(category);
                    updatedEvent.setNumberOfPlaces(numberOfPlaces);
                    updatedEvent.setImageFilename(imageFilename.isEmpty() ? null : imageFilename);
                    return updatedEvent;
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Validation error: " + ex.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedEvent -> {
            try {
                eventService.modifier(updatedEvent);
                loadEvents();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Event updated successfully!");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update event: " + e.getMessage());
            }
        });
    }

    private void deleteEvent(Event event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this event?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    eventService.supprimer(event);
                    loadEvents();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Event deleted successfully!");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete event: " + e.getMessage());
                }
            }
        });
    }

    private void clearForm() {
        titleField.clear();
        descriptionField.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        locationField.clear();
        statusComboBox.setValue("active");
        categoryComboBox.getSelectionModel().clearSelection();
        placesField.clear();
        imageField.clear();
        selectedImageFilename = null;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}