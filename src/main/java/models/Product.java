package models;

public class Product {
    private int id;
    private String productName;
    private String productDescription;
    private double productPrice;
    private String imageUrl;
    private int productStock;

    public Product() {}

    public Product(int id, String productName, String productDescription, double productPrice, String imageUrl, int productStock) {
        this.id = id;
        this.productName = productName;
        this.productDescription = productDescription;
        this.productPrice = productPrice;
        this.imageUrl = imageUrl;
        this.productStock = productStock;
    }

    // Getters & Setters
    public int getId() { return id; }
    public String getProductName() { return productName; }
    public String getProductDescription() { return productDescription; }
    public double getProductPrice() { return productPrice; }
    public String getImageUrl() { return imageUrl; }
    public int getProductStock() { return productStock; }

    public void setId(int id) { this.id = id; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }
    public void setProductPrice(double productPrice) { this.productPrice = productPrice; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setProductStock(int productStock) { this.productStock = productStock; }
}
