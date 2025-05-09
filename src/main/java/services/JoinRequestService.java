package services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import models.JoinRequestDTO;
import java.sql.Timestamp;
import utils.MyDatabase;

public class JoinRequestService {

    private Connection cnx;

    public JoinRequestService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    public String sendJoinRequest(int userId, int commId) throws SQLException {
        // Validate inputs
        if (userId <= 0) {
            return "User not logged in.";
        }

        if (commId <= 0) {
            return "Community not found.";
        }

        // Check for existing pending request
        String checkSql = "SELECT id FROM join_request WHERE user_id = ? AND community_id = ? AND status = ?";
        try (PreparedStatement checkPs = cnx.prepareStatement(checkSql)) {
            checkPs.setInt(1, userId);
            checkPs.setInt(2, commId);
            checkPs.setString(3, "pending");

            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next()) {
                    return "You have already sent a join request.";
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error checking existing join request: " + e.getMessage());
        }

        // Create new join request
        String insertSql = "INSERT INTO join_request (user_id, community_id, join_date, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement insertPs = cnx.prepareStatement(insertSql)) {
            insertPs.setInt(1, userId);
            insertPs.setInt(2, commId);
            insertPs.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            insertPs.setString(4, "pending");

            insertPs.executeUpdate();
            return "Join request sent successfully.";
        } catch (SQLException e) {
            throw new SQLException("Error sending join request: " + e.getMessage());
        }
    }

    // Get all join requests with user full name, community name, join date, and status
    public List<JoinRequestDTO> getJoinRequests() throws SQLException {
        List<JoinRequestDTO> requests = new ArrayList<>();
        String sql = "SELECT jr.id, jr.user_id, jr.community_id, jr.join_date, jr.status, " +
                     "CONCAT(u.first_name, ' ', u.last_name) AS username, c.name AS community_name " +
                     "FROM join_request jr " +
                     "JOIN user u ON jr.user_id = u.id " +
                     "JOIN community c ON jr.community_id = c.id";

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                JoinRequestDTO dto = new JoinRequestDTO();
                dto.setId(rs.getInt("id"));
                dto.setUserId(rs.getInt("user_id"));
                dto.setCommunityId(rs.getInt("community_id"));
                dto.setUserName(rs.getString("username")); // Now contains full name
                dto.setCommunityName(rs.getString("community_name"));
                dto.setJoinDate(rs.getTimestamp("join_date").toLocalDateTime());
                dto.setStatus(rs.getString("status"));
                requests.add(dto);
            }
        } catch (SQLException e) {
            throw new SQLException("Error retrieving join requests: " + e.getMessage());
        }

        return requests;
    }

    // Get join requests filtered by status with user full name
    public List<JoinRequestDTO> getJoinRequestsByStatus(String status) throws SQLException {
        List<JoinRequestDTO> requests = new ArrayList<>();
        String sql = "SELECT jr.id, jr.user_id, jr.community_id, jr.join_date, jr.status, " +
                     "CONCAT(u.first_name, ' ', u.last_name) AS username, c.name AS community_name " +
                     "FROM join_request jr " +
                     "JOIN user u ON jr.user_id = u.id " +
                     "JOIN community c ON jr.community_id = c.id " +
                     "WHERE jr.status = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JoinRequestDTO dto = new JoinRequestDTO();
                    dto.setId(rs.getInt("id"));
                    dto.setUserId(rs.getInt("user_id"));
                    dto.setCommunityId(rs.getInt("community_id"));
                    dto.setUserName(rs.getString("username")); // Now contains full name
                    dto.setCommunityName(rs.getString("community_name"));
                    dto.setJoinDate(rs.getTimestamp("join_date").toLocalDateTime());
                    dto.setStatus(rs.getString("status"));
                    requests.add(dto);
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error retrieving join requests by status: " + e.getMessage());
        }

        return requests;
    }

    // Update request status
    public void updateRequestStatus(int requestId, String newStatus) throws SQLException {
        String updateSql = "UPDATE join_request SET status = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(updateSql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, requestId);
            ps.executeUpdate();
        }
    }
    
    // Get request status 
    public String getRequestStatus(int requestId) throws SQLException {
        String sql = "SELECT status FROM join_request WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("status");
                }
            }
        }
        return null;
    }

    // Accept a join request
    public String acceptRequest(int requestId, int userId, int communityId) throws SQLException {
        // Check current status
        String currentStatus = getRequestStatus(requestId);
        if (currentStatus == null) {
            return "Join request not found.";
        }
        if ("accepted".equals(currentStatus)) {
            return "Request already accepted.";
        }
        if ("rejected".equals(currentStatus)) {
            return "Request already rejected.";
        }
    
        // Update status
        updateRequestStatus(requestId, "accepted");
    
        // Add to community_members
        String insertSql = "INSERT INTO community_members (user_id, community_id, joined_at) VALUES (?, ?, ?)";
        try (PreparedStatement insertPs = cnx.prepareStatement(insertSql)) {
            insertPs.setInt(1, userId);
            insertPs.setInt(2, communityId);
            insertPs.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            insertPs.executeUpdate();
        }
    
        return "Join request accepted successfully.";
    }
    
    // Reject a join request
    public String rejectRequest(int requestId, int userId, int communityId) throws SQLException {
        // Check current status
        String currentStatus = getRequestStatus(requestId);
        if (currentStatus == null) {
            return "Join request not found.";
        }
        if ("rejected".equals(currentStatus)) {
            return "Request already rejected.";
        }
        if ("accepted".equals(currentStatus)) {
            return "Request already accepted.";
        }
    
        // Update status
        updateRequestStatus(requestId, "rejected");
    
        return "Join request rejected successfully.";
    }

    // remove user form community 
    public String removeUserFromCommunity(int userId, int communityId) throws SQLException {
        
        System.out.println("Attempting to remove user: user_id=" + userId + ", community_id=" + communityId);

        String deleteSql = "DELETE FROM community_members WHERE user_id = ? AND community_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(deleteSql)) {
            ps.setInt(1, userId);
            ps.setInt(2, communityId);
            int rowsAffected = ps.executeUpdate();
            
            // Debugging: Log the result
            System.out.println("Rows affected: " + rowsAffected);
            
            if (rowsAffected > 0) {
                return "User removed from community successfully.";
            } else {
                return "User not found in community.";
            }
        } catch (SQLException e) {
            // Debugging: Log the exception
            System.err.println("SQLException: " + e.getMessage());
            throw new SQLException("Error removing user from community: " + e.getMessage());
        }
    }
}