package micheal.must.signuplogin.models;

public class Report {
    private String id;
    private String reportedByUserId;
    private String reportedByUserName;
    private String targetId; // postId, groupId, or userId
    private String targetType; // "post", "group", "user"
    private String reason;
    private String status; // "pending", "resolved"
    private long timestamp;

    public Report(String id, String reportedByUserId, String reportedByUserName,
                  String targetId, String targetType, String reason, String status, long timestamp) {
        this.id = id;
        this.reportedByUserId = reportedByUserId;
        this.reportedByUserName = reportedByUserName;
        this.targetId = targetId;
        this.targetType = targetType;
        this.reason = reason;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getReportedByUserId() { return reportedByUserId; }
    public String getReportedByUserName() { return reportedByUserName; }
    public String getTargetId() { return targetId; }
    public String getTargetType() { return targetType; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
    public long getTimestamp() { return timestamp; }

    public void setStatus(String status) { this.status = status; }
}
