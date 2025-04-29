package services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import interfaces.IService;
import models.Likes;
import utils.MyDatabase;

public class LikesService implements IService<Likes> {

    private Connection cnx;

    public LikesService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Likes t) throws SQLException {
        // Start a transaction to ensure atomicity
        cnx.setAutoCommit(false);
        try {
            // 1. Insert the like into the likes table
            String insertSql = "INSERT INTO likes (post_id, user_id) VALUES (?, ?)";
            try (PreparedStatement ps = cnx.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, t.getPost());
                ps.setString(2, t.getUserId());
                ps.executeUpdate();

                // Retrieve the generated ID
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        t.setId(generatedKeys.getInt(1));
                    }
                }
            }

            // 2. Increment the likes count in the posts table
            String updateSql = "UPDATE post SET likes = likes + 1 WHERE id = ?";
            try (PreparedStatement ps = cnx.prepareStatement(updateSql)) {
                ps.setInt(1, t.getPost());
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Post with id " + t.getPost() + " not found");
                }
            }

            // Commit the transaction
            cnx.commit();
        } catch (SQLException e) {
            // Rollback the transaction on error
            cnx.rollback();
            throw e;
        } finally {
            cnx.setAutoCommit(true);
        }
    }

    @Override
    public void modifier(Likes t) throws SQLException {
        String sql = "UPDATE likes SET post_id = ?, user_id = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, t.getPost());
            ps.setString(2, t.getUserId());
            ps.setInt(3, t.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        // Start a transaction
        cnx.setAutoCommit(false);
        try {
            // 1. Get the post_id of the like to be deleted
            String selectSql = "SELECT post_id FROM likes WHERE id = ?";
            int postId;
            try (PreparedStatement ps = cnx.prepareStatement(selectSql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        postId = rs.getInt("post_id");
                    } else {
                        throw new SQLException("Like with id " + id + " not found");
                    }
                }
            }

            // 2. Delete the like
            String deleteSql = "DELETE FROM likes WHERE id = ?";
            try (PreparedStatement ps = cnx.prepareStatement(deleteSql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            // 3. Decrement the likes count in the posts table
            String updateSql = "UPDATE post SET likes = likes - 1 WHERE id = ?";
            try (PreparedStatement ps = cnx.prepareStatement(updateSql)) {
                ps.setInt(1, postId);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Post with id " + postId + " not found");
                }
            }

            // Commit the transaction
            cnx.commit();
        } catch (SQLException e) {
            // Rollback the transaction on error
            cnx.rollback();
            throw e;
        } finally {
            cnx.setAutoCommit(true);
        }
    }

    @Override
    public List<Likes> recuperer() throws SQLException {
        List<Likes> likes = new ArrayList<>();
        String sql = "SELECT * FROM likes";
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Likes like = new Likes();
                like.setId(rs.getInt("id"));
                like.setPost(rs.getInt("post_id"));
                like.setUserId(rs.getString("user_id"));
                likes.add(like);
            }
        }
        return likes;
    }

    public boolean hasLiked(int postId, String userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM likes WHERE post_id = ? AND user_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, postId);
            ps.setString(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public int countLikes(int postId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM likes WHERE post_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}