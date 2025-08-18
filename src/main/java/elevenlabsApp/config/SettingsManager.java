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
    private File settingsFile;

    public SettingsManager() {
        this.properties = new Properties();
        try {
            this.settingsFile = new File(new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile(), SETTINGS_FILE_NAME);
            if (settingsFile.exists()) {
                try (FileInputStream in = new FileInputStream(settingsFile)) {
                    properties.load(in);
                }
            }
        } catch (IOException | URISyntaxException | SecurityException e) {
            System.err.println("Warning: Could not read settings file. Using default settings. Error: " + e.getMessage());
            this.settingsFile = null; // Indicate that we cannot save.
        }
    }

    public void save() {
        if (settingsFile == null) {
            System.err.println("Warning: Settings file location is read-only or inaccessible. Settings cannot be saved.");
            // Optionally, show a dialog to the user.
            // JOptionPane.showMessageDialog(null, "Ayarlar dosyası konumu salt okunur veya erişilemez. Ayarlar kaydedilemiyor.", "Kaydetme Hatası", JOptionPane.ERROR_MESSAGE);
            return;
        }
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
