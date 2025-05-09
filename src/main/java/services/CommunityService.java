package services;

import enums.CategoryGrp;
import interfaces.IService;
import models.Community;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommunityService implements IService<Community> {

    private Connection cnx;

    public CommunityService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Community community) throws SQLException {
        String sql = "INSERT INTO community (name, description, banner, creation_date, category) " +
                "VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, community.getName());
        ps.setString(2, community.getDescription());
        ps.setString(3, community.getBanner());

        // Utiliser la date actuelle de l'appareil si creationDate est null
        LocalDateTime now = LocalDateTime.now();
        ps.setTimestamp(4, Timestamp.valueOf(
                community.getCreationDate() != null ? community.getCreationDate() : now
        ));

        ps.setString(5, community.getCategory().name());
        ps.executeUpdate();
    }


    @Override
    public void modifier(Community community) throws SQLException {
        String sql = "UPDATE community SET name = ?, description = ?, banner = ?, " +
                "creation_date = ?, category = ? WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, community.getName());
        ps.setString(2, community.getDescription());
        ps.setString(3, community.getBanner());
        ps.setTimestamp(4, community.getCreationDate() != null ?
                Timestamp.valueOf(community.getCreationDate()) : null);
        ps.setString(5, community.getCategory().name());
        ps.setInt(6, community.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM community WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    // public void supprimer(int id) throws SQLException {
    //     // First delete posts related to this community
    //     String deletePostsSql = "DELETE FROM post WHERE community_id = ?";
    //     PreparedStatement ps1 = cnx.prepareStatement(deletePostsSql);
    //     ps1.setInt(1, id);
    //     ps1.executeUpdate();
    
    //     // Then delete the community
    //     String deleteCommunitySql = "DELETE FROM community WHERE id = ?";
    //     PreparedStatement ps2 = cnx.prepareStatement(deleteCommunitySql);
    //     ps2.setInt(1, id);
    //     ps2.executeUpdate();
    // }

    @Override
    public List<Community> recuperer() {
        List<Community> communities = new ArrayList<>();
        String sql = "SELECT * FROM community";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                String banner = rs.getString("banner");
                Timestamp ts = rs.getTimestamp("creation_date");
                LocalDateTime creationDate = ts != null ? ts.toLocalDateTime() : null;
                String categoryStr = rs.getString("category");
                CategoryGrp category = CategoryGrp.valueOf(categoryStr); // Ensure your DB string matches enum values exactly

                Community community = new Community(name, description, banner, creationDate, category);
                community.setId(id);
                communities.add(community);
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Replace with proper logging if needed
        }

        return communities;
    }

    public Community getCommunityById(int communityId) {
        String sql = "SELECT id, name FROM community WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, communityId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Community community = new Community();
                    community.setId(rs.getInt("id"));
                    community.setName(rs.getString("name"));
                    return community;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<Community> getUserCommunity(int userId) {
        List<Community> communities = new ArrayList<>();
        String sql = "SELECT c.id, c.name, c.description, c.banner, c.creation_date, c.category " +
                     "FROM community c " +
                     "INNER JOIN community_members cm ON c.id = cm.community_id " +
                     "WHERE cm.user_id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String description = rs.getString("description");
                    String banner = rs.getString("banner");
                    Timestamp ts = rs.getTimestamp("creation_date");
                    LocalDateTime creationDate = ts != null ? ts.toLocalDateTime() : null;
                    String categoryStr = rs.getString("category");
                    CategoryGrp category = CategoryGrp.valueOf(categoryStr); // Ensure DB string matches enum

                    Community community = new Community(name, description, banner, creationDate, category);
                    community.setId(id);
                    communities.add(community);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Replace with proper logging if needed
        }

        return communities;
    }

}
