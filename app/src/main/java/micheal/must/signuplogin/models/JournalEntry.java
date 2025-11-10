package micheal.must.signuplogin.models;

public class JournalEntry {
    private String entryId;
    private String title;
    private String content;
    private long createdAt;
    private boolean isFavorite;
    private boolean isArchived;
    private String userId;

    // Empty constructor for Firebase
    public JournalEntry() {}

    public JournalEntry(String title, String content, long createdAt) {
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.isFavorite = false;
        this.isArchived = false;
    }

    // Getters and Setters
    public String getEntryId() { return entryId; }
    public void setEntryId(String entryId) { this.entryId = entryId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public boolean isArchived() { return isArchived; }
    public void setArchived(boolean archived) { isArchived = archived; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
