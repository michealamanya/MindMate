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
import java.util.HashMap;
import java.util.List;

import micheal.must.signuplogin.GroupChatActivity;
import micheal.must.signuplogin.R;
import micheal.must.signuplogin.adapters.GroupAdapter;
import micheal.must.signuplogin.models.Group;


public class MyGroupsFragment extends Fragment {

    private static final String TAG = "MyGroupsFragment";
    private RecyclerView rvGroups;
    private GroupAdapter groupAdapter;
    private List<Group> groupList;
    private DatabaseReference groupsRef;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_groups, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvGroups = view.findViewById(R.id.rv_groups);
        groupList = new ArrayList<>();

        // Get current user ID
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (currentUserId == null) {
            Toast.makeText(getContext(), "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialize Firebase reference
        groupsRef = FirebaseDatabase.getInstance().getReference().child("groups");

        // Fetch user's groups in real-time
        loadUserGroupsFromFirebase();
    }

    private void loadUserGroupsFromFirebase() {
        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupList.clear();

                for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                    try {
                        Group group = new Group();
                        group.setGroupId(groupSnapshot.getKey());
                        group.setGroupName((String) groupSnapshot.child("groupName").getValue());
                        group.setDescription((String) groupSnapshot.child("description").getValue());
                        group.setCreatedBy((String) groupSnapshot.child("createdBy").getValue());
                        
                        Object createdAtObj = groupSnapshot.child("createdAt").getValue();
                        long createdAt = (createdAtObj instanceof Number) ? ((Number) createdAtObj).longValue() : 0L;
                        group.setCreatedAt(createdAt);
                        
                        Object memberCountObj = groupSnapshot.child("memberCount").getValue();
                        int memberCount = (memberCountObj instanceof Number) ? ((Number) memberCountObj).intValue() : 0;
                        group.setMemberCount(memberCount);
                        
                        // Handle memberIds
                        List<String> memberIds = new ArrayList<>();
                        Object memberIdsObj = groupSnapshot.child("memberIds").getValue();
                        
                        if (memberIdsObj instanceof List) {
                            memberIds = (List<String>) memberIdsObj;
                        } else if (memberIdsObj instanceof HashMap) {
                            HashMap<String, Object> map = (HashMap<String, Object>) memberIdsObj;
                            memberIds = new ArrayList<>(map.keySet());
                        }
                        
                        group.setMemberIds(memberIds);
                        
                        // Only show groups where user is a member
                        if (memberIds.contains(currentUserId)) {
                            groupList.add(group);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing group: " + e.getMessage());
                    }
                }

                setupAdapter();

                if (groupList.isEmpty()) {
                    Toast.makeText(getContext(), "You haven't joined any groups yet", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading groups: " + error.getMessage());
                Toast.makeText(getContext(), "Failed to load groups", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAdapter() {
        groupAdapter = new GroupAdapter(groupList, new GroupAdapter.OnGroupActionListener() {
            @Override
            public void onGroupClicked(Group group) {
                Intent intent = new Intent(getContext(), GroupChatActivity.class);
                intent.putExtra("groupId", group.getGroupId());
                startActivity(intent);
            }

            @Override
            public void onJoinGroup(Group group) {
                Toast.makeText(getContext(), "You are already a member", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLeaveGroup(Group group) {
                leaveGroup(group);
            }
        });

        rvGroups.setAdapter(groupAdapter);
        rvGroups.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void leaveGroup(Group group) {
        if (currentUserId == null) return;

        List<String> memberIds = group.getMemberIds();
        if (memberIds != null && memberIds.contains(currentUserId)) {
            memberIds.remove(currentUserId);
            group.setMemberIds(memberIds);
            group.setMemberCount(memberIds.size());
        }
        
        DatabaseReference groupRef = groupsRef.child(group.getGroupId());
        groupRef.setValue(group).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Left group", Toast.LENGTH_SHORT).show();
            loadUserGroupsFromFirebase();
        });
    }
}
