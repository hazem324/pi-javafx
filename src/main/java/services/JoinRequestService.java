package services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.sql.Timestamp;


import utils.MyDatabase;

public class JoinRequestService {

    private Connection cnx;

    public JoinRequestService (){
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
}
    
