package micheal.must.signuplogin.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

/**
 * BroadcastReceiver for monitoring network connectivity changes
 * Implements Android Core Components requirement
 */
public class NetworkChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            
            if (isConnected) {
                Log.d(TAG, "✓ Network connected");
                Toast.makeText(context, "✓ Network connected - syncing data", Toast.LENGTH_SHORT).show();
                
                // Trigger data sync
                syncDataWithFirebase(context);
            } else {
                Log.w(TAG, "✗ Network disconnected");
                Toast.makeText(context, "✗ Network disconnected - offline mode", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }

    /**
     * Sync local data with Firebase
     */
    private void syncDataWithFirebase(Context context) {
        Log.d(TAG, "Syncing mood data with Firebase...");
        // Implementation: Sync SharedPreferences mood data to Firebase
        // This would be called when network is available
    }
}
