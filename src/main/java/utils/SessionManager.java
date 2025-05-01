package utils;

import models.User;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    private static SessionManager instance;
    private static User currentUser;
    private final Map<Integer, String> twoFactorTokens = new HashMap<>();
    private final Map<Integer, LocalDateTime> twoFactorTokenExpiries = new HashMap<>();

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public void clearSession() {
        currentUser = null;
        twoFactorTokens.clear();
        twoFactorTokenExpiries.clear();
    }

    public void setTwoFactorToken(int userId, String token, LocalDateTime expiry) {
        twoFactorTokens.put(userId, token);
        twoFactorTokenExpiries.put(userId, expiry);
    }

    public String getTwoFactorToken(int userId) {
        return twoFactorTokens.get(userId);
    }

    public LocalDateTime getTwoFactorTokenExpiry(int userId) {
        return twoFactorTokenExpiries.get(userId);
    }

    public boolean validateTwoFactorToken(int userId, String token) {
        String storedToken = twoFactorTokens.get(userId);
        LocalDateTime expiry = twoFactorTokenExpiries.get(userId);
        if (storedToken == null || expiry == null) {
            return false;
        }
        if (LocalDateTime.now().isAfter(expiry)) {
            twoFactorTokens.remove(userId);
            twoFactorTokenExpiries.remove(userId);
            return false;
        }
        return storedToken.equals(token);
    }

    public void clearTwoFactorToken(int userId) {
        twoFactorTokens.remove(userId);
        twoFactorTokenExpiries.remove(userId);
    }
}