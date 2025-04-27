package services;

import utils.MyDatabase;
import models.ProductCategory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductCategoryService {
    private Connection cnx;

    public ProductCategoryService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    public void addCategory(ProductCategory pc) {
        String sql = "INSERT INTO product_category (name, description) VALUES (?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, pc.getName());
            ps.setString(2, pc.getDescription());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding category: " + e.getMessage());
        }
    }

    public void updateCategory(ProductCategory pc) {
        String sql = "UPDATE product_category SET name = ?, description = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, pc.getName());
            ps.setString(2, pc.getDescription());
            ps.setInt(3, pc.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating category: " + e.getMessage());
        }
    }

    public void deleteCategory(int id) {
        String sql = "DELETE FROM product_category WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting category: " + e.getMessage());
        }
    }

    public List<ProductCategory> getAllCategories() {
        List<ProductCategory> list = new ArrayList<>();
        String sql = "SELECT * FROM product_category";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ProductCategory category = new ProductCategory(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description")
                );
                list.add(category);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving categories: " + e.getMessage());
        }
        return list;
    }
}