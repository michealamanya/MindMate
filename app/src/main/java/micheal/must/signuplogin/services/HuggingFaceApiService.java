package micheal.must.signuplogin.services;

import androidx.annotation.NonNull;

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

    // Add methods to call the API using the provided key. Keep keys out of VCS.
}
