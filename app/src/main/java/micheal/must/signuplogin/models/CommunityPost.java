package micheal.must.signuplogin.models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommunityPost {
    private String postId;
    private String userName;
    private String userId;
    private String content;
    private long timestamp;
    private String group;
    private int likeCount;
    private int commentCount;
    private boolean isLiked = false;
    private List<Comment> comments = new ArrayList<>();

    // Empty constructor for Firebase
    public CommunityPost() {}

    public CommunityPost(String postId, String userName, String userId, String content,
                        long timestamp, String group, int likeCount, int commentCount) {
        this.postId = postId;
        this.userName = userName;
        this.userId = userId;
        this.content = content;
        this.timestamp = timestamp;
        this.group = group;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
    }

    // Getters and Setters
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }

    public void incrementCommentCount() {
        this.commentCount++;
    }

    // Actions
    public void toggleLike() {
        if (isLiked) {
            likeCount = Math.max(0, likeCount - 1);
        } else {
            likeCount++;
        }
        isLiked = !isLiked;
    }

    public void addComment(Comment comment) {
        comments.add(comment);
        commentCount = comments.size();
    }

    // Comment inner class
    public static class Comment {
        private String commentId;
        private String userId;
        private String userName;
        private String content;
        private long timestamp;

        public Comment(String commentId, String userId, String userName, String content, long timestamp) {
            this.commentId = commentId;
            this.userId = userId;
            this.userName = userName;
            this.content = content;
            this.timestamp = timestamp;
        }

        public String getCommentId() { return commentId; }
        public String getUserId() { return userId; }
        public String getUserName() { return userName; }
        public String getContent() { return content; }
        public long getTimestamp() { return timestamp; }
    }
}
