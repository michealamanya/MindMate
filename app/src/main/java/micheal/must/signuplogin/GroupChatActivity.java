package micheal.must.signuplogin;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import micheal.must.signuplogin.adapters.GroupChatAdapter;
import micheal.must.signuplogin.models.GroupMessage;

public class GroupChatActivity extends AppCompatActivity {

    private static final String TAG = "GroupChatActivity";
    private String groupId;
    private String currentUserId;
    private String groupCreatorId;
    private DatabaseReference messagesRef;
    private DatabaseReference groupRef;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private FloatingActionButton btnSend;
    private ProgressBar loadingSpinner;
    private TextView tvGroupTitle;
    private ImageButton btnBack;
    private ImageButton btnDelete;
    private List<GroupMessage> messagesList;
    private GroupChatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        // Get group ID from intent
        groupId = getIntent().getStringExtra("groupId");
        String groupName = getIntent().getStringExtra("groupName");
        groupCreatorId = getIntent().getStringExtra("createdBy");

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (groupId == null || currentUserId == null) {
            Toast.makeText(this, "Error: Missing group or user info", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupUI(groupName);
        loadMessages();
    }

    private void initializeViews() {
        rvMessages = findViewById(R.id.rv_group_messages);
        etMessage = findViewById(R.id.et_group_message);
        btnSend = findViewById(R.id.btn_send_group_message);
        loadingSpinner = findViewById(R.id.loading_spinner);
        tvGroupTitle = findViewById(R.id.tv_group_title);
        btnBack = findViewById(R.id.btn_back);
        btnDelete = findViewById(R.id.btn_delete_group);

        messagesList = new ArrayList<>();
        messagesRef = FirebaseDatabase.getInstance().getReference()
                .child("groups").child(groupId).child("messages");
        groupRef = FirebaseDatabase.getInstance().getReference()
                .child("groups").child(groupId);

        // Setup RecyclerView
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GroupChatAdapter(messagesList, currentUserId);
        rvMessages.setAdapter(adapter);
    }

    private void setupUI(String groupName) {
        tvGroupTitle.setText(groupName != null ? groupName : "Group Chat");

        btnBack.setOnClickListener(v -> onBackPressed());
        btnSend.setOnClickListener(v -> sendMessage());
        btnDelete.setOnClickListener(v -> showDeleteGroupDialog());

        // Show delete button only if user is the group creator
        if (groupCreatorId != null && groupCreatorId.equals(currentUserId)) {
            btnDelete.setVisibility(android.view.View.VISIBLE);
        } else {
            btnDelete.setVisibility(android.view.View.GONE);
        }
    }

    private void loadMessages() {
        loadingSpinner.setVisibility(android.view.View.VISIBLE);

        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                messagesList.clear();
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    try {
                        GroupMessage message = messageSnapshot.getValue(GroupMessage.class);
                        if (message != null) {
                            messagesList.add(message);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                    if (!messagesList.isEmpty()) {
                        rvMessages.scrollToPosition(messagesList.size() - 1);
                    }
                }

                loadingSpinner.setVisibility(android.view.View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                loadingSpinner.setVisibility(android.view.View.GONE);
                Toast.makeText(GroupChatActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();

        if (messageText.isEmpty()) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSend.setEnabled(false);
        loadingSpinner.setVisibility(android.view.View.VISIBLE);

        GroupMessage message = new GroupMessage();
        message.setUserId(currentUserId);
        message.setMessage(messageText);
        message.setTimestamp(System.currentTimeMillis());

        messagesRef.push().setValue(message)
                .addOnSuccessListener(aVoid -> {
                    etMessage.setText("");
                    btnSend.setEnabled(true);
                    loadingSpinner.setVisibility(android.view.View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(GroupChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                    btnSend.setEnabled(true);
                    loadingSpinner.setVisibility(android.view.View.GONE);
                });
    }

    private void showDeleteGroupDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Group")
                .setMessage("Are you sure you want to delete this group? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteGroup())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    private void deleteGroup() {
        loadingSpinner.setVisibility(android.view.View.VISIBLE);

        groupRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    loadingSpinner.setVisibility(android.view.View.GONE);
                    Toast.makeText(this, "Group deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    loadingSpinner.setVisibility(android.view.View.GONE);
                    Toast.makeText(this, "Failed to delete group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
