package services;

import models.User;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserService {

    private final Connection connection;

    public UserService() {
        connection = MyDatabase.getInstance().getCnx();
    }

    public User getUserById(int id) {
        User user = null;

        try {
            String sql = "SELECT * FROM user WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setId(rs.getInt("id"));
                user.setFirstName(rs.getString("first_name")); // ⚠️ adapte si c’est "firstname"
                user.setLastName(rs.getString("last_name"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setVerified(rs.getBoolean("is_verified"));
                user.setBlocked(rs.getBoolean("is_blocked"));
                user.setProfileIMG(rs.getString("profile_img"));
                // Les rôles peuvent être ignorés pour le test ou ajoutés plus tard
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }
}
