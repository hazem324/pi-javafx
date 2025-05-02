package controllers;

import entities.Product;
import entities.ProductCategory;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import services.ProductService;
import services.ProductCategoryService;
import services.CurrencyConversionService;
import services.DynamicPricingService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ProductController {

    // List view components
    @FXML private GridPane productGrid;
    @FXML private Button addButton;
    @FXML private Button showFiltersButton;

    // Global currency selector
    @FXML private ComboBox<String> globalCurrencyComboBox;

    // Filter components (now created programmatically)
    private TextField filterNameField;
    private DatePicker filterDateFromPicker;
    private DatePicker filterDateToPicker;
    private ComboBox<ProductCategory> filterCategoryComboBox;
    private TextField filterPriceMinField;
    private TextField filterPriceMaxField;
    private ComboBox<String> filterAvailabilityComboBox;
    private ComboBox<String> filterDiscountComboBox;
    private ComboBox<String> sortFieldComboBox;
    private ComboBox<String> sortDirectionComboBox;
    private Stage filterPopupStage;

    // Form view components
    @FXML private Label formTitle;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private TextField priceField;
    @FXML private TextField imageUrlField;
    @FXML private Button uploadImageButton;
    @FXML private Spinner<Integer> stockSpinner;
    @FXML private ComboBox<ProductCategory> categoryComboBox;
    @FXML private ComboBox<String> currencyComboBox;
    @FXML private TextField discountField;
    @FXML private CheckBox useDynamicPricingCheckBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final ProductService productService = new ProductService();
    private final ProductCategoryService categoryService = new ProductCategoryService();
    private final CurrencyConversionService currencyService = new CurrencyConversionService();
    private final DynamicPricingService dynamicPricingService = new DynamicPricingService();
    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private final ObservableList<ProductCategory> categoryList = FXCollections.observableArrayList();
    private final List<ProductCardData> productCards = new ArrayList<>();
    private Product editingProduct;

    // Helper class to store product card data
    private static class ProductCardData {
        Label priceLabel; // Discounted price label
        Text originalPriceText; // Original price text (null if no discount)
        double originalPrice; // Price before discount (dynamicPrice)
        double discountedPrice; // Price after discount
        String originalCurrency;
        Label voteScoreLabel; // Track vote score label

        ProductCardData(Label priceLabel, Text originalPriceText, double originalPrice, double discountedPrice, String originalCurrency, Label voteScoreLabel) {
            this.priceLabel = priceLabel;
            this.originalPriceText = originalPriceText;
            this.originalPrice = originalPrice;
            this.discountedPrice = discountedPrice;
            this.originalCurrency = originalCurrency;
            this.voteScoreLabel = voteScoreLabel;
        }
    }

    @FXML
    public void initialize() {
        // Initialize category ComboBox (for form)
        try {
            categoryList.addAll(categoryService.getAllCategories());
        } catch (SQLException e) {
            showAlert("Error", "Failed to load categories: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        if (categoryComboBox != null) {
            categoryComboBox.setItems(categoryList);
            categoryComboBox.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(ProductCategory item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getName());
                }
            });
            categoryComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(ProductCategory item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getName());
                }
            });
        }

        // Initialize stock Spinner
        if (stockSpinner != null) {
            stockSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 1));
            stockSpinner.setEditable(false);
        }

        // Initialize currency ComboBox (for form)
        if (currencyComboBox != null) {
            ObservableList<String> currencies = FXCollections.observableArrayList("TND", "USD", "EUR");
            currencyComboBox.setItems(currencies);
            currencyComboBox.setValue("TND");
        }

        // Initialize global currency ComboBox
        if (globalCurrencyComboBox != null) {
            ObservableList<String> currencies = FXCollections.observableArrayList("TND", "USD", "EUR");
            globalCurrencyComboBox.setItems(currencies);
            globalCurrencyComboBox.setValue("TND");
            globalCurrencyComboBox.setStyle("-fx-font-size: 14px; -fx-background-radius: 5;");
            globalCurrencyComboBox.setOnAction(e -> updateAllPrices());
        }

        // Bind the priceField's disabled property to the useDynamicPricingCheckBox
        if (useDynamicPricingCheckBox != null && priceField != null) {
            priceField.disableProperty().bind(useDynamicPricingCheckBox.selectedProperty());
        }

        // Initialize filter components programmatically
        initializeFilterComponents();

        // Load products
        try {
            refreshProductList();
        } catch (SQLException e) {
            showAlert("Error", "Failed to load products: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void initializeFilterComponents() {
        // Initialize filter components
        filterNameField = new TextField();
        filterNameField.setPromptText("Enter product name");
        filterNameField.setStyle("-fx-pref-width: 150; -fx-background-radius: 5;");

        filterDateFromPicker = new DatePicker();
        filterDateFromPicker.setPromptText("Select start date");
        filterDateFromPicker.setStyle("-fx-pref-width: 150;");

        filterDateToPicker = new DatePicker();
        filterDateToPicker.setPromptText("Select end date");
        filterDateToPicker.setStyle("-fx-pref-width: 150;");

        filterCategoryComboBox = new ComboBox<>();
        filterCategoryComboBox.setItems(categoryList);
        filterCategoryComboBox.setStyle("-fx-pref-width: 150; -fx-background-radius: 5;");
        filterCategoryComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ProductCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "All Categories" : item.getName());
            }
        });
        filterCategoryComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ProductCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "All Categories" : item.getName());
            }
        });

        filterPriceMinField = new TextField();
        filterPriceMinField.setPromptText("Min price");
        filterPriceMinField.setStyle("-fx-pref-width: 100; -fx-background-radius: 5;");

        filterPriceMaxField = new TextField();
        filterPriceMaxField.setPromptText("Max price");
        filterPriceMaxField.setStyle("-fx-pref-width: 100; -fx-background-radius: 5;");

        filterAvailabilityComboBox = new ComboBox<>();
        filterAvailabilityComboBox.setItems(FXCollections.observableArrayList("all", "available", "unavailable"));
        filterAvailabilityComboBox.setValue("all");
        filterAvailabilityComboBox.setStyle("-fx-pref-width: 150; -fx-background-radius: 5;");

        filterDiscountComboBox = new ComboBox<>();
        filterDiscountComboBox.setItems(FXCollections.observableArrayList("all", "discounted", "non_discounted"));
        filterDiscountComboBox.setValue("all");
        filterDiscountComboBox.setStyle("-fx-pref-width: 150; -fx-background-radius: 5;");

        sortFieldComboBox = new ComboBox<>();
        sortFieldComboBox.setItems(FXCollections.observableArrayList("productName", "productPrice", "createdAt", "discount", "voteScore"));
        sortFieldComboBox.setValue("voteScore"); // Default sort by voteScore
        sortFieldComboBox.setStyle("-fx-pref-width: 150; -fx-background-radius: 5;");

        sortDirectionComboBox = new ComboBox<>();
        sortDirectionComboBox.setItems(FXCollections.observableArrayList("ASC", "DESC"));
        sortDirectionComboBox.setValue("DESC");
        sortDirectionComboBox.setStyle("-fx-pref-width: 100; -fx-background-radius: 5;");
    }

    @FXML
    private void showFiltersPopup() {
        if (filterPopupStage != null && filterPopupStage.isShowing()) {
            filterPopupStage.close();
        }

        // Create the filter popup
        filterPopupStage = new Stage();
        filterPopupStage.initStyle(StageStyle.UNDECORATED);
        filterPopupStage.initOwner(showFiltersButton.getScene().getWindow());

        VBox filterVBox = new VBox(10);
        filterVBox.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 10);");
        filterVBox.setPrefWidth(300);

        // Name filter
        HBox nameBox = new HBox(10);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label("Name:");
        nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        nameBox.getChildren().addAll(nameLabel, filterNameField);

        // Date range filter
        HBox dateBox = new HBox(10);
        dateBox.setAlignment(Pos.CENTER_LEFT);
        Label dateFromLabel = new Label("Created From:");
        dateFromLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        Label dateToLabel = new Label("To:");
        dateToLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        dateBox.getChildren().addAll(dateFromLabel, filterDateFromPicker, dateToLabel, filterDateToPicker);

        // Category filter
        HBox categoryBox = new HBox(10);
        categoryBox.setAlignment(Pos.CENTER_LEFT);
        Label categoryLabel = new Label("Category:");
        categoryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        categoryBox.getChildren().addAll(categoryLabel, filterCategoryComboBox);

        // Price range filter
        HBox priceBox = new HBox(10);
        priceBox.setAlignment(Pos.CENTER_LEFT);
        Label priceMinLabel = new Label("Price Min:");
        priceMinLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        Label priceMaxLabel = new Label("Max:");
        priceMaxLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        priceBox.getChildren().addAll(priceMinLabel, filterPriceMinField, priceMaxLabel, filterPriceMaxField);

        // Availability filter
        HBox availabilityBox = new HBox(10);
        availabilityBox.setAlignment(Pos.CENTER_LEFT);
        Label availabilityLabel = new Label("Availability:");
        availabilityLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        availabilityBox.getChildren().addAll(availabilityLabel, filterAvailabilityComboBox);

        // Discount filter
        HBox discountBox = new HBox(10);
        discountBox.setAlignment(Pos.CENTER_LEFT);
        Label discountLabel = new Label("Discount:");
        discountLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        discountBox.getChildren().addAll(discountLabel, filterDiscountComboBox);

        // Sort filter
        HBox sortBox = new HBox(10);
        sortBox.setAlignment(Pos.CENTER_LEFT);
        Label sortLabel = new Label("Sort By:");
        sortLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        sortBox.getChildren().addAll(sortLabel, sortFieldComboBox, sortDirectionComboBox);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        Button applyButton = new Button("Apply");
        applyButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20; -fx-background-radius: 12;");
        applyButton.setOnAction(e -> {
            applyFilters();
            filterPopupStage.close();
        });
        Button clearButton = new Button("Clear");
        clearButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20; -fx-background-radius: 12;");
        clearButton.setOnAction(e -> {
            clearFilters();
            filterPopupStage.close();
        });
        buttonBox.getChildren().addAll(applyButton, clearButton);

        filterVBox.getChildren().addAll(nameBox, dateBox, categoryBox, priceBox, availabilityBox, discountBox, sortBox, buttonBox);

        Scene scene = new Scene(filterVBox);
        filterPopupStage.setScene(scene);

        // Position the popup below the Filters button
        double x = showFiltersButton.getScene().getWindow().getX() + showFiltersButton.getScene().getX() + showFiltersButton.getLayoutX();
        double y = showFiltersButton.getScene().getWindow().getY() + showFiltersButton.getScene().getY() + showFiltersButton.getLayoutY() + showFiltersButton.getHeight();
        filterPopupStage.setX(x);
        filterPopupStage.setY(y);

        filterPopupStage.show();
    }

    private void refreshProductList() throws SQLException {
        String name = filterNameField != null ? filterNameField.getText() : null;
        LocalDateTime dateFrom = filterDateFromPicker != null && filterDateFromPicker.getValue() != null
                ? filterDateFromPicker.getValue().atStartOfDay()
                : null;
        LocalDateTime dateTo = filterDateToPicker != null && filterDateToPicker.getValue() != null
                ? filterDateToPicker.getValue().atTime(LocalTime.MAX)
                : null;
        Integer categoryId = filterCategoryComboBox != null && filterCategoryComboBox.getValue() != null
                ? filterCategoryComboBox.getValue().getId()
                : null;
        Double priceMin = filterPriceMinField != null && !filterPriceMinField.getText().isEmpty()
                ? Double.parseDouble(filterPriceMinField.getText())
                : null;
        Double priceMax = filterPriceMaxField != null && !filterPriceMaxField.getText().isEmpty()
                ? Double.parseDouble(filterPriceMaxField.getText())
                : null;
        String availability = filterAvailabilityComboBox != null ? filterAvailabilityComboBox.getValue() : "all";
        String discountFilter = filterDiscountComboBox != null ? filterDiscountComboBox.getValue() : "all";
        String sortField = sortFieldComboBox != null ? sortFieldComboBox.getValue() : "voteScore"; // Default to voteScore
        String sortDirection = sortDirectionComboBox != null ? sortDirectionComboBox.getValue() : "DESC";

        productList.setAll(productService.searchProducts(
                name, dateFrom, dateTo, categoryId, priceMin, priceMax, availability, discountFilter, sortField, sortDirection
        ));

        if (productGrid != null) {
            productGrid.getChildren().clear();
            productCards.clear(); // Clear the list of tracked cards
            int row = 0, col = 0;
            for (Product p : productList) {
                VBox card = createProductCard(p);
                productGrid.add(card, col, row);
                col++;
                if (col > 3) {
                    col = 0;
                    row++;
                }
            }
            // Update all prices to match the current global currency
            updateAllPrices();
        }
    }

    @FXML
    private void applyFilters() {
        try {
            refreshProductList();
        } catch (SQLException e) {
            showAlert("Error", "Failed to apply filters: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid price format. Please enter valid numbers for price fields.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void clearFilters() {
        if (filterNameField != null) filterNameField.clear();
        if (filterDateFromPicker != null) filterDateFromPicker.setValue(null);
        if (filterDateToPicker != null) filterDateToPicker.setValue(null);
        if (filterCategoryComboBox != null) filterCategoryComboBox.setValue(null);
        if (filterPriceMinField != null) filterPriceMinField.clear();
        if (filterPriceMaxField != null) filterPriceMaxField.clear();
        if (filterAvailabilityComboBox != null) filterAvailabilityComboBox.setValue("all");
        if (filterDiscountComboBox != null) filterDiscountComboBox.setValue("all");
        if (sortFieldComboBox != null) sortFieldComboBox.setValue("voteScore"); // Reset to voteScore
        if (sortDirectionComboBox != null) sortDirectionComboBox.setValue("DESC");

        try {
            refreshProductList();
        } catch (SQLException e) {
            showAlert("Error", "Failed to clear filters: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private VBox createProductCard(Product p) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 10);");
        card.setMaxWidth(Double.MAX_VALUE);

        ImageView imageView = new ImageView();
        if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
            try {
                imageView.setImage(new Image(new File(p.getImageUrl()).toURI().toString()));
            } catch (Exception e) {
                imageView.setImage(new Image("/placeholder.jpg"));
            }
        }
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);

        Label nameLabel = new Label(p.getProductName());
        nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label descLabel = new Label(p.getProductDescription());
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(Double.MAX_VALUE);

        Label categoryLabel = new Label(p.getProductCategory() != null ? p.getProductCategory().getName() : "No Category");
        categoryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");

        String originalCurrency = p.getCurrency() != null ? p.getCurrency() : "TND";
        double referencePrice = p.getProductPrice();
        double dynamicPrice = p.isUseDynamicPricing() && p.getDynamicPrice() != 0.0 ? p.getDynamicPrice() : referencePrice;

        double discount = p.getDiscount();
        double discountedPrice = dynamicPrice * (1 - discount / 100);

        // Debug logging with more details
        System.out.println("Product: " + p.getProductName());
        System.out.println("Stock: " + p.getProductStock());
        System.out.println("Category ID: " + (p.getProductCategory() != null ? p.getProductCategory().getId() : "None"));
        System.out.println("Reference Price: " + referencePrice);
        System.out.println("Dynamic Price: " + dynamicPrice);
        System.out.println("Discount: " + discount);
        System.out.println("Discounted Price: " + discountedPrice);
        System.out.println("Vote Score: " + p.getVoteScore());

        // Price display
        StackPane originalPricePane = null;
        Text originalPriceText = null;
        Label priceLabel = new Label(String.format("%.2f%s", discountedPrice, originalCurrency));
        priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #7ac400;");

        if (discount > 0) {
            // For discounted products, show original price with a custom strikethrough
            originalPriceText = new Text(String.format("%.2f%s", dynamicPrice, originalCurrency));
            originalPriceText.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-fill: #7ac400;");

            // Create a Line for the strikethrough
            Line strikethrough = new Line();
            strikethrough.setStyle("-fx-stroke: #7ac400; -fx-stroke-width: 2;");

            // Bind the line's length to the text width
            strikethrough.endXProperty().bind(originalPriceText.boundsInLocalProperty().map(bounds -> bounds.getWidth()));

            // Position the line in the middle of the text height
            strikethrough.setStartY(8);
            strikethrough.setEndY(8);

            // Use StackPane to overlay the line on the text, aligned to the left
            originalPricePane = new StackPane();
            originalPricePane.setAlignment(Pos.CENTER_LEFT);
            originalPricePane.getChildren().addAll(originalPriceText, strikethrough);
        }

        // Show discount only if it exists (discount > 0)
        Label discountLabel = null;
        if (discount > 0) {
            discountLabel = new Label(String.format("Discount: %.0f%%", discount));
            discountLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #dc3545;");
        }

        Label stockLabel = new Label(p.getProductStock() > 0 ? "In Stock" : "Out of Stock");
        stockLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + (p.getProductStock() > 0 ? "#28a745" : "#dc3545") + ";");

        // Vote score display
        Label voteScoreLabel = new Label("Likes: " + p.getVoteScore());
        voteScoreLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #007bff;");

        // Upvote/Downvote buttons
        HBox voteButtons = new HBox(10);
        voteButtons.setAlignment(Pos.CENTER_LEFT);

        Button upvoteButton = new Button("👍");
        upvoteButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 10; -fx-background-radius: 12; -fx-background-color: #e9ecef;");
        upvoteButton.setOnAction(e -> {
            try {
                productService.incrementVoteScore(p.getId());
                p.setVoteScore(p.getVoteScore() + 1);
                voteScoreLabel.setText("Likes: " + p.getVoteScore());
                // Refresh the list to re-sort by vote score
                refreshProductList();
            } catch (SQLException ex) {
                showAlert("Error", "Failed to upvote: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        Button downvoteButton = new Button("👎");
        downvoteButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 10; -fx-background-radius: 12; -fx-background-color: #e9ecef;");
        downvoteButton.setOnAction(e -> {
            try {
                productService.decrementVoteScore(p.getId());
                p.setVoteScore(p.getVoteScore() - 1);
                voteScoreLabel.setText("Likes: " + p.getVoteScore());
                // Refresh the list to re-sort by vote score
                refreshProductList();
            } catch (SQLException ex) {
                showAlert("Error", "Failed to downvote: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        voteButtons.getChildren().addAll(upvoteButton, downvoteButton);

        // Edit/Delete buttons
        HBox buttons = new HBox(15);
        buttons.setStyle("-fx-alignment: center;");
        Button editButton = new Button("Edit");
        editButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20; -fx-background-radius: 12;");
        editButton.setOnAction(e -> showEditForm(p));
        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20; -fx-background-radius: 12;");
        deleteButton.setOnAction(e -> handleDelete(p));
        buttons.getChildren().addAll(editButton, deleteButton);

        // Track this card's price labels, vote score label, and original price data
        productCards.add(new ProductCardData(priceLabel, originalPriceText, dynamicPrice, discountedPrice, originalCurrency, voteScoreLabel));

        // Add components to the card
        if (discount > 0) {
            // For discounted products, include both original and discounted prices
            if (discountLabel != null) {
                card.getChildren().addAll(imageView, nameLabel, descLabel, categoryLabel, originalPricePane, priceLabel, discountLabel, stockLabel, voteScoreLabel, voteButtons, buttons);
            } else {
                card.getChildren().addAll(imageView, nameLabel, descLabel, categoryLabel, originalPricePane, priceLabel, stockLabel, voteScoreLabel, voteButtons, buttons);
            }
        } else {
            // For non-discounted products, only include the final price
            if (discountLabel != null) {
                card.getChildren().addAll(imageView, nameLabel, descLabel, categoryLabel, priceLabel, discountLabel, stockLabel, voteScoreLabel, voteButtons, buttons);
            } else {
                card.getChildren().addAll(imageView, nameLabel, descLabel, categoryLabel, priceLabel, stockLabel, voteScoreLabel, voteButtons, buttons);
            }
        }
        return card;
    }

    private void updateAllPrices() {
        String selectedCurrency = globalCurrencyComboBox.getValue();
        if (selectedCurrency == null) {
            selectedCurrency = "TND";
            globalCurrencyComboBox.setValue(selectedCurrency);
        }

        for (ProductCardData cardData : productCards) {
            String originalCurrency = cardData.originalCurrency;
            double originalPrice = cardData.originalPrice; // Before discount
            double discountedPrice = cardData.discountedPrice; // After discount
            Label priceLabel = cardData.priceLabel;
            Text originalPriceText = cardData.originalPriceText;

            if (!selectedCurrency.equals(originalCurrency)) {
                try {
                    double conversionRate = currencyService.fetchConversionRate(originalCurrency, selectedCurrency);
                    double convertedDiscountedPrice = discountedPrice * conversionRate;
                    priceLabel.setText(String.format("%.2f%s", convertedDiscountedPrice, selectedCurrency));
                    if (originalPriceText != null) {
                        double convertedOriginalPrice = originalPrice * conversionRate;
                        originalPriceText.setText(String.format("%.2f%s", convertedOriginalPrice, selectedCurrency));
                    }
                } catch (Exception ex) {
                    showAlert("Error", "Failed to convert currency: " + ex.getMessage(), Alert.AlertType.ERROR);
                    priceLabel.setText(String.format("%.2f%s", discountedPrice, originalCurrency));
                    if (originalPriceText != null) {
                        originalPriceText.setText(String.format("%.2f%s", originalPrice, originalCurrency));
                    }
                }
            } else {
                priceLabel.setText(String.format("%.2f%s", discountedPrice, originalCurrency));
                if (originalPriceText != null) {
                    originalPriceText.setText(String.format("%.2f%s", originalPrice, originalCurrency));
                }
            }
        }
    }

    @FXML
    private void showAddForm() {
        editingProduct = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/product_form.fxml"));
            Parent formRoot = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add Product");
            stage.setScene(new Scene(formRoot));
            stage.show();
            if (addButton != null && addButton.getScene() != null) {
                addButton.getScene().getWindow().hide();
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to open form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void showEditForm(Product p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/product_form.fxml"));
            Parent formRoot = loader.load();
            ProductController formController = loader.getController();
            formController.setEditingProduct(p);
            formController.fillForm(p);
            formController.formTitle.setText("Edit Product");
            formController.saveButton.setText("Update");
            Stage stage = new Stage();
            stage.setTitle("Edit Product");
            stage.setScene(new Scene(formRoot));
            stage.show();
            if (productGrid != null && productGrid.getScene() != null) {
                productGrid.getScene().getWindow().hide();
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to open form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void showListView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/product_list.fxml"));
            Parent listRoot = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Products");
            stage.setScene(new Scene(listRoot));
            stage.show();
            if (cancelButton != null && cancelButton.getScene() != null) {
                cancelButton.getScene().getWindow().hide();
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to open product list: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setEditingProduct(Product p) {
        this.editingProduct = p;
    }

    private void fillForm(Product p) {
        nameField.setText(p.getProductName());
        descriptionField.setText(p.getProductDescription());
        priceField.setText(p.getProductPrice() > 0 ? String.valueOf(p.getProductPrice()) : "");
        imageUrlField.setText(p.getImageUrl());
        stockSpinner.getValueFactory().setValue(p.getProductStock());
        categoryComboBox.setValue(p.getProductCategory());
        currencyComboBox.setValue(p.getCurrency() != null ? p.getCurrency() : "TND");
        discountField.setText(String.valueOf(p.getDiscount()));
        useDynamicPricingCheckBox.setSelected(p.isUseDynamicPricing());
        // Ensure priceField is disabled if dynamic pricing is enabled
        priceField.setDisable(p.isUseDynamicPricing());
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        Product product = editingProduct != null ? editingProduct : new Product();
        product.setProductName(nameField.getText());
        product.setProductDescription(descriptionField.getText());

        // Set the reference price: use the entered price if dynamic pricing is off, otherwise set to 0.0
        double referencePrice = useDynamicPricingCheckBox.isSelected() ? 0.0 : Double.parseDouble(priceField.getText());
        product.setProductPrice(referencePrice);

        String selectedCurrency = currencyComboBox.getValue();
        product.setCurrency(selectedCurrency != null ? selectedCurrency : "TND");
        product.setImageUrl(imageUrlField.getText());
        product.setProductStock(stockSpinner.getValue());
        product.setProductCategory(categoryComboBox.getValue());
        double discount = discountField.getText().isEmpty() ? 0.0 : Double.parseDouble(discountField.getText());
        product.setDiscount(discount);
        boolean useDynamicPricing = useDynamicPricingCheckBox.isSelected();
        product.setUseDynamicPricing(useDynamicPricing);

        if (editingProduct == null) {
            product.setCreatedAt(LocalDateTime.now());
            product.setStatus(true);
            product.setVoteScore(0); // Initialize voteScore for new products
        }

        try {
            if (editingProduct == null) {
                // Add new product
                productService.addProduct(product);
                if (useDynamicPricing) {
                    double dynamicPrice = dynamicPricingService.calculateDynamicPrice(product);
                    product.setDynamicPrice(dynamicPrice);
                    // Update the product in the database to save the dynamic price
                    productService.updateProduct(product);
                }
                showAlert("Success", "Product created successfully!", Alert.AlertType.INFORMATION);
            } else {
                // Update existing product
                if (useDynamicPricing && product.getDynamicPrice() == 0.0) {
                    // Recalculate dynamic price only if it hasn't been set yet
                    double dynamicPrice = dynamicPricingService.calculateDynamicPrice(product);
                    product.setDynamicPrice(dynamicPrice);
                } else if (!useDynamicPricing) {
                    // If dynamic pricing is disabled, reset dynamic price to 0.0
                    product.setDynamicPrice(0.0);
                }
                productService.updateProduct(product);
                showAlert("Success", "Product updated successfully!", Alert.AlertType.INFORMATION);
            }
            showListView();
        } catch (SQLException e) {
            showAlert("Error", "Failed to save product: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Error", "Failed to calculate dynamic price: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleDelete(Product p) {
        try {
            productService.deleteProduct(p.getId());
            showAlert("Success", "Product deleted successfully!", Alert.AlertType.INFORMATION);
            refreshProductList();
        } catch (SQLException e) {
            showAlert("Error", "Failed to delete product: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.gif")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                String uploadsDir = "Uploads/products";
                Path uploadsPath = Paths.get(uploadsDir);
                if (!Files.exists(uploadsPath)) {
                    Files.createDirectories(uploadsPath);
                }
                String newFilename = System.currentTimeMillis() + "_" + file.getName();
                Path targetPath = uploadsPath.resolve(newFilename);
                Files.copy(file.toPath(), targetPath);
                imageUrlField.setText(targetPath.toString());
            } catch (IOException e) {
                showAlert("Error", "Failed to upload image: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        // Name validation
        String name = nameField.getText();
        if (name == null || name.trim().isEmpty()) {
            errors.append("Name cannot be blank.\n");
        } else if (name.length() < 2) {
            errors.append("Name must be at least 2 characters long.\n");
        } else if (name.length() > 20) {
            errors.append("Name cannot exceed 20 characters.\n");
        }

        // Description validation
        String description = descriptionField.getText();
        if (description == null || description.trim().isEmpty()) {
            errors.append("Description cannot be blank.\n");
        } else if (description.length() < 10) {
            errors.append("Description must be at least 10 characters long.\n");
        } else if (description.length() > 30) {
            errors.append("Description cannot exceed 30 characters.\n");
        }

        // Price validation (only required if dynamic pricing is not enabled)
        if (!useDynamicPricingCheckBox.isSelected()) {
            String priceText = priceField.getText();
            if (priceText == null || priceText.trim().isEmpty()) {
                errors.append("Price cannot be blank.\n");
            } else {
                try {
                    double price = Double.parseDouble(priceText);
                    if (price < 0.01) {
                        errors.append("Price must be at least 0.01.\n");
                    }
                } catch (NumberFormatException e) {
                    errors.append("Price must be a valid number.\n");
                }
            }
        }

        // Discount validation
        String discountText = discountField.getText();
        if (discountText != null && !discountText.trim().isEmpty()) {
            try {
                double discount = Double.parseDouble(discountText);
                if (discount < 0 || discount > 100) {
                    errors.append("Discount must be between 0 and 100.\n");
                }
            } catch (NumberFormatException e) {
                errors.append("Discount must be a valid number.\n");
            }
        }

        // Stock validation
        int stock = stockSpinner.getValue();
        if (stock < 1) {
            errors.append("Stock must be at least 1.\n");
        } else if (stock > 50) {
            errors.append("Stock cannot exceed 50.\n");
        }

        // Category validation
        if (categoryComboBox.getValue() == null) {
            errors.append("Category must be selected.\n");
        }

        // Image validation
        String imageUrl = imageUrlField.getText();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            errors.append("Image is required.\n");
        } else {
            File imageFile = new File(imageUrl);
            if (!imageFile.exists() || !imageFile.isFile()) {
                errors.append("Image file is invalid.\n");
            } else {
                String lowerCaseImage = imageUrl.toLowerCase();
                if (!lowerCaseImage.endsWith(".jpg") && !lowerCaseImage.endsWith(".png") && !lowerCaseImage.endsWith(".gif")) {
                    errors.append("Image must be a JPG, PNG, or GIF file.\n");
                }
            }
        }

        if (errors.length() > 0) {
            showAlert("Validation Error", errors.toString(), Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void clearForm() {
        nameField.clear();
        descriptionField.clear();
        priceField.clear();
        imageUrlField.clear();
        stockSpinner.getValueFactory().setValue(1);
        categoryComboBox.setValue(null);
        currencyComboBox.setValue("TND");
        discountField.clear();
        useDynamicPricingCheckBox.setSelected(false);
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}