package services;

import models.Product;

public class DynamicPricingService {

    private static final double MINIMUM_DYNAMIC_PRICE = 10.0; // Retained as a reference, though not enforced here

    public DynamicPricingService() {
        // No need for HttpClient initialization
    }

    public double calculateDynamicPrice(Product product) {
        // Disable dynamic pricing by returning the reference price
        System.out.println("Dynamic pricing disabled. Using reference price for product ID " + product.getId() + ": " + product.getProductPrice());
        return product.getProductPrice();
    }
}