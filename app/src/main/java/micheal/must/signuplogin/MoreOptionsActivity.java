package micheal.must.signuplogin;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import androidx.appcompat.widget.SwitchCompat;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayoutMediator;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.ArrayList;
import java.util.List;

import android.content.res.ColorStateList;

import micheal.must.signuplogin.fragments.MoreAboutFragment;
import micheal.must.signuplogin.fragments.MoreAccountFragment;
import micheal.must.signuplogin.fragments.MorePreferencesFragment;

// remove this import - class may not exist at compile time
// import micheal.must.signuplogin.receivers.NotificationSchedulerReceiver;

public class MoreOptionsActivity extends AppCompatActivity {

    private static final String TAG = "MoreOptionsActivity";
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private MorePagerAdapter pagerAdapter;

    // Tab titles
    private final String[] tabTitles = {"Account", "Preferences", "About"};

    // Add Firebase Auth and Google Sign-In client
    private FirebaseAuth mAuth;
    private SignInClient oneTapClient;

    // Add SharedPreferences for storing settings
    private SharedPreferences sharedPreferences;
    private static final String PREF_DARK_MODE = "dark_mode";
    private static final String PREF_REMINDER_FREQ = "reminder_frequency";

    // Notification constants
    private static final String CHANNEL_ID = "mindmate_channel";
    private static final String CHANNEL_NAME = "MindMate Notifications";
    private static final String CHANNEL_DESC = "All notifications from MindMate app";
    private static final int NOTIFICATION_ID = 100;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_more_options);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        oneTapClient = Identity.getSignInClient(this);

        // Initialize SharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Find views
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        setupToolbar();
        setupViewPager();

        // NOTE: appearance handling removed. theme forced dark above.

        // Create notification channel for Android 8.0+
        createNotificationChannel();

        // Check for notification permission on Android 13+
        checkNotificationPermission();
    }

    private void setupToolbar() {
        // Guard against missing toolbar in the layout to avoid crashes when launching this activity.
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Settings");
            }
        } else {
            // Fallback: set the Activity title so UI still makes sense even without a toolbar.
            setTitle("Settings");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager() {
        if (viewPager == null) {
            Log.e(TAG, "ViewPager2 is null");
            return;
        }

        pagerAdapter = new MorePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        if (tabLayout != null) {
            new TabLayoutMediator(tabLayout, viewPager, (tab, position) ->
                    tab.setText(tabTitles[position])
            ).attach();
        }
    }

    // ViewPager adapter to manage fragments
    private class MorePagerAdapter extends FragmentStateAdapter {

        public MorePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            try {
                switch (position) {
                    case 0:
                        return new MoreAccountFragment();
                    case 1:
                        return new MorePreferencesFragment();
                    case 2:
                        return new MoreAboutFragment();
                    default:
                        return new MoreAccountFragment();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error creating fragment at position " + position + ": " + e.getMessage(), e);
                return new MoreAccountFragment();
            }
        }

        @Override
        public int getItemCount() {
            return tabTitles.length;
        }
    }

    /**
     * Check and request notification permission for Android 13+
     */
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied. Some features may not work.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Returns whether the app currently has notification permission (Android 13+).
     */
    public boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Pre-Android 13 doesn't require runtime notification permission
    }

    private void openNotificationSettings() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_notifications, null);
            builder.setView(dialogView);

            // Find switches explicitly with correct androidx type
            SwitchCompat switchCheckIn = (SwitchCompat) dialogView.findViewById(R.id.switch_checkin);
            SwitchCompat switchMeditation = (SwitchCompat) dialogView.findViewById(R.id.switch_meditation);
            SwitchCompat switchJournal = (SwitchCompat) dialogView.findViewById(R.id.switch_journal);
            SwitchCompat switchTips = (SwitchCompat) dialogView.findViewById(R.id.switch_tips);

            // Log which switches are found for debugging
            Log.d("MoreOptionsActivity", "Switches found: " +
                    (switchCheckIn != null) + ", " +
                    (switchMeditation != null) + ", " +
                    (switchJournal != null) + ", " +
                    (switchTips != null));

            // Set up each switch individually with error handling
            if (switchCheckIn != null) {
                try {
                    switchCheckIn.setChecked(sharedPreferences.getBoolean("notify_checkin", true));
                } catch (Exception e) {
                    Log.e("MoreOptionsActivity", "Error with switchCheckIn: " + e.getMessage());
                }
            }

            if (switchMeditation != null) {
                try {
                    switchMeditation.setChecked(sharedPreferences.getBoolean("notify_meditation", true));
                } catch (Exception e) {
                    Log.e("MoreOptionsActivity", "Error with switchMeditation: " + e.getMessage());
                }
            }

            if (switchJournal != null) {
                try {
                    switchJournal.setChecked(sharedPreferences.getBoolean("notify_journal", true));
                } catch (Exception e) {
                    Log.e("MoreOptionsActivity", "Error with switchJournal: " + e.getMessage());
                }
            }

            if (switchTips != null) {
                try {
                    switchTips.setChecked(sharedPreferences.getBoolean("notify_tips", true));
                } catch (Exception e) {
                    Log.e("MoreOptionsActivity", "Error with switchTips: " + e.getMessage());
                }
            }

            // Set up test notification button
            MaterialButton btnTestNotification = dialogView.findViewById(R.id.btn_test_notification);
            if (btnTestNotification != null) {
                btnTestNotification.setOnClickListener(v -> sendTestNotificationSafely());
            }

            // Show the dialog
            builder.setPositiveButton("Save", (dialog, which) -> {
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        try {
                            if (switchCheckIn != null) {
                                boolean isChecked = switchCheckIn.isChecked();
                                editor.putBoolean("notify_checkin", isChecked);
                            }
                        } catch (Exception e) {
                            Log.e("MoreOptionsActivity", "Error saving check-in preference: " + e.getMessage());
                        }

                        try {
                            if (switchMeditation != null) {
                                boolean isChecked = switchMeditation.isChecked();
                                editor.putBoolean("notify_meditation", isChecked);
                            }
                        } catch (Exception e) {
                            Log.e("MoreOptionsActivity", "Error saving meditation preference: " + e.getMessage());
                        }

                        try {
                            if (switchJournal != null) {
                                boolean isChecked = switchJournal.isChecked();
                                editor.putBoolean("notify_journal", isChecked);
                            }
                        } catch (Exception e) {
                            Log.e("MoreOptionsActivity", "Error saving journal preference: " + e.getMessage());
                        }

                        try {
                            if (switchTips != null) {
                                boolean isChecked = switchTips.isChecked();
                                editor.putBoolean("notify_tips", isChecked);
                            }
                        } catch (Exception e) {
                            Log.e("MoreOptionsActivity", "Error saving tips preference: " + e.getMessage());
                        }

                        editor.apply();

                        // Instead of directly calling applyNotificationSettings(), use a safer approach
                        try {
                            // First just show success message
                            Toast.makeText(MoreOptionsActivity.this, "Notification settings saved", Toast.LENGTH_SHORT).show();

                            // Then create a temporary fallback receiver if needed
                            if (!isClassAvailable("micheal.must.signuplogin.receivers.NotificationSchedulerReceiver")) {
                                // Create a simple version of the scheduler class
                                createTempNotificationReceiver();
                            } else {
                                // If the class exists, try to apply settings
                                applyNotificationSettings();
                            }
                        } catch (Exception e) {
                            Log.e("MoreOptionsActivity", "Error in notification settings", e);
                            // At least settings were saved, so don't show error to user
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show();
        } catch (Exception e) {
            Log.e("MoreOptionsActivity", "Error opening notification settings", e);
            Toast.makeText(this, "Error opening settings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void applyNotificationSettings() {
        try {
            // Use reflection so this compiles even when the receiver class is not present at build time.
            if (!isClassAvailable("micheal.must.signuplogin.receivers.NotificationSchedulerReceiver")) {
                Log.w("MoreOptionsActivity", "NotificationSchedulerReceiver class not found; skipping scheduler.");
                return;
            }
            try {
                Class<?> cls = Class.forName("micheal.must.signuplogin.receivers.NotificationSchedulerReceiver");
                try {
                    java.lang.reflect.Method m = cls.getMethod("scheduleAllNotifications", Context.class);
                    m.invoke(null, this);
                } catch (NoSuchMethodException nsme) {
                    try {
                        java.lang.reflect.Method m2 = cls.getMethod("scheduleAllNotifications");
                        m2.invoke(null);
                    } catch (NoSuchMethodException nsme2) {
                        Log.e("MoreOptionsActivity", "No suitable scheduleAllNotifications method found", nsme2);
                    }
                }
                Toast.makeText(this, "Notification schedule updated", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e("MoreOptionsActivity", "Error invoking NotificationSchedulerReceiver via reflection", e);
            }
        } catch (Exception e) {
            Log.e("MoreOptionsActivity", "Error applying notification settings", e);
        }
    }

    /**
     * Check if a class is available
     */
    private boolean isClassAvailable(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Send test notification with error handling
     */
    public void sendTestNotificationSafely() {
        try {
            // Check permission first
            if (!hasNotificationPermission()) {
                Toast.makeText(this, "Notification permission not granted", Toast.LENGTH_LONG).show();
                checkNotificationPermission();
                return;
            }

            // Get notification manager
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager == null) {
                Toast.makeText(this, "Notification service not available", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create intent with safer flags
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Create a simpler notification to reduce chance of errors
            androidx.core.app.NotificationCompat.Builder builder =
                    new androidx.core.app.NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_notifications)
                            .setContentTitle("MindMate Notification")
                            .setContentText("Notification system is working!")
                            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true);

            // Send notification
            notificationManager.notify(NOTIFICATION_ID, builder.build());
            Toast.makeText(this, "Test notification sent", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error sending notification: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Update the sendNotificationSettingsSavedConfirmation method
    private void sendNotificationSettingsSavedConfirmation() {
        try {
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager == null) return;

            Intent intent = new Intent(this, MoreOptionsActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Build a simple notification
            androidx.core.app.NotificationCompat.Builder builder =
                    new androidx.core.app.NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_notifications)
                            .setContentTitle("Settings Updated")
                            .setContentText("Your notification preferences have been saved")
                            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true);

            notificationManager.notify(NOTIFICATION_ID + 1, builder.build());
        } catch (Exception e) {
            // Silent failure - no need to disturb user with error for this notification
        }
    }

    // Improved notification channel creation
    private void createNotificationChannel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription(CHANNEL_DESC);
                channel.enableLights(true);
                channel.setLightColor(Color.BLUE);
                channel.enableVibration(true);

                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            }
        } catch (Exception e) {
            // We'll continue even if channel creation fails as it may work on some devices
        }
    }

    /**
     * Shows the about app dialog
     */
    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Create HTML formatted text for app info
        String aboutText = "<b>MindMate</b><br><br>" +
                "Version 1.1<br><br>" +
                "MindMate is your personal mental wellness companion, " +
                "designed to help you track your mood, practice mindfulness, " +
                "and improve your overall mental well-being.<br><br>" +
                "Developed by Micheal<br>" +
                "© 2025 All Rights Reserved";

        builder.setTitle("About MindMate")
                .setMessage(fromHtmlCompat(aboutText))
                .setPositiveButton("OK", null);

        builder.create().show();
    }

    /**
     * Shows privacy policy
     */
    private void showPrivacyPolicy() {
        // Open privacy policy in browser
        String url = "https://sites.google.com/view/mindmate-privacy-policy/home";
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No browser available to open privacy policy", Toast.LENGTH_SHORT).show();
        }
    }

    private void showHelpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String helpText = "<b>MindMate Help</b><br><br>" +
                "If you need assistance using MindMate, please visit our help center " +
                "or contact our support team for further guidance.<br><br>" +
                                "Contact Support: <a href=\"mailto:amanyamicheal770@gmail.com\">Email Us</a>";
        builder.setTitle("Help")
                .setMessage(fromHtmlCompat(helpText))
                .setPositiveButton("OK", null);
        // Make sure the dialog is actually shown
        builder.create().show();
    }

    /**
     * Shows terms of service
     */
    private void showTermsOfService() {
        // Show the terms of service in a dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String termsText = "<b>MindMate Terms of Service</b><br><br>" +
                "By using MindMate, you agree to our terms of service. " +
                "You are responsible for your use of the app and any data you input. " +
                "We reserve the right to modify these terms at any time. " +
                "For full terms, please visit our website.<a href=\"https://sites.google.com/view/mindmate-terms-of-service/home\">Terms of Service</a>.";
        builder.setTitle("Terms of Service")
                .setMessage(fromHtmlCompat(termsText))
                .setPositiveButton("OK", null);
        builder.create().show();
    }
    @SuppressWarnings("deprecation")
    private Spanned fromHtmlCompat(String html) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }

    /**
     * Temporary notification receiver/fallback.
     * If NotificationSchedulerReceiver exists, invoke its scheduling method; otherwise do a safe fallback.
     */
    private void createTempNotificationReceiver() {
        try {
            // Prefer to call the real scheduler if available
            String clsName = "micheal.must.signuplogin.receivers.NotificationSchedulerReceiver";
            if (isClassAvailable(clsName)) {
                try {
                    Class<?> cls = Class.forName(clsName);
                    // Try common signatures
                    try {
                        java.lang.reflect.Method m = cls.getMethod("scheduleAllNotifications", Context.class);
                        m.invoke(null, this);
                        return;
                    } catch (NoSuchMethodException ignored) { }
                    try {
                        java.lang.reflect.Method m2 = cls.getMethod("scheduleAllNotifications");
                        m2.invoke(null);
                        return;
                    } catch (NoSuchMethodException ignored) { }
                    // If method signatures don't match, attempt a best-effort: search any static no-arg method named scheduleAllNotifications
                    for (java.lang.reflect.Method mm : cls.getMethods()) {
                        if (mm.getName().equals("scheduleAllNotifications") && (mm.getParameterCount() == 0 || (mm.getParameterCount() == 1 && mm.getParameterTypes()[0] == Context.class))) {
                            if (mm.getParameterCount() == 0) mm.invoke(null);
                            else mm.invoke(null, this);
                            return;
                        }
                    }
                    Log.w("MoreOptionsActivity", "NotificationSchedulerReceiver found but no supported scheduleAllNotifications signature.");
                } catch (Exception e) {
                    Log.e("MoreOptionsActivity", "Error invoking NotificationSchedulerReceiver", e);
                }
            }

            // Fallback: simple demo notification (no scheduling) to confirm settings saved
            if (!hasNotificationPermission()) {
                Log.w("MoreOptionsActivity", "No notification permission - skipping demo notification");
                Toast.makeText(this, "Notification settings saved (scheduling not available)", Toast.LENGTH_SHORT).show();
                return;
            }

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager == null) {
                Toast.makeText(this, "Notification manager unavailable", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, MoreOptionsActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            androidx.core.app.NotificationCompat.Builder nb = new androidx.core.app.NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notifications)
                    .setContentTitle("MindMate")
                    .setContentText("Notification preferences saved")
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT);

            notificationManager.notify(NOTIFICATION_ID + 3, nb.build());
            Toast.makeText(this, "Notification settings saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("MoreOptionsActivity", "createTempNotificationReceiver failed", e);
            // Silent safe fallback
        }
    }

    private void setupFontOptions() {
        // Example: If you have a spinner for font selection
        // spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        //     @Override
        //     public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //         String selectedFont = getSelectedFontPath(position);
        //         saveFontPreference(selectedFont);
        //         applyFontToActivity(selectedFont);
        //     }
        //     ...
        // });
    }

    /**
     * Save selected font to SharedPreferences
     */
    private void saveFontPreference(String fontPath) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selected_font", fontPath);
        editor.apply();
    }

    /**
     * Get font path based on user selection
     */
    private String getSelectedFontPath(int position) {
        String[] fonts = {
                "fonts/Roboto-Regular.ttf",
                "fonts/OpenSans-Regular.ttf",
                "fonts/Raleway-Regular.ttf",
                "fonts/Montserrat-Regular.ttf",
                "fonts/Lato-Regular.ttf"
        };
        return (position < fonts.length) ? fonts[position] : fonts[0];
    }

    /**
     * Apply font to current activity
     */
    private void applyFontToActivity(String fontPath) {
        try {
            Typeface typeface = Typeface.createFromAsset(getAssets(), fontPath);
            View rootView = getWindow().getDecorView().getRootView();
            applyFontToViews(rootView, typeface);
        } catch (Exception e) {
            Log.w("FontError", "Error loading font: " + e.getMessage());
        }
    }

    /**
     * Recursively apply font to all TextViews and EditTexts
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
}
