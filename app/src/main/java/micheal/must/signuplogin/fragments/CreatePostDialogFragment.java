package micheal.must.signuplogin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

import micheal.must.signuplogin.R;

public class CreatePostDialogFragment extends DialogFragment {

    private EditText etPostContent;
    private Spinner spinnerGroup;
    private Button btnPost, btnCancel;
    private PostCreationListener listener;

    // Interface for callback
    public interface PostCreationListener {
        void onPostCreated(String content, String group);
    }

    public void setPostCreationListener(PostCreationListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use a style that creates a floating dialog
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Remove the title bar
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return inflater.inflate(R.layout.dialog_create_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        etPostContent = view.findViewById(R.id.et_post_content);
        spinnerGroup = view.findViewById(R.id.spinner_group);
        btnPost = view.findViewById(R.id.btn_post);
        btnCancel = view.findViewById(R.id.btn_cancel);

        // Setup group spinner with sample data
        String[] groups = {"General Discussion", "Anxiety Support", "Meditation Group", "Student Mental Health"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                groups);
        spinnerGroup.setAdapter(adapter);

        // Setup button click listeners
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        // Ensure buttons are properly initialized
        if (btnPost == null || btnCancel == null) {
            Toast.makeText(getContext(), "Error initializing buttons", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPost.setOnClickListener(v -> {
            String content = etPostContent.getText().toString().trim();
            if (content.isEmpty()) {
                etPostContent.setError("Post content cannot be empty");
                return;
            }
            btnPost.setEnabled(false);

            String selectedGroup = spinnerGroup.getSelectedItem().toString();

            // Build post object
            String uid = "anonymous";
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("posts").push();
            Map<String, Object> postObj = new HashMap<>();
            postObj.put("content", content);
            postObj.put("group", selectedGroup);
            postObj.put("authorId", uid);
            postObj.put("createdAt", ServerValue.TIMESTAMP);

            ref.setValue(postObj)
                .addOnSuccessListener(aVoid -> {
                    // notify local listener if needed
                    if (listener != null) listener.onPostCreated(content, selectedGroup);
                    Toast.makeText(getContext(), "Post created successfully", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to create post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnPost.setEnabled(true);
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
