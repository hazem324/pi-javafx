package controllers.community;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import models.JoinRequestDTO;
import services.JoinRequestService;

import java.sql.SQLException;
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

    @FXML
    public void initialize() {
        joinRequestService = new JoinRequestService();
        requestList = FXCollections.observableArrayList();

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

    private void handleAcceptRequest(JoinRequestDTO request) {
        spinner.setVisible(true);
        try {
            String result = joinRequestService.acceptRequest(
                    request.getId(),
                    request.getUserId(),
                    request.getCommunityId()
            );
            showAlert("Succès", result);
            loadRequests(); // Refresh table
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
            showAlert("Succès", result);
            loadRequests(); // Refresh table
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