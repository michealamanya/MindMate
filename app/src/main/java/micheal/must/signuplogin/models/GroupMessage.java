package micheal.must.signuplogin.models;

public class GroupMessage {
    private String userId;
    private String message;
    private long timestamp;
    private String userName;

    public GroupMessage() {}

    public GroupMessage(String userId, String message, long timestamp) {
        this.userId = userId;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId != null ? userId : "";
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message != null ? message : "";
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserName() {
        return userName != null ? userName : "Anonymous";
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}