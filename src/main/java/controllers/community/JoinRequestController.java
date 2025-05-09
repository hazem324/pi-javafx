package controllers.community;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import models.JoinRequestDTO;
import services.JoinRequestService;
import utils.EmailSender;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JoinRequestController {

    @FXML
    private TableView<JoinRequestDTO> requestTable;

    @FXML
    private TableColumn<JoinRequestDTO, String> userColumn;

    @FXML
    private TableColumn<JoinRequestDTO, String> communityColumn;

    @FXML
    private TableColumn<JoinRequestDTO, String> joinDateColumn;

    @FXML
    private TableColumn<JoinRequestDTO, String> statusColumn;

    @FXML
    private TableColumn<JoinRequestDTO, Void> actionColumn;

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private Label emptyLabel;

    @FXML
    private ProgressIndicator spinner;

    private JoinRequestService joinRequestService;
    private ObservableList<JoinRequestDTO> requestList;
    private Connection cnx;

    // Email template with named placeholders
    private static final String EMAIL_TEMPLATE = """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <title>CultureSpace - Join Request Update</title>
    </head>
    <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4; text-align: center;">
        <!-- Header -->
        <table role="presentation" width="100%" style="background-color: #4CAF50; padding: 20px;">
            <tr>
                <td align="center">
                    <h3>Join Request Update</h3>
                </td>
            </tr>
        </table>

        <!-- Main Content -->
        <table role="presentation" width="100%" style="max-width: 600px; margin: 40px auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.1);">
            <tr>
                <td align="center">
                    <h2 style="color: #333;">
                        {header}
                    </h2>

                    {message}

                    <p style="color: #777; font-size: 14px; margin-top: 20px;">Thank you for being a part of CultureSpace!</p>
                </td>
            </tr>
        </table>

        <!-- Footer -->
        <table role="presentation" width="100%" style="background-color: #333; padding: 20px; margin-top: 30px;">
            <tr>
                <td align="center" style="color: white; font-size: 14px;">
                    <p>© {year} CultureSpace Dev Team. All rights reserved.</p>
                </td>
            </tr>
        </table>
    </body>
    </html>
    """;

    @FXML
    public void initialize() {
        joinRequestService = new JoinRequestService();
        requestList = FXCollections.observableArrayList();
        cnx = MyDatabase.getInstance().getCnx();

        // Initialize table columns
        userColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUserName()));
        communityColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCommunityName()));
        joinDateColumn.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return new SimpleStringProperty(cellData.getValue().getJoinDate().format(formatter));
        });
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));

        // Set up action column with Accept, Reject, and Remove buttons
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button acceptButton = new Button("Accepter");
            private final Button rejectButton = new Button("Refuser");
            private final Button removeButton = new Button("Retirer");
            private final HBox pendingBox = new HBox(10, acceptButton, rejectButton);
            private final HBox acceptedBox = new HBox(removeButton);

            {
                acceptButton.getStyleClass().add("action-button");
                rejectButton.getStyleClass().addAll("action-button", "reject-button");
                removeButton.getStyleClass().addAll("action-button", "remove-button");

                acceptButton.setOnAction(event -> {
                    JoinRequestDTO request = getTableView().getItems().get(getIndex());
                    handleAcceptRequest(request);
                });
                rejectButton.setOnAction(event -> {
                    JoinRequestDTO request = getTableView().getItems().get(getIndex());
                    handleRejectRequest(request);
                });
                removeButton.setOnAction(event -> {
                    JoinRequestDTO request = getTableView().getItems().get(getIndex());
                    handleRemoveRequest(request);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    JoinRequestDTO request = getTableView().getItems().get(getIndex());
                    if ("pending".equals(request.getStatus())) {
                        setGraphic(pendingBox); // Show Accept and Reject for pending
                    } else if ("accepted".equals(request.getStatus())) {
                        setGraphic(acceptedBox); // Show Remove for accepted
                    } else {
                        setGraphic(null); // No buttons for rejected
                    }
                }
            }
        });

        // Initialize status filter
        statusFilter.setItems(FXCollections.observableArrayList(
                "Tous les statuts", "pending", "rejected", "accepted"
        ));
        statusFilter.setValue("Tous les statuts");
        statusFilter.setOnAction(event -> loadRequests());

        // Load initial data
        loadRequests();
    }

    private void loadRequests() {
        spinner.setVisible(true);
        requestList.clear();

        try {
            String selectedStatus = statusFilter.getValue();
            if ("Tous les statuts".equals(selectedStatus)) {
                requestList.addAll(joinRequestService.getJoinRequests());
            } else {
                requestList.addAll(joinRequestService.getJoinRequestsByStatus(selectedStatus));
            }

            requestTable.setItems(requestList);
            emptyLabel.setVisible(requestList.isEmpty());
            emptyLabel.setManaged(requestList.isEmpty());
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les demandes : " + e.getMessage());
        } finally {
            spinner.setVisible(false);
        }
    }

    // Fetch user email by user ID
    private String getUserEmail(int userId) throws SQLException {
        String sql = "SELECT email FROM user WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }
        }
        throw new SQLException("User email not found for ID: " + userId);
    }

    // Send email notification
    private void sendNotificationEmail(String decision, JoinRequestDTO request) {
        try {
            String userEmail = getUserEmail(request.getUserId());
            String userFirstName = request.getUserName().split(" ")[0]; // Assuming userName is "firstName lastName"
            String communityName = request.getCommunityName();
            String subject = "CultureSpace - Join Request Update";

            // Prepare the header and message based on decision
            String header;
            String message;
            switch (decision) {
                case "accepted":
                    header = "🎉 Congratulations, " + (userFirstName != null ? userFirstName : "User") + "!";
                    message = "<p style=\"color: #555; font-size: 16px;\">Your request to join <strong>" + communityName + "</strong> has been <b style=\"color: #4CAF50;\">accepted</b>! Welcome to the community.</p>" +
                              "<p style=\"color: #555; font-size: 16px;\">Click below to start engaging with your new community:</p>";
                    break;
                case "rejected":
                    header = "❌ Join Request Denied";
                    message = "<p style=\"color: #555; font-size: 16px;\">Unfortunately, your request to join <strong>" + communityName + "</strong> was <b style=\"color: #d9534f;\">rejected</b>.</p>" +
                              "<p style=\"color: #555; font-size: 16px;\">You can still explore other amazing communities on our platform.</p>";
                    break;
                case "removed":
                    header = "⚠️ Community Membership Update";
                    message = "<p style=\"color: #555; font-size: 16px;\">You have been <b style=\"color: #d9534f;\">removed</b> from the community <strong>" + communityName + "</strong>.</p>" +
                              "<p style=\"color: #555; font-size: 16px;\">You can reapply to join this community or explore other communities on our platform.</p>";
                    break;
                default:
                    return; // No email for unknown decision
            }

            // Build the email body using string replacement
            String emailBody = EMAIL_TEMPLATE
                .replace("{header}", header)
                .replace("{message}", message)
                .replace("{year}", String.valueOf(LocalDateTime.now().getYear()));

            // Send the email
            EmailSender.sendAlertEmail(userEmail, subject, emailBody);
        } catch (SQLException e) {
            System.err.println("Failed to send email notification: " + e.getMessage());
            showAlert("Avertissement", "Action réussie, mais échec de l'envoi de l'email : " + e.getMessage());
        }
    }

    private void handleAcceptRequest(JoinRequestDTO request) {
        spinner.setVisible(true);
        try {
            String result = joinRequestService.acceptRequest(
                    request.getId(),
                    request.getUserId(),
                    request.getCommunityId()
            );
            // Send email notification
            sendNotificationEmail("accepted", request);
            showAlert("Succès", result);
            loadRequests();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible d'accepter la demande : " + e.getMessage());
        } finally {
            spinner.setVisible(false);
        }
    }

    private void handleRejectRequest(JoinRequestDTO request) {
        spinner.setVisible(true);
        try {
            String result = joinRequestService.rejectRequest(
                    request.getId(),
                    request.getUserId(),
                    request.getCommunityId()
            );
            // Send email notification
            sendNotificationEmail("rejected", request);
            showAlert("Succès", result);
            loadRequests();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de rejeter la demande : " + e.getMessage());
        } finally {
            spinner.setVisible(false);
        }
    }

    private void handleRemoveRequest(JoinRequestDTO request) {
        spinner.setVisible(true);
        try {
            System.out.println("Removing user: user_id=" + request.getUserId() + ", community_id=" + request.getCommunityId());
            String result = joinRequestService.removeUserFromCommunity(
                    request.getUserId(),
                    request.getCommunityId()
            );
            joinRequestService.updateRequestStatus(request.getId(), "pending");
            // Send email notification
            sendNotificationEmail("removed", request);
            showAlert("Succès", result + " Request status set to pending.");
            loadRequests();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de retirer l'utilisateur de la communauté : " + e.getMessage());
        } finally {
            spinner.setVisible(false);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}