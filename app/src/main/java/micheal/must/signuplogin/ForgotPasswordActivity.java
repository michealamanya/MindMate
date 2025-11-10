package micheal.must.signuplogin;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText resetEmail;
    private Button resetButton;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private TextInputLayout emailLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Firebase instance
        mAuth = FirebaseAuth.getInstance();

        // Bind UI
        resetEmail = findViewById(R.id.reset_email);
        resetButton = findViewById(R.id.reset_button);

        // Try to get email layout if it exists in the updated layout
        emailLayout = findViewById(R.id.email_layout);

        // Add progress bar programmatically
        addProgressBar();

        // Check for back button in the layout and set up listener if it exists
        try {
            ImageButton backButton = findViewById(R.id.back_button);
            if (backButton != null) {
                backButton.setOnClickListener(v -> finish());
            }
        } catch (Exception e) {
            // Button or resource might not exist, safely ignore
            Log.e("ForgotPasswordActivity", "Back button setup failed", e);
        }

        resetButton.setOnClickListener(v -> {
            if (validateEmail()) {
                if (isNetworkAvailable()) {
                    resetPassword();
                } else {
                    showNetworkErrorDialog();
                }
            }
        });
    }

    /**
     * Add a progress bar programmatically
     */
    private void addProgressBar() {
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        View mainView = findViewById(R.id.main);
        if (mainView instanceof FrameLayout) {
            FrameLayout layout = (FrameLayout) mainView;
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = android.view.Gravity.CENTER;
            progressBar.setLayoutParams(params);
            progressBar.setVisibility(View.GONE);
            layout.addView(progressBar);
        } else if (mainView instanceof LinearLayout) {
            LinearLayout layout = (LinearLayout) mainView;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = android.view.Gravity.CENTER;
            progressBar.setLayoutParams(params);
            progressBar.setVisibility(View.GONE);
            layout.addView(progressBar);
        }
    }

    /**
     * Validate email input with improved validation
     */
    private boolean validateEmail() {
        String email = resetEmail.getText().toString().trim();

        if (email.isEmpty()) {
            if (emailLayout != null) {
                emailLayout.setError("Email is required");
            } else {
                resetEmail.setError("Email is required");
                resetEmail.requestFocus();
            }
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (emailLayout != null) {
                emailLayout.setError("Please enter a valid email address");
            } else {
                resetEmail.setError("Please enter a valid email address");
                resetEmail.requestFocus();
            }
            return false;
        } else {
            if (emailLayout != null) {
                emailLayout.setError(null);
            } else {
                resetEmail.setError(null);
            }
            return true;
        }
    }

    /**
     * Check network connectivity
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Show network error dialog
     */
    private void showNetworkErrorDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("You need to be online to reset your password. Please check your internet connection and try again.")
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * Send reset email with loading indicator
     */
    private void resetPassword() {
        String email = resetEmail.getText().toString().trim();

        // Show loading
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        resetButton.setEnabled(false);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    // Hide loading
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    resetButton.setEnabled(true);

                    if (task.isSuccessful()) {
                        showSuccessDialog();
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() :
                                "Failed to send reset email";
                        Toast.makeText(this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Show success dialog and redirect to login
     */
    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Reset Link Sent")
                .setMessage("Check your email for instructions to reset your password")
                .setPositiveButton("Go to Login", (dialog, which) -> {
                    Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}
