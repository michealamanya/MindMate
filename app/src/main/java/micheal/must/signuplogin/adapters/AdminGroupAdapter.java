package micheal.must.signuplogin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import micheal.must.signuplogin.R;
import micheal.must.signuplogin.models.Group;

public class AdminGroupAdapter extends RecyclerView.Adapter<AdminGroupAdapter.ViewHolder> {
    private List<Group> groups;
    private OnGroupActionListener listener;

    public interface OnGroupActionListener {
        void onDeleteGroup(Group group, int position);
        void onViewMembers(Group group);
    }

    public AdminGroupAdapter(List<Group> groups) {
        this.groups = groups;
    }

    public void setOnGroupActionListener(OnGroupActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Group group = groups.get(position);

        holder.tvGroupName.setText(group.getGroupName() != null ? group.getGroupName() : "Unknown Group");
        holder.tvDescription.setText(group.getDescription() != null ? group.getDescription() : "No description");
        holder.tvMembers.setText("Members: " + group.getMemberCount());
        holder.tvCreatedBy.setText("Created by: " + (group.getCreatedBy() != null ? group.getCreatedBy() : "Unknown"));

        holder.btnViewMembers.setOnClickListener(v -> {
            if (listener != null) listener.onViewMembers(group);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteGroup(group, position);
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroupName, tvDescription, tvMembers, tvCreatedBy;
        Button btnViewMembers, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tv_group_name);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvMembers = itemView.findViewById(R.id.tv_members);
            tvCreatedBy = itemView.findViewById(R.id.tv_created_by);
            btnViewMembers = itemView.findViewById(R.id.btn_view_members);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
