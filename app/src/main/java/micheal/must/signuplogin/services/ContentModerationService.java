package micheal.must.signuplogin.services;

import android.util.Log;

import java.util.Arrays;
import java.util.List;

public class ContentModerationService {
    private static final String TAG = "ContentModerationService";
    
    // List of prohibited words/phrases
    private static final List<String> PROHIBITED_KEYWORDS = Arrays.asList(
            "kill", "murder", "suicide", "hate", "racist", "sexist",
            "violence", "attack", "bomb", "weapon", "harm", "hurt",
            "abuse", "assault", "rape", "illegal", "drug", "cocaine",
            "heroin", "death threat", "curse", "damn", "hell"
    );

    /**
     * Check if message contains inappropriate content
     */
    public static boolean isModerationPassed(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }

        String lowerMessage = message.toLowerCase().trim();

        // Check for prohibited keywords
        for (String keyword : PROHIBITED_KEYWORDS) {
            if (lowerMessage.contains(keyword)) {
                Log.w(TAG, "Content moderation failed: Found prohibited keyword: " + keyword);
                return false;
            }
        }

        // Check for excessive special characters (spam detection)
        int specialCharCount = 0;
        for (char c : message.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c)) {
                specialCharCount++;
            }
        }

        if (specialCharCount > message.length() * 0.3) {
            Log.w(TAG, "Content moderation failed: Too many special characters");
            return false;
        }

        // Check message length (reasonable boundaries)
        if (message.length() < 1 || message.length() > 500) {
            Log.w(TAG, "Content moderation failed: Message length out of bounds");
            return false;
        }

        Log.d(TAG, "Content moderation passed");
        return true;
    }

    /**
     * Get moderation feedback message
     */
    public static String getModerationFailureMessage() {
        return "Your message contains inappropriate content. Please rephrase and try again.";
    }
}
