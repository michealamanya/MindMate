package micheal.must.signuplogin.services;

import androidx.annotation.NonNull;

import micheal.must.signuplogin.BuildConfig;

/**
 * Minimal HuggingFace/Groq API service wrapper.
 *
 * IMPORTANT: Do NOT hardcode API keys. Provide the key at runtime via
 * BuildConfig, secure storage, or an environment injection mechanism and
 * never commit it to source control.
 */
public class HuggingFaceApiService {

    private final String apiKey;

    public HuggingFaceApiService(@NonNull String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    /**
     * Synchronous placeholder implementation for getting a chat response.
     * Replace this with a real HTTP call to your chosen model endpoint.
     */
    public String getChatResponse(String userMessage, String history) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "API key missing. Please set HF_API_KEY in local.properties or environment.";
        }

        // TODO: implement real network call using OkHttp or other HTTP client.
        // For now, return a simple placeholder response.
        return "(AI) I received: " + userMessage;
    }

    /**
     * Backwards-compatible static helper used by existing code.
     * It constructs the service with the HF_API_KEY exposed via BuildConfig.
     */
    public static String getChatResponse(String userMessage, String history) {
        String key = BuildConfig.HF_API_KEY;
        HuggingFaceApiService svc = new HuggingFaceApiService(key);
        return svc.getChatResponse(userMessage, history);
    }
}
