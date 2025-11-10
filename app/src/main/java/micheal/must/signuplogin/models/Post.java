package micheal.must.signuplogin.models;

public class Post {
    private String postId;
    private String content;
    private String group;
    private String authorId;
    private long createdAt;
    private int likes;

    // Empty constructor for Firebase
    public Post() {}

    public Post(String content, String group, String authorId, long createdAt) {
        this.content = content;
        this.group = group;
        this.authorId = authorId;
        this.createdAt = createdAt;
        this.likes = 0;
    }

    // Getters and setters
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }
}
