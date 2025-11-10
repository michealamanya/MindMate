package micheal.must.signuplogin.utils;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminUtils {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Check if a user is an admin
     * @param userId The user ID to check
     * @param callback Callback with boolean result
     */
    public static void isUserAdmin(String userId, AdminCheckCallback callback) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && Boolean.TRUE.equals(documentSnapshot.getBoolean("isAdmin"))) {
                        callback.onResult(true);
                    } else {
                        callback.onResult(false);
                    }
                })
                .addOnFailureListener(e -> callback.onResult(false));
    }

    /**
     * Promote a user to admin status
     * @param userId The user ID to promote
     * @param callback Callback with success/failure result
     */
    public static void promoteToAdmin(String userId, AdminActionCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isAdmin", true);

        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onResult(true, "User promoted to admin"))
                .addOnFailureListener(e -> callback.onResult(false, "Failed to promote user: " + e.getMessage()));
    }

    /**
     * Remove admin status from a user
     * @param userId The user ID to demote
     * @param callback Callback with success/failure result
     */
    public static void revokeAdminStatus(String userId, AdminActionCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isAdmin", false);

        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onResult(true, "Admin status revoked"))
                .addOnFailureListener(e -> callback.onResult(false, "Failed to revoke admin status: " + e.getMessage()));
    }

    /**
     * Delete a post
     * @param postId The post ID to delete
     * @param callback Callback with success/failure result
     */
    public static void deletePost(String postId, AdminActionCallback callback) {
        db.collection("posts").document(postId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onResult(true, "Post deleted successfully"))
                .addOnFailureListener(e -> callback.onResult(false, "Failed to delete post: " + e.getMessage()));
    }

    /**
     * Delete a group
     * @param groupId The group ID to delete
     * @param callback Callback with success/failure result
     */
    public static void deleteGroup(String groupId, AdminActionCallback callback) {
        db.collection("groups").document(groupId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onResult(true, "Group deleted successfully"))
                .addOnFailureListener(e -> callback.onResult(false, "Failed to delete group: " + e.getMessage()));
    }

    // Callbacks
    public interface AdminCheckCallback {
        void onResult(boolean isAdmin);
    }

    public interface AdminActionCallback {
        void onResult(boolean success, String message);
    }
}
