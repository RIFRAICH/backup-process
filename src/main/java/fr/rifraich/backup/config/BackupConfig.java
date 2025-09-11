package fr.rifraich.backup.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class BackupConfig {

    private static final String DEFAULT_HOST = "host";
    private static final int DEFAULT_PORT = 0;
    private static final String DEFAULT_USER = "user";
    private static final String DEFAULT_PASSWORD = "password";
    private static final String DEFAULT_REMOTE_DIR = "/path/to/remote/directory";

    public String host;
    public int port;
    public String user;
    public String password;
    public String remoteDirectory;

    public static BackupConfig load(String resourceName) {
        try {
            InputStream in = BackupConfig.class.getClassLoader().getResourceAsStream(resourceName);
            if (in == null) {
                System.out.println("[INFO] Pas de " + resourceName + " trouvé.");
                return null;
            }
            try (InputStreamReader reader = new InputStreamReader(in)) {
                Gson gson = new GsonBuilder().create();
                JsonObject obj = gson.fromJson(reader, JsonObject.class);
                if (obj == null) return null;

                BackupConfig cfg = new BackupConfig();
                cfg.host = getAsStringSafe(obj, "host");
                cfg.port = getAsIntSafe(obj, "port");
                cfg.user = getAsStringSafe(obj, "user");
                cfg.password = getAsStringSafe(obj, "password");
                cfg.remoteDirectory = getAsStringSafe(obj, "remoteDirectory");
                return cfg;
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Lecture de la config échouée: " + e.getMessage());
            return null;
        }
    }

    public boolean isValidNonDefault() {
        if (isBlank(host) || isBlank(user) || isBlank(password) || isBlank(remoteDirectory) || port <= 0) {
            return false;
        }
        return !Objects.equals(host, DEFAULT_HOST)
                && port != DEFAULT_PORT
                && !Objects.equals(user, DEFAULT_USER)
                && !Objects.equals(password, DEFAULT_PASSWORD)
                && !Objects.equals(remoteDirectory, DEFAULT_REMOTE_DIR);
    }

    private static String getAsStringSafe(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : null;
    }

    private static int getAsIntSafe(JsonObject obj, String key) {
        try {
            return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsInt() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
