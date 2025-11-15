package micheal.must.signuplogin.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

/**
 * Background Service for managing meditation sessions
 * Implements Android Service requirement for background tasks
 */
public class MeditationService extends Service {
    private static final String TAG = "MeditationService";
    private MediaPlayer mediaPlayer;
    private final IBinder binder = new LocalBinder();
    private boolean isPlaying = false;

    /**
     * Local binder for in-process communication
     */
    public class LocalBinder extends Binder {
        MeditationService getService() {
            return MeditationService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "🎵 Meditation Service Created");
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            String videoId = intent.getStringExtra("video_id");
            int duration = intent.getIntExtra("duration", 5);

            if ("START_MEDITATION".equals(action)) {
                startMeditationSession(videoId, duration);
            } else if ("STOP_MEDITATION".equals(action)) {
                stopMeditationSession();
            }
        }

        return START_STICKY;
    }

    /**
     * Start a meditation session
     */
    public void startMeditationSession(String videoId, int durationMinutes) {
        Log.d(TAG, "🧘 Starting meditation: " + videoId + " (" + durationMinutes + "min)");
        
        // Log session to SharedPreferences
        SharedPreferences prefs = getSharedPreferences("meditation_sessions", MODE_PRIVATE);
        long timestamp = System.currentTimeMillis();
        
        prefs.edit()
                .putString("session_" + timestamp, "Meditation Session")
                .putInt("duration_" + timestamp, durationMinutes)
                .putLong("timestamp_" + timestamp, timestamp)
                .apply();
        
        isPlaying = true;
        
        // Notify user
        sendBroadcast(new Intent("MEDITATION_STARTED"));
    }

    /**
     * Stop meditation session
     */
    public void stopMeditationSession() {
        Log.d(TAG, "⏹️ Stopping meditation");
        
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        
        isPlaying = false;
        sendBroadcast(new Intent("MEDITATION_STOPPED"));
    }

    /**
     * Check if meditation is playing
     */
    public boolean isPlaying() {
        return isPlaying;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        Log.d(TAG, "🎵 Meditation Service Destroyed");
    }
}
