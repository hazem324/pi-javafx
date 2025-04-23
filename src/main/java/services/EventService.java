package services;

import entities.Category;
import entities.Event;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventService implements Service<Event> {


    private Connection cnx;

    public EventService() {
        cnx = MyDatabase.getInstance().getCnx();
        if (cnx == null) {
            throw new IllegalStateException("Database connection is null. Ensure MyDatabase is properly configured.");
        }
    }

    @Override
    public void ajouter(Event event) throws SQLException {
        String sql = "INSERT INTO event (title, event_description, event_date, event_end, event_location, status, category_id, number_of_places, image_filename) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, event.getTitle());
        ps.setString(2, event.getEventDescription());
        ps.setTimestamp(3, Timestamp.valueOf(event.getEventDate()));
        if (event.getEndDate() != null) {
            ps.setDate(4, Date.valueOf(event.getEndDate()));
        } else {
            ps.setNull(4, Types.DATE);
        }
        ps.setString(5, event.getEventLocation());
        ps.setString(6, event.getStatus());
        ps.setInt(7, event.getCategory().getId());
        ps.setInt(8, event.getNumberOfPlaces());
        ps.setString(9, event.getImageFilename());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Event event) throws SQLException {
        String sql = "UPDATE event SET title = ?, event_description = ?, event_date = ?, event_end = ?, event_location = ?, status = ?, category_id = ?, number_of_places = ?, image_filename = ? WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, event.getTitle());
        ps.setString(2, event.getEventDescription());
        ps.setTimestamp(3, Timestamp.valueOf(event.getEventDate()));
        if (event.getEndDate() != null) {
            ps.setDate(4, Date.valueOf(event.getEndDate()));
        } else {
            ps.setNull(4, Types.DATE);
        }
        ps.setString(5, event.getEventLocation());
        ps.setString(6, event.getStatus());
        ps.setInt(7, event.getCategory().getId());
        ps.setInt(8, event.getNumberOfPlaces());
        ps.setString(9, event.getImageFilename());
        ps.setInt(10, event.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(Event event) throws SQLException {
        String sql = "DELETE FROM event WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, event.getId());
        ps.executeUpdate();
    }

    @Override
    public List<Event> recuperer() throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT e.*, c.id as cat_id, c.name as cat_name, c.description as cat_description " +
                "FROM event e LEFT JOIN category c ON e.category_id = c.id";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                // Create the Category object
                Category category = new Category();
                category.setId(rs.getInt("cat_id"));
                category.setName(rs.getString("cat_name"));
                category.setDescription(rs.getString("cat_description"));

                // Use the constructor to create the Event object
                Event e = new Event(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("event_description"),
                        rs.getTimestamp("event_date").toLocalDateTime(),
                        rs.getDate("event_end") != null ? rs.getDate("event_end").toLocalDate() : null,
                        rs.getString("event_location"),
                        rs.getString("status"),
                        category,
                        rs.getInt("number_of_places"),
                        rs.getString("image_filename")
                );

                events.add(e);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving events: " + e.getMessage());
            throw e;
        }
        return events;
    }

    // Method to check if an event with the given title already exists
    public boolean eventExists(String title) throws SQLException {
        String sql = "SELECT COUNT(*) FROM event WHERE title = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}