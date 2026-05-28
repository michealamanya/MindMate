package micheal.must.signuplogin.fragments;

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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import micheal.must.signuplogin.R;
import micheal.must.signuplogin.adapters.GroupsAdapter;
import micheal.must.signuplogin.models.Group;

public class GroupsFragment extends Fragment {

    private static final String TAG = "GroupsFragment";
    private RecyclerView rvGroups;
    private FloatingActionButton fabCreateGroup;
    private LinearLayout loadingContainer;
    private TextView tvNoGroups;
    private DatabaseReference groupsRef;
    private List<Group> groupsList;
    private GroupsAdapter adapter;

    public GroupsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_groups, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvGroups = view.findViewById(R.id.rv_groups);
        fabCreateGroup = view.findViewById(R.id.fab_create_group);
        loadingContainer = view.findViewById(R.id.loading_container);
        tvNoGroups = view.findViewById(R.id.tv_no_groups);
        
        groupsList = new ArrayList<>();
        groupsRef = FirebaseDatabase.getInstance().getReference().child("groups");

        // Setup RecyclerView
        if (rvGroups != null) {
            rvGroups.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new GroupsAdapter(requireContext(), groupsList);
            rvGroups.setAdapter(adapter);
            Log.d(TAG, "✓ RecyclerView adapter set");
        }

        // Setup FAB
        if (fabCreateGroup != null) {
            fabCreateGroup.setOnClickListener(v -> showCreateGroupDialog());
        }

        // Load groups
        loadGroups();
    }

    private void loadGroups() {
        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupsList.clear();
                for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                    try {
                        Group group = groupSnapshot.getValue(Group.class);
                        if (group != null && group.getGroupId() != null && group.getGroupName() != null) {
                            groupsList.add(group);
                            Log.d(TAG, "✓ Loaded group: " + group.getGroupName());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing group: " + e.getMessage());
                    }
                }

                // Hide loading spinner
                hideLoadingSpinner();

                // Show appropriate view
                if (groupsList.isEmpty()) {
                    if (tvNoGroups != null) {
                        tvNoGroups.setVisibility(View.VISIBLE);
                    }
                    if (rvGroups != null) {
                        rvGroups.setVisibility(View.GONE);
                    }
                } else {
                    if (rvGroups != null) {
                        rvGroups.setVisibility(View.VISIBLE);
                    }
                    if (tvNoGroups != null) {
                        tvNoGroups.setVisibility(View.GONE);
                    }
                    if (adapter != null) {
                        adapter.updateGroups(groupsList);
                        Log.d(TAG, "✓ Updated adapter with " + groupsList.size() + " groups");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading groups: " + error.getMessage());
                hideLoadingSpinner();
                if (tvNoGroups != null) {
                    tvNoGroups.setVisibility(View.VISIBLE);
                    tvNoGroups.setText("Error loading groups.\nPlease try again.");
                }
            }
        });
    }

    private void hideLoadingSpinner() {
        if (loadingContainer != null) {
            loadingContainer.setVisibility(View.GONE);
        }
    }

    private void showCreateGroupDialog() {
        CreateGroupFragment createGroupFragment = new CreateGroupFragment();
        createGroupFragment.show(getChildFragmentManager(), "CreateGroupDialog");
    }
}
