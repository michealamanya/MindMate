package micheal.must.signuplogin.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import micheal.must.signuplogin.R;
import micheal.must.signuplogin.models.ChatSession;
import micheal.must.signuplogin.services.ChatHistoryManager;
import micheal.must.signuplogin.services.ContentModerationService;
import micheal.must.signuplogin.services.HuggingFaceApiService;

public class ChatBotFragment extends Fragment {

    private static final String TAG = "ChatBotFragment";

    private EditText etChatInput;
    private ImageButton btnSendMessage;
    private RecyclerView rvChatMessages;
    private ProgressBar progressBar;
    private ChatMessageAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private StringBuilder conversationHistory;

    private ChatHistoryManager historyManager;
    private ChatSession currentSession;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chatbot, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeHistoryManager();
        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadOrCreateSession();
    }

    /**
     * Initialize chat history manager
     */
    private void initializeHistoryManager() {
        historyManager = new ChatHistoryManager(requireContext());
    }

    private void initViews(View view) {
        etChatInput = view.findViewById(R.id.et_chat_input);
        btnSendMessage = view.findViewById(R.id.btn_send_message);
        rvChatMessages = view.findViewById(R.id.rv_chat_messages);
        progressBar = view.findViewById(R.id.progress_bar);

        chatMessages = new ArrayList<>();
        conversationHistory = new StringBuilder();
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatMessageAdapter(chatMessages);
        rvChatMessages.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvChatMessages.setAdapter(chatAdapter);
    }

    private void setupClickListeners() {
        btnSendMessage.setOnClickListener(v -> sendMessage());
        
        etChatInput.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    /**
     * Load current session or create new one
     */
    private void loadOrCreateSession() {
        currentSession = historyManager.getCurrentSession();
        
        if (currentSession == null) {
            // Create new session
            currentSession = historyManager.createNewSession("New Chat");
        } else {
            // Load existing session
            loadSessionMessages();
        }
    }

    /**
     * Load messages from current session
     */
    private void loadSessionMessages() {
        if (currentSession == null) return;
        
        chatMessages.clear();
        conversationHistory.setLength(0);
        
        for (ChatSession.ChatMessage msg : currentSession.getMessages()) {
            chatMessages.add(new ChatMessage(msg.text, msg.isUser));
            conversationHistory.append(msg.text).append("\n");
        }
        
        chatAdapter.notifyDataSetChanged();
        
        if (!chatMessages.isEmpty()) {
            rvChatMessages.scrollToPosition(chatMessages.size() - 1);
        }
        
        Log.d(TAG, "✓ Loaded " + chatMessages.size() + " messages from session");
    }

    /**
     * Start a new chat session
     */
    private void startNewChat() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("New Chat")
                .setMessage("Start a new chat session?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    currentSession = historyManager.createNewSession("New Chat");
                    chatMessages.clear();
                    conversationHistory.setLength(0);
                    chatAdapter.notifyDataSetChanged();
                    Toast.makeText(requireContext(), "New chat started", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void sendMessage() {
        String userMessage = etChatInput.getText().toString().trim();

        if (userMessage.isEmpty()) {
            Toast.makeText(requireContext(), "Please type a message", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check content moderation
        if (!ContentModerationService.isModerationPassed(userMessage)) {
            Toast.makeText(requireContext(), ContentModerationService.getModerationFailureMessage(), Toast.LENGTH_LONG).show();
            etChatInput.setText("");
            return;
        }

        // Create chat message
        ChatMessage userChatMessage = new ChatMessage(userMessage, true);
        chatMessages.add(userChatMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        rvChatMessages.scrollToPosition(chatMessages.size() - 1);

        // Save to session
        ChatSession.ChatMessage sessionMsg = new ChatSession.ChatMessage(userMessage, true);
        currentSession.addMessage(sessionMsg);

        // Update title if first message
        if (currentSession.getMessages().size() == 1) {
            String title = historyManager.generateTitleFromMessage(userMessage);
            currentSession.setTitle(title);
        }

        etChatInput.setText("");
        progressBar.setVisibility(View.VISIBLE);
        btnSendMessage.setEnabled(false);

        // Check network
        if (!isNetworkConnected()) {
            progressBar.setVisibility(View.GONE);
            btnSendMessage.setEnabled(true);
            chatMessages.add(new ChatMessage("You're offline. Using local responses...", false));
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            rvChatMessages.scrollToPosition(chatMessages.size() - 1);
            return;
        }

        // Get response
        new Thread(() -> {
            String botResponse = HuggingFaceApiService.getChatResponse(userMessage, conversationHistory.toString());
            
            requireActivity().runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                btnSendMessage.setEnabled(true);

                ChatMessage botMessage = new ChatMessage(botResponse, false);
                chatMessages.add(botMessage);
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                rvChatMessages.scrollToPosition(chatMessages.size() - 1);

                // Save bot response to session
                ChatSession.ChatMessage botSessionMsg = new ChatSession.ChatMessage(botResponse, false);
                currentSession.addMessage(botSessionMsg);
                historyManager.saveSession(currentSession);

                conversationHistory.append(userMessage).append("\n").append(botResponse).append("\n");
            });
        }).start();
    }

    /**
     * Check if network is connected
     */
    private boolean isNetworkConnected() {
        try {
            android.net.ConnectivityManager cm = (android.net.ConnectivityManager) requireContext()
                    .getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
            
            if (cm != null) {
                android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error checking network: " + e.getMessage());
            return true;
        }
    }

    /**
     * Model for chat messages
     */
    public static class ChatMessage {
        public String text;
        public boolean isUser;

        public ChatMessage(String text, boolean isUser) {
            this.text = text;
            this.isUser = isUser;
        }
    }

    /**
     * Adapter for chat messages
     */
    private static class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ViewHolder> {
        private final List<ChatMessage> messages;

        public ChatMessageAdapter(List<ChatMessage> messages) {
            this.messages = messages;
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
            holder.bindMessage(message);
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            private android.widget.TextView tvSenderName;
            private android.widget.TextView tvMessageContent;
            private android.widget.TextView tvMessageTime;
            private android.widget.LinearLayout messageContainer;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvSenderName = itemView.findViewById(R.id.tv_sender_name);
                tvMessageContent = itemView.findViewById(R.id.tv_message_content);
                tvMessageTime = itemView.findViewById(R.id.tv_message_time);
                messageContainer = itemView.findViewById(R.id.message_container);
            }

            public void bindMessage(ChatMessage message) {
                if (tvMessageContent != null) {
                    tvMessageContent.setText(message.text);
                }

                if (tvSenderName != null) {
                    tvSenderName.setText(message.isUser ? "You" : "MindMate AI");
                }

                if (tvMessageTime != null) {
                    tvMessageTime.setText(java.text.SimpleDateFormat.getTimeInstance(java.text.SimpleDateFormat.SHORT).format(new java.util.Date()));
                }

                if (messageContainer != null) {
                    if (message.isUser) {
                        messageContainer.setBackgroundColor(android.graphics.Color.parseColor("#6200EE"));
                        if (tvMessageContent != null) {
                            tvMessageContent.setTextColor(android.graphics.Color.WHITE);
                        }
                        messageContainer.setGravity(android.view.Gravity.END);
                    } else {
                        messageContainer.setBackgroundColor(android.graphics.Color.parseColor("#F0F0F0"));
                        if (tvMessageContent != null) {
                            tvMessageContent.setTextColor(android.graphics.Color.BLACK);
                        }
                        messageContainer.setGravity(android.view.Gravity.START);
                    }
                }
            }
        }
    }
}