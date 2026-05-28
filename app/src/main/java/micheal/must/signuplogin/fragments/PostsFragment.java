package micheal.must.signuplogin.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import micheal.must.signuplogin.GroupChatActivity;
import micheal.must.signuplogin.R;
import micheal.must.signuplogin.adapters.JoinedGroupsAdapter;
import micheal.must.signuplogin.models.Group;

public class PostsFragment extends Fragment {

    private static final String TAG = "PostsFragment";
    private RecyclerView rvJoinedGroups;
    private LinearLayout loadingContainer;
    private ProgressBar loadingSpinner;
    private TextView tvNoGroups;
    private DatabaseReference groupsRef;
    private String currentUserId;
    private List<Group> joinedGroupsList;
    private JoinedGroupsAdapter adapter;
    private ValueEventListener groupsListener;

    public PostsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_posts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views FIRST
        rvJoinedGroups = view.findViewById(R.id.rv_posts);
        loadingContainer = view.findViewById(R.id.loading_container);
        loadingSpinner = view.findViewById(R.id.loading_spinner);
        tvNoGroups = view.findViewById(R.id.tv_no_posts);

        Log.d(TAG, "Views found: rv=" + (rvJoinedGroups != null) + ", spinner=" + (loadingSpinner != null) + ", text=" + (tvNoGroups != null));

        // Get current user ID
        String newUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        Log.d(TAG, "Current User ID: " + newUserId);

        // If user changed, reset the fragment
        if (newUserId != null && !newUserId.equals(currentUserId)) {
            currentUserId = newUserId;
            joinedGroupsList = new ArrayList<>();
            
            if (adapter != null) {
                adapter.updateGroups(joinedGroupsList);
            }
        }

        if (currentUserId == null) {
            currentUserId = newUserId;
        }

        if (currentUserId == null) {
            if (tvNoGroups != null) {
                tvNoGroups.setVisibility(View.VISIBLE);
                tvNoGroups.setText("Please log in to see your groups");
            }
            if (loadingContainer != null) {
                loadingContainer.setVisibility(View.GONE);
            }
            return;
        }

        joinedGroupsList = new ArrayList<>();
        groupsRef = FirebaseDatabase.getInstance().getReference().child("groups");

        // Setup RecyclerView
        if (rvJoinedGroups != null) {
            rvJoinedGroups.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new JoinedGroupsAdapter(requireContext(), joinedGroupsList, group -> openGroupChat(group));
            rvJoinedGroups.setAdapter(adapter);
            Log.d(TAG, "✓ RecyclerView adapter set");
        }

        loadJoinedGroups();
    }

    private void loadJoinedGroups() {
        Log.d(TAG, "Starting to load joined groups for user: " + currentUserId);
        
        if (loadingContainer != null) {
            loadingContainer.setVisibility(View.VISIBLE);
        }
        if (rvJoinedGroups != null) {
            rvJoinedGroups.setVisibility(View.GONE);
        }
        if (tvNoGroups != null) {
            tvNoGroups.setVisibility(View.GONE);
        }

        // Remove old listener if exists
        if (groupsListener != null && groupsRef != null) {
            groupsRef.removeEventListener(groupsListener);
        }

        groupsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Firebase snapshot received with " + snapshot.getChildrenCount() + " groups");
                joinedGroupsList.clear();
                
                for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                    try {
                        // Manually parse group data from Firebase
                        Map<String, Object> groupData = (Map<String, Object>) groupSnapshot.getValue();
                        
                        if (groupData != null) {
                            String groupId = groupSnapshot.getKey();
                            String groupName = (String) groupData.get("groupName");
                            String description = (String) groupData.get("description");
                            String createdBy = (String) groupData.get("createdBy");
                            
                            Log.d(TAG, "Processing group: " + groupName + " (ID: " + groupId + ")");
                            
                            // Get members - handle both List and Map formats
                            List<String> members = new ArrayList<>();
                            Object membersObj = groupData.get("members");
                            
                            if (membersObj instanceof List) {
                                members = (List<String>) membersObj;
                                Log.d(TAG, "Members found as List: " + members.size());
                            } else if (membersObj instanceof Map) {
                                Map<String, Object> membersMap = (Map<String, Object>) membersObj;
                                for (Object value : membersMap.values()) {
                                    if (value instanceof String) {
                                        members.add((String) value);
                                    }
                                }
                                Log.d(TAG, "Members found as Map: " + members.size());
                            }
                            
                            // Only show groups where current user is a member
                            Log.d(TAG, "Checking if user " + currentUserId + " is in members: " + members);
                            if (members != null && members.contains(currentUserId)) {
                                Group group = new Group();
                                group.setGroupId(groupId);
                                group.setGroupName(groupName);
                                group.setDescription(description);
                                group.setCreatedBy(createdBy);
                                group.setMembers(members);
                                
                                joinedGroupsList.add(group);
                                Log.d(TAG, "✓ Loaded joined group: " + groupName);
                            } else {
                                Log.d(TAG, "User not member of: " + groupName);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing group: " + e.getMessage(), e);
                    }
                }

                // Hide loading spinner
                if (loadingContainer != null) {
                    loadingContainer.setVisibility(View.GONE);
                }

                Log.d(TAG, "Total joined groups loaded: " + joinedGroupsList.size());

                // Show appropriate view
                if (joinedGroupsList.isEmpty()) {
                    Log.d(TAG, "No joined groups found");
                    if (tvNoGroups != null) {
                        tvNoGroups.setVisibility(View.VISIBLE);
                        tvNoGroups.setText("No groups joined yet.\nGo to Groups tab to join!");
                    }
                    if (rvJoinedGroups != null) {
                        rvJoinedGroups.setVisibility(View.GONE);
                    }
                } else {
                    Log.d(TAG, "Showing " + joinedGroupsList.size() + " groups");
                    if (rvJoinedGroups != null) {
                        rvJoinedGroups.setVisibility(View.VISIBLE);
                    }
                    if (tvNoGroups != null) {
                        tvNoGroups.setVisibility(View.GONE);
                    }
                    if (adapter != null) {
                        adapter.updateGroups(joinedGroupsList);
                        Log.d(TAG, "✓ Updated adapter with " + joinedGroupsList.size() + " joined groups");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading groups: " + error.getMessage());
                if (loadingContainer != null) {
                    loadingContainer.setVisibility(View.GONE);
                }
                if (tvNoGroups != null) {
                    tvNoGroups.setVisibility(View.VISIBLE);
                    tvNoGroups.setText("Error loading groups.\nPlease try again.");
                }
                if (rvJoinedGroups != null) {
                    rvJoinedGroups.setVisibility(View.GONE);
                }
            }
        };

        groupsRef.addValueEventListener(groupsListener);
    }

    private void openGroupChat(Group group) {
        Log.d(TAG, "Opening group chat: " + group.getGroupName());
        Intent intent = new Intent(requireContext(), GroupChatActivity.class);
        intent.putExtra("groupId", group.getGroupId());
        intent.putExtra("groupName", group.getGroupName());
        intent.putExtra("createdBy", group.getCreatedBy());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove listener when fragment is destroyed
        if (groupsListener != null && groupsRef != null) {
            groupsRef.removeEventListener(groupsListener);
        }
    }
}
