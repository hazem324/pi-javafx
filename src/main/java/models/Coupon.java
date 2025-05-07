package models;

import java.time.LocalDateTime;

public class Coupon {
    private int id;
    private String code;
    private int discountPercentage;
    private boolean isUsed;
    private LocalDateTime expirationDate;

    // Constructeurs
    public Coupon() {}

    public Coupon(int id, String code, int discountPercentage, boolean isUsed, LocalDateTime expirationDate) {
        this.id = id;
        this.code = code;
        this.discountPercentage = discountPercentage;
        this.isUsed = isUsed;
        this.expirationDate = expirationDate;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(int discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public boolean isExpired() {
        return expirationDate.isBefore(LocalDateTime.now());
    }

}