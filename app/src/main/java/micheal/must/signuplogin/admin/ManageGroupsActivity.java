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

import micheal.must.signuplogin.MainActivity;
import micheal.must.signuplogin.R;
import micheal.must.signuplogin.adapters.AdminGroupAdapter;
import micheal.must.signuplogin.models.Group;

public class ManageGroupsActivity extends AppCompatActivity {

    private RecyclerView rvGroups;
    private SearchView searchGroups;
    private SwipeRefreshLayout swipeRefresh;
    private AdminGroupAdapter adapter;
    private List<Group> allGroups = new ArrayList<>();
    private List<Group> filteredGroups = new ArrayList<>();
    
    private FirebaseFirestore firestore;
    private DatabaseReference rtDatabase;
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "ManageGroupsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_groups);

        firebaseAuth = FirebaseAuth.getInstance();
        initializeFirebase();
        initializeUI();
        setupRecyclerView();
        setupSearchView();
        setupSwipeRefresh();
        loadAllGroups();
    }

    private void initializeFirebase() {
        firestore = FirebaseFirestore.getInstance();
        rtDatabase = FirebaseDatabase.getInstance().getReference("groups");
    }

    private void initializeUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Groups");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rvGroups = findViewById(R.id.rv_groups);
        searchGroups = findViewById(R.id.search_groups);
        swipeRefresh = findViewById(R.id.swipe_refresh);
    }

    private void setupRecyclerView() {
        rvGroups.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminGroupAdapter(filteredGroups);
        rvGroups.setAdapter(adapter);

        adapter.setOnGroupActionListener(new AdminGroupAdapter.OnGroupActionListener() {
            @Override
            public void onDeleteGroup(Group group, int position) {
                deleteGroup(group, position);
            }

            @Override
            public void onViewMembers(Group group) {
                showGroupMembers(group);
            }
        });
    }

    private void setupSearchView() {
        searchGroups.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterGroups(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterGroups(newText);
                return true;
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> {
            loadAllGroups();
            swipeRefresh.setRefreshing(false);
        });
    }

    private void loadAllGroups() {
        allGroups.clear();
        filteredGroups.clear();
        adapter.notifyDataSetChanged();

        rtDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        try {
                            Group group = new Group();
                            group.setGroupId(child.getKey());
                            
                            Object nameObj = child.child("name").getValue();
                            if (nameObj != null) {
                                group.setGroupName(nameObj.toString());
                            }
                            
                            Object descObj = child.child("description").getValue();
                            if (descObj != null) {
                                group.setDescription(descObj.toString());
                            }
                            
                            Object createdByObj = child.child("createdBy").getValue();
                            if (createdByObj != null) {
                                group.setCreatedBy(createdByObj.toString());
                            }
                            
                            Object memberCountObj = child.child("memberCount").getValue();
                            if (memberCountObj instanceof Number) {
                                group.setMemberCount(((Number) memberCountObj).intValue());
                            }
                            
                            allGroups.add(group);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing group: " + e.getMessage());
                        }
                    }
                    filteredGroups.addAll(allGroups);
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Loaded " + allGroups.size() + " groups");
                } else {
                    Toast.makeText(ManageGroupsActivity.this, "No groups found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading groups: " + error.getMessage());
                Toast.makeText(ManageGroupsActivity.this, "Error loading groups", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterGroups(String query) {
        filteredGroups.clear();
        if (query == null || query.isEmpty()) {
            filteredGroups.addAll(allGroups);
        } else {
            String searchQuery = query.toLowerCase();
            for (Group group : allGroups) {
                if ((group.getGroupName() != null && group.getGroupName().toLowerCase().contains(searchQuery)) ||
                    (group.getDescription() != null && group.getDescription().toLowerCase().contains(searchQuery))) {
                    filteredGroups.add(group);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void deleteGroup(Group group, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Group")
                .setMessage("Are you sure you want to delete \"" + group.getGroupName() + "\"?\n\nMembers: " + group.getMemberCount())
                .setPositiveButton("Delete", (dialog, which) -> {
                    rtDatabase.child(group.getGroupId()).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                allGroups.remove(group);
                                filteredGroups.remove(position);
                                adapter.notifyItemRemoved(position);
                                Toast.makeText(this, "Group deleted successfully", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> 
                                    Toast.makeText(this, "Error deleting group: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showGroupMembers(Group group) {
        String memberInfo = "Group: " + group.getGroupName() + "\n" +
                           "Members: " + group.getMemberCount() + "\n" +
                           "Created by: " + group.getCreatedBy() + "\n" +
                           "Description: " + group.getDescription();
        
        new AlertDialog.Builder(this)
                .setTitle("Group Information")
                .setMessage(memberInfo)
                .setPositiveButton("Close", null)
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
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    firebaseAuth.signOut();
                    Intent intent = new Intent(ManageGroupsActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
