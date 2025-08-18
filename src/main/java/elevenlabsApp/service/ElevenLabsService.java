package elevenlabsApp.service;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ElevenLabsService {

    private static final String BASE_URL = "https://api.elevenlabs.io/v1";
    private final OkHttpClient client = new OkHttpClient();

    // Nested class for Voice Settings
    public static class VoiceSettings {
        public final double stability;
        public final double similarityBoost;
        public final double style;
        public final boolean useSpeakerBoost;

        public VoiceSettings(double stability, double similarityBoost, double style, boolean useSpeakerBoost) {
            this.stability = stability;
            this.similarityBoost = similarityBoost;
            this.style = style;
            this.useSpeakerBoost = useSpeakerBoost;
        }
    }

    // Nested class for Voice
    public static class Voice {
        public final String voiceId;
        public final String name;

        public Voice(String voiceId, String name) {
            this.voiceId = voiceId;
            this.name = name;
        }

        @Override
        public String toString() {
            return name; // This will be displayed in the JComboBox
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Voice voice = (Voice) o;
            return Objects.equals(voiceId, voice.voiceId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(voiceId);
        }
    }

    public List<Voice> getAvailableVoices(String apiKey) throws IOException, ApiException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/voices")
                .header("xi-api-key", apiKey)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (!response.isSuccessful()) {
                throw new ApiException("Failed to fetch voices: " + response.code() + " " + responseBody);
            }
            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONArray voicesArray = jsonResponse.getJSONArray("voices");
            List<Voice> voices = new ArrayList<>();
            for (int i = 0; i < voicesArray.length(); i++) {
                JSONObject voiceJson = voicesArray.getJSONObject(i);
                voices.add(new Voice(voiceJson.getString("voice_id"), voiceJson.getString("name")));
            }
            return voices;
        }
    }

    public InputStream textToSpeech(String apiKey, String text, String voiceId, VoiceSettings settings) throws IOException, ApiException {
        String url = BASE_URL + "/text-to-speech/" + voiceId + "?output_format=pcm_24000";

        JSONObject voiceSettingsJson = new JSONObject();
        voiceSettingsJson.put("stability", settings.stability);
        voiceSettingsJson.put("similarity_boost", settings.similarityBoost);
        voiceSettingsJson.put("style", settings.style);
        voiceSettingsJson.put("use_speaker_boost", settings.useSpeakerBoost);

        JSONObject json = new JSONObject();
        json.put("text", text);
        json.put("model_id", "eleven_multilingual_v2");
        json.put("voice_settings", voiceSettingsJson);

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .header("xi-api-key", apiKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            byte[] responseBytes = response.body().bytes();
            if (!response.isSuccessful()) {
                throw new ApiException("ElevenLabs API request failed with code: " + response.code() + " and message: " + new String(responseBytes));
            }
            return new java.io.ByteArrayInputStream(responseBytes);
        }
    }

    public static class ApiException extends Exception {
        public ApiException(String message) {
            super(message);
        }
    }
}
