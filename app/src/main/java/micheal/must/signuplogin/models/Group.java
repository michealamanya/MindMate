package micheal.must.signuplogin.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Group {
    private String groupId;
    private String groupName;
    private String description;
    private String createdBy;
    private long createdAt;
    private List<String> members;
    private List<String> memberIds;
    private int memberCount;
    @Exclude
    private Map<String, Object> messages;

    // Default constructor
    public Group() {
        this.members = new ArrayList<>();
    }

    // Constructor with parameters
    public Group(String groupId, String groupName, String description, String createdBy) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = System.currentTimeMillis();
        this.members = new ArrayList<>();
    }

    // Getters
    public String getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public List<String> getMembers() {
        return members != null ? members : new ArrayList<>();
    }

    public List<String> getMemberIds() {
        return memberIds != null ? memberIds : new ArrayList<>();
    }

    public int getMemberCount() {
        return this.members != null ? this.members.size() : memberCount;
    }

    public Map<String, Object> getMessages() {
        return messages;
    }

    // Setters
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public void setMessages(Map<String, Object> messages) {
        this.messages = messages;
    }

    // Helper methods
    public void addMember(String userId) {
        if (this.members == null) {
            this.members = new ArrayList<>();
        }
        if (!this.members.contains(userId)) {
            this.members.add(userId);
        }
    }

    public void removeMember(String userId) {
        if (this.members != null) {
            this.members.remove(userId);
        }
    }

    public boolean isMember(String userId) {
        return this.members != null && this.members.contains(userId);
    }

    @Override
    public String toString() {
        return "Group{" +
                "groupId='" + groupId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", description='" + description + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", memberCount=" + getMemberCount() +
                '}';
    }
}
