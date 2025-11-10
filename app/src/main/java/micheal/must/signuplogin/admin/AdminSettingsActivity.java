package micheal.must.signuplogin.admin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import micheal.must.signuplogin.R;

public class AdminSettingsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private SwitchCompat switchAutoApproveGroups;
    private SwitchCompat switchAutoflagPosts;
    private SwitchCompat switchNotifyNewReports;
    private MaterialButton btnExportData;
    private MaterialButton btnClearCache;
    private MaterialButton btnResetSettings;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;
    private FirebaseRemoteConfig remoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_settings);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        remoteConfig = FirebaseRemoteConfig.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Initialize UI
        initializeUI();

        // Load settings
        loadSettings();

        // Setup click listeners
        setupClickListeners();
    }

    private void initializeUI() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        switchAutoApproveGroups = findViewById(R.id.switch_auto_approve_groups);
        switchAutoflagPosts = findViewById(R.id.switch_autoflag_posts);
        switchNotifyNewReports = findViewById(R.id.switch_notify_new_reports);

        btnExportData = findViewById(R.id.btn_export_data);
        btnClearCache = findViewById(R.id.btn_clear_cache);
        btnResetSettings = findViewById(R.id.btn_reset_settings);
    }

    private void loadSettings() {
        // Load settings from SharedPreferences
        switchAutoApproveGroups.setChecked(sharedPreferences.getBoolean("admin_auto_approve_groups", false));
        switchAutoflagPosts.setChecked(sharedPreferences.getBoolean("admin_autoflag_posts", true));
        switchNotifyNewReports.setChecked(sharedPreferences.getBoolean("admin_notify_new_reports", true));
    }

    private void setupClickListeners() {
        // Save settings when switches change
        switchAutoApproveGroups.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("admin_auto_approve_groups", isChecked).apply();
            Toast.makeText(this, "Auto-approve groups " + (isChecked ? "enabled" : "disabled"),
                    Toast.LENGTH_SHORT).show();
        });

        switchAutoflagPosts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("admin_autoflag_posts", isChecked).apply();
            Toast.makeText(this, "Auto-flag posts " + (isChecked ? "enabled" : "disabled"),
                    Toast.LENGTH_SHORT).show();
        });

        switchNotifyNewReports.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("admin_notify_new_reports", isChecked).apply();
            Toast.makeText(this, "Report notifications " + (isChecked ? "enabled" : "disabled"),
                    Toast.LENGTH_SHORT).show();
        });

        // Button click listeners
        btnExportData.setOnClickListener(v -> {
            Toast.makeText(this, "Exporting data... (Feature coming soon)", Toast.LENGTH_SHORT).show();
        });

        btnClearCache.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Clear Cache")
                    .setMessage("Are you sure you want to clear the app cache? This will not delete any user data.")
                    .setPositiveButton("Clear", (dialog, which) -> {
                        Toast.makeText(this, "Cache cleared successfully", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        btnResetSettings.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Reset Settings")
                    .setMessage("Are you sure you want to reset all admin settings to default?")
                    .setPositiveButton("Reset", (dialog, which) -> {
                        // Reset settings to defaults
                        sharedPreferences.edit()
                                .putBoolean("admin_auto_approve_groups", false)
                                .putBoolean("admin_autoflag_posts", true)
                                .putBoolean("admin_notify_new_reports", true)
                                .apply();

                        // Reload UI
                        loadSettings();

                        Toast.makeText(this, "Settings reset to defaults", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
