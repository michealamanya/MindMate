package micheal.must.signuplogin.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import micheal.must.signuplogin.R;
import micheal.must.signuplogin.adapters.PostAdapter;
import micheal.must.signuplogin.models.CommunityPost;
import micheal.must.signuplogin.models.Post;

public class AllPostsFragment extends Fragment {

    private static final String TAG = "AllPostsFragment";
    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyView;
    private List<CommunityPost> posts = new ArrayList<>();
    private FloatingActionButton fabCreatePost;
    private DatabaseReference postsRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts_list, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        emptyView = view.findViewById(R.id.empty_view);
        // Find the FAB if it exists in this layout
        fabCreatePost = view.findViewById(R.id.fab_create_post);

        setupRecyclerView();
        setupSwipeRefresh();
        setupFab();
        loadPosts();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Make sure our FAB is visible
        if (fabCreatePost != null) {
            fabCreatePost.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // Hide our FAB when leaving this fragment
        if (fabCreatePost != null) {
            fabCreatePost.setVisibility(View.GONE);
        }
    }

    private void setupFab() {
        if (fabCreatePost != null) {
            fabCreatePost.setOnClickListener(v -> {
                // Show dialog to create a new post
                showCreatePostDialog();
            });
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PostAdapter(posts, getContext());

        // Set up listeners for comments and sharing
        adapter.setOnCommentClickListener(this::showCommentDialog);
        adapter.setOnShareClickListener(this::sharePost);

        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.accent);
        swipeRefreshLayout.setOnRefreshListener(this::refreshPosts);
    }

    private void loadPosts() {
        // In a real app, this would fetch from a database or API
        posts.clear();
        posts.addAll(generateSamplePosts());

        updateUI();
    }

    private void refreshPosts() {
        // Simulate network delay
        swipeRefreshLayout.postDelayed(() -> {
            posts.clear();
            posts.addAll(generateSamplePosts());
            adapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
            updateUI();
        }, 1000);
    }

    private void updateUI() {
        if (posts.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }

    private List<CommunityPost> generateSamplePosts() {
        List<CommunityPost> samplePosts = new ArrayList<>();

        String[] userNames = {"Sarah Johnson", "Mark Lee", "Priya Patel", "James Wilson", "Maria Garcia"};
        String[] groups = {"Anxiety Support", "Meditation Group", "Student Mental Health", "Self-Care Circle", "Mindfulness Practice"};
        String[] contents = {
                "Just finished a 10-minute meditation session and I feel so much better. Has anyone else tried the new guided meditation in the app?",
                "Having a rough day today. Could use some positive encouragement from this amazing community.",
                "I've been practicing gratitude journaling for a month now and it's amazing how much it has changed my perspective on daily challenges.",
                "What are your favorite coping mechanisms when anxiety hits unexpectedly?",
                "Made a huge breakthrough in therapy today. It's a long journey but I'm making progress!",
                "Anyone else struggle with work-life balance? Would love some tips from the community.",
                "Just wanted to share that I've been depression-free for 3 months now. Keep going everyone, it does get better!"
        };

        Random random = new Random();

        // Generate between 5 and 10 posts
        int postCount = random.nextInt(6) + 5;

        for (int i = 0; i < postCount; i++) {
            String userName = userNames[random.nextInt(userNames.length)];
            String group = groups[random.nextInt(groups.length)];
            String content = contents[random.nextInt(contents.length)];
            int likeCount = random.nextInt(50);
            int commentCount = random.nextInt(15);
            long timestamp = System.currentTimeMillis() - random.nextInt(604800000); // Within the last week

            samplePosts.add(new CommunityPost(
                    "post_" + i,
                    userName,
                    "user_" + i,
                    content,
                    timestamp,
                    group,
                    likeCount,
                    commentCount
            ));
        }

        return samplePosts;
    }

    /**
     * Show dialog for adding a comment to a post
     * @param post The post to comment on
     * @param position Position of the post in the list
     */
    private void showCommentDialog(CommunityPost post, int position) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_comment, null);
        EditText commentInput = dialogView.findViewById(R.id.et_comment);

        builder.setView(dialogView)
                .setTitle("Add Comment")
                .setPositiveButton("Post", (dialog, which) -> {
                    String commentText = commentInput.getText().toString().trim();
                    if (!commentText.isEmpty()) {
                        addComment(post, commentText, position);
                    }
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    /**
     * Adds a comment to a post and updates the UI
     * @param post The post to comment on
     * @param commentText The comment text
     * @param position Position of the post in the list
     */
    private void addComment(CommunityPost post, String commentText, int position) {
        // In a real app, this would save to a database
        // For now, we'll just increment the comment count and show a toast
        post.incrementCommentCount();
        adapter.notifyItemChanged(position);

        Toast.makeText(getContext(), "Comment added successfully", Toast.LENGTH_SHORT).show();

        // In a real app, this could open a detailed comments view
        // showCommentsView(post);
    }

    /**
     * Shares a post via Android's share intent
     * @param post The post to share
     */
    private void sharePost(CommunityPost post) {
        if (getContext() == null) return;

        String shareText = String.format("Post from %s in %s:\n\n%s",
                post.getUserName(), post.getGroup(), post.getContent());

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Shared from MindMate");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    private void showCreatePostDialog() {
        // Show dialog for creating a new post
        // Implementation would go here
        Toast.makeText(getContext(), "Create post dialog would show here", Toast.LENGTH_SHORT).show();
    }

    private void loadPostsFromFirebase() {
        postsRef.orderByChild("createdAt")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        posts.clear();

                        // Iterate through all posts in reverse order (newest first)
                        List<CommunityPost> tempList = new ArrayList<>();
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            try {
                                CommunityPost post = postSnapshot.getValue(CommunityPost.class);
                                if (post != null) {
                                    post.setPostId(postSnapshot.getKey());
                                    tempList.add(post);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing post: " + e.getMessage());
                            }
                        }

                        // Reverse to show newest first
                        for (int i = tempList.size() - 1; i >= 0; i--) {
                            posts.add(tempList.get(i));
                        }

                        adapter.notifyDataSetChanged();

                        if (posts.isEmpty()) {
                            Toast.makeText(getContext(), "No posts yet", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error loading posts: " + error.getMessage());
                        Toast.makeText(getContext(), "Failed to load posts", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
