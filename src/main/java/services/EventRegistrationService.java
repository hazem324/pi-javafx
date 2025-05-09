package services;

import entities.Event;
import entities.EventRegistration;
import entities.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventRegistrationService {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/culturespacedb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Check if the user is already registered for the event
    public boolean isUserRegisteredForEvent(Event event, User user) throws SQLException {
        String sql = "SELECT COUNT(*) FROM event_registration WHERE event_id = ? AND user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, event.getId());
            stmt.setInt(2, user.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    // Update the number of places in the event table
    private void decrementNumberOfPlaces(Event event) throws SQLException {
        String sql = "UPDATE event SET number_of_places = number_of_places - 1 WHERE id = ? AND number_of_places > 0";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, event.getId());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No places left to decrement or event not found.");
            }
            // Update the event object in memory to reflect the change
            event.decrementPlaces();
        }
    }

    // Fetch registrations per event for the bar chart
    public Map<String, Integer> getRegistrationsPerEvent() throws SQLException {
        Map<String, Integer> registrationsPerEvent = new HashMap<>();
        String sql = "SELECT e.title, COUNT(er.id) as registration_count " +
                "FROM event e " +
                "LEFT JOIN event_registration er ON e.id = er.event_id " +
                "GROUP BY e.id, e.title";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String eventTitle = rs.getString("title");
                int registrationCount = rs.getInt("registration_count");
                registrationsPerEvent.put(eventTitle, registrationCount);
            }
        }
        return registrationsPerEvent;
    }

    public EventRegistration registerUserForEvent(Event event, User user) throws SQLException {
        // Check if the user is already registered
        if (isUserRegisteredForEvent(event, user)) {
            throw new SQLException("User is already registered for this event.");
        }

        // Check if there are available places (already checked in EventDetailsController, but adding here for safety)
        if (event.getNumberOfPlaces() <= 0) {
            throw new SQLException("No places left for this event.");
        }

        EventRegistration registration = new EventRegistration();
        registration.setEvent(event);
        registration.setUser(user);
        registration.setRegistrationDate(LocalDateTime.now());

        // Generate a unique ticket number
        String ticketNumber = "TICKET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        registration.setTicketNumber(ticketNumber);

        // Generate QR code path (we'll generate the actual QR code later)
        String qrCodePath = System.getProperty("user.home") + "/esprit-event/qrcodes/" + ticketNumber + ".png";
        registration.setQrCode(qrCodePath);

        // Insert into database
        String sql = "INSERT INTO event_registration (event_id, user_id, registration_date, ticket_number, qr_code) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, event.getId());
            stmt.setInt(2, user.getId());
            stmt.setTimestamp(3, Timestamp.valueOf(registration.getRegistrationDate()));
            stmt.setString(4, registration.getTicketNumber());
            stmt.setString(5, registration.getQrCode());
            stmt.executeUpdate();

            // Get the generated ID
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                registration.setId(rs.getInt(1));
            }

            // After successful registration, decrement the number of places
            decrementNumberOfPlaces(event);
        }

        return registration;
    }
}