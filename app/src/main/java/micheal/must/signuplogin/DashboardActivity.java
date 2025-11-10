package micheal.must.signuplogin;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.os.Looper;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import micheal.must.signuplogin.fragments.ChatFragment;
import micheal.must.signuplogin.fragments.CommunityFragment;
import micheal.must.signuplogin.fragments.JournalFragment;
import micheal.must.signuplogin.fragments.MoreFragment;

public class DashboardActivity extends AppCompatActivity {

    // UI Components
    private TextView tvGreeting, tvGreetingSubtext, tvDailyQuote;
    private TextView tvMoodScore, tvTipsCount, tvSessionsTime;
    private ShapeableImageView ivProfile, ivSettings;
    private CardView chatbotCard, moodCard, tipsCard, sessionCard;
    private MaterialButton btnCheckin, btnJournal, btnMeditation, btnResources;
    private RecyclerView rvRecommended;
    private BottomNavigationView bottomNavigation;
    private LinearProgressIndicator moodProgress;
    private FloatingActionButton fabCaptureMood;
    private ShapeableImageView ivMoodImage;

    // Data
    private List<RecommendedItem> recommendedItems;

    // Mood detection with TensorFlow Lite
    private static final String TAG = "MoodDetection";
    private static final String MODEL_PATH = "model.tflite";
    private static final String LABELS_PATH = "labels.txt";
    private Interpreter tflite;
    private List<String> labels;
    private int modelInputWidth = 224;
    private int modelInputHeight = 224;

    // Image capture
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    // Face detector
    private FaceDetector faceDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        // Load and apply saved font preference BEFORE setting content view
        applyGlobalFont();
        
        setContentView(R.layout.activity_dashboard);

        // Initialize UI components
        initViews();

        // Initialize TensorFlow Lite model asynchronously to avoid blocking UI thread
        loadModelAsync();

        // Initialize activity result launchers
        initActivityResultLaunchers();

        // Set up greeting based on time of day
        setGreeting();
        
        // Load and display user profile picture
        loadUserProfilePicture();

        // Load motivational quote
        loadDailyQuote();

        // Set up click listeners
        setupClickListeners();

        // Set up bottom navigation
        setupBottomNavigation();

        // Load recommended items
        loadRecommendedItems();

        // Initialize statistics
        initializeStats();
        
        // Add smooth scrolling animations
        setupScrollAnimations();
        
        // Add entrance animation to dashboard
        addEntranceAnimation();

        // Initialize face detector
        initializeFaceDetector();
    }

    /**
     * Setup smooth scroll animations for recommended section
     */
    private void setupScrollAnimations() {
        if (rvRecommended != null) {
            rvRecommended.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    // Smooth fade effect as user scrolls
                    float alpha = 0.8f + (0.2f * (1 - Math.min(dx, 100) / 100f));
                    recyclerView.setAlpha(alpha);
                }
            });
        }
    }

    private void initViews() {
        // TextViews (use runtime lookup to avoid compile-time failures if some ids were removed)
        tvGreeting = findViewByName("tv_greeting", TextView.class);
        tvGreetingSubtext = findViewByName("tv_greeting_subtext", TextView.class);
        tvDailyQuote = findViewByName("tv_daily_quote", TextView.class);
        tvMoodScore = findViewByName("tv_mood_score", TextView.class);
        tvTipsCount = findViewByName("tv_tips_count", TextView.class);
        tvSessionsTime = findViewByName("tv_sessions_time", TextView.class);

        // ImageViews
        ivProfile = findViewByName("iv_profile", ShapeableImageView.class);
        ivSettings = findViewByName("iv_settings", ShapeableImageView.class);
        ivMoodImage = findViewByName("iv_mood_image", ShapeableImageView.class);

        // Cards (may be missing after layout changes)
        chatbotCard = findViewByName("chatbot_card", CardView.class);
        moodCard = findViewByName("mood_card", CardView.class);
        tipsCard = findViewByName("tips_card", CardView.class);
        sessionCard = findViewByName("session_card", CardView.class);

        // Buttons (may be missing)
        btnCheckin = findViewByName("button_checkin", MaterialButton.class);
        btnJournal = findViewByName("button_journal", MaterialButton.class);
        btnMeditation = findViewByName("button_meditation", MaterialButton.class);
        btnResources = findViewByName("button_resources", MaterialButton.class);
        fabCaptureMood = findViewByName("fab_capture_mood", FloatingActionButton.class);

        // Progress indicators
        moodProgress = findViewByName("mood_progress", LinearProgressIndicator.class);

        // RecyclerView
        rvRecommended = findViewByName("rv_recommended", RecyclerView.class);

        // Bottom Navigation
        bottomNavigation = findViewByName("bottom_navigation", BottomNavigationView.class);
    }

    /**
     * Lookup view id by name at runtime to avoid compile-time R.id references for removed ids.
     */
    private <T extends View> T findViewByName(String name, Class<T> cls) {
        try {
            int id = getResources().getIdentifier(name, "id", getPackageName());
            if (id != 0) {
                View v = findViewById(id);
                if (v != null && cls.isInstance(v)) {
                    return cls.cast(v);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "findViewByName failed for: " + name, e);
        }
        return null;
    }

    private void initializeModel() throws IOException {
        // Load the TFLite model
        MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(this, MODEL_PATH);
        tflite = new Interpreter(tfliteModel);

        // Load labels
        labels = loadLabelsFromAsset(LABELS_PATH);

        Log.d(TAG, "Model loaded successfully with " + labels.size() + " labels");
    }

    private List<String> loadLabelsFromAsset(String filePath) throws IOException {
        List<String> labelList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open(filePath)));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    private void initActivityResultLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        analyzeMood(imageBitmap);
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                            analyzeMood(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Add permission launcher for camera
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Permission granted, launch camera
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        cameraLauncher.launch(cameraIntent);
                    } else {
                        // Permission denied
                        Toast.makeText(this, "Camera permission is required to capture mood", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Load user profile picture from Firebase or local storage
     */
    private void loadUserProfilePicture() {
        if (ivProfile == null) return;
        
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null && auth.getCurrentUser().getPhotoUrl() != null) {
                // Load profile picture from Firebase
                Uri photoUrl = auth.getCurrentUser().getPhotoUrl();
                loadImageWithGlide(photoUrl, ivProfile);
            } else {
                // Check local storage for saved profile picture
                SharedPreferences prefs = getSharedPreferences("user_profile", MODE_PRIVATE);
                String savedPhotoPath = prefs.getString("profile_photo_path", null);
                
                if (savedPhotoPath != null) {
                    loadImageWithGlide(Uri.parse(savedPhotoPath), ivProfile);
                } else {
                    // Keep default avatar if no picture available
                    Log.d(TAG, "No profile picture available, using default avatar");
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Error loading profile picture: " + e.getMessage());
        }
    }

    /**
     * Load image using Glide library (add dependency if not present)
     * Alternative: Use built-in ImageView methods if Glide not available
     */
    private void loadImageWithGlide(Uri imageUri, ImageView imageView) {
        try {
            // Try using Glide if available
            Class.forName("com.bumptech.glide.Glide");
            
            // Using reflection to avoid hard dependency
            Object glide = Class.forName("com.bumptech.glide.Glide")
                    .getMethod("with", android.app.Activity.class)
                    .invoke(null, this);
            
            Object requestBuilder = Class.forName("com.bumptech.glide.RequestBuilder")
                    .getMethod("load", Uri.class)
                    .invoke(glide, imageUri);
            
            Class.forName("com.bumptech.glide.RequestBuilder")
                    .getMethod("circleCrop")
                    .invoke(requestBuilder);
            
            Class.forName("com.bumptech.glide.RequestBuilder")
                    .getMethod("into", android.widget.ImageView.class)
                    .invoke(requestBuilder, imageView);
        } catch (Exception e) {
            // Fallback: Load image directly if Glide not available
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException ex) {
                Log.w(TAG, "Could not load profile picture: " + ex.getMessage());
            }
        }
    }

    /**
     * Add smooth entrance animation to dashboard
     */
    private void addEntranceAnimation() {
        View rootView = findViewById(R.id.dashboard_main);
        if (rootView != null) {
            // Start with subtle scale and fade
            rootView.setAlpha(0f);
            rootView.setScaleX(0.95f);
            rootView.setScaleY(0.95f);
            
            // Animate entrance
            rootView.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(500)
                    .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                    .start();
        }
        
        // Add staggered animations for key elements
        if (tvGreeting != null) {
            tvGreeting.setAlpha(0f);
            tvGreeting.setTranslationY(-20f);
            tvGreeting.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(600)
                    .setStartDelay(100)
                    .setInterpolator(new android.view.animation.OvershootInterpolator())
                    .start();
        }
        
        if (ivProfile != null) {
            ivProfile.setAlpha(0f);
            ivProfile.setScaleX(0.8f);
            ivProfile.setScaleY(0.8f);
            ivProfile.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(500)
                    .setInterpolator(new android.view.animation.BounceInterpolator())
                    .start();
        }
    }

    private void setGreeting() {
        // Get user's name (from intent or Firebase Auth)
        String userName = getUserName();

        // Get the current hour
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (hourOfDay < 12) {
            greeting = "Good Morning, " + userName + "!";
        } else if (hourOfDay < 17) {
            greeting = "Good Afternoon, " + userName + "!";
        } else {
            greeting = "Good Evening, " + userName + "!";
        }

        if (tvGreeting != null) {
            tvGreeting.setText(greeting);
        }
    }

    /**
     * Gets the current user's name from various sources
     * @return The user's first name or "Friend" if not available
     */
    private String getUserName() {
        // Try to get name from intent extras first
        String name = getIntent().getStringExtra("name");

        // If name not in intent, try to get from Firebase Auth
        if (name == null || name.isEmpty()) {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null && auth.getCurrentUser().getDisplayName() != null) {
                name = auth.getCurrentUser().getDisplayName();
            }
        }

        // Extract first name if full name is provided
        if (name != null && !name.isEmpty()) {
            // Split by space and get first part as first name
            String[] parts = name.split(" ");
            return parts[0];
        }

        // Default if no name is found
        return "Friend";
    }

    private void loadDailyQuote() {
        // In a real app, this could come from a remote source or database
        String[] quotes = {
                "Take a deep breath—you've got this.",
                "Every step forward is progress, no matter how small.",
                "Self-care isn't selfish, it's necessary.",
                "Your feelings are valid, but they don't define you.",
                "Today is a new opportunity to grow and heal."
        };

        // Simple random selection
        int randomIndex = (int) (Math.random() * quotes.length);
        tvDailyQuote.setText(quotes[randomIndex]);
    }

    private void initializeStats() {
        // Set mood score emoji based on progress
        if (moodProgress != null) {
            int moodValue = moodProgress.getProgress();
            if (moodValue >= 75) {
                tvMoodScore.setText("😊");
            } else if (moodValue >= 50) {
                tvMoodScore.setText("😐");
            } else {
                tvMoodScore.setText("😔");
            }
        }

        // Update tip count - could be from preferences or remote data
        int newTips = 3; // Example value
        if (tvTipsCount != null) {
            tvTipsCount.setText(newTips + " New");
        }

        // Update session time - could be calculated from user's schedule
        if (tvSessionsTime != null) {
            tvSessionsTime.setText("45:00"); // Example value
        }
        
        // Load mood trends from history
        loadMoodTrends();
    }

    /**
     * Load and display mood trends from saved history
     */
    private void loadMoodTrends() {
        SharedPreferences prefs = getSharedPreferences("mood_history", MODE_PRIVATE);
        
        // Get all saved moods from today
        long currentTime = System.currentTimeMillis();
        long dayInMillis = 24 * 60 * 60 * 1000;
        long startOfDay = currentTime - (currentTime % dayInMillis);
        
        int totalMoods = 0;
        int happyCount = 0;
        
        // Iterate through all saved moods
        for (String key : prefs.getAll().keySet()) {
            if (key.startsWith("last_mood_")) {
                try {
                    long timestamp = Long.parseLong(key.replace("last_mood_", ""));
                    if (timestamp >= startOfDay) {
                        String mood = prefs.getString(key, "");
                        totalMoods++;
                        if (mood.equalsIgnoreCase("happy")) {
                            happyCount++;
                        }
                    }
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Failed to parse mood timestamp");
                }
            }
        }
        
        // Update mood progress based on today's average
        if (totalMoods > 0) {
            int moodPercentage = (happyCount * 100) / totalMoods;
            if (moodProgress != null) {
                moodProgress.setProgress(moodPercentage);
            }
        }
    }

    private void setupClickListeners() {
        // Defensive listener setup - only attach listeners if the view exists in current layout
        if (ivProfile != null) {
            ivProfile.setOnClickListener(v -> Toast.makeText(this, "Opening profile", Toast.LENGTH_SHORT).show());
        }

        if (ivSettings != null) {
            ivSettings.setOnClickListener(v -> {
                Toast.makeText(this, "Opening settings", Toast.LENGTH_SHORT).show();
                navigateToMoreOptions();
            });
        }

        if (chatbotCard != null && bottomNavigation != null) {
            chatbotCard.setOnClickListener(v -> {
                // Try selecting chat tab if id exists
                int id = getResources().getIdentifier("nav_chat", "id", getPackageName());
                if (id != 0) bottomNavigation.setSelectedItemId(id);
            });
        }

        if (moodCard != null) {
            moodCard.setOnClickListener(v -> showMoodCaptureOptions());
        }

        if (fabCaptureMood != null) {
            fabCaptureMood.setOnClickListener(v -> showMoodCaptureOptions());
        }

        if (tipsCard != null) {
            tipsCard.setOnClickListener(v -> showDailyTips());
        }

        if (sessionCard != null) {
            sessionCard.setOnClickListener(v -> {
                Toast.makeText(this, "Starting meditation session", Toast.LENGTH_SHORT).show();
                // Activate meditation session
                activateMeditationSession();
            });
        }

        if (btnCheckin != null) {
            btnCheckin.setOnClickListener(v -> showDailyCheckInDialog());
        }

        if (btnJournal != null) {
            btnJournal.setOnClickListener(v -> {
                if (bottomNavigation != null) {
                    int id = getResources().getIdentifier("nav_journal", "id", getPackageName());
                    if (id != 0) bottomNavigation.setSelectedItemId(id);
                } else {
                    navigateToJournalScreen();
                }
            });
        }

        if (btnMeditation != null) {
            btnMeditation.setOnClickListener(v -> showMeditationLibrary());
        }

        if (btnResources != null) {
            btnResources.setOnClickListener(v -> {
                Intent resourcesIntent = new Intent(DashboardActivity.this, ResourcesActivity.class);
                startActivity(resourcesIntent);
            });
        }
    }

    private void setupBottomNavigation() {
        if (bottomNavigation == null) return;
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            int idHome = getResources().getIdentifier("nav_home", "id", getPackageName());
            int idChat = getResources().getIdentifier("nav_chat", "id", getPackageName());
            int idJournal = getResources().getIdentifier("nav_journal", "id", getPackageName());
            int idCommunity = getResources().getIdentifier("nav_community", "id", getPackageName());
            int idMore = getResources().getIdentifier("nav_more", "id", getPackageName());

            if (itemId == idHome) {
                refreshDashboardData();
                return true;
            } else if (itemId == idChat) {
                navigateToChatScreen();
                return true;
            } else if (itemId == idJournal) {
                navigateToJournalScreen();
                return true;
            } else if (itemId == idCommunity) {
                navigateToCommunityScreen();
                return true;
            } else if (itemId == idMore) {
                navigateToMoreOptions();
                return true;
            }
            return false;
        });
    }

    private void loadRecommendedItems() {
        // Create adapter and set up the recycler view
        recommendedItems = getRecommendedActivities();
        RecommendedAdapter adapter = new RecommendedAdapter(recommendedItems, item -> {
            // Handle recommendation item click
            handleRecommendationClick(item);
        });
        if (rvRecommended != null) {
            rvRecommended.setAdapter(adapter);
        }
    }

    /**
     * Handle clicks on recommended items
     */
    private void handleRecommendationClick(RecommendedItem item) {
        String title = item.getTitle();
        
        if (title.contains("Breathing")) {
            showMeditationPlayer("Calm Breathing", "5 minute breathing exercise",
                    R.drawable.ic_breathing, "inpok4MKVLM", 5);
            logMeditationSession("Calm Breathing", 5);
        } else if (title.contains("Stress") || title.contains("Meditation")) {
            showMeditationPlayer("Stress Relief Meditation", "10 minute guided meditation",
                    R.drawable.ic_meditation, "O-6f5wQXSu8", 10);
            logMeditationSession("Stress Relief", 10);
        } else if (title.contains("Sleep")) {
            showMeditationPlayer("Sleep Better", "20 minute bedtime meditation",
                    R.drawable.ic_sleep, "aEqlQvczMcQ", 20);
            logMeditationSession("Sleep Session", 20);
        } else if (title.contains("Walking")) {
            Toast.makeText(this, "Go for a mindful walk outdoors! 🚶", Toast.LENGTH_LONG).show();
            logMeditationSession("Mindful Walk", 15);
        } else if (title.contains("Journal") || title.contains("Gratitude")) {
            Toast.makeText(this, "Opening journal...", Toast.LENGTH_SHORT).show();
            if (btnJournal != null) {
                btnJournal.performClick();
            }
        } else if (title.contains("Joy") || title.contains("Energy")) {
            Toast.makeText(this, "Keep up the positive energy! 😊", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Starting: " + title, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Activate a meditation session with real tracking
     */
    private void activateMeditationSession() {
        // Show meditation player with default session
        showMeditationPlayer(
                "Quick Meditation Session",
                "A guided 5-minute meditation to calm your mind",
                android.R.drawable.ic_menu_slideshow,
                "inpok4MKVLM",
                5
        );
        
        // Log session start
        logMeditationSession("Quick Session", 5);
    }

    /**
     * Log meditation session for tracking user activity
     */
    private void logMeditationSession(String sessionName, int durationMinutes) {
        SharedPreferences prefs = getSharedPreferences("meditation_sessions", MODE_PRIVATE);
        long timestamp = System.currentTimeMillis();
        
        // Save session info
        prefs.edit()
                .putString("session_" + timestamp, sessionName)
                .putInt("duration_" + timestamp, durationMinutes)
                .putLong("timestamp_" + timestamp, timestamp)
                .apply();
        
        Log.d(TAG, "Meditation session logged: " + sessionName + " (" + durationMinutes + " min)");
    }

    private void refreshDashboardData() {
        // Refresh data on dashboard
        loadDailyQuote();
        initializeStats();
        loadRecommendedItems();
       // Toast.makeText(this, "Dashboard refreshed", Toast.LENGTH_SHORT).show();
    }

    private void navigateToChatScreen() {
        // In a real app, this could be an Activity or Fragment transition
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }

    private void navigateToJournalScreen() {
        Intent intent = new Intent(this, JournalActivity.class);
        startActivity(intent);
    }

    private void navigateToCommunityScreen() {
        Intent intent = new Intent(this, CommunityActivity.class);
        startActivity(intent);
    }

    private void navigateToMoreOptions() {
        Intent intent = new Intent(this, MoreOptionsActivity.class);
        startActivity(intent);
    }

    private List<RecommendedItem> getRecommendedActivities() {
        List<RecommendedItem> items = new ArrayList<>();

        // Get personalized recommendations based on mood history
        SharedPreferences prefs = getSharedPreferences("mood_history", MODE_PRIVATE);
        String lastMood = prefs.getString("last_detected_mood", "neutral");

        // Dynamically recommend based on mood
        if (lastMood.equalsIgnoreCase("happy")) {
            items.add(new RecommendedItem("Share Your Joy", "Tell someone about your day", R.drawable.ic_breathing));
            items.add(new RecommendedItem("Continue the Energy", "Do something fun", R.drawable.ic_meditation));
        } else if (lastMood.equalsIgnoreCase("sad")) {
            items.add(new RecommendedItem("Calm Breathing", "5 min exercise", R.drawable.ic_breathing));
            items.add(new RecommendedItem("Stress Relief", "10 min meditation", R.drawable.ic_meditation));
        } else if (lastMood.equalsIgnoreCase("angry")) {
            items.add(new RecommendedItem("Anger Relief", "Breathing exercise", R.drawable.ic_breathing));
            items.add(new RecommendedItem("Mindful Walking", "15 min outdoor", R.drawable.ic_walking));
        } else {
            // Default neutral recommendations
            items.add(new RecommendedItem("Calm Breathing", "5 min exercise", R.drawable.ic_breathing));
            items.add(new RecommendedItem("Stress Relief", "10 min meditation", R.drawable.ic_meditation));
        }

        // Add always-relevant items
        items.add(new RecommendedItem("Sleep Better", "Bedtime routine", R.drawable.ic_sleep));
        items.add(new RecommendedItem("Mindful Walking", "15 min outdoor activity", R.drawable.ic_walking));
        items.add(new RecommendedItem("Gratitude Journal", "Write 3 things", R.drawable.ic_journal));

        return items;
    }

    // New methods for mood detection

    private void showMoodCaptureOptions() {
        // Show a dialog with camera and gallery options
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Capture Mood");
        builder.setMessage("Take a photo or select one from gallery for mood analysis");

        builder.setPositiveButton("Camera", (dialog, which) -> {
            // Request camera permission before opening camera
            requestCameraPermission();
        });

        builder.setNegativeButton("Gallery", (dialog, which) -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(galleryIntent);
        });

        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    /**
     * Request camera permission from user
     */
    private void requestCameraPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Permission already granted
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraLauncher.launch(cameraIntent);
            } else {
                // Request permission
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA);
            }
        } else {
            // For devices below Android 6.0, permissions are granted at install time
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(cameraIntent);
        }
    }

    private void analyzeMood(Bitmap bitmap) {
        // Show loading state
        moodProgress.setIndeterminate(true);
        tvMoodScore.setText("...");

        // Process on background thread
        new Thread(() -> {
            try {
                // Step 1: Detect face first
                detectFaceAndAnalyzeMood(bitmap);
            } catch (Exception e) {
                Log.e(TAG, "Error analyzing mood: " + e.getMessage(), e);

                // Update UI on error
                runOnUiThread(() -> {
                    moodProgress.setIndeterminate(false);
                    moodProgress.setProgress(50);
                    tvMoodScore.setText("😐");
                    Toast.makeText(this, "Error detecting mood: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    /**
     * Detect face in bitmap before mood analysis
     */
    private void detectFaceAndAnalyzeMood(Bitmap bitmap) {
        try {
            // Create InputImage from bitmap
            // Create InputImage from bitmap
            InputImage image = InputImage.fromBitmap(bitmap, 0);// Run face detection
            faceDetector.process(image)
                    .addOnSuccessListener(faces -> {
                        if (faces.isEmpty()) {
                            // No face detected
                            runOnUiThread(() -> {
                                moodProgress.setIndeterminate(false);
                                moodProgress.setProgress(50);
                                tvMoodScore.setText("😐");
                                showFaceDetectionFailedDialog();
                            });
                        } else if (faces.size() > 1) {
                            // Multiple faces detected
                            runOnUiThread(() -> {
                                moodProgress.setIndeterminate(false);
                                moodProgress.setProgress(50);
                                tvMoodScore.setText("😐");
                                Toast.makeText(DashboardActivity.this, 
                                        "Please upload an image with only one face", 
                                        Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            // Single face detected - proceed with mood analysis
                            Face detectedFace = faces.get(0);
                            Log.d(TAG, "Face detected successfully. Analyzing mood...");
                            
                            // Resize the bitmap to match model input size
                            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, modelInputWidth, modelInputHeight, true);

                            // Proceed with mood analysis
                            try {
                                analyzeMoodFromBitmap(resizedBitmap);
                            } catch (Exception e) {
                                Log.e(TAG, "Error during mood analysis: " + e.getMessage());
                                runOnUiThread(() -> {
                                    moodProgress.setIndeterminate(false);
                                    moodProgress.setProgress(50);
                                    tvMoodScore.setText("😐");
                                    Toast.makeText(DashboardActivity.this, 
                                            "Error analyzing mood: " + e.getMessage(), 
                                            Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Face detection failed: " + e.getMessage());
                        runOnUiThread(() -> {
                            moodProgress.setIndeterminate(false);
                            moodProgress.setProgress(50);
                            tvMoodScore.setText("😐");
                            Toast.makeText(DashboardActivity.this, 
                                    "Face detection failed. Please try again.", 
                                    Toast.LENGTH_SHORT).show();
                        });
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error preparing face detection: " + e.getMessage());
            runOnUiThread(() -> {
                moodProgress.setIndeterminate(false);
                moodProgress.setProgress(50);
                tvMoodScore.setText("😐");
                Toast.makeText(this, "Error processing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    /**
     * Analyze mood from bitmap after face detection passed
     */
    private void analyzeMoodFromBitmap(Bitmap resizedBitmap) throws Exception {
        // Get model input information to understand what it expects
        int[] inputShape = tflite.getInputTensor(0).shape();
        int dataType = tflite.getInputTensor(0).dataType().ordinal();
        
        try {
            if (inputShape.length == 4) {
                float[][][][] inputArray = new float[inputShape[0]][modelInputHeight][modelInputWidth][inputShape.length > 3 ? inputShape[3] : 3];

                for (int y = 0; y < modelInputHeight; y++) {
                    for (int x = 0; x < modelInputWidth; x++) {
                        int pixel = resizedBitmap.getPixel(x, y);
                        inputArray[0][y][x][0] = ((pixel >> 16) & 0xFF) / 127.5f - 1.0f;
                        inputArray[0][y][x][1] = ((pixel >> 8) & 0xFF) / 127.5f - 1.0f;
                        inputArray[0][y][x][2] = (pixel & 0xFF) / 127.5f - 1.0f;
                    }
                }

                float[][] outputProbabilities = new float[1][labels.size()];
                tflite.run(inputArray, outputProbabilities);
                processInferenceResults(outputProbabilities);
            } else {
                // Try with TensorImage approach as fallback
                Log.d(TAG, "Using TensorImage approach instead");
                TensorImage tensorImage = TensorImage.fromBitmap(resizedBitmap);
                tensorImage = new ImageProcessor.Builder()
                        .add(new ResizeOp(modelInputHeight, modelInputWidth, ResizeOp.ResizeMethod.BILINEAR))
                        .build()
                        .process(tensorImage);

                float[][] outputProbabilities = new float[1][labels.size()];
                tflite.run(tensorImage.getBuffer(), outputProbabilities);
                processInferenceResults(outputProbabilities);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in inference: " + e.getMessage(), e);

            try {
                Log.d(TAG, "Attempting one more approach");

                int[] intValues = new int[modelInputWidth * modelInputHeight];
                resizedBitmap.getPixels(intValues, 0, modelInputWidth, 0, 0, modelInputWidth, modelInputHeight);

                float[][] outputProbabilities = new float[1][labels.size()];

                float[] floatValues = new float[modelInputWidth * modelInputHeight * 3];
                for (int i = 0; i < intValues.length; i++) {
                    final int val = intValues[i];
                    floatValues[i*3] = ((val >> 16) & 0xFF) / 255.0f;
                    floatValues[i*3+1] = ((val >> 8) & 0xFF) / 255.0f;
                    floatValues[i*3+2] = (val & 0xFF) / 255.0f;
                }

                tflite.run(floatValues, outputProbabilities);
                processInferenceResults(outputProbabilities);
            } catch (Exception fallbackException) {
                throw new Exception("All input approaches failed: " + e.getMessage() +
                        " AND " + fallbackException.getMessage());
            }
        }
    }

    /**
     * Show dialog when face detection fails
     */
    private void showFaceDetectionFailedDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = 
                new androidx.appcompat.app.AlertDialog.Builder(this);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(dpToPx(this, 24), dpToPx(this, 24), dpToPx(this, 24), dpToPx(this, 16));

        // Error emoji
        TextView errorEmojiView = new TextView(this);
        errorEmojiView.setTextSize(60);
        errorEmojiView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        errorEmojiView.setPadding(0, 0, 0, dpToPx(this, 16));
        errorEmojiView.setText("😕");
        mainLayout.addView(errorEmojiView);

        // Error title
        TextView errorTitleView = new TextView(this);
        errorTitleView.setText("No Face Detected");
        errorTitleView.setTextSize(20);
        errorTitleView.setTypeface(null, android.graphics.Typeface.BOLD);
        errorTitleView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        errorTitleView.setTextColor(Color.BLACK);
        errorTitleView.setPadding(0, 0, 0, dpToPx(this, 12));
        mainLayout.addView(errorTitleView);

        // Error message
        TextView errorMessageView = new TextView(this);
        errorMessageView.setText("Please ensure:\n• Your face is clearly visible\n• The image is well-lit\n• Only one person is in the photo\n\nTry again with a clear selfie or portrait photo.");
        errorMessageView.setTextSize(14);
        errorMessageView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        errorMessageView.setTextColor(Color.DKGRAY);
        errorMessageView.setLineSpacing(dpToPx(this, 4), 1.0f);
        errorMessageView.setPadding(0, 0, 0, dpToPx(this, 20));
        mainLayout.addView(errorMessageView);

        // Retry button
        MaterialButton retryBtn = new MaterialButton(this);
        retryBtn.setText("Try Again");
        retryBtn.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        mainLayout.addView(retryBtn);

        builder.setView(mainLayout);
        builder.setCancelable(true);

        androidx.appcompat.app.AlertDialog dialog = builder.create();

        retryBtn.setOnClickListener(v -> {
            dialog.dismiss();
            showMoodCaptureOptions();
        });

        dialog.show();
    }

    /**
     * Initialize Google ML Kit Face Detector
     */
    private void initializeFaceDetector() {
        try {
            FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                    .build();

            faceDetector = FaceDetection.getClient(options);
            Log.d(TAG, "Face detector initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing face detector: " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tflite != null) {
            tflite.close();
        }
        if (faceDetector != null) {
            try {
                faceDetector.close();
            } catch (Exception e) {
                Log.w(TAG, "Error closing face detector: " + e.getMessage());
            }
        }
    }

    /**
     * Load the TFLite model off the UI thread and report errors on the UI thread.
     */
    private void loadModelAsync() {
        new Thread(() -> {
            try {
                initializeModel(); // may throw IOException
                runOnUiThread(() -> {
                    Log.d(TAG, "TFLite model loaded on background thread");
                    // Optionally update UI to indicate model ready
                });
            } catch (IOException e) {
                Log.e(TAG, "Error initializing TFLite model (background): " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(DashboardActivity.this,
                        "Error loading mood detection model", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error loading model", e);
                runOnUiThread(() -> Toast.makeText(DashboardActivity.this,
                        "Unexpected error initializing model", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // Reset bottom navigation to Home when returning from other activities
        resetBottomNavigationToHome();
    }

    /**
     * Reset the bottom navigation to the Home tab
     */
    private void resetBottomNavigationToHome() {
        if (bottomNavigation != null) {
            int idHome = getResources().getIdentifier("nav_home", "id", getPackageName());
            if (idHome != 0) {
                bottomNavigation.setSelectedItemId(idHome);
                refreshDashboardData();
            }
        }
    }
    
    /**
     * Load the saved font preference and apply it globally to all text views
     */
    private void applyGlobalFont() {
        SharedPreferences prefs = getSharedPreferences("app_preferences", MODE_PRIVATE);
        String fontPath = prefs.getString("selected_font", "fonts/Roboto-Regular.ttf");
        
        try {
            Typeface typeface = Typeface.createFromAsset(getAssets(), fontPath);
            
            // Apply font to the root view after layout is inflated
            View rootView = getWindow().getDecorView().getRootView();
            applyFontToViews(rootView, typeface);
        } catch (Exception e) {
            Log.w(TAG, "Error loading font: " + e.getMessage());
            // Fall back to default font - no action needed
        }
    }

    /**
     * Recursively apply font to all TextViews and EditTexts in a view hierarchy
     */
    private void applyFontToViews(View view, Typeface typeface) {
        if (view instanceof android.widget.TextView) {
            ((android.widget.TextView) view).setTypeface(typeface);
        } else if (view instanceof android.widget.EditText) {
            ((android.widget.EditText) view).setTypeface(typeface);
        }
        
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                applyFontToViews(viewGroup.getChildAt(i), typeface);
            }
        }
    }

    /**
     * Helper method to convert dp to pixels
     */
    private static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    /**
     * Model for recommended items
     */
    public static class RecommendedItem {
        private final String title;
        private final String description;
        private final int imageResId;

        public RecommendedItem(String title, String description, int imageResId) {
            this.title = title;
            this.description = description;
            this.imageResId = imageResId;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public int getImageResId() { return imageResId; }
    }

    /**
     * Adapter for recommended items RecyclerView
     */
    private static class RecommendedAdapter extends RecyclerView.Adapter<RecommendedAdapter.ViewHolder> {
        private final List<RecommendedItem> items;
        private final OnItemClickListener clickListener;

        public interface OnItemClickListener {
            void onItemClick(RecommendedItem item);
        }

        public RecommendedAdapter(List<RecommendedItem> items, OnItemClickListener listener) {
            this.items = items;
            this.clickListener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            CardView cardView = new CardView(parent.getContext());
            cardView.setLayoutParams(new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT));
            cardView.setCardElevation(dpToPx(parent.getContext(), 4));
            cardView.setRadius(dpToPx(parent.getContext(), 8));
            cardView.setContentPadding(dpToPx(parent.getContext(), 8), 
                    dpToPx(parent.getContext(), 8),
                    dpToPx(parent.getContext(), 8),
                    dpToPx(parent.getContext(), 8));

            LinearLayout layout = new LinearLayout(parent.getContext());
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new LinearLayout.LayoutParams(
                    dpToPx(parent.getContext(), 60),
                    dpToPx(parent.getContext(), 60)));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setId(View.generateViewId());
            layout.addView(imageView);

            LinearLayout textLayout = new LinearLayout(parent.getContext());
            textLayout.setOrientation(LinearLayout.VERTICAL);
            textLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1));
            textLayout.setPadding(dpToPx(parent.getContext(), 12), 0, 0, 0);

            TextView titleView = new TextView(parent.getContext());
            titleView.setTextSize(14);
            titleView.setTypeface(null, android.graphics.Typeface.BOLD);
            titleView.setId(View.generateViewId());
            textLayout.addView(titleView);

            TextView descView = new TextView(parent.getContext());
            descView.setTextSize(12);
            descView.setTextColor(Color.GRAY);
            descView.setId(View.generateViewId());
            textLayout.addView(descView);

            layout.addView(textLayout);
            cardView.addView(layout);

            return new ViewHolder(cardView, imageView.getId(), titleView.getId(), descView.getId());
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RecommendedItem item = items.get(position);
            holder.imageView.setImageResource(item.getImageResId());
            holder.titleView.setText(item.getTitle());
            holder.descView.setText(item.getDescription());

            holder.itemView.setOnClickListener(v -> clickListener.onItemClick(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView titleView;
            TextView descView;

            public ViewHolder(@NonNull View itemView, int imageId, int titleId, int descId) {
                super(itemView);
                imageView = itemView.findViewById(imageId);
                titleView = itemView.findViewById(titleId);
                descView = itemView.findViewById(descId);
            }
        }
    }

    /**
     * Helper method to process inference results
     */
    private void processInferenceResults(float[][] outputProbabilities) {
        // Find the label with highest probability
        int maxIndex = 0;
        float maxProb = outputProbabilities[0][0];

        for (int i = 1; i < outputProbabilities[0].length; i++) {
            if (outputProbabilities[0][i] > maxProb) {
                maxProb = outputProbabilities[0][i];
                maxIndex = i;
            }
        }

        // Get the mood label
        final String detectedMood = labels.get(maxIndex);
        final int moodIndex = maxIndex;
        final float confidence = maxProb;

        // Log the result
        Log.d(TAG, "Detected mood: " + detectedMood + " with confidence: " + confidence);

        // Update UI on main thread
        runOnUiThread(() -> {
            moodProgress.setIndeterminate(false);
            updateMoodUI(moodIndex, confidence);

            // Show enhanced mood detection dialog instead of toast
            showMoodDetectionDialog(detectedMood, confidence, moodIndex);
        });
    }

    /**
     * Show an enhanced dialog displaying the detected mood with confidence and recommendations
     */
    private void showMoodDetectionDialog(String mood, float confidence, int moodIndex) {
        androidx.appcompat.app.AlertDialog.Builder builder = 
                new androidx.appcompat.app.AlertDialog.Builder(this);

        // Create main container
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(dpToPx(this, 24), dpToPx(this, 24), dpToPx(this, 24), dpToPx(this, 16));

        // Mood emoji display (large)
        TextView moodEmojiView = new TextView(this);
        moodEmojiView.setTextSize(80);
        moodEmojiView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        moodEmojiView.setPadding(0, 0, 0, dpToPx(this, 16));
        
        String emoji = getMoodEmoji(moodIndex);
        moodEmojiView.setText(emoji);
        mainLayout.addView(moodEmojiView);

        // Mood title
        TextView moodTitleView = new TextView(this);
        moodTitleView.setText("You're feeling " + mood);
        moodTitleView.setTextSize(22);
        moodTitleView.setTypeface(null, android.graphics.Typeface.BOLD);
        moodTitleView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        moodTitleView.setTextColor(Color.BLACK);
        moodTitleView.setPadding(0, 0, 0, dpToPx(this, 8));
        mainLayout.addView(moodTitleView);

        // Confidence percentage
        TextView confidenceView = new TextView(this);
        confidenceView.setText("Confidence: " + String.format("%.0f%%", confidence * 100));
        confidenceView.setTextSize(14);
        confidenceView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        confidenceView.setTextColor(Color.GRAY);
        confidenceView.setPadding(0, 0, 0, dpToPx(this, 16));
        mainLayout.addView(confidenceView);

        // Confidence progress bar
        ProgressBar confidenceProgress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        confidenceProgress.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(this, 8)));
        confidenceProgress.setProgress((int)(confidence * 100));
        confidenceProgress.setPadding(0, 0, 0, dpToPx(this, 16));
        try {
            confidenceProgress.getProgressDrawable().setColorFilter(
                    Color.parseColor("#6200EE"), android.graphics.PorterDuff.Mode.SRC_IN);
        } catch (Exception e) {
            Log.w(TAG, "Error setting progress bar color");
        }
        mainLayout.addView(confidenceProgress);

        // Suggested action based on mood
        String suggestion = getMoodSuggestion(moodIndex);
        TextView suggestionView = new TextView(this);
        suggestionView.setText(suggestion);
        suggestionView.setTextSize(14);
        suggestionView.setLineSpacing(dpToPx(this, 4), 1.0f);
        suggestionView.setTextColor(Color.DKGRAY);
        suggestionView.setPadding(0, dpToPx(this, 8), 0, dpToPx(this, 16));
        mainLayout.addView(suggestionView);

        // Divider
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1));
        divider.setBackgroundColor(Color.parseColor("#E0E0E0"));
        divider.setPadding(0, dpToPx(this, 12), 0, dpToPx(this, 12));
        mainLayout.addView(divider);

        // Quick action buttons
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        buttonLayout.setPadding(0, dpToPx(this, 8), 0, 0);

        // Recommend activity button
        MaterialButton recommendBtn = new MaterialButton(this);
        recommendBtn.setText("Get Help");
        LinearLayout.LayoutParams recommendParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1);
        recommendParams.setMarginEnd(dpToPx(this, 8));
        recommendBtn.setLayoutParams(recommendParams);
        buttonLayout.addView(recommendBtn);

        // Save mood button
        MaterialButton saveBtn = new MaterialButton(this);
        saveBtn.setText("Save Mood");
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1);
        saveBtn.setLayoutParams(saveParams);
        buttonLayout.addView(saveBtn);

        mainLayout.addView(buttonLayout);

        builder.setView(mainLayout);
        builder.setCancelable(true);

        androidx.appcompat.app.AlertDialog dialog = builder.create();

        // Set button listeners
        recommendBtn.setOnClickListener(v -> {
            dialog.dismiss();
            showPersonalizedRecommendations(getMoodRating(moodIndex), 
                    false, moodIndex >= 4, false);
        });

        saveBtn.setOnClickListener(v -> {
            dialog.dismiss();
            saveMoodDetection(mood, confidence, moodIndex);
            // Save the detected mood for recommendations
            SharedPreferences prefs = getSharedPreferences("mood_history", MODE_PRIVATE);
            prefs.edit().putString("last_detected_mood", mood).apply();
            // Refresh recommendations
            loadRecommendedItems();
            Toast.makeText(DashboardActivity.this, "Mood saved!", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    /**
     * Get emoji for mood index
     */
    private String getMoodEmoji(int moodIndex) {
        switch (moodIndex) {
            case 0: return "😊"; // happy
            case 1: return "😔"; // sad
            case 2: return "😐"; // neutral
            case 3: return "😮"; // surprised
            case 4: return "😠"; // angry
            case 5: return "🤢"; // disgusted
            case 6: return "😨"; // fearful
            default: return "😐";
        }
    }

    /**
     * Get personalized suggestion based on detected mood
     */
    private String getMoodSuggestion(int moodIndex) {
        switch (moodIndex) {
            case 0: // happy
                return "You're in a great place! Keep up this positive energy by engaging in activities you love.";
            case 1: // sad
                return "It's okay to feel down sometimes. Consider reaching out to someone you trust or try a calming meditation.";
            case 2: // neutral
                return "You seem calm and collected. This is a good time to focus on your goals or practice self-care.";
            case 3: // surprised
                return "You seem startled or excited! Take a moment to process what surprised you before moving forward.";
            case 4: return "It's natural to feel angry. Take some deep breaths and try our anger management meditation.";
            case 5: return "You might be feeling uncomfortable. Distance yourself from the source and engage in uplifting activities.";
            case 6: return "Fear is normal. Try grounding techniques or our anxiety relief meditation to feel more at ease.";
            default:
                return "Remember to check in with yourself and practice self-care regularly.";
        }
    }

    /**
     * Get mood rating (0-5) based on mood index for recommendations
     */
    private float getMoodRating(int moodIndex) {
        switch (moodIndex) {
            case 0: return 5.0f; // happy
            case 1: return 2.0f; // sad
            case 2: return 3.0f; // neutral
            case 3: return 4.0f; // surprised
            case 4: return 1.5f; // angry
            case 5: return 1.0f; // disgusted
            case 6: return 2.0f; // fearful
            default: return 3.0f;
        }
    }

    /**
     * Save mood detection to local storage or backend
     */
    private void saveMoodDetection(String mood, float confidence, int moodIndex) {
        // In a real app, save to local database or Firebase
        Log.d(TAG, "Saved mood: " + mood + " with confidence: " + confidence);
        
        // Could also save to SharedPreferences for historical tracking
        SharedPreferences prefs = getSharedPreferences("mood_history", MODE_PRIVATE);
        long timestamp = System.currentTimeMillis();
        prefs.edit()
                .putString("last_mood_" + timestamp, mood)
                .putFloat("confidence_" + timestamp, confidence)
                .putInt("mood_index_" + timestamp, moodIndex)
                .apply();
    }

    private void updateMoodUI(int moodIndex, float confidence) {
        // Calculate progress value (0-100)
        int progressValue;
        String moodEmoji;

        // Map mood index to emoji and progress value
        switch (moodIndex) {
            case 0: // happy
                moodEmoji = "😊";
                progressValue = 90;
                break;
            case 1: // sad
                moodEmoji = "😔";
                progressValue = 30;
                break;
            case 2: // neutral
                moodEmoji = "😐";
                progressValue = 50;
                break;
            case 3: // surprised
                moodEmoji = "😮";
                progressValue = 70;
                break;
            case 4: // angry
                moodEmoji = "😠";
                progressValue = 20;
                break;
            case 5: // disgusted
                moodEmoji = "🤢";
                progressValue = 25;
                break;
            case 6: // fearful
                moodEmoji = "😨";
                progressValue = 35;
                break;
            default:
                moodEmoji = "😐";
                progressValue = 50;
                break;
        }

        // Update UI
        tvMoodScore.setText(moodEmoji);
        moodProgress.setProgress(progressValue);
    }

    /**
     * Shows a dialog with daily mental health tips
     */
    private void showDailyTips() {
        // Create list of tips
        final String[] tips = {
                "Practice deep breathing for 5 minutes when feeling stressed",
                "Write down three things you're grateful for today",
                "Take a 10-minute walk outdoors for a mental refresh",
                "Limit social media use to reduce comparison and anxiety",
                "Stay hydrated - dehydration can affect your mood",
                "Get at least 7-8 hours of sleep for better mental clarity",
                "Try a 5-minute meditation to center yourself",
                "Call a friend or family member for a quick chat",
                "Take regular breaks when working on demanding tasks",
                "Practice positive self-talk when facing challenges"
        };

        // Show simple dialog with tips
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Daily Mental Health Tips")
                .setItems(tips, null)
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

        builder.create().show();

        // Update counter after viewing
        tvTipsCount.setText("0 New");
        Toast.makeText(this, "Daily tips refreshed", Toast.LENGTH_SHORT).show();
    }

    /**
     * Shows a daily check-in dialog to collect user's current mental state
     */
    private void showDailyCheckInDialog() {
        final androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);

        builder.setTitle("Daily Check-In");

        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);

        TextView instructions = new TextView(this);
        instructions.setText("How are you feeling today?");
        instructions.setTextSize(18);
        instructions.setPadding(0, 0, 0, 20);
        layout.addView(instructions);

        TextView ratingLabel = new TextView(this);
        ratingLabel.setText("Rate your mood:");
        layout.addView(ratingLabel);

        final RatingBar moodRating = new RatingBar(this);
        moodRating.setNumStars(5);
        moodRating.setStepSize(0.5f);
        moodRating.setRating(3.0f);
        layout.addView(moodRating);

        TextView feelingsLabel = new TextView(this);
        feelingsLabel.setText("Tell us more about your feelings:");
        feelingsLabel.setPadding(0, 20, 0, 0);
        layout.addView(feelingsLabel);

        final EditText feelingsInput = new EditText(this);
        feelingsInput.setHint("How are you feeling? (optional)");
        feelingsInput.setMinLines(2);
        layout.addView(feelingsInput);

        TextView issuesLabel = new TextView(this);
        issuesLabel.setText("Are you experiencing any of these?");
        issuesLabel.setPadding(0, 20, 0, 0);
        layout.addView(issuesLabel);

        final CheckBox sleepCheckbox = new CheckBox(this);
        sleepCheckbox.setText("Sleep difficulties");
        layout.addView(sleepCheckbox);

        final CheckBox anxietyCheckbox = new CheckBox(this);
        anxietyCheckbox.setText("Anxiety or worry");
        layout.addView(anxietyCheckbox);

        final CheckBox energyCheckbox = new CheckBox(this);
        energyCheckbox.setText("Low energy or motivation");
        layout.addView(energyCheckbox);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(layout);
        builder.setView(scrollView);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            float rating = moodRating.getRating();
            String feelings = feelingsInput.getText().toString();
            boolean sleepIssue = sleepCheckbox.isChecked();
            boolean anxietyIssue = anxietyCheckbox.isChecked();
            boolean energyIssue = energyCheckbox.isChecked();

            saveDailyCheckIn(rating, feelings, sleepIssue, anxietyIssue, energyIssue);
            showPersonalizedRecommendations(rating, sleepIssue, anxietyIssue, energyIssue);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    /**
     * Save the daily check-in data
     */
    private void saveDailyCheckIn(float rating, String feelings, boolean sleepIssue,
                                  boolean anxietyIssue, boolean energyIssue) {
        int moodValue = (int)(rating * 20);
        moodProgress.setProgress(moodValue);

        if (rating >= 4) {
            tvMoodScore.setText("😊");
        } else if (rating >= 3) {
            tvMoodScore.setText("😐");
        } else {
            tvMoodScore.setText("😔");
        }

        Toast.makeText(this, "Check-in recorded. Thank you!", Toast.LENGTH_SHORT).show();
        Log.d("CheckIn", "Mood: " + rating + ", Sleep issues: " + sleepIssue +
                ", Anxiety: " + anxietyIssue + ", Energy: " + energyIssue);
    }

    /**
     * Show personalized recommendations based on check-in data
     */
    private void showPersonalizedRecommendations(float rating, boolean sleepIssue,
                                                 boolean anxietyIssue, boolean energyIssue) {
        String title = "Your Personalized Recommendations";
        StringBuilder message = new StringBuilder();

        if (rating < 3) {
            message.append("• Consider talking to someone you trust about how you're feeling\n\n");
            message.append("• Try a 10-minute mindfulness meditation\n\n");
        }

        if (sleepIssue) {
            message.append("• Avoid screens 1 hour before bedtime\n\n");
            message.append("• Try our 'Sleep Better' meditation tonight\n\n");
        }

        if (anxietyIssue) {
            message.append("• Practice deep breathing: 4 counts in, hold for 4, out for 6\n\n");
            message.append("• Try our 'Calm Anxiety' guided exercise\n\n");
        }

        if (energyIssue) {
            message.append("• Take a 5-minute walk outside\n\n");
            message.append("• Ensure you're staying hydrated throughout the day\n\n");
        }

        message.append("• Remember that your feelings are valid and temporary\n\n");

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message.toString())
                .setPositiveButton("Try a Recommended Activity", (dialog, which) -> {
                    if (sleepIssue) {
                        showMeditationPlayer("Sleep Better", "Bedtime relaxation",
                                android.R.drawable.ic_lock_idle_alarm, "aEqlQvczMcQ", 20);
                    } else if (anxietyIssue) {
                        showMeditationPlayer("Calm Anxiety", "Guided breathing",
                                android.R.drawable.ic_menu_compass, "O-6f5wQXSu8", 10);
                    } else {
                        showMeditationPlayer("Mindful Moment", "Present awareness",
                                android.R.drawable.ic_menu_slideshow, "inpok4MKVLM", 5);
                    }
                })
                .setNegativeButton("Later", null)
                .show();
    }

    /**
     * Shows the meditation library screen with various options
     */
    private void showMeditationLibrary() {
        // Create a more polished dialog
        final androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);

        builder.setTitle("🧘 Meditation Library");

        // Create a container LinearLayout
        LinearLayout containerLayout = new LinearLayout(this);
        containerLayout.setOrientation(LinearLayout.VERTICAL);
        containerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // Add a subtitle
        TextView subtitleView = new TextView(this);
        subtitleView.setText("Find peace and calm with guided meditations");
        subtitleView.setTextSize(14);
        subtitleView.setTextColor(Color.GRAY);
        subtitleView.setPadding(dpToPx(this, 16), dpToPx(this, 8), dpToPx(this, 16), dpToPx(this, 16));
        containerLayout.addView(subtitleView);

        // Create RecyclerView for meditations
        RecyclerView meditationRecyclerView = new RecyclerView(this);
        meditationRecyclerView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(this, 400)));
        meditationRecyclerView.setBackgroundColor(Color.WHITE);

        // Setup the RecyclerView with meditation items
        List<MeditationItem> meditations = getMeditationItems();

        MeditationAdapter adapter = new MeditationAdapter(meditations,
                new MeditationAdapter.MeditationClickListener() {
                    @Override
                    public void onMeditationClicked(String title, String description,
                                                    int imageResId, String youtubeVideoId, int durationMinutes) {
                        showMeditationPlayer(title, description, imageResId, youtubeVideoId, durationMinutes);
                    }
                });

        meditationRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        meditationRecyclerView.setAdapter(adapter);
        containerLayout.addView(meditationRecyclerView);

        // Set the layout and add close button
        builder.setView(containerLayout);
        builder.setNegativeButton("Close", null);
        builder.setCancelable(true);
        builder.create().show();
    }

    /**
     * Get list of meditation items for the library
     */
    private List<MeditationItem> getMeditationItems() {
        List<MeditationItem> items = new ArrayList<>();

        // Add meditation items with real YouTube videos
        items.add(new MeditationItem("Deep Relaxation", "sleep", 15,
                android.R.drawable.ic_lock_idle_alarm, "gU_ABkZv_dY"));
        items.add(new MeditationItem("Anxiety Relief", "anxiety", 10,
                android.R.drawable.ic_menu_compass, "O-6f5wQXSu8"));
        items.add(new MeditationItem("Quick Calm", "anxiety", 5,
                android.R.drawable.ic_menu_slideshow, "inpok4MKVLM"));
        items.add(new MeditationItem("Better Sleep", "sleep", 20,
                android.R.drawable.ic_lock_idle_alarm, "aEqlQvczMcQ"));
        items.add(new MeditationItem("Focus Time", "focus", 15,
                android.R.drawable.ic_menu_view, "nMfPqeZjc2c"));
        items.add(new MeditationItem("Morning Energy", "focus", 8,
                android.R.drawable.ic_menu_today, "ENYYb5vW4Qg"));
        items.add(new MeditationItem("Letting Go", "anxiety", 12,
                android.R.drawable.ic_menu_rotate, "syx3a1_LeFo"));
        items.add(new MeditationItem("Body Scan", "sleep", 18,
                android.R.drawable.ic_menu_search, "T0nuKBVQS4M"));

        return items;
    }

    /**
     * Model class for meditation items
     */
    private static class MeditationItem {
        private final String title;
        private final String category;
        private final int durationMinutes;
        private final int imageResId;
        private final String youtubeVideoId;

        public MeditationItem(String title, String category, int durationMinutes, int imageResId, String youtubeVideoId) {
            this.title = title;
            this.category = category;
            this.durationMinutes = durationMinutes;
            this.imageResId = imageResId;
            this.youtubeVideoId = youtubeVideoId;
        }

        public String getTitle() { return title; }
        public String getCategory() { return category; }
        public int getDurationMinutes() { return durationMinutes; }
        public int getImageResId() { return imageResId; }
        public String getYoutubeVideoId() { return youtubeVideoId; }
    }

    /**
     * Adapter for meditation items
     */
    private static class MeditationAdapter extends RecyclerView.Adapter<MeditationAdapter.ViewHolder> {
        private final List<MeditationItem> allItems;
        private List<MeditationItem> filteredItems;
        private final MeditationClickListener clickListener;

        public interface MeditationClickListener {
            void onMeditationClicked(String title, String description, int imageResId, String youtubeVideoId, int durationMinutes);
        }

        public MeditationAdapter(List<MeditationItem> items, MeditationClickListener clickListener) {
            this.allItems = items;
            this.filteredItems = new ArrayList<>(items);
            this.clickListener = clickListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            CardView cardView = new CardView(parent.getContext());
            cardView.setLayoutParams(new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT));
            cardView.setCardElevation(dpToPx(parent.getContext(), 4));
            cardView.setRadius(dpToPx(parent.getContext(), 12));
            cardView.setContentPadding(0, 0, 0, 0);
            cardView.setUseCompatPadding(true);
            cardView.setCardBackgroundColor(Color.WHITE);

            LinearLayout layout = new LinearLayout(parent.getContext());
            layout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setBackgroundColor(Color.WHITE);

            FrameLayout imageContainer = new FrameLayout(parent.getContext());
            imageContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(parent.getContext(), 120)));

            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setId(View.generateViewId());
            imageContainer.addView(imageView);

            View overlay = new View(parent.getContext());
            overlay.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));
            overlay.setBackgroundColor(Color.argb(80, 0, 0, 0));
            imageContainer.addView(overlay);

            layout.addView(imageContainer);

            LinearLayout contentLayout = new LinearLayout(parent.getContext());
            contentLayout.setOrientation(LinearLayout.VERTICAL);
            contentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            int padding = dpToPx(parent.getContext(), 12);
            contentLayout.setPadding(padding, padding, padding, padding);

            TextView titleView = new TextView(parent.getContext());
            titleView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            titleView.setTextSize(16);
            titleView.setTypeface(null, android.graphics.Typeface.BOLD);
            titleView.setTextColor(Color.BLACK);
            titleView.setId(View.generateViewId());
            contentLayout.addView(titleView);

            LinearLayout durationLayout = new LinearLayout(parent.getContext());
            durationLayout.setOrientation(LinearLayout.HORIZONTAL);
            durationLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            durationLayout.setPadding(0, dpToPx(parent.getContext(), 6), 0, 0);

            TextView durationView = new TextView(parent.getContext());
            durationView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            durationView.setTextSize(13);
            durationView.setTextColor(Color.parseColor("#6200EE"));
            durationView.setId(View.generateViewId());
            durationLayout.addView(durationView);

            contentLayout.addView(durationLayout);
            layout.addView(contentLayout);

            cardView.addView(layout);
            cardView.setClickable(true);
            cardView.setForeground(getRippleDrawable(parent.getContext()));

            return new ViewHolder(cardView, imageView.getId(), titleView.getId(), durationView.getId());
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MeditationItem item = filteredItems.get(position);
            holder.imageView.setImageResource(item.getImageResId());
            holder.titleView.setText(item.getTitle());
            holder.durationView.setText("⏱ " + item.getDurationMinutes() + " minutes");

            holder.itemView.setOnClickListener(v -> clickListener.onMeditationClicked(
                    item.getTitle(),
                    "A guided " + item.getCategory() + " meditation",
                    item.getImageResId(),
                    item.getYoutubeVideoId(),
                    item.getDurationMinutes()
            ));
        }

        @Override
        public int getItemCount() {
            return filteredItems.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView titleView;
            TextView durationView;

            public ViewHolder(@NonNull View itemView, int imageViewId, int titleViewId, int durationViewId) {
                super(itemView);
                imageView = itemView.findViewById(imageViewId);
                titleView = itemView.findViewById(titleViewId);
                durationView = itemView.findViewById(durationViewId);
            }
        }

        private android.graphics.drawable.RippleDrawable getRippleDrawable(Context context) {
            try {
                int rippleColor = Color.parseColor("#6200EE");
                return new android.graphics.drawable.RippleDrawable(
                        android.content.res.ColorStateList.valueOf(rippleColor),
                        null,
                        null
                );
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * Show meditation player dialog
     */
    private void showMeditationPlayer(String title, String description, int imageResId, String youtubeVideoId, int durationMinutes) {
        try {
            androidx.appcompat.app.AlertDialog.Builder builder =
                    new androidx.appcompat.app.AlertDialog.Builder(this);

            builder.setTitle(title)
                    .setMessage(description + "\n\nDuration: " + durationMinutes + " minutes")
                    .setPositiveButton("Play on YouTube", (dialog, which) -> {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("https://www.youtube.com/watch?v=" + youtubeVideoId));
                            startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(this, "Could not open YouTube app", Toast.LENGTH_SHORT).show();
                            try {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                                browserIntent.setData(Uri.parse("https://www.youtube.com/watch?v=" + youtubeVideoId));
                                startActivity(browserIntent);
                            } catch (Exception ex) {
                                Toast.makeText(this, "Could not open YouTube. Please check your connection.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            builder.create().show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing meditation player: " + e.getMessage());
            Toast.makeText(this, "Unable to open meditation video: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }
}

