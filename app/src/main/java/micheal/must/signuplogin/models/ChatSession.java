package micheal.must.signuplogin.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChatSession implements Serializable {
    private String sessionId;
    private String title;
    private long createdAt;
    private long lastModifiedAt;
    private List<ChatMessage> messages;

    public ChatSession(String sessionId, String title) {
        this.sessionId = sessionId;
        this.title = title;
        this.createdAt = System.currentTimeMillis();
        this.lastModifiedAt = System.currentTimeMillis();
        this.messages = new ArrayList<>();
    }

    public static class ChatMessage implements Serializable {
        public String id;
        public String text;
        public boolean isUser;
        public long timestamp;

        public ChatMessage(String text, boolean isUser) {
            this.id = System.currentTimeMillis() + "_" + Math.random();
            this.text = text;
            this.isUser = isUser;
            this.timestamp = System.currentTimeMillis();
        }
    }

    // Getters and setters
    public String getSessionId() { return sessionId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public long getCreatedAt() { return createdAt; }
    public long getLastModifiedAt() { return lastModifiedAt; }
    public void setLastModifiedAt(long time) { this.lastModifiedAt = time; }
    public List<ChatMessage> getMessages() { return messages; }
    public void addMessage(ChatMessage msg) { 
        messages.add(msg);
        this.lastModifiedAt = System.currentTimeMillis();
    }
}
