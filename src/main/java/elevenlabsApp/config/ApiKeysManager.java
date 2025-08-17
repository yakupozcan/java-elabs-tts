package elevenlabsApp.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Set;

public class ApiKeysManager {

    private static final String PROPERTIES_FILE_NAME = "apikeys.properties";
    private final Properties properties;
    private final File propertiesFile;

    public ApiKeysManager() {
        try {
            this.propertiesFile = getPropertiesFile();
            this.properties = new Properties();
            if (propertiesFile.exists()) {
                try (FileInputStream in = new FileInputStream(propertiesFile)) {
                    properties.load(in);
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Failed to initialize ApiKeysManager", e);
        }
    }

    private File getPropertiesFile() throws URISyntaxException {
        URL location = ApiKeysManager.class.getProtectionDomain().getCodeSource().getLocation();
        // If running from a JAR, this will be the path to the JAR file.
        // If running from an IDE, this will be the path to the classes directory.
        File runningPath = new File(location.toURI()).getParentFile();
        return new File(runningPath, PROPERTIES_FILE_NAME);
    }

    public String getApiKey(String name) {
        return properties.getProperty(name);
    }

    public void addApiKey(String name, String key) throws IOException {
        properties.setProperty(name, key);
        saveProperties();
    }

    public void removeApiKey(String name) throws IOException {
        properties.remove(name);
        saveProperties();
    }

    public Set<String> getApiKeyNames() {
        return properties.stringPropertyNames();
    }

    private void saveProperties() throws IOException {
        try (FileOutputStream out = new FileOutputStream(propertiesFile)) {
            properties.store(out, "ElevenLabs API Keys");
        }
    }
}
