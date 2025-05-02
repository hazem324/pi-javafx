package utils;

public class ResetPasswordUtil {
    private static String email;
    private static String otp;

    public static void setEmail(String email) {
        ResetPasswordUtil.email = email;
    }

    public static String getEmail() {
        return email;
    }

    public static void setOtp(String otp) {
        ResetPasswordUtil.otp = otp;
    }

    public static String getOtp() {
        return otp;
    }

    public static void clear() {
        email = null;
        otp = null;
    }
}
