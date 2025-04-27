package services;

import models.Cart;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartService implements ICartService<Cart> {

    private Connection cnx;

    public CartService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Cart cart) throws SQLException {
        String sql = "INSERT INTO cart (user_id, product_id, order_id, price, total, product_quantity) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, cart.getUserId());
        ps.setInt(2, cart.getProductId());
        ps.setInt(3, cart.getOrderId());
        ps.setDouble(4, cart.getPrice());
        ps.setDouble(5, cart.getTotal());
        ps.setInt(6, cart.getProductQuantity());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Cart cart) throws SQLException {
        String sql = "UPDATE cart SET user_id = ?, product_id = ?, order_id = ?, price = ?, total = ?, product_quantity = ? WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, cart.getUserId());
        ps.setInt(2, cart.getProductId());
        ps.setInt(3, cart.getOrderId());
        ps.setDouble(4, cart.getPrice());
        ps.setDouble(5, cart.getTotal());
        ps.setInt(6, cart.getProductQuantity());
        ps.setInt(7, cart.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM cart WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Cart> recuperer() throws SQLException {
        List<Cart> list = new ArrayList<>();
        String sql = "SELECT * FROM cart";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Cart c = new Cart(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getInt("product_id"),
                    rs.getInt("order_id"),
                    rs.getDouble("price"),
                    rs.getDouble("total"),
                    rs.getInt("product_quantity")
            );
            list.add(c);
        }
        return list;
    }
    public List<Cart> getByOrderId(int orderId) throws SQLException {
        List<Cart> list = new ArrayList<>();
        String sql = "SELECT * FROM cart WHERE order_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, orderId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Cart c = new Cart(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getInt("product_id"),
                    rs.getInt("order_id"),
                    rs.getDouble("price"),
                    rs.getDouble("total"),
                    rs.getInt("product_quantity")
            );
            list.add(c);
        }
        return list;
    }

}