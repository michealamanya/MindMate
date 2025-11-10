package micheal.must.signuplogin.models;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private String groupId;
    private String groupName;
    private String description;
    private String createdBy;
    private long createdAt;
    private List<String> memberIds;
    private String groupImageUrl;
    private int memberCount;

    public Group() {
        this.memberIds = new ArrayList<>();
        this.memberCount = 0;
    }

    public Group(String groupName, String description, String createdBy) {
        this.groupName = groupName;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = System.currentTimeMillis();
        this.memberIds = new ArrayList<>();
        this.memberCount = 1; // Creator is first member
    }

    // Getters and Setters
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public List<String> getMemberIds() { return memberIds; }
    public void setMemberIds(List<String> memberIds) { this.memberIds = memberIds; }

    public String getGroupImageUrl() { return groupImageUrl; }
    public void setGroupImageUrl(String groupImageUrl) { this.groupImageUrl = groupImageUrl; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public boolean isMember(String userId) {
        return memberIds != null && memberIds.contains(userId);
    }

    public void addMember(String userId) {
        if (memberIds == null) memberIds = new ArrayList<>();
        if (!memberIds.contains(userId)) {
            memberIds.add(userId);
            memberCount++;
        }
    }

    public void removeMember(String userId) {
        if (memberIds != null && memberIds.contains(userId)) {
            memberIds.remove(userId);
            memberCount--;
        }
    }
}
