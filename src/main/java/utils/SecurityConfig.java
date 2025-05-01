
package utils;

public class SecurityConfig {
    private static SecurityConfig instance;
    private boolean email2FAEnabled = true; // Default: enabled
    private boolean totp2FAEnabled = false; // Default: disabled

    private SecurityConfig() {}

    public static SecurityConfig getInstance() {
        if (instance == null) {
            instance = new SecurityConfig();
        }
        return instance;
    }

    // New method to update settings based on user preference (is_verified)
    public void initializeBasedOnUserPreference(boolean isVerified) {
        if (isVerified) {
            // If is_verified = true, user prefers TOTP
            totp2FAEnabled = true;
            email2FAEnabled = false; // Disable email 2FA to prioritize TOTP
        } else {
            // If is_verified = false, user prefers email 2FA
            totp2FAEnabled = false;
            email2FAEnabled = true; // Enable email 2FA as default
        }
    }

    public boolean isEmail2FAEnabled() {
        return email2FAEnabled;
    }

    public void setEmail2FAEnabled(boolean enabled) {
        this.email2FAEnabled = enabled;
    }

    public boolean isTotp2FAEnabled() {
        return totp2FAEnabled;
    }

    public void setTotp2FAEnabled(boolean enabled) {
        this.totp2FAEnabled = enabled;
    }
}