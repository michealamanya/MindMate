package micheal.must.signuplogin.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import micheal.must.signuplogin.models.ChatSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ChatHistoryManager {
    private static final String TAG = "ChatHistoryManager";
    private static final String PREFS_NAME = "chat_history";
    private static final String SESSIONS_KEY = "sessions";
    private static final String CURRENT_SESSION_KEY = "current_session_id";

    private SharedPreferences prefs;
    private Gson gson = new Gson();

    public ChatHistoryManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Create a new chat session
     */
    public ChatSession createNewSession(String title) {
        String sessionId = UUID.randomUUID().toString();
        ChatSession session = new ChatSession(sessionId, title);
        
        // Generate title from first message if not provided
        if (title == null || title.isEmpty()) {
            session.setTitle("New Chat - " + formatDate(System.currentTimeMillis()));
        }
        
        saveSession(session);
        setCurrentSession(sessionId);
        
        Log.d(TAG, "✓ New session created: " + sessionId);
        return session;
    }

    /**
     * Save/update a session
     */
    public void saveSession(ChatSession session) {
        try {
            List<ChatSession> sessions = getAllSessions();
            
            // Remove if already exists
            sessions.removeIf(s -> s.getSessionId().equals(session.getSessionId()));
            
            // Add updated session
            sessions.add(session);
            
            // Sort by last modified (newest first)
            Collections.sort(sessions, (a, b) -> 
                Long.compare(b.getLastModifiedAt(), a.getLastModifiedAt()));
            
            // Save all sessions
            String json = gson.toJson(sessions);
            prefs.edit().putString(SESSIONS_KEY, json).apply();
            
            Log.d(TAG, "✓ Session saved: " + session.getSessionId());
        } catch (Exception e) {
            Log.e(TAG, "Error saving session: " + e.getMessage());
        }
    }

    /**
     * Get all chat sessions
     */
    public List<ChatSession> getAllSessions() {
        try {
            String json = prefs.getString(SESSIONS_KEY, "[]");
            return gson.fromJson(json, new TypeToken<List<ChatSession>>(){}.getType());
        } catch (Exception e) {
            Log.e(TAG, "Error getting sessions: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get a specific session by ID
     */
    public ChatSession getSession(String sessionId) {
        try {
            List<ChatSession> sessions = getAllSessions();
            for (ChatSession session : sessions) {
                if (session.getSessionId().equals(sessionId)) {
                    return session;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting session: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get current session
     */
    public ChatSession getCurrentSession() {
        String sessionId = prefs.getString(CURRENT_SESSION_KEY, null);
        if (sessionId != null) {
            return getSession(sessionId);
        }
        return null;
    }

    /**
     * Set current session
     */
    public void setCurrentSession(String sessionId) {
        prefs.edit().putString(CURRENT_SESSION_KEY, sessionId).apply();
        Log.d(TAG, "✓ Current session set: " + sessionId);
    }

    /**
     * Delete a session
     */
    public void deleteSession(String sessionId) {
        try {
            List<ChatSession> sessions = getAllSessions();
            sessions.removeIf(s -> s.getSessionId().equals(sessionId));
            
            String json = gson.toJson(sessions);
            prefs.edit().putString(SESSIONS_KEY, json).apply();
            
            // If deleted session was current, clear it
            if (sessionId.equals(prefs.getString(CURRENT_SESSION_KEY, null))) {
                prefs.edit().remove(CURRENT_SESSION_KEY).apply();
            }
            
            Log.d(TAG, "✓ Session deleted: " + sessionId);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting session: " + e.getMessage());
        }
    }

    /**
     * Clear all sessions
     */
    public void clearAllSessions() {
        prefs.edit()
                .remove(SESSIONS_KEY)
                .remove(CURRENT_SESSION_KEY)
                .apply();
        Log.d(TAG, "✓ All sessions cleared");
    }

    /**
     * Format timestamp for display
     */
    private String formatDate(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy");
        return sdf.format(new java.util.Date(timestamp));
    }

    /**
     * Generate title from message
     */
    public String generateTitleFromMessage(String message) {
        if (message.length() > 30) {
            return message.substring(0, 30) + "...";
        }
        return message;
    }
}
