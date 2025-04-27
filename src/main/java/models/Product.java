package models;

import java.time.LocalDateTime;

public class Product {
    private int id;
    private String productName;
    private String productDescription;
    private double productPrice;
    private String imageUrl;
    private boolean status;
    private int productStock;
    private LocalDateTime createdAt;
    private ProductCategory productCategory;
    private double discount;
    private boolean useDynamicPricing;
    private double dynamicPrice;
    private int voteScore;

    // Constructors
    public Product() {
    }


    public Product(int id, String productName, String productDescription, double productPrice,
                   String imageUrl, boolean status, int productStock, LocalDateTime createdAt,
                   ProductCategory productCategory, double discount, boolean useDynamicPricing,
                   double dynamicPrice, int voteScore) {
        this.id = id;
        this.productName = productName;
        this.productDescription = productDescription;
        this.productPrice = productPrice;
        this.imageUrl = imageUrl;
        this.status = status;
        this.productStock = productStock;
        this.createdAt = createdAt;
        this.productCategory = productCategory;
        this.discount = discount;
        this.useDynamicPricing = useDynamicPricing;
        this.dynamicPrice = dynamicPrice;
        this.voteScore = voteScore;
    }

    public Product(int id, String productName, String productDescription, double productPrice, String imageUrl, int productStock) {
        this.id = id;
        this.productName = productName;
        this.productDescription = productDescription;
        this.productPrice = productPrice;
        this.imageUrl = imageUrl;
        this.productStock = productStock;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public int getProductStock() {
        return productStock;
    }

    public void setProductStock(int productStock) {
        this.productStock = productStock;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ProductCategory getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(ProductCategory productCategory) {
        this.productCategory = productCategory;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public boolean isUseDynamicPricing() {
        return useDynamicPricing;
    }

    public void setUseDynamicPricing(boolean useDynamicPricing) {
        this.useDynamicPricing = useDynamicPricing;
    }

    public double getDynamicPrice() {
        return dynamicPrice;
    }

    public void setDynamicPrice(double dynamicPrice) {
        this.dynamicPrice = dynamicPrice;
    }

    public int getVoteScore() {
        return voteScore;
    }

    public void setVoteScore(int voteScore) {
        this.voteScore = voteScore;
    }
}