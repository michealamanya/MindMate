package micheal.must.signuplogin.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

import micheal.must.signuplogin.GroupChatActivity;
import micheal.must.signuplogin.R;
import micheal.must.signuplogin.adapters.DiscoverGroupAdapter;
import micheal.must.signuplogin.models.Group;

public class DiscoverFragment extends Fragment {

    private static final String TAG = "DiscoverFragment";
    private RecyclerView rvGroups;
    private DiscoverGroupAdapter adapter;
    private List<Group> groupList;
    private DatabaseReference groupsRef;
    private String currentUserId;
    private ValueEventListener groupListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discover, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvGroups = view.findViewById(R.id.rv_groups);
        groupList = new ArrayList<>();

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (currentUserId == null) {
            Toast.makeText(getContext(), "Please log in first", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return;
        }

        Log.d(TAG, "Current User ID: " + currentUserId);

        // Setup RecyclerView with Grid Layout for grid view
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvGroups.setLayoutManager(layoutManager);
        
        adapter = new DiscoverGroupAdapter(groupList, new DiscoverGroupAdapter.OnGroupActionListener() {
            @Override
            public void onGroupClick(Group group) {
                Intent intent = new Intent(getContext(), GroupChatActivity.class);
                intent.putExtra("groupId", group.getGroupId());
                startActivity(intent);
            }

            @Override
            public void onJoinGroup(Group group) {
                joinGroup(group);
            }

            @Override
            public void onLeaveGroup(Group group) {
                leaveGroup(group);
            }
        });

        rvGroups.setAdapter(adapter);

        groupsRef = FirebaseDatabase.getInstance().getReference().child("groups");
        loadAllGroups();
    }

    private void loadAllGroups() {
        groupListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupList.clear();
                Log.d(TAG, "Snapshot exists: " + snapshot.exists() + ", Children count: " + snapshot.getChildrenCount());

                if (!snapshot.exists()) {
                    Log.w(TAG, "No groups node in database");
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "No groups exist yet. Create one!", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                    try {
                        Log.d(TAG, "Processing group key: " + groupSnapshot.getKey());
                        
                        Group group = new Group();
                        group.setGroupId(groupSnapshot.getKey());
                        
                        // Database uses "name" not "groupName"
                        Object nameObj = groupSnapshot.child("name").getValue();
                        Log.d(TAG, "Name value: " + nameObj);
                        if (nameObj != null) {
                            group.setGroupName(nameObj.toString());
                        }
                        
                        Object descriptionObj = groupSnapshot.child("description").getValue();
                        if (descriptionObj != null) {
                            group.setDescription(descriptionObj.toString());
                        }
                        
                        Object createdByObj = groupSnapshot.child("createdBy").getValue();
                        if (createdByObj != null) {
                            group.setCreatedBy(createdByObj.toString());
                        }
                        
                        Object createdAtObj = groupSnapshot.child("createdAt").getValue();
                        if (createdAtObj instanceof Number) {
                            group.setCreatedAt(((Number) createdAtObj).longValue());
                        }
                        
                        Object memberCountObj = groupSnapshot.child("memberCount").getValue();
                        if (memberCountObj instanceof Number) {
                            group.setMemberCount(((Number) memberCountObj).intValue());
                        } else {
                            group.setMemberCount(0);
                        }
                        
                        // Database uses "members" not "memberIds"
                        java.util.List<String> memberIds = new ArrayList<>();
                        Object membersObj = groupSnapshot.child("members").getValue();
                        Log.d(TAG, "Members object: " + membersObj);
                        
                        if (membersObj instanceof java.util.List) {
                            memberIds = (java.util.List<String>) membersObj;
                        } else if (membersObj instanceof java.util.Map) {
                            memberIds = new ArrayList<>(((java.util.Map<String, Object>) membersObj).keySet());
                        }
                        
                        group.setMemberIds(memberIds);
                        
                        if (group.getGroupName() != null && !group.getGroupName().isEmpty()) {
                            groupList.add(group);
                            Log.d(TAG, "Successfully loaded group: " + group.getGroupName());
                        } else {
                            Log.w(TAG, "Skipped group with null/empty name");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing group: " + e.getMessage(), e);
                        e.printStackTrace();
                    }
                }

                adapter.notifyDataSetChanged();
                Log.d(TAG, "Total groups loaded: " + groupList.size());
                
                if (groupList.isEmpty()) {
                    Toast.makeText(getContext(), "No groups found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading groups: " + error.getMessage());
                Log.e(TAG, "Error code: " + error.getCode());
                Toast.makeText(getContext(), "Failed to load groups: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        groupsRef.addValueEventListener(groupListener);
    }

    private void joinGroup(Group group) {
        if (group == null || group.getGroupId() == null) {
            Toast.makeText(getContext(), "Invalid group", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        
        if (currentUserId == null) {
            Toast.makeText(getContext(), "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        group.addMember(currentUserId);
        Log.d(TAG, "Joining group: " + group.getGroupId() + " with user: " + currentUserId);
        
        DatabaseReference groupRef = FirebaseDatabase.getInstance()
                .getReference("groups").child(group.getGroupId());
        groupRef.setValue(group).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Joined group!", Toast.LENGTH_SHORT).show();
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error joining group: " + e.getMessage());
            Toast.makeText(getContext(), "Failed to join group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void leaveGroup(Group group) {
        if (group == null || group.getGroupId() == null) {
            Toast.makeText(getContext(), "Invalid group", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        
        if (currentUserId == null) {
            Toast.makeText(getContext(), "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        group.removeMember(currentUserId);
        Log.d(TAG, "Leaving group: " + group.getGroupId() + " with user: " + currentUserId);
        
        DatabaseReference groupRef = FirebaseDatabase.getInstance()
                .getReference("groups").child(group.getGroupId());
        groupRef.setValue(group).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Left group", Toast.LENGTH_SHORT).show();
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error leaving group: " + e.getMessage());
            Toast.makeText(getContext(), "Failed to leave group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (groupsRef != null && groupListener != null) {
            groupsRef.removeEventListener(groupListener);
        }
    }
}
