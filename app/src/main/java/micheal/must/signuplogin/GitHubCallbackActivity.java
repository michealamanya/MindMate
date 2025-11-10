package micheal.must.signuplogin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class GitHubCallbackActivity extends AppCompatActivity {

    private static final String TAG = "GitHubCallback";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri uri = getIntent().getData();
        if (uri != null) {
            String code = uri.getQueryParameter("code");
            String error = uri.getQueryParameter("error");

            if (code != null) {
                Log.d(TAG, "GitHub authorization code received");
                
                // Send code back to MainActivity
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("github_code", code);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } else if (error != null) {
                Log.e(TAG, "GitHub OAuth error: " + error);
            }
        }

        finish();
    }
}
