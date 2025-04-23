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
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM category";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Category c = new Category();
            c.setId(rs.getInt("id"));
            c.setName(rs.getString("name"));
            c.setDescription(rs.getString("description"));
            categories.add(c);
        }

        return categories;
    }

    // Nouvelle méthode pour vérifier si une catégorie existe déjà
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