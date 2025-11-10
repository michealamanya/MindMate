package micheal.must.signuplogin.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.button.MaterialButton;

import micheal.must.signuplogin.MoreOptionsActivity;
import micheal.must.signuplogin.R;

public class MorePreferencesFragment extends Fragment {

    private MaterialButton btnLanguage, btnReminders, btnAppearance, btnNotifications;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_more_preferences, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        initViews(view);
        setupClickListeners();
    }

    private void initViews(View view) {
        btnLanguage = view.findViewById(R.id.btn_language);
        btnReminders = view.findViewById(R.id.btn_reminders);
        btnAppearance = view.findViewById(R.id.btn_appearance);
        btnNotifications = view.findViewById(R.id.btn_notifications);
    }

    private void setupClickListeners() {
        if (btnLanguage != null) {
            btnLanguage.setOnClickListener(v -> 
                Toast.makeText(getContext(), "Language settings coming soon", Toast.LENGTH_SHORT).show());
        }
        if (btnReminders != null) {
            btnReminders.setOnClickListener(v -> showReminderOptions());
        }
        if (btnAppearance != null) {
            btnAppearance.setOnClickListener(v -> 
                Toast.makeText(getContext(), "Theme is dark by default", Toast.LENGTH_SHORT).show());
        }
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> showNotificationSettings());
        }
    }

    private void showReminderOptions() {
        new AlertDialog.Builder(getContext())
                .setTitle("Reminder Frequency")
                .setItems(new CharSequence[]{"Never", "Daily", "Weekly", "Monthly"}, (dialog, which) -> {
                    String[] frequencies = {"Never", "Daily", "Weekly", "Monthly"};
                    sharedPreferences.edit().putString("reminder_frequency", frequencies[which]).apply();
                    Toast.makeText(getContext(), "Reminder set to: " + frequencies[which], Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showNotificationSettings() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_notifications, null);
            builder.setView(dialogView);

            SwitchCompat switchCheckIn = dialogView.findViewById(R.id.switch_checkin);
            SwitchCompat switchMeditation = dialogView.findViewById(R.id.switch_meditation);
            SwitchCompat switchJournal = dialogView.findViewById(R.id.switch_journal);
            SwitchCompat switchTips = dialogView.findViewById(R.id.switch_tips);

            // Load saved preferences
            if (switchCheckIn != null) {
                switchCheckIn.setChecked(sharedPreferences.getBoolean("notify_checkin", true));
            }
            if (switchMeditation != null) {
                switchMeditation.setChecked(sharedPreferences.getBoolean("notify_meditation", true));
            }
            if (switchJournal != null) {
                switchJournal.setChecked(sharedPreferences.getBoolean("notify_journal", true));
            }
            if (switchTips != null) {
                switchTips.setChecked(sharedPreferences.getBoolean("notify_tips", true));
            }

            MaterialButton btnTestNotification = dialogView.findViewById(R.id.btn_test_notification);
            if (btnTestNotification != null) {
                btnTestNotification.setOnClickListener(v -> {
                    if (getActivity() instanceof MoreOptionsActivity) {
                        ((MoreOptionsActivity) getActivity()).sendTestNotificationSafely();
                    }
                });
            }

            builder.setPositiveButton("Save", (dialog, which) -> {
                SharedPreferences.Editor editor = sharedPreferences.edit();

                if (switchCheckIn != null) {
                    editor.putBoolean("notify_checkin", switchCheckIn.isChecked());
                }
                if (switchMeditation != null) {
                    editor.putBoolean("notify_meditation", switchMeditation.isChecked());
                }
                if (switchJournal != null) {
                    editor.putBoolean("notify_journal", switchJournal.isChecked());
                }
                if (switchTips != null) {
                    editor.putBoolean("notify_tips", switchTips.isChecked());
                }

                editor.apply();
                Toast.makeText(getContext(), "Notification settings saved", Toast.LENGTH_SHORT).show();
            }).setNegativeButton("Cancel", null).show();

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error opening notification settings", Toast.LENGTH_SHORT).show();
        }
    }
}
