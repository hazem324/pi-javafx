package controllers.marketplace;

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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Cart;
import models.Product;
import models.ProductCategory;
import services.ProductCategoryService;
import services.ProductService;
import test.MainFX;
import utils.CartStorage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class ProductController {

    @FXML
    private BorderPane bp;

    @FXML private GridPane productGrid;
    @FXML private Button addButton;

    @FXML private Label formTitle;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private TextField priceField;
    @FXML private TextField imageUrlField;
    @FXML private Button uploadImageButton;
    @FXML private Spinner<Integer> stockSpinner;
    @FXML private ComboBox<ProductCategory> categoryComboBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final ProductService productService = new ProductService();
    private final ProductCategoryService categoryService = new ProductCategoryService();
    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private final ObservableList<ProductCategory> categoryList = FXCollections.observableArrayList();
    private Product editingProduct;

    @FXML
    public void initialize() {
        categoryList.addAll(categoryService.getAllCategories());
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

        try {
            refreshProductList();
        } catch (SQLException e) {
            showAlert("Error", "Failed to load products: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void refreshProductList() throws SQLException {
        productList.setAll(productService.getAllProducts());
        if (productGrid != null) {
            productGrid.getChildren().clear();
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
        }
    }

    private VBox createProductCard(Product p) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 10);");
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(200);

        // Image
        ImageView imageView = new ImageView();
        if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
            try {
                imageView.setImage(new Image(new File(p.getImageUrl()).toURI().toString()));
            } catch (Exception e) {
                imageView.setImage(new Image("/placeholder.jpg"));
            }
        }
        imageView.setFitWidth(130);
        imageView.setFitHeight(130);
        imageView.setPreserveRatio(true);

        // Labels
        Label nameLabel = new Label(p.getProductName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label descLabel = new Label(p.getProductDescription());
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-font-size: 13px;");

        Label categoryLabel = new Label(p.getProductCategory() != null ? p.getProductCategory().getName() : "No Category");

        Label priceLabel = new Label(String.format("$%.2f", p.getProductPrice()));
        priceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        Label stockLabel = new Label(p.getProductStock() > 0 ? "In Stock" : "Out of Stock");
        stockLabel.setStyle("-fx-text-fill: " + (p.getProductStock() > 0 ? "#28a745" : "#e74c3c") + ";");

        // Quantity controls
        TextField quantityField = new TextField("1");
        quantityField.setPrefWidth(40);
        quantityField.setAlignment(Pos.CENTER);

        Button plusBtn = new Button("+");
        plusBtn.setOnAction(e -> {
            int qty = Integer.parseInt(quantityField.getText());
            if (qty < p.getProductStock()) {
                quantityField.setText(String.valueOf(qty + 1));
            }
        });

        Button minusBtn = new Button("-");
        minusBtn.setOnAction(e -> {
            int qty = Integer.parseInt(quantityField.getText());
            if (qty > 1) {
                quantityField.setText(String.valueOf(qty - 1));
            }
        });

        HBox quantityBox = new HBox(5, minusBtn, quantityField, plusBtn);
        quantityBox.setAlignment(Pos.CENTER);

        // Edit button
        Button editButton = new Button("Edit");
        editButton.setMaxWidth(Double.MAX_VALUE);
        editButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        editButton.setOnAction(e -> showEditForm(p));

        // Add to cart button
        Button addToCartButton = new Button("Ajouter au panier");
        addToCartButton.setMaxWidth(Double.MAX_VALUE);
        addToCartButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        if (p.getProductStock() == 0) {
            addToCartButton.setDisable(true);
            addToCartButton.setStyle("-fx-background-color: grey; -fx-text-fill: white;");
        }
        addToCartButton.setOnAction(evt -> {
            int qty = Integer.parseInt(quantityField.getText());
            boolean produitExiste = false;

            for (Cart c : CartStorage.panier) {
                if (c.getProductId() == p.getId()) {
                    c.setProductQuantity(c.getProductQuantity() + qty);
                    c.setTotal(c.getProductQuantity() * c.getPrice());
                    produitExiste = true;
                    break;
                }
            }

            if (!produitExiste) {
                Cart item = new Cart();
                item.setProductId(p.getId());
                item.setProductQuantity(qty);
                item.setPrice(p.getProductPrice());
                item.setTotal(p.getProductPrice() * qty);
                item.setUserId(1);
                item.setOrderId(0);
                CartStorage.panier.add(item);
            }

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
        });

        // Delete button
        Button deleteButton = new Button("Delete");
        deleteButton.setMaxWidth(Double.MAX_VALUE);
        deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> handleDelete(p));

        // Assemble everything
        card.getChildren().addAll(
                imageView,
                nameLabel,
                descLabel,
                categoryLabel,
                priceLabel,
                stockLabel,
                quantityBox,
                editButton,
                addToCartButton,
                deleteButton
        );

        return card;
    }

    private void ajouterAuPanier(Product p) {
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

    @FXML
    private void showAddForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplace/product_form.fxml"));
            if (loader.getLocation() == null) {
                showAlert("Error", "FXML file '/marketplace/product_form.fxml' not found.", Alert.AlertType.ERROR);
                return;
            }
            Parent root = loader.load();
            ProductController formController = loader.getController();
            formController.setBorderPane(bp);
            if (bp == null) {
                showAlert("Error", "BorderPane is not initialized.", Alert.AlertType.ERROR);
                return;
            }
            bp.setCenter(root);
        } catch (IOException e) {
            showAlert("Error", "Failed to open form: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void showEditForm(Product p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplace/product_form.fxml"));
            if (loader.getLocation() == null) {
                showAlert("Error", "FXML file '/marketplace/product_form.fxml' not found.", Alert.AlertType.ERROR);
                return;
            }
            Parent formRoot = loader.load();
            ProductController formController = loader.getController();
            formController.setBorderPane(bp);
            formController.setEditingProduct(p);
            formController.fillForm(p);
            formController.formTitle.setText("Edit Product");
            formController.saveButton.setText("Update");
            if (bp == null) {
                showAlert("Error", "BorderPane is not initialized.", Alert.AlertType.ERROR);
                return;
            }
            bp.setCenter(formRoot);
        } catch (IOException e) {
            showAlert("Error", "Failed to open form: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void showListView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplace/product_list.fxml"));
            if (loader.getLocation() == null) {
                showAlert("Error", "FXML file '/marketplace/ProductList.fxml' not found.", Alert.AlertType.ERROR);
                return;
            }
            Parent root = loader.load();
            ProductController controller = loader.getController();
            controller.setBorderPane(bp);
            if (bp == null) {
                showAlert("Error", "BorderPane is not initialized.", Alert.AlertType.ERROR);
                return;
            }
            bp.setCenter(root);
        } catch (IOException e) {
            showAlert("Error", "Failed to open product list: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public void setBorderPane(BorderPane bp) {
        this.bp = bp;
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
        if (editingProduct == null) {
            product.setCreatedAt(LocalDateTime.now());
            product.setStatus(true);
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
            e.printStackTrace();
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
        }

        // Price validation
        String priceText = priceField.getText();
        if (priceText == null || priceText.trim().isEmpty()) {
            errors.append("Price cannot be blank.\n");
        } else {
            try {
                double price = Double.parseDouble(priceText);
                if (price < 0.01) {
                    errors.append("Price must be at least $0.01.\n");
                }
            } catch (NumberFormatException e) {
                errors.append("Price must be a valid number.\n");
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
}