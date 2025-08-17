package elevenlabsApp;

import com.google.gson.Gson;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

public class ElevenLabsApiService {

    private static final String API_URL = "https://api.elevenlabs.io/v1/text-to-speech/21m00Tcm4TlvDq8ikWAM"; // Voice ID for "Rachel"
    private String apiKey;

    public ElevenLabsApiService() {
        try {
            this.apiKey = loadApiKey();
        } catch (IOException e) {
            System.err.println("Error loading API configuration: " + e.getMessage());
            System.err.println("Please ensure a 'config.properties' file exists in the project root directory and contains the line: ELEVENLABS_API_KEY=your_actual_key");
            this.apiKey = null;
        }
    }

    private String loadApiKey() throws IOException {
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream("config.properties");
            prop.load(input);
            String key = prop.getProperty("ELEVENLABS_API_KEY");
            if (key == null || key.trim().isEmpty() || key.equals("your_api_key_here")) {
                throw new IOException("API key not found or not set in config.properties. Please create a config.properties file and add your ElevenLabs API key.");
            }
            return key;
        } catch (java.io.FileNotFoundException e) {
            throw new IOException("config.properties file not found. Please create it in the root directory of the project.", e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void sendTextToSpeechRequest(String text) {
        if (apiKey == null) {
            System.err.println("API key is not available. Cannot make the request.");
            return;
        }

        try {
            Gson gson = new Gson();
            Map<String, Object> payload = Map.of(
                    "text", text,
                    "model_id", "eleven_multilingual_v2",
                    "voice_settings", Map.of(
                            "stability", 0.5,
                            "similarity_boost", 0.75
                    )
            );
            String jsonPayload = gson.toJson(payload);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("xi-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 200) {
                Path outputPath = Paths.get("output.mp3");
                try (InputStream body = response.body()) {
                    Files.copy(body, outputPath);
                    System.out.println("Successfully received audio data and saved to " + outputPath.toAbsolutePath());
                }
            } else {
                // Handle non-200 responses
                String responseBody = new String(response.body().readAllBytes());
                System.err.println("API request failed with status code: " + response.statusCode());
                System.err.println("Response body: " + responseBody);
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("An error occurred during the API request: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
