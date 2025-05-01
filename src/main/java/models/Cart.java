package models;

import services.ProductService;

import java.sql.SQLException;

public class Cart {
    private int id;
    private int userId;
    private int productId;
    private int orderId;
    private double price;
    private double total;
    private int productQuantity;

    public Cart() {}

    public Cart(int id, int userId, int productId, int orderId, double price, double total, int productQuantity) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.orderId = orderId;
        this.price = price;
        this.total = total;
        this.productQuantity = productQuantity;
    }

    // Getters & Setters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getProductId() { return productId; }
    public int getOrderId() { return orderId; }
    public double getPrice() { return price; }
    public double getTotal() { return total; }
    public int getProductQuantity() { return productQuantity; }

    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setProductId(int productId) { this.productId = productId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public void setPrice(double price) { this.price = price; }
    public void setTotal(double total) { this.total = total; }
    public void setProductQuantity(int productQuantity) { this.productQuantity = productQuantity; }

    public String getProductName() throws SQLException {
        return new ProductService().getProductById(this.getProductId()).getProductName();
    }

}
