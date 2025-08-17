package elevenlabsApp.service;

import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class ElevenLabsService {

    private static final String API_URL = "https://api.elevenlabs.io/v1/text-to-speech/{voice_id}";
    private static final String VOICE_ID = "21m00Tcm4TlvDq8ikWAM"; // Rachel
    private final OkHttpClient client = new OkHttpClient();

    public InputStream textToSpeech(String apiKey, String text) throws IOException, ApiException {
        String urlWithParams = API_URL.replace("{voice_id}", VOICE_ID) + "?output_format=pcm_24000";

        JSONObject json = new JSONObject();
        json.put("text", text);
        json.put("model_id", "eleven_multilingual_v2");
        json.put("voice_settings", new JSONObject().put("stability", 0.5).put("similarity_boost", 0.75));

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(urlWithParams)
                .header("xi-api-key", apiKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new ApiException("ElevenLabs API request failed with code: " + response.code() + " and message: " + response.body().string());
            }
            // Read the body into a byte array to allow the response to be closed.
            byte[] responseBytes = response.body().bytes();
            return new java.io.ByteArrayInputStream(responseBytes);
        }
    }

    public static class ApiException extends Exception {
        public ApiException(String message) {
            super(message);
        }
    }
}
