package services;

import entities.Category;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryService implements Service<Category> {

    private Connection cnx;

    public CategoryService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Category category) throws SQLException {
        String sql = "INSERT INTO category (name, description) VALUES (?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, category.getName());
        ps.setString(2, category.getDescription());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Category category) throws SQLException {
        String sql = "UPDATE category SET name = ?, description = ? WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, category.getName());
        ps.setString(2, category.getDescription());
        ps.setInt(3, category.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(Category category) throws SQLException {
        String sql = "DELETE FROM category WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, category.getId());
        ps.executeUpdate();
    }

    @Override
    public List<Category> recuperer() throws SQLException {
        return getCategories(null);
    }

    // Fetch categories with search
    public List<Category> getCategories(String searchName) throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM category WHERE 1=1";
        List<String> conditions = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        if (searchName != null && !searchName.trim().isEmpty()) {
            conditions.add("name LIKE ?");
            parameters.add("%" + searchName.trim() + "%");
        }

        if (!conditions.isEmpty()) {
            sql += " AND " + String.join(" AND ", conditions);
        }

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            for (int i = 0; i < parameters.size(); i++) {
                ps.setObject(i + 1, parameters.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Category c = new Category();
                    c.setId(rs.getInt("id"));
                    c.setName(rs.getString("name"));
                    c.setDescription(rs.getString("description"));
                    categories.add(c);
                }
            }
        }
        return categories;
    }

    // Check if a category with the given name already exists
    public boolean categoryExists(String name) throws SQLException {
        String sql = "SELECT COUNT(*) FROM category WHERE name = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, name);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }
}