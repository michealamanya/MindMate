package micheal.must.signuplogin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import micheal.must.signuplogin.R;
import micheal.must.signuplogin.models.Group;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {
    private final List<Group> groups;
    private final OnGroupActionListener listener;

    public interface OnGroupActionListener {
        void onGroupClicked(Group group);
        void onJoinGroup(Group group);
        void onLeaveGroup(Group group);
    }

    public GroupAdapter(List<Group> groups, OnGroupActionListener listener) {
        this.groups = groups;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Group group = groups.get(position);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        holder.groupName.setText(group.getGroupName());
        holder.groupDescription.setText(group.getDescription());
        holder.memberCount.setText(group.getMemberCount() + " members");

        boolean isMember = currentUserId != null && group.isMember(currentUserId);

        // Show/hide join button based on membership
        if (isMember) {
            holder.btnJoin.setVisibility(View.GONE);
            holder.btnLeave.setVisibility(View.VISIBLE);
        } else {
            holder.btnJoin.setVisibility(View.VISIBLE);
            holder.btnLeave.setVisibility(View.GONE);
        }

        holder.cardView.setOnClickListener(v -> listener.onGroupClicked(group));

        holder.btnJoin.setOnClickListener(v -> {
            if (currentUserId != null) {
                listener.onJoinGroup(group);
            } else {
                Toast.makeText(holder.itemView.getContext(), "Please log in first", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnLeave.setOnClickListener(v -> {
            if (currentUserId != null) {
                listener.onLeaveGroup(group);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView groupName, groupDescription, memberCount;
        Button btnJoin, btnLeave;
        CardView cardView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.tv_group_name);
            groupDescription = itemView.findViewById(R.id.tv_group_description);
            memberCount = itemView.findViewById(R.id.tv_member_count);
            btnJoin = itemView.findViewById(R.id.btn_join_group);
            btnLeave = itemView.findViewById(R.id.btn_leave_group);
            cardView = itemView.findViewById(R.id.group_card);
        }
    }
}
