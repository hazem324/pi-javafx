package services;

import entities.Product;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;

public class DynamicPricingService {

    private static final String GEMINI_API_KEY = "AIzaSyBTENlXLi53lN1bBmUs7VZITAm7SUulRmE";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent?key=" + GEMINI_API_KEY;
    private final HttpClient httpClient;
    private static final double DEFAULT_PRICE_ADJUSTMENT = 10.0; // Default adjustment if API fails
    private static final double MINIMUM_DYNAMIC_PRICE = 10.0; // Minimum dynamic price

    public DynamicPricingService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public double calculateDynamicPrice(Product product) throws Exception {
        if (!product.isUseDynamicPricing()) {
            return product.getProductPrice();
        }

        // If dynamic price is already set, return it (no need to recalculate)
        if (product.getDynamicPrice() != 0.0) {
            System.out.println("Using existing dynamic price for product ID " + product.getId() + ": " + product.getDynamicPrice());
            return product.getDynamicPrice();
        }

        double referencePrice = product.getProductPrice();
        int stock = product.getProductStock();
        double discount = product.getDiscount();
        int categoryId = product.getProductCategory() != null ? product.getProductCategory().getId() : 0;

        // Construct a stricter prompt to ensure a numerical response
        String prompt = String.format(
                "Calculate a price adjustment in dollars for a product with the following details: " +
                        "stock=%d, categoryId=%d, discount=%.2f%%, referencePrice=%.2f. " +
                        "Increase the adjustment for lower stock (e.g., stock <= 2: add 10.0, stock <= 5: add 5.0). " +
                        "Increase the adjustment for certain categories (e.g., categoryId=3: add 2.0). " +
                        "Decrease the adjustment if the discount is high (e.g., discount >= 20%%: subtract 3.0). " +
                        "If no adjustments apply, return a default adjustment of 10.0. " +
                        "Return ONLY a single numerical value (e.g., 5.0) with no text, code, or explanation. " +
                        "Do not include any additional characters, words, or symbols (e.g., no '$', 'Output:', or code blocks).",
                stock, categoryId, discount, referencePrice
        );

        System.out.println("Dynamic pricing prompt for product ID " + product.getId() + ": " + prompt);

        // Create the JSON request body
        JSONObject requestBody = new JSONObject();
        JSONObject content = new JSONObject();
        JSONObject part = new JSONObject();
        part.put("text", prompt);
        content.put("parts", new JSONObject[]{part});
        requestBody.put("contents", new JSONObject[]{content});

        // Build the HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        double dynamicPrice;
        try {
            // Send the request and get the response
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();

            if (statusCode == 429 || statusCode != 200) {
                System.err.println("Gemini API request failed with status: " + statusCode + ". Using fallback pricing logic.");
                dynamicPrice = calculateFallbackDynamicPrice(referencePrice, stock, discount, categoryId);
            } else {
                // Parse the response
                JSONObject responseJson = new JSONObject(response.body());
                String generatedText = responseJson
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");

                System.out.println("Gemini API response for product ID " + product.getId() + ": " + generatedText);

                // Extract the numerical value using a regular expression
                Pattern numberPattern = Pattern.compile("-?\\d+(\\.\\d+)?");
                Matcher matcher = numberPattern.matcher(generatedText.trim());
                double priceAdjustment;
                if (matcher.find()) {
                    try {
                        priceAdjustment = Double.parseDouble(matcher.group());
                    } catch (NumberFormatException e) {
                        System.err.println("Failed to parse price adjustment from Gemini response: " + generatedText);
                        priceAdjustment = DEFAULT_PRICE_ADJUSTMENT;
                    }
                } else {
                    System.err.println("No numerical value found in Gemini response: " + generatedText);
                    priceAdjustment = DEFAULT_PRICE_ADJUSTMENT;
                }

                // Calculate the dynamic price
                dynamicPrice = referencePrice + priceAdjustment;

                // If the adjustment is 0.0, use the default adjustment
                if (priceAdjustment == 0.0) {
                    System.out.println("API returned 0.0 adjustment for product ID " + product.getId() + ". Using default adjustment: " + DEFAULT_PRICE_ADJUSTMENT);
                    dynamicPrice = referencePrice + DEFAULT_PRICE_ADJUSTMENT;
                }
            }
        } catch (Exception e) {
            System.err.println("Error during Gemini API request for product ID " + product.getId() + ": " + e.getMessage());
            System.out.println("Using fallback pricing logic.");
            dynamicPrice = calculateFallbackDynamicPrice(referencePrice, stock, discount, categoryId);
        }

        // Ensure the dynamic price is at least the minimum
        if (dynamicPrice < MINIMUM_DYNAMIC_PRICE) {
            System.out.println("Dynamic price " + dynamicPrice + " for product ID " + product.getId() + " is below minimum. Setting to " + MINIMUM_DYNAMIC_PRICE);
            dynamicPrice = MINIMUM_DYNAMIC_PRICE;
        }

        // Ensure the dynamic price is reasonable (if referencePrice > 0)
        if (referencePrice > 0) {
            if (dynamicPrice < referencePrice * 0.5) {
                dynamicPrice = referencePrice * 0.5; // Minimum 50% of reference price
            }
            if (dynamicPrice > referencePrice * 1.5) {
                dynamicPrice = referencePrice * 1.5; // Maximum 150% of reference price
            }
        }

        System.out.println("Calculated dynamic price for product ID " + product.getId() + ": " + dynamicPrice);
        return dynamicPrice;
    }

    // Fallback logic to calculate dynamic price without API
    private double calculateFallbackDynamicPrice(double referencePrice, int stock, double discount, int categoryId) {
        double priceAdjustment = DEFAULT_PRICE_ADJUSTMENT;

        // Increase adjustment for lower stock
        if (stock <= 2) {
            priceAdjustment += 10.0;
        } else if (stock <= 5) {
            priceAdjustment += 5.0;
        }

        // Increase adjustment for certain categories
        if (categoryId == 3) {
            priceAdjustment += 2.0;
        }

        // Decrease adjustment if discount is high
        if (discount >= 20) {
            priceAdjustment -= 3.0;
        }

        double dynamicPrice = referencePrice + priceAdjustment;
        System.out.println("Fallback dynamic price calculated: " + dynamicPrice);
        return dynamicPrice;
    }
}