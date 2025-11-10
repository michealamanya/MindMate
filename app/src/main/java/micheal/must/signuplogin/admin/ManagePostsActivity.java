package micheal.must.signuplogin.admin;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import micheal.must.signuplogin.R;
import micheal.must.signuplogin.models.CommunityPost;
import micheal.must.signuplogin.utils.AdminUtils;

public class ManagePostsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private List<CommunityPost> posts = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_posts);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI
        initializeUI();

        // Load posts
        loadPosts();
    }

    private void initializeUI() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Posts");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        progressBar = findViewById(R.id.progress_bar);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // TODO: Create and set AdminPostAdapter

        // Setup refresh listener
        swipeRefreshLayout.setOnRefreshListener(this::refreshPosts);
    }

    private void loadPosts() {
        progressBar.setVisibility(View.VISIBLE);
        posts.clear();

        db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // TODO: Convert document to CommunityPost and add to list
                        }

                        // TODO: Update adapter
                        Toast.makeText(this, "Posts loaded: " + posts.size(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to load posts", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void refreshPosts() {
        loadPosts();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void deletePost(String postId, int position) {
        AdminUtils.deletePost(postId, (success, message) -> {
            runOnUiThread(() -> {
                Toast.makeText(ManagePostsActivity.this, message, Toast.LENGTH_SHORT).show();
                if (success) {
                    posts.remove(position);
                    // TODO: Update adapter
                }
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
