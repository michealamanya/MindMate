package micheal.must.signuplogin;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import micheal.must.signuplogin.adapters.ChatMessageAdapter;
import micheal.must.signuplogin.models.ChatMessage;
import micheal.must.signuplogin.models.Group;

public class GroupChatActivity extends AppCompatActivity {

    private static final String TAG = "GroupChatActivity";
    private Toolbar toolbar;
    private RecyclerView rvMessages;
    private MaterialButton btnSend;
    private ImageButton btnMembers, btnOptions;
    private androidx.appcompat.widget.AppCompatEditText etMessage;
    private ChatMessageAdapter adapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private String groupId;
    private Group group;
    private String currentUserId;
    private DatabaseReference groupRef, messagesRef;
    private ValueEventListener messagesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        groupId = getIntent().getStringExtra("groupId");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (groupId == null || currentUserId == null) {
            Toast.makeText(this, "Invalid group", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadGroup();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvMessages = findViewById(R.id.rv_messages);
        btnSend = findViewById(R.id.btn_send);
        etMessage = findViewById(R.id.et_message);
        btnMembers = findViewById(R.id.btn_members);
        btnOptions = findViewById(R.id.btn_group_options);

        groupRef = FirebaseDatabase.getInstance().getReference("groups").child(groupId);
        messagesRef = groupRef.child("messages");
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatMessageAdapter(messages, currentUserId);
        rvMessages.setAdapter(adapter);
    }

    private void loadGroup() {
        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                group = snapshot.getValue(Group.class);
                if (group == null) {
                    Toast.makeText(GroupChatActivity.this, "Group not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // Check if user is a member
                if (!group.isMember(currentUserId)) {
                    showJoinPrompt();
                } else {
                    setupChat();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading group: " + error.getMessage());
                Toast.makeText(GroupChatActivity.this, "Failed to load group", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void showJoinPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Join Group?");
        builder.setMessage("You must join this group to view and send messages.");
        builder.setPositiveButton("Join", (dialog, which) -> {
            joinGroup();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            finish();
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void joinGroup() {
        group.addMember(currentUserId);
        groupRef.setValue(group).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Joined group!", Toast.LENGTH_SHORT).show();
            setupChat();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error joining group: " + e.getMessage());
            Toast.makeText(this, "Failed to join group", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void setupChat() {
        updateToolbarTitle();
        setupButtons();
        loadMessages();
    }

    private void updateToolbarTitle() {
        if (toolbar != null && group != null) {
            toolbar.setTitle(group.getGroupName());
            toolbar.setSubtitle(group.getMemberCount() + " members");
        }
    }

    private void setupButtons() {
        if (btnMembers != null) {
            btnMembers.setOnClickListener(v -> showMembersList());
        }

        if (btnOptions != null) {
            btnOptions.setOnClickListener(v -> showGroupOptions());
        }

        if (btnSend != null) {
            btnSend.setOnClickListener(v -> sendMessage());
        }
    }

    private void loadMessages() {
        messagesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();

                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    try {
                        ChatMessage message = messageSnapshot.getValue(ChatMessage.class);
                        if (message != null) {
                            message.setMessageId(messageSnapshot.getKey());
                            messages.add(message);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing message: " + e.getMessage());
                    }
                }

                adapter.notifyDataSetChanged();
                // Scroll to bottom
                if (!messages.isEmpty()) {
                    rvMessages.scrollToPosition(messages.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading messages: " + error.getMessage());
            }
        };

        messagesRef.addValueEventListener(messagesListener);
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty()) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String senderName = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : "Anonymous";

        ChatMessage message = new ChatMessage(
                currentUserId,
                senderName,
                messageText,
                System.currentTimeMillis(),
                groupId
        );

        messagesRef.push().setValue(message).addOnSuccessListener(aVoid -> {
            etMessage.setText("");
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error sending message: " + e.getMessage());
            Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
        });
    }

    private void showMembersList() {
        if (group == null || group.getMemberIds() == null) {
            Toast.makeText(this, "No members to display", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] memberArray = group.getMemberIds().toArray(new String[0]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Group Members (" + group.getMemberCount() + ")");
        builder.setItems(memberArray, null);
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    private void showGroupOptions() {
        boolean isGroupAdmin = group != null && group.getCreatedBy().equals(currentUserId);
        String[] options = isGroupAdmin ?
                new String[]{"View Members", "Group Info", "Mute Notifications", "Leave Group", "Delete Group"} :
                new String[]{"View Members", "Group Info", "Mute Notifications", "Leave Group"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Group Options");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    showMembersList();
                    break;
                case 1:
                    showGroupInfo();
                    break;
                case 2:
                    muteNotifications();
                    break;
                case 3:
                    leaveGroup();
                    break;
                case 4:
                    if (isGroupAdmin) deleteGroup();
                    break;
            }
        });
        builder.show();
    }

    private void showGroupInfo() {
        if (group == null) return;

        String info = "Name: " + group.getGroupName() + "\n\n" +
                "Description: " + group.getDescription() + "\n\n" +
                "Members: " + group.getMemberCount() + "\n\n" +
                "Created by: " + group.getCreatedBy();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Group Information");
        builder.setMessage(info);
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    private void muteNotifications() {
        Toast.makeText(this, "Notifications muted for 8 hours", Toast.LENGTH_SHORT).show();
    }

    private void leaveGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Leave Group");
        builder.setMessage("Are you sure you want to leave this group?");
        builder.setPositiveButton("Leave", (dialog, which) -> {
            if (group != null && currentUserId != null) {
                group.removeMember(currentUserId);
                groupRef.setValue(group).addOnSuccessListener(aVoid -> {
                    Toast.makeText(GroupChatActivity.this, "You left the group", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deleteGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Group");
        builder.setMessage("Are you sure you want to delete this group? This action cannot be undone.");
        builder.setPositiveButton("Delete", (dialog, which) -> {
            groupRef.removeValue().addOnSuccessListener(aVoid -> {
                Toast.makeText(GroupChatActivity.this, "Group deleted", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesRef != null && messagesListener != null) {
            messagesRef.removeEventListener(messagesListener);
        }
    }
}
