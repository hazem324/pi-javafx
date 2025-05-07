package services;

import models.Order;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderService {

    private Connection cnx;

    public OrderService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    public List<Order> recuperer() throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM `order`";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Order o = new Order(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("creation_date"),
                    rs.getString("status"),
                    rs.getDouble("total_price")
            );
            list.add(o);
        }
        return list;
    }

    public void supprimer(Order order) throws SQLException {
        String sqlCart = "DELETE FROM cart WHERE order_id = ?";
        PreparedStatement psCart = cnx.prepareStatement(sqlCart);
        psCart.setInt(1, order.getId());
        psCart.executeUpdate();

        String sqlOrder = "DELETE FROM `order` WHERE id = ?";
        PreparedStatement psOrder = cnx.prepareStatement(sqlOrder);
        psOrder.setInt(1, order.getId());
        psOrder.executeUpdate();
    }



    public void modifierstatus(int id, String nouveaustatus) throws SQLException {
        String sql = "UPDATE `order` SET status = ? WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, nouveaustatus);
        ps.setInt(2, id);
        ps.executeUpdate();
    }

    public int ajouterEtRetournerId(Order order) throws SQLException {
        String sql = "INSERT INTO `order` (user_id, creation_date, status, total_price) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, order.getUserId());
        ps.setString(2, order.getCreationDate());
        ps.setString(3, order.getStatus());
        ps.setDouble(4, order.getTotalPrice());
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            return rs.getInt(1);
        }
        return -1;
    }

    public void ajouterTracking(int orderId, String status) {
        String req = "INSERT INTO tracking_event (order_id, status) VALUES (?, ?)";
        try (PreparedStatement ps = MyDatabase.getInstance().getCnx().prepareStatement(req)) {
            ps.setInt(1, orderId);
            ps.setString(2, status);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur ajout tracking : " + e.getMessage());
        }
    }

    public void updateOrderStatus(int orderId, String newStatus) {
        String sql = "UPDATE `order` SET status = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}