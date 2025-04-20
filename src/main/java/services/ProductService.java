package services;

import models.Product;
import models.ProductCategory;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductService {

    private Connection cnx;

    public ProductService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    public void addProduct(Product product) throws SQLException {
        String sql = "INSERT INTO product (product_name, product_description, product_price, image_url, status, product_stock, created_at, product_category_id, vote_score) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, product.getProductName());
            ps.setString(2, product.getProductDescription());
            ps.setDouble(3, product.getProductPrice());
            ps.setString(4, product.getImageUrl());
            ps.setBoolean(5, product.isStatus());
            ps.setInt(6, product.getProductStock());
            ps.setTimestamp(7, Timestamp.valueOf(product.getCreatedAt()));
            ps.setInt(8, product.getProductCategory() != null ? product.getProductCategory().getId() : 0);
            ps.setInt(9, 0); // Default vote_score to 0
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to insert product: no rows affected.");
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    product.setId(rs.getInt(1));
                } else {
                    throw new SQLException("Failed to retrieve generated product ID.");
                }
            }
            if (!cnx.getAutoCommit()) {
                cnx.commit();
            }
        } catch (SQLException e) {
            System.err.println("Error inserting product: " + e.getMessage());
            throw e;
        }
    }

    public void updateProduct(Product product) throws SQLException {
        String sql = "UPDATE product SET product_name = ?, product_description = ?, product_price = ?, image_url = ?, status = ?, product_stock = ?, created_at = ?, product_category_id = ?, vote_score = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, product.getProductName());
            ps.setString(2, product.getProductDescription());
            ps.setDouble(3, product.getProductPrice());
            ps.setString(4, product.getImageUrl());
            ps.setBoolean(5, product.isStatus());
            ps.setInt(6, product.getProductStock());
            ps.setTimestamp(7, Timestamp.valueOf(product.getCreatedAt()));
            ps.setInt(8, product.getProductCategory() != null ? product.getProductCategory().getId() : 0);
            ps.setInt(9, 0); // Default vote_score to 0
            ps.setInt(10, product.getId());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to update product: no rows affected.");
            }
            if (!cnx.getAutoCommit()) {
                cnx.commit();
            }
        } catch (SQLException e) {
            System.err.println("Error updating product: " + e.getMessage());
            throw e;
        }
    }

    public void deleteProduct(int id) throws SQLException {
        String sql = "DELETE FROM product WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to delete product: no rows affected.");
            }
            if (!cnx.getAutoCommit()) {
                cnx.commit();
            }
        } catch (SQLException e) {
            System.err.println("Error deleting product: " + e.getMessage());
            throw e;
        }
    }

    public List<Product> getAllProducts() throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.id as category_id, c.name as category_name, c.description as category_description FROM product p LEFT JOIN product_category c ON p.product_category_id = c.id";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setProductName(rs.getString("product_name"));
                product.setProductDescription(rs.getString("product_description"));
                product.setProductPrice(rs.getDouble("product_price"));
                product.setImageUrl(rs.getString("image_url"));
                product.setStatus(rs.getBoolean("status"));
                product.setProductStock(rs.getInt("product_stock"));
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) {
                    product.setCreatedAt(ts.toLocalDateTime());
                } else {
                    product.setCreatedAt(LocalDateTime.now());
                }

                ProductCategory category = new ProductCategory();
                category.setId(rs.getInt("category_id"));
                category.setName(rs.getString("category_name"));
                category.setDescription(rs.getString("category_description"));
                product.setProductCategory(category);
                product.setVoteScore(rs.getInt("vote_score"));
                list.add(product);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving products: " + e.getMessage());
            throw e;
        }
        return list;
    }

    public Product getProductById(int productId) throws SQLException {
        String sql = "SELECT * FROM product WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, productId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return new Product(
                    rs.getInt("id"),
                    rs.getString("product_name"),
                    rs.getString("product_description"),
                    rs.getDouble("product_price"),
                    rs.getString("image_url"),
                    rs.getInt("product_stock")
            );
        }

        return null;
    }
}
