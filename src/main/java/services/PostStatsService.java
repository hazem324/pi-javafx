package services;

import models.statique.CommunityPostStats;
import models.UserPostStats;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostStatsService {

    private Connection cnx;

    public PostStatsService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    // Get total number of communities
    public int getTotalCommunities() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM community";
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    // Get total number of posts
    public int getTotalPosts() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM post";
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    // Get post counts per community
    public List<CommunityPostStats> getCommunityPostStats() throws SQLException {
    List<CommunityPostStats> stats = new ArrayList<>();
    String sql = "SELECT c.name AS community_name, COUNT(p.id) AS post_count " +
                 "FROM community c LEFT JOIN post p ON c.id = p.community_id " +
                 "GROUP BY c.id, c.name";
    
    try (PreparedStatement ps = cnx.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            CommunityPostStats stat = new CommunityPostStats();
            stat.setCommunityName(rs.getString("community_name"));
            stat.setPostCount(rs.getInt("post_count"));
            stats.add(stat);
        }
    } catch (SQLException e) {
        // Log the error and rethrow to be handled by the caller
        System.err.println("Error fetching community post stats: " + e.getMessage());
        throw e;
    }
    return stats;
}

    // Get post counts per user
    public List<UserPostStats> getUserPostStats() throws SQLException {
        List<UserPostStats> stats = new ArrayList<>();
        String sql = "SELECT CONCAT(u.first_name, ' ', u.last_name) AS username, COUNT(p.id) AS post_count " +
                     "FROM user u LEFT JOIN post p ON u.id = p.user_id " +
                     "GROUP BY u.id, u.first_name, u.last_name";
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String fullName = rs.getString("username");
                int postCount = rs.getInt("post_count");
                UserPostStats stat = new UserPostStats(fullName, postCount);
                stats.add(stat);
            }
        }
        return stats;
    }
}