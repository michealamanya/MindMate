package micheal.must.signuplogin.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import micheal.must.signuplogin.R;
import micheal.must.signuplogin.models.Group;

public class CreateGroupFragment extends Fragment {

    private static final String TAG = "CreateGroupFragment";
    private ProgressDialog progressDialog;
    
    private TextInputEditText etGroupName;
    private TextInputEditText etGroupDescription;
    private MaterialButton btnCreateGroup;
    private DatabaseReference groupsRef;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_group, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (currentUserId == null) {
            Toast.makeText(getContext(), "Please log in first", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return;
        }

        groupsRef = FirebaseDatabase.getInstance().getReference().child("groups");
        btnCreateGroup.setOnClickListener(v -> validateAndCreateGroup());
    }

    private void initViews(View view) {
        etGroupName = view.findViewById(R.id.et_group_name);
        etGroupDescription = view.findViewById(R.id.et_group_description);
        btnCreateGroup = view.findViewById(R.id.btn_create_group);
        
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setTitle("Creating Group");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
    }

    private void validateAndCreateGroup() {
        String groupName = etGroupName.getText().toString().trim();
        String description = etGroupDescription.getText().toString().trim();

        if (groupName.isEmpty()) {
            etGroupName.setError("Group name is required");
            etGroupName.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            etGroupDescription.setError("Description is required");
            etGroupDescription.requestFocus();
            return;
        }

        createGroup(groupName, description);
    }

    private void createGroup(String groupName, String description) {
        String groupId = groupsRef.push().getKey();
        if (groupId == null) {
            Toast.makeText(getContext(), "Failed to generate group ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Group group = new Group();
        group.setGroupId(groupId);
        group.setGroupName(groupName);
        group.setDescription(description);
        group.setCreatedBy(currentUserId);
        group.setCreatedAt(System.currentTimeMillis());
        group.addMember(currentUserId);

        progressDialog.show();

        groupsRef.child(groupId).setValue(group)
            .addOnSuccessListener(aVoid -> {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Group created successfully!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Group created with ID: " + groupId);
                requireActivity().onBackPressed(); // Return to discover section
            })
            .addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Failed to create group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error creating group", e);
            });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
