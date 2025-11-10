package micheal.must.signuplogin.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import micheal.must.signuplogin.ChatActivity;
import micheal.must.signuplogin.R;
import micheal.must.signuplogin.adapters.MessageAdapter;
import micheal.must.signuplogin.models.Message;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatBotFragment extends Fragment {

    private static final String TAG = "ChatBotFragment";

    // Google Gemini API Configuration
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";
    private static final String GEMINI_API_KEY = "AIzaSyBff0qNjK_SJVn1PiS1IYBVd3FlGhOAER4";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private RecyclerView rvMessages;
    private EditText etMessage;
    private MaterialButton btnSend;
    private Chip chipFeelingAnxious, chipNeedMotivation, chipSleepHelp, chipFeelingDown;

    private List<Message> messageList;
    private MessageAdapter messageAdapter;
    private final OkHttpClient client = new OkHttpClient();
    private final Executor executor = Executors.newSingleThreadExecutor();

    private MentalHealthResponseEngine responseEngine;
    private boolean useOnlineAPI = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_bot, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupClickListeners();

        responseEngine = new MentalHealthResponseEngine();
        receiveAiMessage("Hello! I'm here to support you. How are you feeling today? 🌻\n\nFeel free to share what's on your mind or tap one of the quick response buttons below.");
    }

    private void initViews(View view) {
        rvMessages = view.findViewById(R.id.rv_messages);
        etMessage = view.findViewById(R.id.et_message);
        btnSend = view.findViewById(R.id.btn_send);

        chipFeelingAnxious = view.findViewById(R.id.chip_feeling_anxious);
        chipNeedMotivation = view.findViewById(R.id.chip_need_motivation);
        chipSleepHelp = view.findViewById(R.id.chip_sleep_help);
        chipFeelingDown = view.findViewById(R.id.chip_feeling_down);
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(getContext(), messageList);
        rvMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMessages.setAdapter(messageAdapter);
    }

    private void setupClickListeners() {
        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                if (!message.isEmpty()) {
                    sendMessage(message);
                    etMessage.setText("");
                }
            }
        });

        chipFeelingAnxious.setOnClickListener(v -> sendQuickResponse("I'm feeling anxious."));
        chipNeedMotivation.setOnClickListener(v -> sendQuickResponse("I need some motivation."));
        chipSleepHelp.setOnClickListener(v -> sendQuickResponse("I need help with sleep."));
        chipFeelingDown.setOnClickListener(v -> sendQuickResponse("I'm feeling down today."));
    }

    private void sendMessage(String messageText) {
        Message userMessage = new Message(messageText, Message.SENT_BY_USER);
        messageAdapter.addMessage(userMessage);
        rvMessages.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        generateAiResponse(messageText);
    }

    private void sendQuickResponse(String response) {
        sendMessage(response);
    }

    private void generateAiResponse(String userInput) {
        getActivity().runOnUiThread(() -> receiveAiMessage("Thinking... 🤔"));

        if (useOnlineAPI) {
            generateGeminiResponse(userInput);
        } else {
            generateOfflineResponse(userInput);
        }
    }

    private void generateGeminiResponse(String userInput) {
        executor.execute(() -> {
            try {
                JSONObject jsonBody = new JSONObject();
                JSONArray contentsArray = new JSONArray();
                JSONObject contentObject = new JSONObject();
                JSONArray partsArray = new JSONArray();
                JSONObject partObject = new JSONObject();

                String enhancedPrompt = "You are a compassionate mental health support assistant. " +
                        "Provide empathetic, supportive, and helpful responses. " +
                        "Keep responses concise (2-3 sentences). " +
                        "User says: " + userInput;

                partObject.put("text", enhancedPrompt);
                partsArray.put(partObject);
                contentObject.put("parts", partsArray);
                contentsArray.put(contentObject);
                jsonBody.put("contents", contentsArray);

                JSONObject generationConfig = new JSONObject();
                generationConfig.put("temperature", 0.7);
                generationConfig.put("maxOutputTokens", 200);
                jsonBody.put("generationConfig", generationConfig);

                String urlWithKey = GEMINI_API_URL + "?key=" + GEMINI_API_KEY;
                RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
                Request request = new Request.Builder()
                        .url(urlWithKey)
                        .addHeader("Content-Type", "application/json")
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "Gemini API call failed, falling back to offline mode", e);
                        getActivity().runOnUiThread(() -> {
                            messageAdapter.removeLastMessage();
                            generateOfflineResponse(userInput);
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            if (!response.isSuccessful()) {
                                throw new IOException("Unexpected response code: " + response);
                            }

                            String responseBody = response.body().string();
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            JSONArray candidates = jsonResponse.getJSONArray("candidates");
                            if (candidates.length() > 0) {
                                JSONObject candidate = candidates.getJSONObject(0);
                                JSONObject content = candidate.getJSONObject("content");
                                JSONArray parts = content.getJSONArray("parts");
                                if (parts.length() > 0) {
                                    String text = parts.getJSONObject(0).getString("text");
                                    getActivity().runOnUiThread(() -> {
                                        messageAdapter.removeLastMessage();
                                        receiveAiMessage(text.trim());
                                    });
                                } else {
                                    throw new IOException("No parts in response");
                                }
                            } else {
                                throw new IOException("No candidates in response");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing Gemini response, using offline mode", e);
                            getActivity().runOnUiThread(() -> {
                                messageAdapter.removeLastMessage();
                                generateOfflineResponse(userInput);
                            });
                        }
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error creating Gemini request, using offline mode", e);
                getActivity().runOnUiThread(() -> {
                    messageAdapter.removeLastMessage();
                    generateOfflineResponse(userInput);
                });
            }
        });
    }

    private void generateOfflineResponse(String userInput) {
        executor.execute(() -> {
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String response = responseEngine.generateResponse(userInput);
            getActivity().runOnUiThread(() -> {
                if (messageAdapter.getItemCount() > 0 &&
                        messageAdapter.getLastMessage().getContent().contains("Thinking")) {
                    messageAdapter.removeLastMessage();
                }
                receiveAiMessage(response);
            });
        });
    }

    private void receiveAiMessage(String messageText) {
        Message aiMessage = new Message(messageText, Message.SENT_BY_AI);
        messageAdapter.addMessage(aiMessage);
        rvMessages.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
    }

    private static class MentalHealthResponseEngine {
        private final Map<String, List<String>> responseDatabase;
        private final List<String> generalSupport;
        private final Random random;

        public MentalHealthResponseEngine() {
            this.random = new Random();
            this.responseDatabase = new HashMap<>();
            this.generalSupport = new ArrayList<>();
            initializeResponses();
        }

        private void initializeResponses() {
            // Anxiety responses
            List<String> anxietyResponses = new ArrayList<>();
            anxietyResponses.add("I understand anxiety can be overwhelming. Try taking a few deep breaths—in through your nose for 4 seconds, hold for 7, and out through your mouth for 8. How are you feeling right now? 🌬️");
            anxietyResponses.add("Anxiety is difficult, but you're not alone. Ground yourself by naming 5 things you can see, 4 you can touch, 3 you can hear, 2 you can smell, and 1 you can taste. How are you feeling right now? 💙");
            anxietyResponses.add("I hear you. When anxiety strikes, remember it's temporary. Try progressive muscle relaxation - tense and release each muscle group. What specific situation is causing you stress? 🌿");
            anxietyResponses.add("Feeling anxious is valid. Consider stepping outside for fresh air or doing a quick 5-minute walk. Movement can help calm your nervous system. What usually helps you feel better? 🍃");
            responseDatabase.put("anxiety", anxietyResponses);

            // Depression/feeling down responses
            List<String> depressionResponses = new ArrayList<>();
            depressionResponses.add("I'm sorry you're feeling down. Remember that it's okay to not be okay. Small steps matter - even getting out of bed is an accomplishment. What's one small thing you could do for yourself today? 💜");
            depressionResponses.add("Feeling down is tough, and I want you to know your feelings are valid. Consider reaching out to someone you trust. Sometimes connection helps. Is there someone you'd feel comfortable talking to? 🌻");
            depressionResponses.add("When we're feeling low, self-compassion is crucial. Speak to yourself like you would to a friend. What's one thing that brought you even a moment of peace recently? 🌙");
            depressionResponses.add("I understand you're struggling. Depression can make everything feel heavy. Try to do one activity you normally enjoy, even if you don't feel like it. What activities used to bring you joy? ☀️");
            responseDatabase.put("depression", depressionResponses);

            // Motivation responses
            List<String> motivationResponses = new ArrayList<>();
            motivationResponses.add("You've got this! Remember, progress isn't always linear. Celebrate small wins - they add up to big achievements. What's one small goal you can set for today? 🎯");
            motivationResponses.add("Motivation can be elusive, but action often creates motivation, not the other way around. Start with just 5 minutes of whatever you need to do. What's the first tiny step you can take? 💪");
            motivationResponses.add("You're stronger than you think! Break your task into smaller pieces. Focus on just the next step, not the entire journey. What's one thing you'd like to accomplish? ⭐");
            motivationResponses.add("Remember why you started. Visualize how you'll feel when you accomplish your goal. You're capable of amazing things! What's driving you right now? 🚀");
            responseDatabase.put("motivation", motivationResponses);

            // Sleep help responses
            List<String> sleepResponses = new ArrayList<>();
            sleepResponses.add("Sleep is so important. Try creating a bedtime routine: dim lights 30 minutes before bed, avoid screens, and try reading or gentle stretching. What time do you usually try to sleep? 🌙");
            sleepResponses.add("Sleep difficulties are common. Consider the 4-7-8 breathing technique: breathe in for 4, hold for 7, exhale for 8. This activates your parasympathetic nervous system. How long have you been having trouble sleeping? 😴");
            sleepResponses.add("Good sleep hygiene helps: keep your room cool, dark, and quiet. Avoid caffeine after 2pm. Try a body scan meditation in bed. What do you think might be interfering with your sleep? 🌃");
            sleepResponses.add("If your mind races at night, try keeping a notebook by your bed to jot down thoughts. Progressive muscle relaxation can also help. What thoughts typically keep you awake? 💤");
            responseDatabase.put("sleep", sleepResponses);

            // Stress responses
            List<String> stressResponses = new ArrayList<>();
            stressResponses.add("Stress is challenging. Remember to take breaks and prioritize self-care. Even 5 minutes of deep breathing can help reset your nervous system. What's your main source of stress right now? 🌊");
            stressResponses.add("When stressed, our bodies need support. Make sure you're eating regularly, staying hydrated, and moving your body. What usually helps you de-stress? 🍵");
            stressResponses.add("Chronic stress affects us physically and mentally. Consider delegating tasks if possible, and practice saying 'no' to protect your energy. Can you identify what's most urgent vs. important? 📋");
            responseDatabase.put("stress", stressResponses);

            // Self-care responses
            List<String> selfCareResponses = new ArrayList<>();
            selfCareResponses.add("Self-care isn't selfish! It can be as simple as drinking water, taking a shower, or stepping outside. What's one form of self-care you can do right now? 🌺");
            selfCareResponses.add("Remember: self-care includes saying no, setting boundaries, and resting without guilt. You deserve care and compassion. What makes you feel most cared for? 💝");
            selfCareResponses.add("Self-care looks different for everyone. It might be exercise, creativity, solitude, or socializing. What recharges your battery? 🔋");
            responseDatabase.put("self-care", selfCareResponses);

            // Gratitude/positive responses
            List<String> gratitudeResponses = new ArrayList<>();
            gratitudeResponses.add("That's wonderful! Gratitude is powerful for mental health. Taking time to notice the good things, even small ones, can shift our perspective. What else are you grateful for today? 🌟");
            gratitudeResponses.add("I'm so glad to hear that! Celebrating wins, big and small, is important. Keep building on this positive momentum! 🎉");
            gratitudeResponses.add("That's great! Research shows that gratitude practices can improve mood and wellbeing. Consider keeping a gratitude journal. ✨");
            responseDatabase.put("gratitude", gratitudeResponses);

            // General supportive responses
            generalSupport.add("I'm here to listen. Your feelings are valid, and you don't have to go through this alone. What's on your mind? 💙");
            generalSupport.add("Thank you for sharing that with me. It takes courage to talk about how we're feeling. How can I best support you right now? 🌸");
            generalSupport.add("I hear you, and your experience matters. Would you like to tell me more about what you're going through? 🤗");
            generalSupport.add("You're taking an important step by reaching out. Mental health is health, and seeking support is a sign of strength. What would be most helpful for you right now? 💪");
            generalSupport.add("I appreciate you opening up. Remember, difficult emotions are temporary, and you have the strength to navigate them. What's one thing that usually helps you feel better? 🌈");
            generalSupport.add("That sounds challenging. Remember to be gentle with yourself - healing isn't linear. What kind of support are you looking for today? 🕊️");
        }

        public String generateResponse(String userInput) {
            String input = userInput.toLowerCase();

            // Check for specific keywords and return appropriate responses
            if (containsKeywords(input, "anxious", "anxiety", "nervous", "worried", "panic", "fear")) {
                return getRandomResponse("anxiety");
            } else if (containsKeywords(input, "sad", "depressed", "down", "hopeless", "empty", "lonely")) {
                return getRandomResponse("depression");
            } else if (containsKeywords(input, "motivation", "motivated", "lazy", "procrastinate", "unmotivated", "stuck")) {
                return getRandomResponse("motivation");
            } else if (containsKeywords(input, "sleep", "insomnia", "tired", "exhausted", "can't sleep", "restless")) {
                return getRandomResponse("sleep");
            } else if (containsKeywords(input, "hello", "hi", "hey", "greetings")) {
                return "Hello! I'm here to support you. How are you feeling today? Feel free to share what's on your mind. 🌻";
            } else if (containsKeywords(input, "help", "support", "advice")) {
                return "I'm here to help! You can talk to me about anxiety, stress, sleep issues, motivation, or anything else on your mind. What would you like to discuss? 💬";
            } else if (containsKeywords(input, "bye", "goodbye", "see you")) {
                return "Take care of yourself! Remember, I'm here whenever you need support. You're doing great by prioritizing your mental health. 🌟";
            } else {
                return getRandomResponse("general");
            }
        }

        private boolean containsKeywords(String input, String... keywords) {
            for (String keyword : keywords) {
                if (input.contains(keyword.toLowerCase())) {
                    return true;
                }
            }
            return false;
        }

        private String getRandomResponse(String category) {
            if (category.equals("general")) {
                return generalSupport.get(random.nextInt(generalSupport.size()));
            }

            List<String> responses = responseDatabase.get(category);
            if (responses != null && !responses.isEmpty()) {
                return responses.get(random.nextInt(responses.size()));
            }
            return generalSupport.get(random.nextInt(generalSupport.size()));
        }
    }
}