package elevenlabsApp.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

public class SettingsManager {

    private static final String SETTINGS_FILE_NAME = "settings.properties";
    private final Properties properties;
    private final File settingsFile;

    public SettingsManager() {
        try {
            this.settingsFile = new File(new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile(), SETTINGS_FILE_NAME);
            this.properties = new Properties();
            if (settingsFile.exists()) {
                try (FileInputStream in = new FileInputStream(settingsFile)) {
                    properties.load(in);
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Failed to initialize SettingsManager", e);
        }
    }

    public void save() {
        try (FileOutputStream out = new FileOutputStream(settingsFile)) {
            properties.store(out, "ElevenLabs Anons Sistemi Settings");
        } catch (IOException e) {
            e.printStackTrace();
            // In a real app, you might want to show an error to the user
        }
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
}
