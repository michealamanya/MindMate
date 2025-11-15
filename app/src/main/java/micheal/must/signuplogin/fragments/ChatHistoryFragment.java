package micheal.must.signuplogin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import micheal.must.signuplogin.R;
import micheal.must.signuplogin.models.ChatSession;
import micheal.must.signuplogin.services.ChatHistoryManager;

public class ChatHistoryFragment extends Fragment {

    private RecyclerView rvChatHistory;
    private TextView tvNoChatHistory;
    private MaterialButton btnClearAll;
    private MaterialButton btnNewChat;
    private ChatHistoryManager historyManager;
    private ChatHistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeHistoryManager();
        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadChatHistory();
    }

    private void initializeHistoryManager() {
        historyManager = new ChatHistoryManager(requireContext());
    }

    private void initViews(View view) {
        rvChatHistory = view.findViewById(R.id.rv_chat_history);
        tvNoChatHistory = view.findViewById(R.id.tv_no_chat_history);
        btnClearAll = view.findViewById(R.id.btn_clear_all);
        btnNewChat = view.findViewById(R.id.btn_new_chat);
    }

    private void setupRecyclerView() {
        rvChatHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ChatHistoryAdapter(requireContext(), historyManager, this::loadChatHistory);
        rvChatHistory.setAdapter(adapter);
    }

    private void setupClickListeners() {
        if (btnClearAll != null) {
            btnClearAll.setOnClickListener(v -> showClearAllDialog());
        }
        
        if (btnNewChat != null) {
            btnNewChat.setOnClickListener(v -> startNewChat());
        }
    }

    /**
     * Start a new chat session
     */
    private void startNewChat() {
        new AlertDialog.Builder(requireContext())
                .setTitle("New Chat")
                .setMessage("Start a new chat session?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    ChatSession newSession = historyManager.createNewSession("New Chat");
                    historyManager.setCurrentSession(newSession.getSessionId());
                    loadChatHistory();
                    Toast.makeText(requireContext(), "New chat started", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Load all chat sessions
     */
    private void loadChatHistory() {
        List<ChatSession> sessions = historyManager.getAllSessions();

        if (sessions.isEmpty()) {
            rvChatHistory.setVisibility(View.GONE);
            tvNoChatHistory.setVisibility(View.VISIBLE);
        } else {
            rvChatHistory.setVisibility(View.VISIBLE);
            tvNoChatHistory.setVisibility(View.GONE);
            adapter.updateSessions(sessions);
        }
    }

    /**
     * Show clear all confirmation dialog
     */
    private void showClearAllDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Clear All Chats?")
                .setMessage("This will delete all chat history and cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    historyManager.clearAllSessions();
                    loadChatHistory();
                    Toast.makeText(requireContext(), "All chats cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Adapter for chat history list
     */
    public static class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.ViewHolder> {
        private final android.content.Context context;
        private List<ChatSession> sessions;
        private final ChatHistoryManager historyManager;
        private final Runnable onRefresh;

        public ChatHistoryAdapter(android.content.Context context, ChatHistoryManager historyManager, Runnable onRefresh) {
            this.context = context;
            this.historyManager = historyManager;
            this.onRefresh = onRefresh;
        }

        public void updateSessions(List<ChatSession> newSessions) {
            this.sessions = newSessions;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChatSession session = sessions.get(position);
            holder.bind(session, historyManager, onRefresh);
        }

        @Override
        public int getItemCount() {
            return sessions != null ? sessions.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView tvTitle;
            private final TextView tvMessageCount;
            private final TextView tvLastModified;
            private final MaterialButton btnOpen;
            private final MaterialButton btnDelete;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_chat_title);
                tvMessageCount = itemView.findViewById(R.id.tv_message_count);
                tvLastModified = itemView.findViewById(R.id.tv_last_modified);
                btnOpen = itemView.findViewById(R.id.btn_open_chat);
                btnDelete = itemView.findViewById(R.id.btn_delete_chat);
            }

            public void bind(ChatSession session, ChatHistoryManager historyManager, Runnable onRefresh) {
                tvTitle.setText(session.getTitle());
                tvMessageCount.setText(session.getMessages().size() + " messages");
                tvLastModified.setText("Last: " + formatDate(session.getLastModifiedAt()));

                btnOpen.setOnClickListener(v -> {
                    historyManager.setCurrentSession(session.getSessionId());
                    Toast.makeText(itemView.getContext(), "Opened: " + session.getTitle(), Toast.LENGTH_SHORT).show();
                });

                btnDelete.setOnClickListener(v -> {
                    new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Delete Chat?")
                            .setMessage("Delete \"" + session.getTitle() + "\"?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                historyManager.deleteSession(session.getSessionId());
                                onRefresh.run();
                                Toast.makeText(itemView.getContext(), "Chat deleted", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });
            }

            private String formatDate(long timestamp) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
                return sdf.format(new Date(timestamp));
            }
        }
    }
}
