package utils;

import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TotpSecretStorage {
    private static final String FILE_PATH = "totp_secrets.json";
    private static JSONObject secrets;

    static {
        // Initialize the secrets file if it doesn't exist
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            secrets = new JSONObject();
            saveSecrets();
        } else {
            try {
                String content = new String(Files.readAllBytes(Paths.get(FILE_PATH)));
                secrets = content.isEmpty() ? new JSONObject() : new JSONObject(content);
            } catch (IOException e) {
                e.printStackTrace();
                secrets = new JSONObject();
            }
        }
    }

    public static void saveSecret(int userId, String secret) {
        secrets.put(String.valueOf(userId), secret);
        saveSecrets();
    }

    public static String getSecret(int userId) {
        return secrets.optString(String.valueOf(userId), null);
    }

    private static void saveSecrets() {
        try (FileWriter file = new FileWriter(FILE_PATH)) {
            file.write(secrets.toString(4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
