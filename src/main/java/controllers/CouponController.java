package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import models.Coupon;
import services.CouponService;

public class CouponController {

    @FXML private TextField couponField;
    @FXML private Label messageLabel;
    @FXML private Button applyBtn;
    @FXML private Button cancelBtn;

    private CartController cartController;
    private boolean reductionActive = false;

    public void setCartController(CartController controller) {
        this.cartController = controller;
    }

    @FXML
    public void applyCoupon() {
        String code = couponField.getText().trim();
        CouponService service = new CouponService();
        Coupon coupon = service.getCouponByCode(code);

        if (coupon != null && !coupon.isExpired()) {
            double discount = coupon.getDiscountPercentage();
            cartController.applyDiscount(discount, code);

            messageLabel.setText("✔ Réduction de " + discount + "% appliquée !");
            messageLabel.setStyle("-fx-text-fill: green;");
            reductionActive = true;
            cancelBtn.setVisible(true);
        } else {
            messageLabel.setText("❌ Code invalide ou expiré.");
            messageLabel.setStyle("-fx-text-fill: red;");
            cancelBtn.setVisible(false);
            reductionActive = false;
        }
    }
    @FXML
    public void cancelCoupon() {
        couponField.clear();
        messageLabel.setText("");
        cartController.cancelDiscount();
        cancelBtn.setVisible(false);
        reductionActive = false;
    }
    @FXML
    public void onTypingCoupon() {
        boolean hasText = !couponField.getText().trim().isEmpty();
        cancelBtn.setVisible(hasText && reductionActive);
    }

}
