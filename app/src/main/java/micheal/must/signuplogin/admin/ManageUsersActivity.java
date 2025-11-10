package micheal.must.signuplogin.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import micheal.must.signuplogin.R;
import micheal.must.signuplogin.adapters.AdminUserAdapter;
import micheal.must.signuplogin.models.User;

public class ManageUsersActivity extends AppCompatActivity {

    private RecyclerView rvUsers;
    private SearchView searchUsers;
    private SwipeRefreshLayout swipeRefresh;
    private AdminUserAdapter adapter;
    private List<User> allUsers = new ArrayList<>();
    private List<User> filteredUsers = new ArrayList<>();

    private FirebaseFirestore firestore;
    private DatabaseReference rtDatabase;
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "ManageUsersActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        firebaseAuth = FirebaseAuth.getInstance();
        initializeFirebase();
        initializeUI();
        setupRecyclerView();
        setupSearchView();
        setupSwipeRefresh();
        loadAllUsers();
    }

    private void initializeFirebase() {
        firestore = FirebaseFirestore.getInstance();
        rtDatabase = FirebaseDatabase.getInstance().getReference("users");
    }

    private void initializeUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Users");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rvUsers = findViewById(R.id.rv_users);
        searchUsers = findViewById(R.id.search_users);
        swipeRefresh = findViewById(R.id.swipe_refresh);
    }

    private void setupRecyclerView() {
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminUserAdapter(filteredUsers);
        rvUsers.setAdapter(adapter);

        adapter.setOnUserActionListener(new AdminUserAdapter.OnUserActionListener() {
            @Override
            public void onPromoteToAdmin(User user, int position) {
                promoteToAdmin(user, position);
            }

            @Override
            public void onRevokeAdminStatus(User user, int position) {
                revokeAdminStatus(user, position);
            }

            @Override
            public void onBanUser(User user, int position) {
                banUser(user, position);
            }
        });
    }

    private void setupSearchView() {
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

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> {
            loadAllUsers();
            swipeRefresh.setRefreshing(false);
        });
    }

    private void loadAllUsers() {
        allUsers.clear();
        filteredUsers.clear();
        adapter.notifyDataSetChanged();

        rtDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String uid = child.getKey();
                        String name = child.child("displayName").getValue(String.class);
                        String email = child.child("email").getValue(String.class);

                        User user = new User(uid, name, email);
                        allUsers.add(user);
                    }
                    filteredUsers.addAll(allUsers);
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Loaded " + allUsers.size() + " users");
                } else {
                    Toast.makeText(ManageUsersActivity.this, "No users found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading users: " + error.getMessage());
                Toast.makeText(ManageUsersActivity.this, "Error loading users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterUsers(String query) {
        filteredUsers.clear();
        if (query == null || query.isEmpty()) {
            filteredUsers.addAll(allUsers);
        } else {
            String searchQuery = query.toLowerCase();
            for (User user : allUsers) {
                if ((user.getDisplayName() != null && user.getDisplayName().toLowerCase().contains(searchQuery)) ||
                        (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchQuery))) {
                    filteredUsers.add(user);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void promoteToAdmin(User user, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Promote to Admin")
                .setMessage("Promote " + user.getDisplayName() + " to Admin?")
                .setPositiveButton("Promote", (dialog, which) -> {
                    firestore.collection("users").document(user.getUserId())
                            .update("role", "admin")
                            .addOnSuccessListener(aVoid -> {
                                user.setAdmin(true);
                                adapter.notifyItemChanged(position);
                                Toast.makeText(this, "User promoted to admin", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error promoting user: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void revokeAdminStatus(User user, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Revoke Admin Status")
                .setMessage("Remove admin privileges from " + user.getDisplayName() + "?")
                .setPositiveButton("Revoke", (dialog, which) -> {
                    firestore.collection("users").document(user.getUserId())
                            .update("role", "user")
                            .addOnSuccessListener(aVoid -> {
                                user.setAdmin(false);
                                adapter.notifyItemChanged(position);
                                Toast.makeText(this, "Admin status revoked", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error revoking admin: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void banUser(User user, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Ban User")
                .setMessage("Are you sure you want to ban " + user.getDisplayName() + "?")
                .setPositiveButton("Ban", (dialog, which) -> {
                    firestore.collection("users").document(user.getUserId())
                            .update("banned", true, "bannedAt", System.currentTimeMillis())
                            .addOnSuccessListener(aVoid -> {
                                rtDatabase.child(user.getUserId()).removeValue();
                                allUsers.remove(user);
                                filteredUsers.remove(position);
                                adapter.notifyItemRemoved(position);
                                Toast.makeText(this, "User banned successfully", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error banning user: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            showLogoutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    firebaseAuth.signOut();
                    Intent intent = new Intent(ManageUsersActivity.this, micheal.must.signuplogin.MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
