package micheal.must.signuplogin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import micheal.must.signuplogin.R;

public class CreateGroupDialogFragment extends DialogFragment {

    private EditText etGroupName;
    private EditText etGroupDescription;
    private Button btnCreate, btnCancel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Remove the title bar
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return inflater.inflate(R.layout.dialog_create_group, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etGroupName = view.findViewById(R.id.et_group_name);
        etGroupDescription = view.findViewById(R.id.et_group_description);
        btnCreate = view.findViewById(R.id.btn_create);
        btnCancel = view.findViewById(R.id.btn_cancel);

        btnCreate.setOnClickListener(v -> {
            String name = etGroupName.getText().toString().trim();
            String description = etGroupDescription.getText().toString().trim();

            if (name.isEmpty()) {
                etGroupName.setError("Group name is required");
                return;
            }

            // disable to prevent double taps
            btnCreate.setEnabled(false);

            // Get current user
            String uid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                    FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

            if (uid == null) {
                Toast.makeText(getContext(), "Please log in first", Toast.LENGTH_SHORT).show();
                btnCreate.setEnabled(true);
                return;
            }

            // Create group in Firebase
            DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference().child("groups").push();
            
            // Create members as a proper array/list
            List<String> members = new ArrayList<>();
            members.add(uid);

            Map<String, Object> groupData = new HashMap<>();
            groupData.put("name", name);
            groupData.put("description", description);
            groupData.put("createdBy", uid);
            groupData.put("createdAt", ServerValue.TIMESTAMP);
            groupData.put("memberCount", 1);
            groupData.put("members", members);

            groupsRef.setValue(groupData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Group created successfully! You have joined.", Toast.LENGTH_SHORT).show();
                        dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to create group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnCreate.setEnabled(true);
                    });
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onStart() {
        super.onStart();
        // Make dialog width match parent
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
