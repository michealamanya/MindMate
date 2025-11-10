package micheal.must.signuplogin.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import micheal.must.signuplogin.DashboardActivity;

/**
 * Receiver for handling notification actions like "Check In" or other quick actions
 * from notifications without opening the full app UI.
 */
public class NotificationActionReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    public static final String ACTION_CHECK_IN = "micheal.must.signuplogin.ACTION_CHECK_IN";
    public static final String ACTION_REMINDER = "micheal.must.signuplogin.ACTION_REMINDER";
    public static final String EXTRA_NOTIFICATION_ID = "notification_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Received action: " + action);

        if (action == null) return;

        switch (action) {
            case ACTION_CHECK_IN:
                performCheckIn(context);
                break;

            case ACTION_REMINDER:
                handleReminder(context, intent);
                break;

            default:
                // Launch the app dashboard for other actions
                Intent launchIntent = new Intent(context, DashboardActivity.class);
                launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);
                break;
        }
    }

    private void performCheckIn(Context context) {
        // Handle check-in action - record user check-in or open check-in dialog
        Intent checkInIntent = new Intent(context, DashboardActivity.class);
        checkInIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        checkInIntent.putExtra("open_check_in", true);
        context.startActivity(checkInIntent);
    }

    private void handleReminder(Context context, Intent intent) {
        // Handle reminder action - maybe dismiss the reminder or reschedule
        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0);
        if (notificationId > 0) {
            // Cancel this specific notification
            android.app.NotificationManager notificationManager =
                    (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
        }
    }
}
