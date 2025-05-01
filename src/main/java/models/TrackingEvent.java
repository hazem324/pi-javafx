package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TrackingEvent {
    private int id;
    private int orderId;
    private String status;
    private LocalDateTime createdAt;


    public TrackingEvent(int id, int orderId, String status, LocalDateTime createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.status = status;
        this.createdAt = createdAt;
    }
    public TrackingEvent(String status, LocalDateTime createdAt) {
        this.status = status;
        this.createdAt = createdAt;
    }
    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
