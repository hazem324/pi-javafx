package services;

import entities.User;
import utils.MyDatabase;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserService implements Service<User> {

    private Connection cnx;

    public UserService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(User user) throws SQLException {
        String sql = "INSERT INTO user (last_name, first_name, email, password, is_verified, is_blocked, roles) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, user.getLastName());
        ps.setString(2, user.getFirstName());
        ps.setString(3, user.getEmail());
        ps.setString(4, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        ps.setBoolean(5, false);
        ps.setBoolean(6, false);
        ps.setString(7, "[\"ROLE_USER\"]");
        ps.executeUpdate();
    }

    @Override
    public void modifier(User user) throws SQLException {
        String sql = "update user set last_name = ?, first_name = ?, email = ?, password = ?, is_verified = ?, is_blocked = ?, profile_img = ?, roles = ? where id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, user.getLastName());
        ps.setString(2, user.getFirstName());
        ps.setString(3, user.getEmail());
        ps.setString(4, user.getPassword());
        ps.setBoolean(5, user.isVerified());
        ps.setBoolean(6, user.isBlocked());
        ps.setString(7, user.getProfileIMG());
        ps.setString(8, String.join(",", user.getRoles()));
        ps.setInt(9, user.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(User user) throws SQLException {
        String sql = "delete from user where id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, user.getId());
        ps.executeUpdate();
    }

    @Override
    public List<User> recuperer() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, first_name, last_name, email, roles, is_blocked FROM user";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            int id = rs.getInt("id");
            String firstName = rs.getString("first_name");
            String lastName = rs.getString("last_name");
            String email = rs.getString("email");
            String rolesStr = rs.getString("roles");
            boolean isBlocked = rs.getBoolean("is_blocked");
            List<String> roles = rolesStr != null ? Arrays.asList(rolesStr.replace("[\"", "").replace("\"]", "").split(",")) : new ArrayList<>();

            // Set other fields to defaults since not retrieved
            User user = new User(id, firstName, lastName, email, null, false, isBlocked, null, roles);
            users.add(user);
        }

        return users;
    }

    public User findByEmail(String email) throws SQLException {
        String sql = "select * from user where email = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            int id = rs.getInt("id");
            String lastName = rs.getString("last_name");
            String firstName = rs.getString("first_name");
            String password = rs.getString("password");
            boolean isVerified = rs.getBoolean("is_verified");
            boolean isBlocked = rs.getBoolean("is_blocked");
            String profileIMG = rs.getString("profile_img");
            String rolesStr = rs.getString("roles");
            List<String> roles = rolesStr != null ? Arrays.asList(rolesStr.replace("[\"", "").replace("\"]", "").split(",")) : new ArrayList<>();

            return new User(id, firstName, lastName, email, password, isVerified, isBlocked, profileIMG, roles);
        }

        throw new SQLException("User not found");
    }
}