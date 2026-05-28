package micheal.must.signuplogin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import micheal.must.signuplogin.R;
import micheal.must.signuplogin.models.Group;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {

    private List<Group> groupsList;
    private Context context;
    private String currentUserId;

    public GroupsAdapter(Context context, List<Group> groupsList) {
        this.context = context;
        this.groupsList = groupsList;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groupsList.get(position);
        holder.bind(group);
    }

    @Override
    public int getItemCount() {
        return groupsList.size();
    }

    public void updateGroups(List<Group> newGroups) {
        this.groupsList = newGroups;
        notifyDataSetChanged();
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroupName, tvDescription, tvMembers;
        Button btnJoin;

        GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tv_group_name);
            tvDescription = itemView.findViewById(R.id.tv_group_description);
            tvMembers = itemView.findViewById(R.id.tv_member_count);
            btnJoin = itemView.findViewById(R.id.btn_join_group);
        }

        void bind(Group group) {
            // Safety checks for null values
            if (group == null || group.getGroupId() == null) {
                tvGroupName.setText("Invalid Group");
                tvDescription.setText("This group data is corrupted");
                btnJoin.setEnabled(false);
                return;
            }

            tvGroupName.setText(group.getGroupName() != null ? group.getGroupName() : "Unnamed Group");
            tvDescription.setText(group.getDescription() != null ? group.getDescription() : "No description");
            tvMembers.setText("Members: " + (group.getMembers() != null ? group.getMembers().size() : 0));

            // Check if user is already a member
            boolean isMember = group.getMembers() != null && group.getMembers().contains(currentUserId);
            btnJoin.setText(isMember ? "Member" : "Join");
            btnJoin.setEnabled(!isMember && currentUserId != null);

            btnJoin.setOnClickListener(v -> {
                if (!isMember && currentUserId != null && group.getGroupId() != null) {
                    joinGroup(group);
                } else if (currentUserId == null) {
                    Toast.makeText(context, "Please log in first", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void joinGroup(Group group) {
            if (group.getGroupId() == null) {
                Toast.makeText(context, "Cannot join: Invalid group ID", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference groupRef = FirebaseDatabase.getInstance()
                    .getReference("groups").child(group.getGroupId()).child("members");
            
            groupRef.push().setValue(currentUserId)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Joined " + group.getGroupName(), Toast.LENGTH_SHORT).show();
                        btnJoin.setText("Member");
                        btnJoin.setEnabled(false);
                        
                        // Navigate to group chat
                        android.content.Intent intent = new android.content.Intent(context, 
                                micheal.must.signuplogin.GroupChatActivity.class);
                        intent.putExtra("groupId", group.getGroupId());
                        intent.putExtra("groupName", group.getGroupName());
                        context.startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to join group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
