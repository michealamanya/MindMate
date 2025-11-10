package micheal.must.signuplogin.receivers;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import java.util.Calendar;
import java.util.Random;

import micheal.must.signuplogin.DashboardActivity;
import micheal.must.signuplogin.R;

public class NotificationSchedulerReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationScheduler";
    private static final String CHANNEL_ID = "mindmate_channel";
    public static final String ACTION_SCHEDULED_NOTIFICATION = "micheal.must.signuplogin.ACTION_SCHEDULED_NOTIFICATION";
    public static final String EXTRA_NOTIFICATION_TYPE = "notification_type";

    public static final String TYPE_CHECKIN = "checkin";
    public static final String TYPE_MEDITATION = "meditation";
    public static final String TYPE_JOURNAL = "journal";
    public static final String TYPE_TIPS = "tips";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String action = intent.getAction();
            if (action == null) return;

            if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                // Reschedule all notifications after device reboot
                scheduleAllNotifications(context);
            }
            else if (action.equals(ACTION_SCHEDULED_NOTIFICATION)) {
                // Handle the notification that was triggered
                String notificationType = intent.getStringExtra(EXTRA_NOTIFICATION_TYPE);
                if (notificationType != null) {
                    sendNotification(context, notificationType);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onReceive", e);
        }
    }

    /**
     * Schedule all enabled notifications based on user preferences
     * This is a static method so it can be called from MoreOptionsActivity
     */
    public static void scheduleAllNotifications(Context context) {
        try {
            Log.d(TAG, "Starting to schedule notifications");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            // Check which notifications are enabled
            boolean checkInEnabled = prefs.getBoolean("notify_checkin", true);
            boolean meditationEnabled = prefs.getBoolean("notify_meditation", true);
            boolean journalEnabled = prefs.getBoolean("notify_journal", true);
            boolean tipsEnabled = prefs.getBoolean("notify_tips", true);

            // Get frequency setting
            String frequency = prefs.getString("reminder_frequency", "daily");
            Log.d(TAG, "Frequency setting: " + frequency);

            // As a simple test, schedule a notification for 15 seconds from now
            // This ensures the user sees something happening right away
            scheduleTestNotification(context);

            // Schedule each enabled notification
            if (checkInEnabled) {
                scheduleNotification(context, TYPE_CHECKIN, frequency, 9, 0); // 9:00 AM
            }

            if (meditationEnabled) {
                scheduleNotification(context, TYPE_MEDITATION, frequency, 7, 30); // 7:30 AM
            }

            if (journalEnabled) {
                scheduleNotification(context, TYPE_JOURNAL, frequency, 20, 0); // 8:00 PM
            }

            if (tipsEnabled) {
                scheduleNotification(context, TYPE_TIPS, frequency, 12, 0); // 12:00 PM
            }

            Log.d(TAG, "Finished scheduling notifications");
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling notifications", e);
        }
    }

    /**
     * Schedule a test notification to appear shortly
     */
    private static void scheduleTestNotification(Context context) {
        try {
            // Show a notification in 15 seconds as feedback
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, 15);

            Intent intent = new Intent(context, NotificationSchedulerReceiver.class);
            intent.setAction(ACTION_SCHEDULED_NOTIFICATION);
            intent.putExtra(EXTRA_NOTIFICATION_TYPE, "test");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    999, // unique code for test notifications
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                } else {
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                }
                Log.d(TAG, "Test notification scheduled for 15 seconds from now");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling test notification", e);
        }
    }

    /**
     * Schedule a specific notification
     */
    private static void scheduleNotification(Context context, String type, String frequency,
                                             int hour, int minute) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            // Create intent for the notification
            Intent intent = new Intent(context, NotificationSchedulerReceiver.class);
            intent.setAction(ACTION_SCHEDULED_NOTIFICATION);
            intent.putExtra(EXTRA_NOTIFICATION_TYPE, type);

            // Create unique ID for each notification type
            int requestCode = type.hashCode();

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // For simplicity in this implementation, we'll schedule a notification
            // for tomorrow at the specified time
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, 1);  // Tomorrow
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }

            Log.d(TAG, "Scheduled " + type + " notification for " + calendar.getTime());
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling " + type + " notification", e);
        }
    }

    /**
     * Send the actual notification
     */
    private static void sendNotification(Context context, String type) {
        try {
            // Create notification channel for Android 8.0+
            createNotificationChannel(context);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager == null) return;

            // Generate notification content based on type
            String title = "";
            String message = "";
            int notificationId = new Random().nextInt(1000) + 1;

            switch (type) {
                case TYPE_CHECKIN:
                    title = "Daily Check-in";
                    message = "How are you feeling today? Take a moment to check in with your emotions.";
                    break;

                case TYPE_MEDITATION:
                    title = "Meditation Reminder";
                    message = "Take a few minutes to meditate and clear your mind.";
                    break;

                case TYPE_JOURNAL:
                    title = "Journal Entry";
                    message = "Reflect on your day by writing in your journal.";
                    break;

                case TYPE_TIPS:
                    title = "Wellness Tip";
                    String[] tips = {
                            "Remember to take deep breaths when feeling stressed.",
                            "Drinking water can improve your mood and energy levels.",
                            "A short walk outside can boost your mental well-being.",
                            "Try practicing gratitude by noting three things you're thankful for."
                    };
                    title = "Today's Wellness Tip";
                    message = tips[new Random().nextInt(tips.length)];
                    break;

                case "test":
                    title = "Notifications Scheduled";
                    message = "Your notifications have been set up successfully and will appear at the scheduled times.";
                    break;
            }

            // Create the notification intent (opens app when clicked)
            Intent intent = new Intent(context, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("notification_type", type);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Build notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notifications)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            // Show notification
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Sent notification: " + title);
        } catch (Exception e) {
            Log.e(TAG, "Error sending notification", e);
        }
    }

    /**
     * Create notification channel for Android 8.0+
     */
    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "MindMate Notifications",
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                channel.setDescription("Scheduled notifications from MindMate");
                channel.enableLights(true);
                channel.setLightColor(Color.BLUE);
                channel.enableVibration(true);

                NotificationManager notificationManager =
                        context.getSystemService(NotificationManager.class);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error creating notification channel", e);
            }
        }
    }
}
