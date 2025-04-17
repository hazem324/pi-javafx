package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import services.UserService; // If you need user data

import java.sql.SQLException;

public class HomePageController {

    @FXML
    private Label homeWelcomeLabel;

    @FXML
    private Label homeTotalUsersLabel;

    @FXML
    private Label homeActiveUsersLabel;

    private UserService userService = new UserService();

    @FXML
    public void initialize() {
        loadHomePageData();
    }

    private void loadHomePageData() {
        try {
            long totalUsers = userService.recuperer().size();
            long activeUsers = userService.recuperer().stream().filter(user -> !user.isBlocked()).count();

            homeTotalUsersLabel.setText("Total Users: " + totalUsers);
            homeActiveUsersLabel.setText("Active Users: " + activeUsers);
            homeWelcomeLabel.setText("Welcome back, Admin!"); // Example
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle error
        }
    }
}