package elevenlabsApp.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {

    private final Properties properties;

    public Configuration() {
        properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (inputStream == null) {
                throw new RuntimeException("config.properties not found in classpath.");
            }
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Could not load config.properties.", e);
        }
    }

    public String getApiKey() {
        String apiKey = properties.getProperty("ELEVENLABS_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty() || "YOUR_API_KEY_HERE".equals(apiKey)) {
            throw new RuntimeException("API key not found or not set in config.properties.");
        }
        return apiKey;
    }
}
