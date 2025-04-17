package test;

import entities.ProductCategory;
import services.ProductCategoryService;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        ProductCategoryService service = new ProductCategoryService();

        // Test adding a category
        ProductCategory newCategory = new ProductCategory("Test Category", "This is a test category.");
        service.addCategory(newCategory);
        System.out.println("Category added.");

        // Test retrieving all categories
        List<ProductCategory> categories = service.getAllCategories();
        System.out.println("All Categories:");
        for (ProductCategory pc : categories) {
            System.out.println(pc);
        }

        // Test updating the first category (if exists)
        if (!categories.isEmpty()) {
            ProductCategory first = categories.get(0);
            first.setName("Updated Name");
            first.setDescription("Updated Description");
            service.updateCategory(first);
            System.out.println("First category updated.");
        }

        // Test deleting the last category (if exists)
        if (!categories.isEmpty()) {
            int lastId = categories.get(categories.size() - 1).getId();
            service.deleteCategory(lastId);
            System.out.println("Last category deleted.");
        }
    }
}
