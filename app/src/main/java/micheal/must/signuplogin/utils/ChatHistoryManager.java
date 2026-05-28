package micheal.must.signuplogin.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import micheal.must.signuplogin.models.ChatMessage;

public class ChatHistoryManager {
    private static final String PREF_NAME = "chat_history_prefs";
    private static final String KEY_HISTORY = "chat_history";
    private SharedPreferences sharedPreferences;

    public ChatHistoryManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveMessage(ChatMessage message) {
        List<ChatMessage> history = getChatHistory();
        history.add(message);
        saveHistory(history);
    }

    public List<ChatMessage> getChatHistory() {
        List<ChatMessage> messages = new ArrayList<>();
        String json = sharedPreferences.getString(KEY_HISTORY, null);
        if (json == null) {
            return messages;
        }
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String text = obj.getString("message");
                boolean isUser = obj.getBoolean("isUser");
                long timestamp = obj.getLong("timestamp");
                messages.add(new ChatMessage(text, isUser, timestamp));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return messages;
    }

    private void saveHistory(List<ChatMessage> history) {
        JSONArray jsonArray = new JSONArray();
        for (ChatMessage msg : history) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("message", msg.getMessage());
                obj.put("isUser", msg.isUser());
                obj.put("timestamp", msg.getTimestamp());
                jsonArray.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        sharedPreferences.edit().putString(KEY_HISTORY, jsonArray.toString()).apply();
    }
    
    public void clearHistory() {
        sharedPreferences.edit().remove(KEY_HISTORY).apply();
    }
}
