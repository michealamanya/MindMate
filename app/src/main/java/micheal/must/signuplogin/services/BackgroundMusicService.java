package micheal.must.signuplogin.services;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;

public class BackgroundMusicService extends Service {
    private static final String TAG = "BackgroundMusicService";
    private MediaPlayer mediaPlayer;
    private static final String MUSIC_FILE = "My Heart Will Go On.mp3"; // Ensure exact filename match

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setLooping(true); // Loop the music
        mediaPlayer.setVolume(0.5f, 0.5f); // Set volume (0.0 to 1.0)
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mediaPlayer.isPlaying()) {
            try {
                AssetFileDescriptor afd = getAssets().openFd(MUSIC_FILE);
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
                mediaPlayer.prepare();
                mediaPlayer.start();
                Log.d(TAG, "Background music started");
            } catch (IOException e) {
                Log.e(TAG, "Error playing background music: " + e.getMessage());
                e.printStackTrace();
            } catch (IllegalStateException e) {
                Log.w(TAG, "MediaPlayer in illegal state, resetting");
                mediaPlayer.reset();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
