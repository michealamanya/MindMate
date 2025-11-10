package micheal.must.signuplogin.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.widget.SearchView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import micheal.must.signuplogin.R;
import micheal.must.signuplogin.adapters.AdminUserAdapter;
import micheal.must.signuplogin.models.User;
import micheal.must.signuplogin.utils.AdminUtils;

public class AdminDashboardActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CardView cardManageUsers, cardManagePosts, cardManageGroups, cardManageReports;
    private CardView cardAnalytics, cardSettings;
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private ImageView ivLogout;

    // Realtime DB reference for regular users
    private DatabaseReference rtDbRef;

    // New fields
    private RecyclerView rvUsers;
    private AdminUserAdapter userAdapter;
    private final List<User> allUsers = new ArrayList<>();
    private SwipeRefreshLayout swipeUsers;
    private SearchView searchUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Realtime Database reference (regular users stored here)
        rtDbRef = FirebaseDatabase.getInstance().getReference("users");

        // Check if user is admin
        checkAdminStatus();

        // Initialize UI
        initializeUI();

        // Set up click listeners
        setupClickListeners();

        // New: set up logout and admin features
        setupLogout();
        setupUsersSection();
        loadStats();
        loadUsers();
    }

    private void checkAdminStatus() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            // Not logged in, redirect to login
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check Firestore for admin by email OR UID
        db.collection("users")
                .whereEqualTo("email", currentUser.getEmail())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean isAdmin = false;
                    if (!querySnapshot.isEmpty()) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            if ("admin".equals(doc.getString("role"))) {
                                isAdmin = true;
                                break;
                            }
                        }
                    }
                    if (isAdmin) {
                        // User is admin, allow access
                        // ...existing code...
                    } else {
                        Toast.makeText(this, "Admin access required", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking admin status", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void initializeUI() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Dashboard");
        }

        cardManageUsers = findViewById(R.id.card_manage_users);
        cardManagePosts = findViewById(R.id.card_manage_posts);
        cardManageGroups = findViewById(R.id.card_manage_groups);
        cardManageReports = findViewById(R.id.card_manage_reports);
        cardAnalytics = findViewById(R.id.card_analytics);
        cardSettings = findViewById(R.id.card_settings);

        // New view bindings
        ivLogout = findViewById(R.id.iv_logout);
        rvUsers = findViewById(R.id.rv_admin_users);
        swipeUsers = findViewById(R.id.swipe_users);
        searchUsers = findViewById(R.id.search_users);
    }

    private void setupClickListeners() {
        if (cardManageUsers != null) {
            cardManageUsers.setOnClickListener(v -> startActivity(new Intent(this, ManageUsersActivity.class)));
        }
        if (cardManagePosts != null) {
            cardManagePosts.setOnClickListener(v -> startActivity(new Intent(this, ManagePostsActivity.class)));
        }
        if (cardManageGroups != null) {
            cardManageGroups.setOnClickListener(v -> startActivity(new Intent(this, ManageGroupsActivity.class)));
        }
        if (cardManageReports != null) {
            cardManageReports.setOnClickListener(v -> startActivity(new Intent(this, ManageReportsActivity.class)));
        }
        if (cardAnalytics != null) {
            cardAnalytics.setOnClickListener(v -> startActivity(new Intent(this, AdminAnalyticsActivity.class)));
        }
        if (cardSettings != null) {
            cardSettings.setOnClickListener(v -> startActivity(new Intent(this, AdminSettingsActivity.class)));
        }
    }

    // New: logout handling
    private void setupLogout() {
        if (ivLogout != null) {
            ivLogout.setOnClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Logout", (dialog, which) -> {
                            firebaseAuth.signOut();
                            Intent i = new Intent(AdminDashboardActivity.this, micheal.must.signuplogin.MainActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
    }

    // New: load numeric stats from Firestore
    private void loadStats() {
        DatabaseReference statsRef = FirebaseDatabase.getInstance().getReference("stats");
        statsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    int totalUsers = 0, totalPosts = 0, activeGroups = 0, pendingReports = 0;

                    // Safely extract stats values with null checks
                    if (snapshot.child("totalUsers").exists()) {
                        totalUsers = snapshot.child("totalUsers").getValue(Integer.class);
                    }
                    if (snapshot.child("totalPosts").exists()) {
                        totalPosts = snapshot.child("totalPosts").getValue(Integer.class);
                    }
                    if (snapshot.child("activeGroups").exists()) {
                        activeGroups = snapshot.child("activeGroups").getValue(Integer.class);
                    }
                    if (snapshot.child("pendingReports").exists()) {
                        pendingReports = snapshot.child("pendingReports").getValue(Integer.class);
                    }

                    // Add null checks before setting text
                    if (findViewById(R.id.tv_total_users) != null) {
                        ((android.widget.TextView)findViewById(R.id.tv_total_users)).setText(String.valueOf(totalUsers));
                    }
                    if (findViewById(R.id.tv_total_posts) != null) {
                        ((android.widget.TextView)findViewById(R.id.tv_total_posts)).setText(String.valueOf(totalPosts));
                    }
                    if (findViewById(R.id.tv_total_groups) != null) {
                        ((android.widget.TextView)findViewById(R.id.tv_total_groups)).setText(String.valueOf(activeGroups));
                    }
                    if (findViewById(R.id.tv_pending_reports) != null) {
                        ((android.widget.TextView)findViewById(R.id.tv_pending_reports)).setText(String.valueOf(pendingReports));
                    }
                } catch (Exception e) {
                    Log.e("AdminDashboard", "Error loading stats: " + e.getMessage(), e);
                    Toast.makeText(AdminDashboardActivity.this, "Error loading stats", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AdminDashboard", "Database error: " + error.getMessage());
            }
        });
    }

    // New: users section setup
    private void setupUsersSection() {
        // Use the shared allUsers list for adapter so updates reflect automatically
        rvUsers.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        userAdapter = new AdminUserAdapter(allUsers);
        rvUsers.setAdapter(userAdapter);

        userAdapter.setOnUserActionListener(new AdminUserAdapter.OnUserActionListener() {
            @Override
            public void onPromoteToAdmin(User user, int position) {
                AdminUtils.promoteToAdmin(user.getUserId(), (success, message) -> {
                    runOnUiThread(() -> {
                        Toast.makeText(AdminDashboardActivity.this, message, Toast.LENGTH_SHORT).show();
                        if (success) {
                            user.setAdmin(true);
                            userAdapter.notifyItemChanged(position);
                            // Update Firestore with admin role
                            db.collection("users").document(user.getUserId())
                                    .update("role", "admin")
                                    .addOnFailureListener(e -> Log.e("Admin", "Error updating role", e));
                        }
                    });
                });
            }

            @Override
            public void onRevokeAdminStatus(User user, int position) {
                AdminUtils.revokeAdminStatus(user.getUserId(), (success, message) -> {
                    runOnUiThread(() -> {
                        Toast.makeText(AdminDashboardActivity.this, message, Toast.LENGTH_SHORT).show();
                        if (success) {
                            user.setAdmin(false);
                            userAdapter.notifyItemChanged(position);
                            // Update Firestore role
                            db.collection("users").document(user.getUserId())
                                    .update("role", "user")
                                    .addOnFailureListener(e -> Log.e("Admin", "Error updating role", e));
                        }
                    });
                });
            }

            @Override
            public void onBanUser(User user, int position) {
                // Show confirmation dialog
                new androidx.appcompat.app.AlertDialog.Builder(AdminDashboardActivity.this)
                        .setTitle("Ban User")
                        .setMessage("Are you sure you want to ban " + user.getDisplayName() + "?")
                        .setPositiveButton("Ban", (dialog, which) -> {
                            // Ban in Firestore
                            db.collection("users").document(user.getUserId())
                                    .update("banned", true, "bannedAt", System.currentTimeMillis())
                                    .addOnSuccessListener(aVoid -> {
                                        // Remove from Realtime DB
                                        rtDbRef.child(user.getUserId()).removeValue()
                                                .addOnSuccessListener(v -> runOnUiThread(() -> {
                                                    allUsers.remove(position);
                                                    userAdapter.notifyItemRemoved(position);
                                                    Toast.makeText(AdminDashboardActivity.this, 
                                                            "User banned successfully", Toast.LENGTH_SHORT).show();
                                                }));
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(AdminDashboardActivity.this,
                                            "Error banning user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        swipeUsers.setOnRefreshListener(() -> {
            loadUsers();
            loadStats();
            swipeUsers.setRefreshing(false);
        });

        // Search
        searchUsers.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterUsers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return true;
            }
        });
    }

    private void filterUsers(String query) {
        if (query == null) query = "";
        String q = query.toLowerCase().trim();
        List<User> filtered = new ArrayList<>();
        for (User u : allUsers) {
            if ((u.getDisplayName() != null && u.getDisplayName().toLowerCase().contains(q)) ||
                    (u.getEmail() != null && u.getEmail().toLowerCase().contains(q))) {
                filtered.add(u);
            }
        }
        // Update adapter data without recreating listeners
        userAdapter.updateData(filtered);
    }

    // Updated: load recent users from Realtime Database and check admin role in Firestore
    private void loadUsers() {
        allUsers.clear();
        userAdapter.notifyDataSetChanged();

        rtDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String uid = child.getKey();
                        if (uid == null) continue;

                        // Read display name/email from RTDB
                        String name = null;
                        if (child.child("displayName").exists()) {
                            name = child.child("displayName").getValue(String.class);
                        } else if (child.child("name").exists()) {
                            name = child.child("name").getValue(String.class);
                        }

                        String email = child.child("email").getValue(String.class);

                        User user = new User(uid, name, email);
                        user.setAdmin(false);

                        allUsers.add(user);
                        int insertIndex = allUsers.size() - 1;
                        runOnUiThread(() -> userAdapter.notifyItemInserted(insertIndex));

                        db.collection("users").document(uid).get()
                                .addOnSuccessListener(doc -> {
                                    if (doc.exists() && "admin".equals(doc.getString("role"))) {
                                        for (int i = 0; i < allUsers.size(); i++) {
                                            User u = allUsers.get(i);
                                            if (u.getUserId().equals(uid)) {
                                                u.setAdmin(true);
                                                final int pos = i;
                                                runOnUiThread(() -> userAdapter.notifyItemChanged(pos));
                                                break;
                                            }
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Ignore failure to read role
                                });
                    }
                } else {
                    Toast.makeText(AdminDashboardActivity.this, "No users found in Realtime DB", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AdminDashboardActivity.this, "Failed to load users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
