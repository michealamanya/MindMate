package micheal.must.signuplogin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;

import java.util.List;

import micheal.must.signuplogin.R;
import micheal.must.signuplogin.models.User;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    private final List<User> users;
    private OnUserActionListener actionListener;

    public interface OnUserActionListener {
        void onPromoteToAdmin(User user, int position);
        void onRevokeAdminStatus(User user, int position);
        void onBanUser(User user, int position);
    }

    public AdminUserAdapter(List<User> users) {
        this.users = users;
    }

    public void setOnUserActionListener(OnUserActionListener listener) {
        this.actionListener = listener;
    }

    // Add this helper to update adapter data safely
    public void updateData(List<User> newUsers) {
        users.clear();
        if (newUsers != null && !newUsers.isEmpty()) {
            users.addAll(newUsers);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);

        // Set user info
        holder.tvUserName.setText(user.getDisplayName());
        holder.tvUserEmail.setText(user.getEmail());

        // Load profile image if available
        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.default_avatar)
                    .circleCrop()
                    .into(holder.ivUserPhoto);
        } else {
            holder.ivUserPhoto.setImageResource(R.drawable.default_avatar);
        }

        // Show admin/moderator status
        holder.chipAdmin.setVisibility(user.isAdmin() ? View.VISIBLE : View.GONE);
        holder.chipModerator.setVisibility(user.isModerator() ? View.VISIBLE : View.GONE);

        // Setup action buttons
        if (user.isAdmin()) {
            holder.btnPromote.setVisibility(View.GONE);
            holder.btnRevoke.setVisibility(View.VISIBLE);
        } else {
            holder.btnPromote.setVisibility(View.VISIBLE);
            holder.btnRevoke.setVisibility(View.GONE);
        }

        // Set button click listeners
        holder.btnPromote.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onPromoteToAdmin(user, holder.getAdapterPosition());
            }
        });

        holder.btnRevoke.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onRevokeAdminStatus(user, holder.getAdapterPosition());
            }
        });

        holder.btnBan.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onBanUser(user, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserPhoto;
        TextView tvUserName, tvUserEmail;
        Chip chipAdmin, chipModerator;
        Button btnPromote, btnRevoke, btnBan;

        UserViewHolder(View itemView) {
            super(itemView);
            ivUserPhoto = itemView.findViewById(R.id.iv_user_photo);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvUserEmail = itemView.findViewById(R.id.tv_user_email);
            chipAdmin = itemView.findViewById(R.id.chip_admin);
            chipModerator = itemView.findViewById(R.id.chip_moderator);
            btnPromote = itemView.findViewById(R.id.btn_promote);
            btnRevoke = itemView.findViewById(R.id.btn_revoke);
            btnBan = itemView.findViewById(R.id.btn_ban);
        }
    }
}
