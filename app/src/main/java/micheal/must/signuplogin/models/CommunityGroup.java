package micheal.must.signuplogin.models;

public class CommunityGroup {
    private String id;
    private String name;
    private String description;
    private int memberCount;
    private boolean isJoined = false;

    public CommunityGroup(String id, String name, String description, int memberCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.memberCount = memberCount;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getMemberCount() { return memberCount; }
    public boolean isJoined() { return isJoined; }

    // Toggle joining a group
    public void toggleJoin() {
        isJoined = !isJoined;
        if (isJoined) {
            memberCount++;
        } else {
            memberCount = Math.max(0, memberCount - 1);
        }
    }
}
