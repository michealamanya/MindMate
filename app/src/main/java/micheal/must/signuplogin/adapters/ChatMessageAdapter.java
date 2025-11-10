package micheal.must.signuplogin.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import micheal.must.signuplogin.R;
import micheal.must.signuplogin.models.ChatMessage;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ViewHolder> {
    private List<ChatMessage> messages;
    private String currentUserId;

    public ChatMessageAdapter(List<ChatMessage> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        holder.tvSenderName.setText(message.getSenderName());
        holder.tvContent.setText(message.getMessageText());

        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String timeText = sdf.format(new Date(message.getTimestamp()));
        holder.tvTimestamp.setText(timeText);

        // Style message based on sender
        if (message.getSenderId().equals(currentUserId)) {
            holder.messageContainer.setBackgroundColor(Color.parseColor("#E3F2FD"));
        } else {
            holder.messageContainer.setBackgroundColor(Color.parseColor("#F5F5F5"));
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSenderName, tvContent, tvTimestamp;
        LinearLayout messageContainer;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSenderName = itemView.findViewById(R.id.tv_sender_name);
            tvContent = itemView.findViewById(R.id.tv_message_content);
            tvTimestamp = itemView.findViewById(R.id.tv_message_time);
            messageContainer = itemView.findViewById(R.id.message_container);
        }
    }
}
