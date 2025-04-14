package test;

import entities.User;
import services.UserService;

import java.sql.SQLException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        UserService us = new UserService();
        try {
            // Example: Uncomment to test modifier
            // us.modifier(new User(1, "Foulen", "Ben Foulen", "foulen@example.com", "password123", false, false, null, List.of("ROLE_USER")));
            System.out.println(us.recuperer());
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}