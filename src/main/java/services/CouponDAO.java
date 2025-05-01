package services;

import models.Coupon;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CouponDAO {
    private Connection connection;

    public CouponDAO() {
        connection = MyDatabase.getInstance().getCnx();
    }

    // Ajouter un coupon
    public void addCoupon(Coupon coupon) {
        String query = "INSERT INTO coupon (code, discount_percentage, is_used, expiration_date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, coupon.getCode());
            ps.setInt(2, coupon.getDiscountPercentage());
            ps.setBoolean(3, coupon.isUsed());
            ps.setTimestamp(4, Timestamp.valueOf(coupon.getExpirationDate()));
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur addCoupon : " + e.getMessage());
        }
    }

    // Supprimer un coupon
    public void deleteCoupon(int id) {
        String query = "DELETE FROM coupon WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur deleteCoupon : " + e.getMessage());
        }
    }

    // Récupérer un coupon par ID
    public Coupon getCouponById(int id) {
        String query = "SELECT * FROM coupon WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToCoupon(rs);
            }
        } catch (SQLException e) {
            System.out.println("Erreur getCouponById : " + e.getMessage());
        }
        return null;
    }

    // Récupérer tous les coupons
    public List<Coupon> getAllCoupons() {
        List<Coupon> coupons = new ArrayList<>();
        String query = "SELECT * FROM coupon";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                coupons.add(mapResultSetToCoupon(rs));
            }
        } catch (SQLException e) {
            System.out.println("Erreur getAllCoupons : " + e.getMessage());
        }
        return coupons;
    }

    // Mapper un ResultSet vers un Coupon
    private Coupon mapResultSetToCoupon(ResultSet rs) throws SQLException {
        Coupon coupon = new Coupon();
        coupon.setId(rs.getInt("id"));
        coupon.setCode(rs.getString("code"));
        coupon.setDiscountPercentage(rs.getInt("discount_percentage"));
        coupon.setUsed(rs.getBoolean("is_used"));
        coupon.setExpirationDate(rs.getTimestamp("expiration_date").toLocalDateTime());
        return coupon;
    }
}
