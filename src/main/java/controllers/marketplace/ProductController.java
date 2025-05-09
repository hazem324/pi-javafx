package controllers.marketplace;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import javafx.stage.Stage;
import javafx.stage.StageStyle;


import models.Product;
import models.ProductCategory;
import models.Cart;
import services.ProductService;

import services.ProductCategoryService;
import services.CurrencyConversionService;
import services.DynamicPricingService;

import utils.CartStorage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ProductController {
    @FXML
    private BorderPane bp;

    @FXML private GridPane productGrid;
    @FXML private Button addButton;
    @FXML private Button showFiltersButton;
    @FXML private ComboBox<String> globalCurrencyComboBox;
    private TextField filterNameField;
    private DatePicker filterDateFromPicker;
    private DatePicker filterDateToPicker;
    private ComboBox<ProductCategory> filterCategoryComboBox;
    private TextField filterPriceMinField;
    private TextField filterPriceMaxField;
    private ComboBox<String> filterAvailabilityComboBox;
    private ComboBox<String> filterDiscountComboBox;
    private ComboBox<String> filterVoteComboBox;
    private Stage filterPopupStage;
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

    private static class ProductCardData {
        Label priceLabel;
        Text originalPriceText;
        double originalPrice;
        double discountedPrice;
        String originalCurrency;
        Label voteScoreLabel;

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
        if (stockSpinner != null) {
            stockSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 1));
            stockSpinner.setEditable(false);
        }
        if (currencyComboBox != null) {
            ObservableList<String> currencies = FXCollections.observableArrayList("TND", "USD", "EUR");
            currencyComboBox.setItems(currencies);
            currencyComboBox.setValue("TND");
        }
        if (globalCurrencyComboBox != null) {
            ObservableList<String> currencies = FXCollections.observableArrayList("TND", "USD", "EUR");
            globalCurrencyComboBox.setItems(currencies);
            globalCurrencyComboBox.setValue("TND");
            globalCurrencyComboBox.setStyle("-fx-font-size: 14px; -fx-background-radius: 5;");
            globalCurrencyComboBox.setOnAction(e -> updateAllPrices());
        }
        if (useDynamicPricingCheckBox != null && priceField != null) {
            priceField.disableProperty().bind(useDynamicPricingCheckBox.selectedProperty());
        }
        initializeFilterComponents();
        try {
            refreshProductList();
        } catch (SQLException e) {
            showAlert("Error", "Failed to load products: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void initializeFilterComponents() {
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
        filterVoteComboBox = new ComboBox<>();
        filterVoteComboBox.setItems(FXCollections.observableArrayList("all", "above 0", "above 10", "above 50"));
        filterVoteComboBox.setValue("all");
        filterVoteComboBox.setStyle("-fx-pref-width: 150; -fx-background-radius: 5;");
    }

    @FXML
    private void showFiltersPopup() {
        if (filterPopupStage != null && filterPopupStage.isShowing()) {
            filterPopupStage.close();
        }
        filterPopupStage = new Stage();
        filterPopupStage.initStyle(StageStyle.UNDECORATED);
        filterPopupStage.initOwner(showFiltersButton.getScene().getWindow());
        VBox filterVBox = new VBox(10);
        filterVBox.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 10);");
        filterVBox.setPrefWidth(300);
        HBox nameBox = new HBox(10);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label("Name:");
        nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        nameBox.getChildren().addAll(nameLabel, filterNameField);
        HBox dateBox = new HBox(10);
        dateBox.setAlignment(Pos.CENTER_LEFT);
        Label dateFromLabel = new Label("Created From:");
        dateFromLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        Label dateToLabel = new Label("To:");
        dateToLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        dateBox.getChildren().addAll(dateFromLabel, filterDateFromPicker, dateToLabel, filterDateToPicker);
        HBox categoryBox = new HBox(10);
        categoryBox.setAlignment(Pos.CENTER_LEFT);
        Label categoryLabel = new Label("Category:");
        categoryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        categoryBox.getChildren().addAll(categoryLabel, filterCategoryComboBox);
        HBox priceBox = new HBox(10);
        priceBox.setAlignment(Pos.CENTER_LEFT);
        Label priceMinLabel = new Label("Price Min:");
        priceMinLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        Label priceMaxLabel = new Label("Max:");
        priceMaxLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        priceBox.getChildren().addAll(priceMinLabel, filterPriceMinField, priceMaxLabel, filterPriceMaxField);
        HBox availabilityBox = new HBox(10);
        availabilityBox.setAlignment(Pos.CENTER_LEFT);
        Label availabilityLabel = new Label("Availability:");
        availabilityLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        availabilityBox.getChildren().addAll(availabilityLabel, filterAvailabilityComboBox);
        HBox discountBox = new HBox(10);
        discountBox.setAlignment(Pos.CENTER_LEFT);
        Label discountLabel = new Label("Discount:");
        discountLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        discountBox.getChildren().addAll(discountLabel, filterDiscountComboBox);
        HBox voteBox = new HBox(10);
        voteBox.setAlignment(Pos.CENTER_LEFT);
        Label voteLabel = new Label("Votes:");
        voteLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        voteBox.getChildren().addAll(voteLabel, filterVoteComboBox);
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
        filterVBox.getChildren().addAll(nameBox, dateBox, categoryBox, priceBox, availabilityBox, discountBox, voteBox, buttonBox);
        Scene scene = new Scene(filterVBox);
        filterPopupStage.setScene(scene);
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
        String voteFilter = filterVoteComboBox != null ? filterVoteComboBox.getValue() : "all";
        productList.setAll(productService.searchProducts(
                name, dateFrom, dateTo, categoryId, priceMin, priceMax, availability, discountFilter, null, null
        ));
        if ("above 0".equals(voteFilter)) {
            productList.removeIf(p -> p.getVoteScore() <= 0);
        } else if ("above 10".equals(voteFilter)) {
            productList.removeIf(p -> p.getVoteScore() <= 10);
        } else if ("above 50".equals(voteFilter)) {
            productList.removeIf(p -> p.getVoteScore() <= 50);
        }
        productList.sort(Comparator
                .comparingInt(Product::getVoteScore)
                .thenComparingDouble(Product::getDiscount)
                .reversed());
        if (productGrid != null) {
            productGrid.getChildren().clear();
            productCards.clear();
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
        if (filterVoteComboBox != null) filterVoteComboBox.setValue("all");
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

        System.out.println("Product: " + p.getProductName());
        System.out.println("Stock: " + p.getProductStock());
        System.out.println("Category ID: " + (p.getProductCategory() != null ? p.getProductCategory().getId() : "None"));
        System.out.println("Reference Price: " + referencePrice);
        System.out.println("Dynamic Price: " + dynamicPrice);
        System.out.println("Discount: " + discount);
        System.out.println("Discounted Price: " + discountedPrice);
        System.out.println("Vote Score: " + p.getVoteScore());

        StackPane originalPricePane = null;
        Text originalPriceText = null;
        Label priceLabel = new Label(String.format("%.2f%s", discountedPrice, originalCurrency));
        priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #7ac400;");

        if (discount > 0) {
            originalPriceText = new Text(String.format("%.2f%s", dynamicPrice, originalCurrency));
            originalPriceText.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-fill: #7ac400;");
            Line strikethrough = new Line();
            strikethrough.setStyle("-fx-stroke: #7ac400; -fx-stroke-width: 2;");
            strikethrough.endXProperty().bind(originalPriceText.boundsInLocalProperty().map(bounds -> bounds.getWidth()));
            strikethrough.setStartY(8);
            strikethrough.setEndY(8);
            originalPricePane = new StackPane();
            originalPricePane.setAlignment(Pos.CENTER_LEFT);
            originalPricePane.getChildren().addAll(originalPriceText, strikethrough);
        }

        Label discountLabel = null;
        if (discount > 0) {
            discountLabel = new Label(String.format("Discount: %.0f%%", discount));
            discountLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #dc3545;");
        }

        Label stockLabel = new Label(p.getProductStock() > 0 ? "In Stock" : "Out of Stock");
        stockLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + (p.getProductStock() > 0 ? "#28a745" : "#dc3545") + ";");

        Label voteScoreLabel = new Label("Likes: " + p.getVoteScore());
        voteScoreLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #007bff;");

        HBox voteButtons = new HBox(10);
        voteButtons.setAlignment(Pos.CENTER_LEFT);
        Button upvoteButton = new Button("👍");
        upvoteButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 10; -fx-background-radius: 12; -fx-background-color: #e9ecef;");
        upvoteButton.setOnAction(e -> {
            try {
                productService.incrementVoteScore(p.getId());
                p.setVoteScore(p.getVoteScore() + 1);
                voteScoreLabel.setText("Likes: " + p.getVoteScore());
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
                refreshProductList();
            } catch (SQLException ex) {
                showAlert("Error", "Failed to downvote: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });
        voteButtons.getChildren().addAll(upvoteButton, downvoteButton);

        HBox quantityBox = new HBox(10);
        quantityBox.setAlignment(Pos.CENTER_LEFT);
        Label quantityLabel = new Label("Quantity:");
        quantityLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        Spinner<Integer> quantitySpinner = new Spinner<>();
        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, p.getProductStock() > 0 ? p.getProductStock() : 1, 1));
        quantitySpinner.setEditable(false);
        quantitySpinner.setPrefWidth(80);
        quantityBox.getChildren().addAll(quantityLabel, quantitySpinner);

        Button addToCartButton = new Button("Add to Cart");
        addToCartButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20; -fx-background-radius: 12;");
        if (p.getProductStock() == 0) {
            addToCartButton.setDisable(true);
            addToCartButton.setStyle("-fx-background-color: grey; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20; -fx-background-radius: 12;");
        }
        addToCartButton.setOnAction(evt -> {
            int qty = quantitySpinner.getValue();
            boolean produitExiste = false;

            for (Cart c : CartStorage.panier) {
                if (c.getProductId() == p.getId()) {
                    int newQty = c.getProductQuantity() + qty;
                    if (newQty > p.getProductStock()) {
                        showAlert("Error", "Quantity requested exceeds available stock!", Alert.AlertType.ERROR);
                        return;
                    }
                    c.setProductQuantity(newQty);
                    c.setTotal(newQty * c.getPrice());
                    produitExiste = true;
                    break;
                }
            }

            if (!produitExiste) {
                Cart item = new Cart();
                item.setProductId(p.getId());
                item.setProductQuantity(qty);
                item.setPrice(discountedPrice);
                item.setTotal(discountedPrice * qty);
                item.setUserId(1);
                item.setOrderId(0);
                CartStorage.panier.add(item);
            }
            showAlert("Success", "Product added to cart!", Alert.AlertType.INFORMATION);
        });

        HBox buttons = new HBox(15);
        buttons.setStyle("-fx-alignment: center;");
        Button editButton = new Button("Edit");
        editButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20; -fx-background-radius: 12;");
        editButton.setOnAction(e -> showEditForm(p));
        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20; -fx-background-radius: 12;");
        deleteButton.setOnAction(e -> handleDelete(p));
        buttons.getChildren().addAll(editButton, deleteButton);

        productCards.add(new ProductCardData(priceLabel, originalPriceText, dynamicPrice, discountedPrice, originalCurrency, voteScoreLabel));

        if (discount > 0) {
            if (discountLabel != null) {
                card.getChildren().addAll(imageView, nameLabel, descLabel, categoryLabel, originalPricePane, priceLabel, discountLabel, stockLabel, voteScoreLabel, voteButtons, quantityBox, addToCartButton, buttons);
            } else {
                card.getChildren().addAll(imageView, nameLabel, descLabel, categoryLabel, originalPricePane, priceLabel, stockLabel, voteScoreLabel, voteButtons, quantityBox, addToCartButton, buttons);
            }
        } else {
            if (discountLabel != null) {
                card.getChildren().addAll(imageView, nameLabel, descLabel, categoryLabel, priceLabel, discountLabel, stockLabel, voteScoreLabel, voteButtons, quantityBox, addToCartButton, buttons);
            } else {
                card.getChildren().addAll(imageView, nameLabel, descLabel, categoryLabel, priceLabel, stockLabel, voteScoreLabel, voteButtons, quantityBox, addToCartButton, buttons);
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
            double originalPrice = cardData.originalPrice;
            double discountedPrice = cardData.discountedPrice;
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
            if (bp == null) {
                showAlert("Error", "BorderPane is not initialized. Please ensure the controller is properly linked to the main layout.", Alert.AlertType.ERROR);
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplace/product_form.fxml"));
            Parent root = loader.load();
            bp.setCenter(root);
        } catch (IOException e) {
            showAlert("Error", "Failed to open form: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public void setBorderPane(BorderPane bp) {
        this.bp = bp;
        System.out.println("BorderPane set in ProductController: " + bp);
    }

    @FXML
    private void showEditForm(Product p) {
        try {
            if (bp == null) {
                showAlert("Error", "BorderPane is not initialized. Please ensure the controller is properly linked to the main layout.", Alert.AlertType.ERROR);
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplace/product_form.fxml"));
            Parent formRoot = loader.load();
            setEditingProduct(p);
            fillForm(p);
            formTitle.setText("Edit Product");
            saveButton.setText("Update");
            bp.setCenter(formRoot);
        } catch (IOException e) {
            showAlert("Error", "Failed to open form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void showListView() {
        try {
            if (bp == null) {
                showAlert("Error", "BorderPane is not initialized. Please ensure the controller is properly linked to the main layout.", Alert.AlertType.ERROR);
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplace/product_list.fxml"));
            Parent root = loader.load();
            bp.setCenter(root);
        } catch (IOException e) {
            showAlert("Error", "Failed to open product list: " + e.getMessage(), Alert.AlertType.ERROR);

            e.printStackTrace();
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
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        Product product = editingProduct != null ? editingProduct : new Product();
        product.setProductName(nameField.getText());
        product.setProductDescription(descriptionField.getText());
        product.setProductPrice(Double.parseDouble(priceField.getText()));
        product.setImageUrl(imageUrlField.getText());
        product.setProductStock(stockSpinner.getValue());
        product.setProductCategory(categoryComboBox.getValue());
        product.setCurrency(currencyComboBox.getValue() != null ? currencyComboBox.getValue() : "TND");
        product.setDiscount(discountField.getText().isEmpty() ? 0.0 : Double.parseDouble(discountField.getText()));
        product.setUseDynamicPricing(useDynamicPricingCheckBox.isSelected());
        if (editingProduct == null) {
            product.setCreatedAt(LocalDateTime.now());
            product.setStatus(true);
            product.setVoteScore(0);
        }

        try {
            if (editingProduct == null) {
                productService.addProduct(product);
                showAlert("Success", "Product created successfully!", Alert.AlertType.INFORMATION);
            } else {
                productService.updateProduct(product);
                showAlert("Success", "Product updated successfully!", Alert.AlertType.INFORMATION);
            }
            showListView();
        } catch (SQLException e) {
            showAlert("Error", "Failed to save product: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
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
        String name = nameField.getText();
        if (name == null || name.trim().isEmpty()) {
            errors.append("Name cannot be blank.\n");
        } else if (name.length() < 2) {
            errors.append("Name must be at least 2 characters long.\n");
        } else if (name.length() > 20) {
            errors.append("Name cannot exceed 20 characters.\n");
        }
        String description = descriptionField.getText();
        if (description == null || description.trim().isEmpty()) {
            errors.append("Description cannot be blank.\n");
        } else if (description.length() < 10) {
            errors.append("Description must be at least 10 characters long.\n");
        } else if (description.length() > 30) {
            errors.append("Description cannot exceed 30 characters.\n");
        }
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
        int stock = stockSpinner.getValue();
        if (stock < 1) {
            errors.append("Stock must be at least 1.\n");
        } else if (stock > 50) {
            errors.append("Stock cannot exceed 50.\n");
        }
        if (categoryComboBox.getValue() == null) {
            errors.append("Category must be selected.\n");
        }
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
        String currency = currencyComboBox.getValue();
        if (currency == null || currency.trim().isEmpty()) {
            errors.append("Currency must be selected.\n");
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


    @FXML
    private void onVoirPanier() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplace/CartView.fxml"));
            if (loader.getLocation() == null) {
                showAlert("Error", "FXML file '/marketplace/CartView.fxml' not found.", Alert.AlertType.ERROR);
                return;
            }
            Parent root = loader.load();
            if (bp == null) {
                showAlert("Error", "BorderPane is not initialized.", Alert.AlertType.ERROR);
                return;
            }
            bp.setCenter(root);
        } catch (IOException e) {
            showAlert("Error", "Failed to load cart view: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public void reloadProducts() {
        try {
            refreshProductList();
        } catch (Exception e) {
            System.out.println("Erreur rechargement produits : " + e.getMessage());
        }
    }

}