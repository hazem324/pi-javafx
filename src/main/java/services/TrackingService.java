package services;

import models.TrackingEvent;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TrackingService {

    private Connection connection;

    public TrackingService() {
        connection = MyDatabase.getInstance().getCnx();
    }

    public List<String> getStatutsByOrderId(int orderId) {
        List<String> statusList = new ArrayList<>();
        String query = "SELECT status FROM tracking_event WHERE order_id = ? ORDER BY created_at ASC";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                statusList.add(rs.getString("status"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur getStatutsByOrderId: " + e.getMessage());
        }

        return statusList;
    }

    public void ajouterEvenement(int orderId, String status) {
        String query = "INSERT INTO tracking_event (order_id, status) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, orderId);
            ps.setString(2, status);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur ajouterEvenement : " + e.getMessage());
        }
    }

    public List<TrackingEvent> getEventsByOrderId(int orderId) {
        List<TrackingEvent> events = new ArrayList<>();
        String sql = "SELECT * FROM tracking_event WHERE order_id = ? ORDER BY created_at ASC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String status = rs.getString("status");
                LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                events.add(new TrackingEvent(id, orderId, status, createdAt));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return events;
    }

}
