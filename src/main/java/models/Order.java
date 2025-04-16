package models;

public class Order {
    private int id;
    private int userId;
    private String creationDate;
    private String status;
    private double totalPrice;

    public Order() {
    }

    public Order(int id, int userId, String creationDate, String status, double totalPrice) {
        this.id = id;
        this.userId = userId;
        this.creationDate = creationDate;
        this.status = status;
        this.totalPrice = totalPrice;
    }

    public Order(int id, String creationDate, String status, double totalPrice) {
        this.id = id;
        this.creationDate = creationDate;
        this.status = status;
        this.totalPrice = totalPrice;
    }


    // Getters
    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getStatus() {
        return status;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}
