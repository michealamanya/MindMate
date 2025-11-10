package micheal.must.signuplogin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import micheal.must.signuplogin.R;
import micheal.must.signuplogin.models.Group;

public class DiscoverGroupAdapter extends RecyclerView.Adapter<DiscoverGroupAdapter.GroupViewHolder> {

    private List<Group> groupList;
    private OnGroupActionListener listener;

    public interface OnGroupActionListener {
        void onGroupClick(Group group);
        void onJoinGroup(Group group);
        void onLeaveGroup(Group group);
    }

    public DiscoverGroupAdapter(List<Group> groupList, OnGroupActionListener listener) {
        this.groupList = groupList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_discover_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groupList.get(position);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        holder.tvName.setText(group.getGroupName());
        holder.tvDescription.setText(group.getDescription());
        holder.tvMembers.setText(group.getMemberCount() + " members");

        boolean isMember = currentUserId != null && group.isMember(currentUserId);

        if (isMember) {
            holder.btnJoin.setVisibility(View.GONE);
            holder.btnLeave.setVisibility(View.VISIBLE);
        } else {
            holder.btnJoin.setVisibility(View.VISIBLE);
            holder.btnLeave.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onGroupClick(group));

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
        return groupList.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription, tvMembers;
        Button btnJoin, btnLeave;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_group_name);
            tvDescription = itemView.findViewById(R.id.tv_group_description);
            tvMembers = itemView.findViewById(R.id.tv_member_count);
            btnJoin = itemView.findViewById(R.id.btn_join_group);
            btnLeave = itemView.findViewById(R.id.btn_leave_group);
        }
    }
}
