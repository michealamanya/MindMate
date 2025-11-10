package micheal.must.signuplogin.models;

public class User {
    private String userId;
    private String displayName;
    private String email;
    private String photoUrl;
    private boolean isAdmin;
    private boolean isModerator;
    private long createdAt;
    private long lastLogin;

    // Basic constructor
    public User(String userId, String displayName, String email) {
        this.userId = userId;
        this.displayName = displayName;
        this.email = email;
        this.createdAt = System.currentTimeMillis();
        this.lastLogin = System.currentTimeMillis();
        this.isAdmin = false;
        this.isModerator = false;
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getEmail() { return email; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }
    public boolean isModerator() { return isModerator; }
    public void setModerator(boolean moderator) { isModerator = moderator; }
    public long getCreatedAt() { return createdAt; }
    public long getLastLogin() { return lastLogin; }
    public void updateLastLogin() { this.lastLogin = System.currentTimeMillis(); }
}
