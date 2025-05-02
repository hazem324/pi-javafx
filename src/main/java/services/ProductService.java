package services;

import entities.Product;
import entities.ProductCategory;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductService {

    private Connection cnx;
    private DynamicPricingService dynamicPricingService;

    public ProductService() {
        cnx = MyDatabase.getInstance().getCnx();
        if (cnx == null) {
            throw new RuntimeException("Database connection is null. Check MyDatabase configuration.");
        }
        dynamicPricingService = new DynamicPricingService();
    }

    public void addProduct(Product product) throws SQLException {
        try {
            // Calculate dynamic price if enabled
            double dynamicPrice = dynamicPricingService.calculateDynamicPrice(product);
            product.setDynamicPrice(dynamicPrice);
        } catch (Exception e) {
            System.err.println("Failed to calculate dynamic price: " + e.getMessage());
            product.setDynamicPrice(product.getProductPrice()); // Fallback to base price
        }

        String sql = "INSERT INTO product (product_name, product_description, product_price, currency, image_url, status, product_stock, created_at, product_category_id, vote_score, discount, use_dynamic_pricing, dynamic_price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, product.getProductName());
            ps.setString(2, product.getProductDescription());
            ps.setDouble(3, product.getProductPrice());
            ps.setString(4, product.getCurrency());
            ps.setString(5, product.getImageUrl());
            ps.setBoolean(6, product.isStatus());
            ps.setInt(7, product.getProductStock());
            ps.setTimestamp(8, Timestamp.valueOf(product.getCreatedAt()));
            ps.setInt(9, product.getProductCategory() != null ? product.getProductCategory().getId() : 0);
            ps.setInt(10, 0); // Default vote_score to 0 for new products
            ps.setDouble(11, product.getDiscount());
            ps.setBoolean(12, product.isUseDynamicPricing());
            ps.setDouble(13, product.getDynamicPrice());
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
        try {
            // Calculate dynamic price if enabled
            double dynamicPrice = dynamicPricingService.calculateDynamicPrice(product);
            product.setDynamicPrice(dynamicPrice);
        } catch (Exception e) {
            System.err.println("Failed to calculate dynamic price: " + e.getMessage());
            product.setDynamicPrice(product.getProductPrice()); // Fallback to base price
        }

        String sql = "UPDATE product SET product_name = ?, product_description = ?, product_price = ?, currency = ?, image_url = ?, status = ?, product_stock = ?, created_at = ?, product_category_id = ?, vote_score = ?, discount = ?, use_dynamic_pricing = ?, dynamic_price = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, product.getProductName());
            ps.setString(2, product.getProductDescription());
            ps.setDouble(3, product.getProductPrice());
            ps.setString(4, product.getCurrency());
            ps.setString(5, product.getImageUrl());
            ps.setBoolean(6, product.isStatus());
            ps.setInt(7, product.getProductStock());
            ps.setTimestamp(8, Timestamp.valueOf(product.getCreatedAt()));
            ps.setInt(9, product.getProductCategory() != null ? product.getProductCategory().getId() : 0);
            ps.setInt(10, product.getVoteScore()); // Use the actual voteScore
            ps.setDouble(11, product.getDiscount());
            ps.setBoolean(12, product.isUseDynamicPricing());
            ps.setDouble(13, product.getDynamicPrice());
            ps.setInt(14, product.getId());
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
        return searchProducts(null, null, null, null, null, null, null, null, "vote_score", "DESC");
    }

    public List<Product> searchProducts(
            String name,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Integer categoryId,
            Double priceMin,
            Double priceMax,
            String availability,
            String discountFilter,
            String sortField,
            String sortDirection
    ) throws SQLException {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, c.id as category_id, c.name as category_name, c.description as category_description " +
                        "FROM product p LEFT JOIN product_category c ON p.product_category_id = c.id WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        if (name != null && !name.trim().isEmpty()) {
            sql.append(" AND p.product_name LIKE ?");
            params.add("%" + name + "%");
        }

        if (dateFrom != null) {
            sql.append(" AND p.created_at >= ?");
            params.add(Timestamp.valueOf(dateFrom));
        }

        if (dateTo != null) {
            sql.append(" AND p.created_at <= ?");
            params.add(Timestamp.valueOf(dateTo));
        }

        if (categoryId != null) {
            sql.append(" AND p.product_category_id = ?");
            params.add(categoryId);
        }

        if (priceMin != null) {
            sql.append(" AND p.product_price >= ?");
            params.add(priceMin);
        }

        if (priceMax != null) {
            sql.append(" AND p.product_price <= ?");
            params.add(priceMax);
        }

        if (availability != null && !availability.equals("all")) {
            if (availability.equals("available")) {
                sql.append(" AND p.product_stock > 0");
            } else if (availability.equals("unavailable")) {
                sql.append(" AND p.product_stock <= 0");
            }
        }

        if (discountFilter != null && !discountFilter.equals("all")) {
            if (discountFilter.equals("discounted")) {
                sql.append(" AND p.discount > 0");
            } else if (discountFilter.equals("non_discounted")) {
                sql.append(" AND (p.discount IS NULL OR p.discount = 0)");
            }
        }

        if (sortField != null && !sortField.isEmpty()) {
            // Map Java field name to database column name for voteScore
            String dbSortField = sortField.equals("voteScore") ? "vote_score" : sortField;
            sql.append(" ORDER BY p.").append(dbSortField).append(" ").append(sortDirection != null ? sortDirection : "DESC");
        } else {
            sql.append(" ORDER BY p.vote_score DESC");
        }

        try (PreparedStatement ps = cnx.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product();
                    product.setId(rs.getInt("id"));
                    product.setProductName(rs.getString("product_name"));
                    product.setProductDescription(rs.getString("product_description"));
                    product.setProductPrice(rs.getDouble("product_price"));
                    product.setCurrency(rs.getString("currency"));
                    product.setImageUrl(rs.getString("image_url"));
                    product.setStatus(rs.getBoolean("status"));
                    product.setProductStock(rs.getInt("product_stock"));
                    product.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    product.setDiscount(rs.getDouble("discount"));
                    product.setUseDynamicPricing(rs.getBoolean("use_dynamic_pricing"));
                    product.setDynamicPrice(rs.getDouble("dynamic_price"));
                    ProductCategory category = new ProductCategory();
                    category.setId(rs.getInt("category_id"));
                    category.setName(rs.getString("category_name"));
                    category.setDescription(rs.getString("category_description"));
                    product.setProductCategory(category);
                    product.setVoteScore(rs.getInt("vote_score"));
                    list.add(product);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving products: " + e.getMessage());
            throw e;
        }
        return list;
    }

    // New method to increment vote score
    public void incrementVoteScore(int productId) throws SQLException {
        String sql = "UPDATE product SET vote_score = vote_score + 1 WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, productId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to increment vote score: no rows affected.");
            }
            if (!cnx.getAutoCommit()) {
                cnx.commit();
            }
        } catch (SQLException e) {
            System.err.println("Error incrementing vote score: " + e.getMessage());
            throw e;
        }
    }

    // New method to decrement vote score
    public void decrementVoteScore(int productId) throws SQLException {
        String sql = "UPDATE product SET vote_score = vote_score - 1 WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, productId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to decrement vote score: no rows affected.");
            }
            if (!cnx.getAutoCommit()) {
                cnx.commit();
            }
        } catch (SQLException e) {
            System.err.println("Error decrementing vote score: " + e.getMessage());
            throw e;
        }
    }
}