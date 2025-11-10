package micheal.must.signuplogin.adapters;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;
import java.util.List;

import micheal.must.signuplogin.R;
import micheal.must.signuplogin.models.JournalEntry;

public class JournalEntryAdapter extends RecyclerView.Adapter<JournalEntryAdapter.ViewHolder> {

    private final List<JournalEntry> entries;
    private final OnEntryActionListener listener;

    public interface OnEntryActionListener {
        void onAction(JournalEntry entry, String action);
    }

    public JournalEntryAdapter(List<JournalEntry> entries, OnEntryActionListener listener) {
        this.entries = entries;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_journal_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JournalEntry entry = entries.get(position);

        holder.tvTitle.setText(entry.getTitle());
        holder.tvContent.setText(entry.getContent());

        // Format date
        Date date = new Date(entry.getCreatedAt());
        CharSequence dateText = DateFormat.format("MMM d, yyyy", date);
        holder.tvDate.setText(dateText);

        // Set favorite button state
        updateFavoriteButtonState(holder.btnFavorite, entry.isFavorite());

        // Click listeners
        holder.btnFavorite.setOnClickListener(v -> {
            listener.onAction(entry, "favorite");
            // Update button immediately for visual feedback
            entry.setFavorite(!entry.isFavorite());
            updateFavoriteButtonState(holder.btnFavorite, entry.isFavorite());
        });

        holder.btnArchive.setOnClickListener(v -> 
                listener.onAction(entry, "archive"));
    }

    private void updateFavoriteButtonState(ImageButton button, boolean isFavorite) {
        button.setImageResource(
                isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite
        );
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvDate;
        ImageButton btnFavorite, btnArchive;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_entry_title);
            tvContent = itemView.findViewById(R.id.tv_entry_content);
            tvDate = itemView.findViewById(R.id.tv_entry_date);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
            btnArchive = itemView.findViewById(R.id.btn_archive);
        }
    }
}
