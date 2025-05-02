package controllers;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.borders.SolidBorder;
import entities.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import services.UserService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserManagementController {

    @FXML private Label totalUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Button addUserButton;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Number> idColumn;
    @FXML private TableColumn<User, String> firstNameColumn;
    @FXML private TableColumn<User, String> lastNameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, Boolean> isBlockedColumn;
    @FXML private TableColumn<User, String> rolesColumn;
    @FXML private TableColumn<User, Void> actionsColumn;
    @FXML private HBox paginationContainer;
    @FXML private Button exportExcelButton;
    @FXML private Button exportPdfButton;
    @FXML private TextField searchFilter;

    private int totalUserCount = 0;
    private int currentPage = 0;
    private final int USERS_PER_PAGE = 10;
    private List<User> allUsers;
    private final ObservableList<User> usersOnPage = FXCollections.observableArrayList();
    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        setupTableColumns();
        setupActionColumn();
        loadAllUsers();
        updatePagination();
        usersTable.setItems(usersOnPage);

        // Add clear button to search field
        addClearButtonToSearchField();

        // Add listener for real-time filtering
        searchFilter.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void addClearButtonToSearchField() {
        // Find the HBox containing the searchFilter and ImageView
        HBox parentHBox = (HBox) searchFilter.getParent();
        if (parentHBox != null) {
            Button clearButton = new Button("✖");
            clearButton.getStyleClass().add("clear-button");
            clearButton.setVisible(false);

            // Show/hide clear button based on text presence
            searchFilter.textProperty().addListener((obs, oldVal, newVal) -> {
                clearButton.setVisible(!newVal.isEmpty());
            });

            // Clear the text field when the button is clicked
            clearButton.setOnAction(event -> searchFilter.clear());

            // Add the clear button to the right side of the HBox
            parentHBox.getChildren().add(clearButton);
            HBox.setMargin(clearButton, new javafx.geometry.Insets(0, 10, 0, 0));
        }
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        isBlockedColumn.setCellValueFactory(new PropertyValueFactory<>("blocked"));
        isBlockedColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean blocked, boolean empty) {
                super.updateItem(blocked, empty);
                if (empty || blocked == null) {
                    setText(null);
                } else {
                    setText(blocked ? "Blocked" : "Active");
                }
            }
        });
        rolesColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                String.join(", ", cellData.getValue().getRoles())
        ));
    }

    private void setupActionColumn() {
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final Button toggleBlockButton = new Button();
            private final HBox buttons = new HBox(10, editButton, deleteButton, toggleBlockButton);

            {
                editButton.setStyle("-fx-background-color: #00897b; -fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-font-family: 'Segoe UI', sans-serif; -fx-background-radius: 8; -fx-padding: 8 15 8 15; -fx-cursor: hand;");
                deleteButton.setStyle("-fx-background-color: #e57373; -fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-font-family: 'Segoe UI', sans-serif; -fx-background-radius: 8; -fx-padding: 8 15 8 15; -fx-cursor: hand;");
                toggleBlockButton.setStyle("-fx-background-color: #ffb300; -fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-font-family: 'Segoe UI', sans-serif; -fx-background-radius: 8; -fx-padding: 8 15 8 15; -fx-cursor: hand;");
                buttons.setStyle("-fx-alignment: CENTER;");

                editButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    if (user != null) editUser(user);
                });

                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    if (user != null) deleteUser(user);
                });

                toggleBlockButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    if (user != null) toggleBlockUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    if (user != null) {
                        toggleBlockButton.setText(user.isBlocked() ? "Unblock" : "Block");
                        setGraphic(buttons);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    void loadAllUsers() {
        try {
            allUsers = userService.recuperer();
            totalUserCount = allUsers.size();
            loadStats();
            applyFilters();
        } catch (SQLException e) {
            showError("Failed to load users: " + e.getMessage());
        }
    }

    private void loadStats() {
        try {
            long activeUsers = allUsers.stream().filter(user -> !user.isBlocked()).count();
            totalUsersLabel.setText("Total Users: " + totalUserCount);
            activeUsersLabel.setText("Active Users: " + activeUsers);
        } catch (Exception e) {
            showError("Failed to load stats: " + e.getMessage());
            totalUsersLabel.setText("Total Users: N/A");
            activeUsersLabel.setText("Active Users: N/A");
        }
    }

    private void applyFilters() {
        String searchTerm = searchFilter.getText().toLowerCase().trim();

        List<User> filteredUsers = allUsers.stream()
                .filter(user -> searchTerm.isEmpty() ||
                        user.getEmail().toLowerCase().contains(searchTerm) ||
                        user.getFirstName().toLowerCase().contains(searchTerm) ||
                        user.getLastName().toLowerCase().contains(searchTerm) ||
                        String.join(", ", user.getRoles()).toLowerCase().contains(searchTerm) ||
                        matchesStatus(user.isBlocked(), searchTerm))
                .collect(Collectors.toList());

        totalUserCount = filteredUsers.size();
        loadStats();
        currentPage = 0; // Reset to first page on filter change
        showUsersOnPage(currentPage);
        updatePagination();
    }

    private boolean matchesStatus(boolean isBlocked, String searchTerm) {
        // Check both text and numeric representations of status
        String statusText = isBlocked ? "blocked" : "active";
        int statusValue = isBlocked ? 1 : 0;
        return statusText.contains(searchTerm) ||
                String.valueOf(statusValue).contains(searchTerm);
    }

    private void updatePagination() {
        paginationContainer.getChildren().clear();
        int pageCount = (int) Math.ceil((double) totalUserCount / USERS_PER_PAGE);
        if (pageCount <= 1) return;

        Button firstButton = new Button("First");
        firstButton.setOnAction(event -> {
            currentPage = 0;
            showUsersOnPage(currentPage);
            updatePagination();
        });
        paginationContainer.getChildren().add(firstButton);

        Button prevButton = new Button("Previous");
        prevButton.setOnAction(event -> {
            if (currentPage > 0) {
                currentPage--;
                showUsersOnPage(currentPage);
                updatePagination();
            }
        });
        paginationContainer.getChildren().add(prevButton);

        int maxPagesToShow = 5;
        int startPage = Math.max(0, currentPage - maxPagesToShow / 2);
        int endPage = Math.min(pageCount - 1, currentPage + maxPagesToShow / 2);

        if (endPage - startPage < maxPagesToShow - 1) {
            if (startPage == 0) {
                endPage = Math.min(pageCount - 1, maxPagesToShow - 1);
            } else if (endPage == pageCount - 1) {
                startPage = Math.max(0, pageCount - maxPagesToShow);
            }
        }

        for (int i = startPage; i <= endPage; i++) {
            final int page = i;
            Button pageButton = new Button(String.valueOf(page + 1));
            if (page == currentPage) {
                pageButton.setStyle("-fx-font-weight: bold; -fx-background-color: #b0bec5;");
            }
            pageButton.setOnAction(event -> {
                currentPage = page;
                showUsersOnPage(currentPage);
                updatePagination();
            });
            paginationContainer.getChildren().add(pageButton);
        }

        Button nextButton = new Button("Next");
        nextButton.setOnAction(event -> {
            if (currentPage < pageCount - 1) {
                currentPage++;
                showUsersOnPage(currentPage);
                updatePagination();
            }
        });
        paginationContainer.getChildren().add(nextButton);

        Button lastButton = new Button("Last");
        lastButton.setOnAction(event -> {
            currentPage = pageCount - 1;
            showUsersOnPage(currentPage);
            updatePagination();
        });
        paginationContainer.getChildren().add(lastButton);
    }

    private void showUsersOnPage(int page) {
        usersOnPage.clear();
        int start = page * USERS_PER_PAGE;
        int end = Math.min(start + USERS_PER_PAGE, totalUserCount);
        if (start < totalUserCount) {
            List<User> filteredUsers = applyFiltersToList(allUsers);
            usersOnPage.addAll(filteredUsers.subList(start, end));
        }
    }

    private List<User> applyFiltersToList(List<User> users) {
        String searchTerm = searchFilter.getText().toLowerCase().trim();

        return users.stream()
                .filter(user -> searchTerm.isEmpty() ||
                        user.getEmail().toLowerCase().contains(searchTerm) ||
                        user.getFirstName().toLowerCase().contains(searchTerm) ||
                        user.getLastName().toLowerCase().contains(searchTerm) ||
                        String.join(", ", user.getRoles()).toLowerCase().contains(searchTerm) ||
                        matchesStatus(user.isBlocked(), searchTerm))
                .collect(Collectors.toList());
    }

//    private boolean matchesStatus(boolean isBlocked, String searchTerm) {
//        // Check both text and numeric representations of status
//        String statusText = isBlocked ? "blocked" : "active";
//        int statusValue = isBlocked ? 1 : 0;
//        return statusText.contains(searchTerm) ||
//                String.valueOf(statusValue).contains(searchTerm);
//    }

    @FXML
    public void addUser(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddUserForm.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add New User");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadAllUsers();
            updatePagination();
        } catch (IOException e) {
            showError("Failed to load add user form: " + e.getMessage());
        }
    }

    public void editUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditUserForm.fxml"));
            Parent root = loader.load();
            EditUserFormController editUserController = loader.getController();
            editUserController.initData(user);

            Stage stage = new Stage();
            stage.setTitle("Edit User");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadAllUsers();
            updatePagination();
        } catch (IOException e) {
            showError("Failed to load edit user form: " + e.getMessage());
        }
    }

    public void deleteUser(User userToDelete) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete User");
        alert.setHeaderText("Confirm Delete");
        alert.setContentText("Are you sure you want to delete user: " + userToDelete.getFirstName() + " " + userToDelete.getLastName() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                userService.supprimer(userToDelete);
                loadAllUsers();
                updatePagination();
                showAlert(Alert.AlertType.INFORMATION, "Success", "User deleted successfully.");
            } catch (SQLException e) {
                showError("Could not delete user: " + e.getMessage());
            }
        }
    }

    public void toggleBlockUser(User user) {
        boolean newBlockedStatus = !user.isBlocked();
        try {
            userService.updateBlockStatus(user.getId(), newBlockedStatus);
            loadAllUsers();
            updatePagination();
            showAlert(Alert.AlertType.INFORMATION, "User Block Status", "User " + user.getEmail() + " is now " + (newBlockedStatus ? "blocked" : "unblocked") + ".");
        } catch (SQLException e) {
            showError("Failed to update block status: " + e.getMessage());
        }
    }

    @FXML
    private void exportToExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        fileChooser.setInitialFileName("Users_" + timestamp + ".xlsx");

        Stage stage = (Stage) usersTable.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Users");

                // Header style
                org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
                org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);

                // Header row
                Row header = sheet.createRow(0);
                String[] headers = {"ID", "First Name", "Last Name", "Email", "Roles", "Status"};
                for (int i = 0; i < headers.length; i++) {
                    org.apache.poi.ss.usermodel.Cell cell = header.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Data rows
                int rowNum = 1;
                List<User> exportUsers = applyFiltersToList(allUsers);
                for (User user : exportUsers) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(user.getId());
                    row.createCell(1).setCellValue(user.getFirstName());
                    row.createCell(2).setCellValue(user.getLastName());
                    row.createCell(3).setCellValue(user.getEmail());
                    row.createCell(4).setCellValue(String.join(", ", user.getRoles()));
                    row.createCell(5).setCellValue(user.isBlocked() ? "Blocked" : "Active");
                }

                // Auto-size columns
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                // Write to file
                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }

                showAlert(Alert.AlertType.INFORMATION, "Success", "Users exported to Excel successfully!");
            } catch (IOException e) {
                showError("Failed to export to Excel: " + e.getMessage());
            }
        }
    }

    @FXML
    private void exportToPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        fileChooser.setInitialFileName("Users_" + timestamp + ".pdf");

        Stage stage = (Stage) usersTable.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (PdfWriter writer = new PdfWriter(file);
                 PdfDocument pdf = new PdfDocument(writer)) {
                Document document = new Document(pdf);

                // Add page number footer
                pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new IEventHandler() {
                    @Override
                    public void handleEvent(Event event) {
                        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
                        PdfDocument pdfDoc = docEvent.getDocument();
                        PdfPage page = docEvent.getPage();
                        int pageNumber = pdfDoc.getPageNumber(page);
                        int totalPages = pdfDoc.getNumberOfPages();
                        PdfCanvas canvas = new PdfCanvas(page);
                        try {
                            canvas.beginText()
                                    .setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont("Helvetica"), 8)
                                    .moveText(500, 20)
                                    .showText(String.format("Page %d of %d", pageNumber, totalPages))
                                    .endText();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        canvas.release();
                    }
                });

                // Title
                Paragraph title = new Paragraph("User List")
                        .setFontSize(20)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(20);
                document.add(title);

                // Timestamp
                Paragraph timestampPara = new Paragraph("Generated on: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
                        .setFontSize(10)
                        .setItalic()
                        .setTextAlignment(TextAlignment.LEFT)
                        .setMarginBottom(20);
                document.add(timestampPara);

                // Table
                Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2, 2, 3, 2, 1}))
                        .useAllAvailableWidth()
                        .setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));

                // Headers
                String[] headers = {"ID", "First Name", "Last Name", "Email", "Roles", "Status"};
                for (String header : headers) {
                    table.addHeaderCell(new Cell()
                            .add(new Paragraph(header)
                                    .setFontSize(10)
                                    .setBold())
                            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f)));
                }

                // Data rows
                boolean alternate = false;
                List<User> exportUsers = applyFiltersToList(allUsers);
                for (User user : exportUsers) {
                    table.addCell(new Cell()
                            .add(new Paragraph(String.valueOf(user.getId())).setFontSize(10))
                            .setBackgroundColor(alternate ? new com.itextpdf.kernel.colors.DeviceRgb(245, 245, 245) : ColorConstants.WHITE)
                            .setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f)));
                    table.addCell(new Cell()
                            .add(new Paragraph(user.getFirstName()).setFontSize(10))
                            .setBackgroundColor(alternate ? new com.itextpdf.kernel.colors.DeviceRgb(245, 245, 245) : ColorConstants.WHITE)
                            .setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f)));
                    table.addCell(new Cell()
                            .add(new Paragraph(user.getLastName()).setFontSize(10))
                            .setBackgroundColor(alternate ? new com.itextpdf.kernel.colors.DeviceRgb(245, 245, 245) : ColorConstants.WHITE)
                            .setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f)));
                    table.addCell(new Cell()
                            .add(new Paragraph(user.getEmail()).setFontSize(10))
                            .setBackgroundColor(alternate ? new com.itextpdf.kernel.colors.DeviceRgb(245, 245, 245) : ColorConstants.WHITE)
                            .setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f)));
                    table.addCell(new Cell()
                            .add(new Paragraph(String.join(", ", user.getRoles())).setFontSize(10))
                            .setBackgroundColor(alternate ? new com.itextpdf.kernel.colors.DeviceRgb(245, 245, 245) : ColorConstants.WHITE)
                            .setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f)));
                    table.addCell(new Cell()
                            .add(new Paragraph(user.isBlocked() ? "Blocked" : "Active").setFontSize(10))
                            .setBackgroundColor(alternate ? new com.itextpdf.kernel.colors.DeviceRgb(245, 245, 245) : ColorConstants.WHITE)
                            .setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f)));
                    alternate = !alternate;
                }

                document.add(table);
                document.close();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Users exported to PDF successfully!");
            } catch (IOException e) {
                showError("Failed to export to PDF: " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }

    private void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Error", message);
    }
}