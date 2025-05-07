package controllers.marketplace;

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

    // Constants for styling
    private static final String SUCCESS_STYLE = "-fx-text-fill: green;";
    private static final String ERROR_STYLE = "-fx-text-fill: red;";

    public void setCartController(CartController controller) {
        this.cartController = controller;
    }

    @FXML
    public void applyCoupon() {
        String code = couponField.getText().trim();
        if (code.isEmpty()) {
            messageLabel.setText("❌ Veuillez entrer un code promo.");
            messageLabel.setStyle(ERROR_STYLE);
            cancelBtn.setVisible(false);
            return;
        }

        try {
            CouponService service = new CouponService();
            Coupon coupon = service.getCouponByCode(code);

            if (coupon != null && !coupon.isExpired()) {
                double discount = coupon.getDiscountPercentage();
                cartController.applyDiscount(discount, code);

                messageLabel.setText("✔ Réduction de " + discount + "% appliquée !");
                messageLabel.setStyle(SUCCESS_STYLE);
                reductionActive = true;
                cancelBtn.setVisible(true);
                applyBtn.setDisable(true); // Prevent re-applying the same coupon
            } else {
                messageLabel.setText("❌ Code invalide ou expiré.");
                messageLabel.setStyle(ERROR_STYLE);
                cancelBtn.setVisible(false);
                reductionActive = false;
            }
        } catch (Exception e) {
            messageLabel.setText("❌ Erreur lors de la vérification du code.");
            messageLabel.setStyle(ERROR_STYLE);
            cancelBtn.setVisible(false);
            reductionActive = false;
            e.printStackTrace();
        }
    }

    @FXML
    public void cancelCoupon() {
        // Reset the UI and state
        couponField.clear();
        messageLabel.setText("");
        cartController.cancelDiscount();
        cancelBtn.setVisible(false);
        applyBtn.setDisable(false);
        reductionActive = false;
    }

    @FXML
    public void onTypingCoupon() {
        // Update UI based on coupon field input
        String text = couponField.getText().trim();
        boolean hasText = !text.isEmpty();
        if (!hasText) {
            // Clear message and reset state if the field is empty
            messageLabel.setText("");
            cancelBtn.setVisible(false);
            applyBtn.setDisable(false);
            if (reductionActive) {
                cartController.cancelDiscount();
                reductionActive = false;
            }
        } else if (reductionActive) {
            // Show cancel button if a coupon is applied
            cancelBtn.setVisible(true);
        }
    }
}