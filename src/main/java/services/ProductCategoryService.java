package services;

import models.ProductCategory;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductCategoryService {

    private Connection cnx;

    public ProductCategoryService() {
        cnx = MyDatabase.getInstance().getCnx();
        if (cnx == null) {
            throw new RuntimeException("Database connection is null. Check MyDatabase configuration.");
        }
    }

    public void addCategory(ProductCategory pc) throws SQLException {
        if (pc == null || pc.getName() == null || pc.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty.");
        }

        String sql = "INSERT INTO product_category (name, description) VALUES (?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, pc.getName());
            ps.setString(2, pc.getDescription());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to insert category: no rows affected.");
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    pc.setId(rs.getInt(1));
                } else {
                    throw new SQLException("Failed to retrieve generated category ID.");
                }
            }
            if (!cnx.getAutoCommit()) {
                cnx.commit();
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    public void updateCategory(ProductCategory pc) throws SQLException {
        if (pc == null || pc.getId() <= 0) {
            throw new IllegalArgumentException("Category ID must be a positive integer.");
        }
        if (pc.getName() == null || pc.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty.");
        }

        String sql = "UPDATE product_category SET name = ?, description = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, pc.getName());
            ps.setString(2, pc.getDescription());
            ps.setInt(3, pc.getId());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to update category: no rows affected.");
            }
            if (!cnx.getAutoCommit()) {
                cnx.commit();
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    public void deleteCategory(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Category ID must be a positive integer.");
        }

        String sql = "DELETE FROM product_category WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to delete category: no rows affected.");
            }
            if (!cnx.getAutoCommit()) {
                cnx.commit();
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    public List<ProductCategory> getAllCategories() throws SQLException {
        List<ProductCategory> list = new ArrayList<>();
        String sql = "SELECT * FROM product_category";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new ProductCategory(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            throw e;
        }
        return list;
    }
}