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
        String sqlOrder = "DELETE FROM `order` WHERE id = ?";

        Connection cnx = null;
        PreparedStatement psCart = null;
        PreparedStatement psOrder = null;

        try {
            cnx = MyDatabase.getInstance().getCnx(); // ou ton gestionnaire de connexion
            cnx.setAutoCommit(false); // début de la transaction

            // Supprimer les lignes du panier associées à la commande
            psCart = cnx.prepareStatement(sqlCart);
            psCart.setInt(1, order.getId());
            psCart.executeUpdate();

            // Supprimer la commande elle-même
            psOrder = cnx.prepareStatement(sqlOrder);
            psOrder.setInt(1, order.getId());
            psOrder.executeUpdate();

            cnx.commit(); // valider la transaction
        } catch (SQLException e) {
            if (cnx != null) {
                try {
                    cnx.rollback(); // annuler les changements en cas d'erreur
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e; // relancer l'exception après rollback
        } finally {
            // fermer les ressources
            if (psCart != null) try { psCart.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (psOrder != null) try { psOrder.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (cnx != null) try { cnx.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
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
}
