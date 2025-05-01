package services;

import models.Coupon;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;

public class CouponService {
    private Connection connection;

    public CouponService() {
        connection = MyDatabase.getInstance().getCnx();
    }

    // Vérifier si un coupon est valide
    public boolean isValid(String code) {
        String query = "SELECT * FROM coupon WHERE code = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                boolean isUsed = rs.getBoolean("is_used");
                Timestamp expiration = rs.getTimestamp("expiration_date");

                if (isUsed) return false;
                if (expiration.toLocalDateTime().isBefore(LocalDateTime.now())) return false;

                return true;
            }
        } catch (SQLException e) {
            System.out.println("Erreur isValid : " + e.getMessage());
        }
        return false;
    }

    // Appliquer la réduction et retourner le prix final
    public Double applyDiscount(String code, double originalPrice) {
        String query = "SELECT discount_percentage FROM coupon WHERE code = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int discount = rs.getInt("discount_percentage");
                double discountAmount = (discount / 100.0) * originalPrice;
                return Math.round((originalPrice - discountAmount) * 100.0) / 100.0;
            }
        } catch (SQLException e) {
            System.out.println("Erreur applyDiscount : " + e.getMessage());
        }
        return null;
    }

    // Marquer un coupon comme utilisé
    public void markAsUsed(String code) {
        String query = "UPDATE coupon SET is_used = true WHERE code = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, code);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur markAsUsed : " + e.getMessage());
        }
    }



    public Coupon getCouponByCode(String code) {
        String query = "SELECT * FROM coupon WHERE code = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                boolean isUsed = rs.getBoolean("is_used");
                Timestamp expiration = rs.getTimestamp("expiration_date");

                // ❌ Si le coupon est utilisé ou expiré → retour null
                if (isUsed || expiration.toLocalDateTime().isBefore(LocalDateTime.now())) {
                    return null;
                }

                // ✅ Sinon on retourne le coupon
                Coupon coupon = new Coupon();
                coupon.setId(rs.getInt("id"));
                coupon.setCode(rs.getString("code"));
                coupon.setDiscountPercentage(rs.getInt("discount_percentage"));
                coupon.setUsed(isUsed);
                coupon.setExpirationDate(expiration.toLocalDateTime());
                return coupon;
            }
        } catch (SQLException e) {
            System.out.println("Erreur getCouponByCode : " + e.getMessage());
        }
        return null;
    }




}
