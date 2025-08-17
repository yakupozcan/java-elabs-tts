package elevenlabsApp;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class ElevenLabsAPI {

    private static final String API_URL = "https://api.elevenlabs.io/v1/text-to-speech/{voice_id}";
    private static final String VOICE_ID = "21m00Tcm4TlvDq8ikWAM"; // Rachel's voice ID as an example
    private final OkHttpClient client = new OkHttpClient();

    public InputStream textToSpeech(String text, String apiKey) throws IOException {
        String url = API_URL.replace("{voice_id}", VOICE_ID);

        JSONObject json = new JSONObject();
        json.put("text", text);
        json.put("model_id", "eleven_multilingual_v2");
        json.put("voice_settings", new JSONObject().put("stability", 0.5).put("similarity_boost", 0.75));

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .header("xi-api-key", apiKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }

        return response.body().byteStream();
    }
}
