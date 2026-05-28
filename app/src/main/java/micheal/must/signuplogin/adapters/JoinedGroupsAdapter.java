package micheal.must.signuplogin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import micheal.must.signuplogin.R;
import micheal.must.signuplogin.models.Group;

public class JoinedGroupsAdapter extends RecyclerView.Adapter<JoinedGroupsAdapter.GroupViewHolder> {

    private List<Group> groupsList;
    private Context context;
    private OnGroupClickListener listener;

    public interface OnGroupClickListener {
        void onGroupClicked(Group group);
    }

    public JoinedGroupsAdapter(Context context, List<Group> groupsList, OnGroupClickListener listener) {
        this.context = context;
        this.groupsList = groupsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_joined_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groupsList.get(position);
        holder.bind(group, position);
    }

    @Override
    public int getItemCount() {
        return groupsList.size();
    }

    public void updateGroups(List<Group> newGroups) {
        this.groupsList = newGroups;
        notifyDataSetChanged();
    }

    private void removeGroup(int position) {
        groupsList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, groupsList.size());
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroupName, tvDescription, tvMembers;
        Button btnEnter;

        GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tv_group_name);
            tvDescription = itemView.findViewById(R.id.tv_group_description);
            tvMembers = itemView.findViewById(R.id.tv_member_count);
            btnEnter = itemView.findViewById(R.id.btn_enter_group);
        }

        void bind(Group group, int position) {
            tvGroupName.setText(group.getGroupName() != null ? group.getGroupName() : "Unnamed Group");
            tvDescription.setText(group.getDescription() != null ? group.getDescription() : "No description");
            tvMembers.setText("Members: " + (group.getMembers() != null ? group.getMembers().size() : 0));

            btnEnter.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onGroupClicked(group);
                }
            });

            // Long click to show options
            itemView.setOnLongClickListener(v -> {
                showGroupOptions(group, position);
                return true;
            });
        }

        private void showGroupOptions(Group group, int position) {
            String[] options = {"Delete Group", "More Info", "Cancel"};

            new AlertDialog.Builder(context)
                    .setTitle(group.getGroupName())
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            // Delete Group
                            showDeleteConfirmation(group, position);
                        } else if (which == 1) {
                            // More Info
                            showGroupInfo(group);
                        }
                    })
                    .show();
        }

        private void showDeleteConfirmation(Group group, int position) {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Group")
                    .setMessage("Are you sure you want to delete \"" + group.getGroupName() + "\"? This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> deleteGroup(group, position))
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .setCancelable(false)
                    .show();
        }

        private void deleteGroup(Group group, int position) {
            DatabaseReference groupRef = FirebaseDatabase.getInstance()
                    .getReference("groups").child(group.getGroupId());

            groupRef.removeValue()
                    .addOnSuccessListener(aVoid -> {
                        // Remove from list immediately
                        removeGroup(position);
                        Toast.makeText(context, "Group deleted successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to delete group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }

        private void showGroupInfo(Group group) {
            StringBuilder info = new StringBuilder();
            info.append("Group Name: ").append(group.getGroupName()).append("\n\n");
            info.append("Description: ").append(group.getDescription()).append("\n\n");
            info.append("Members: ").append(group.getMembers() != null ? group.getMembers().size() : 0).append("\n\n");

            if (group.getCreatedBy() != null) {
                info.append("Created by: ").append(group.getCreatedBy());
            }

            new AlertDialog.Builder(context)
                    .setTitle("Group Information")
                    .setMessage(info.toString())
                    .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }
}
