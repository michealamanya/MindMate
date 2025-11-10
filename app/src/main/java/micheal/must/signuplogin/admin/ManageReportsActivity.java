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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import micheal.must.signuplogin.R;
import micheal.must.signuplogin.models.Report;

public class ManageReportsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private List<Report> reports = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_reports);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI
        initializeUI();

        // Load reports
        loadReports();
    }

    private void initializeUI() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Reports");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        progressBar = findViewById(R.id.progress_bar);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // TODO: Create and set AdminReportAdapter

        // Setup refresh listener
        swipeRefreshLayout.setOnRefreshListener(this::refreshReports);
    }

    private void loadReports() {
        progressBar.setVisibility(View.VISIBLE);
        reports.clear();

        db.collection("reports")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .whereEqualTo("status", "pending")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // TODO: Convert document to Report and add to list
                        }

                        // TODO: Update adapter
                        Toast.makeText(this, "Reports loaded: " + reports.size(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to load reports", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void refreshReports() {
        loadReports();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void resolveReport(String reportId, int position) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "resolved");

        db.collection("reports")
                .document(reportId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Report marked as resolved", Toast.LENGTH_SHORT).show();
                    reports.remove(position);
                    // TODO: Update adapter
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to resolve report: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
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
