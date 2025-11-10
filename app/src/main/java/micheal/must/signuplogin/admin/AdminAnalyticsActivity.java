package micheal.must.signuplogin.admin;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import micheal.must.signuplogin.R;

public class AdminAnalyticsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvTotalUsers, tvActiveUsers;
    private TextView tvTotalPosts, tvTotalGroups;
    private TextView tvReportsResolved, tvAverageResponse;
    private TextView tvLastUpdated;
    private CardView cardUserActivity, cardContentGrowth;
    private ProgressBar progressBar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_analytics);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI
        initializeUI();

        // Load analytics data
        loadAnalyticsData();
    }

    private void initializeUI() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Analytics");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvTotalUsers = findViewById(R.id.tv_total_users);
        tvActiveUsers = findViewById(R.id.tv_active_users);
        tvTotalPosts = findViewById(R.id.tv_total_posts);
        tvTotalGroups = findViewById(R.id.tv_total_groups);
        tvReportsResolved = findViewById(R.id.tv_reports_resolved);
        tvAverageResponse = findViewById(R.id.tv_average_response);
        tvLastUpdated = findViewById(R.id.tv_last_updated);

        cardUserActivity = findViewById(R.id.card_user_activity);
        cardContentGrowth = findViewById(R.id.card_content_growth);

        progressBar = findViewById(R.id.progress_bar);

        // Set refresh button click listener
        findViewById(R.id.btn_refresh).setOnClickListener(v -> refreshAnalytics());
    }

    private void loadAnalyticsData() {
        progressBar.setVisibility(View.VISIBLE);

        // Get total users
        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            tvTotalUsers.setText(String.valueOf(queryDocumentSnapshots.size()));
        });

        // Get total posts
        db.collection("posts").get().addOnSuccessListener(queryDocumentSnapshots -> {
            tvTotalPosts.setText(String.valueOf(queryDocumentSnapshots.size()));
        });

        // Get total groups
        db.collection("groups").get().addOnSuccessListener(queryDocumentSnapshots -> {
            tvTotalGroups.setText(String.valueOf(queryDocumentSnapshots.size()));
        });

        // Get active users (simplified for demo)
        tvActiveUsers.setText("43");

        // Get reports resolved
        tvReportsResolved.setText("12");

        // Get average response time
        tvAverageResponse.setText("2.4 hours");

        // Update the last updated time
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        tvLastUpdated.setText("Last updated: " + sdf.format(new Date()));

        progressBar.setVisibility(View.GONE);
    }

    private void refreshAnalytics() {
        Toast.makeText(this, "Refreshing analytics data...", Toast.LENGTH_SHORT).show();
        loadAnalyticsData();
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
