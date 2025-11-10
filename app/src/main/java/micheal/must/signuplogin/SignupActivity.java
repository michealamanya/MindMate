package micheal.must.signuplogin;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText signupName, signupEmail, signupUsername, signupPassword;
    private TextInputLayout nameLayout, emailLayout, usernameLayout, passwordLayout;
    private TextView loginRedirectText;
    private MaterialButton signupButton;
    private CheckBox termsCheckbox;
    private ImageButton backButton;
    private ProgressBar progressBar;

    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");
        mAuth = FirebaseAuth.getInstance();

        // Bind UI elements
        signupName = findViewById(R.id.signup_name);
        signupEmail = findViewById(R.id.signup_email);
        signupUsername = findViewById(R.id.signup_username);
        signupPassword = findViewById(R.id.signup_password);

        nameLayout = findViewById(R.id.name_layout);
        emailLayout = findViewById(R.id.email_layout);
        usernameLayout = findViewById(R.id.username_layout);
        passwordLayout = findViewById(R.id.password_layout);

        loginRedirectText = findViewById(R.id.to_login);
        signupButton = findViewById(R.id.signup_button);
        termsCheckbox = findViewById(R.id.terms_checkbox);
        backButton = findViewById(R.id.btn_back);
        progressBar = findViewById(R.id.signup_progress);

        // Set up back button
        backButton.setOnClickListener(v -> onBackPressed());

        // Set up signup button click listener
        signupButton.setOnClickListener(v -> {
            if (validateInputs()) {
                // Check network connection
                if (!isNetworkAvailable()) {
                    showNetworkErrorDialog();
                    return;
                }

                // Show loading state
                showLoading(true);

                // Get user input
                String name = signupName.getText().toString().trim();
                String email = signupEmail.getText().toString().trim();
                String username = signupUsername.getText().toString().trim();
                String password = signupPassword.getText().toString().trim();

                // Create Firebase Auth user first
                createFirebaseAuthUser(name, email, username, password);
            }
        });

        // Login redirect
        loginRedirectText.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Create a Firebase Auth user and then add to Realtime Database
     */
    private void createFirebaseAuthUser(String name, String email, String username, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Now save user data to Realtime Database
                        HelperClass helperClass = new HelperClass(name, email, username, password);
                        reference.child(username).setValue(helperClass)
                                .addOnSuccessListener(aVoid -> {
                                    showLoading(false);
                                    Toast.makeText(SignupActivity.this, "Signed up successfully!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    showLoading(false);
                                    Toast.makeText(SignupActivity.this, "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        showLoading(false);
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Authentication failed";
                        Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Validate all user inputs
     */
    private boolean validateInputs() {
        boolean isValid = true;

        // Validate name
        String name = signupName.getText().toString().trim();
        if (name.isEmpty()) {
            nameLayout.setError("Name is required");
            isValid = false;
        } else {
            nameLayout.setError(null);
        }

        // Validate email
        String email = signupEmail.getText().toString().trim();
        if (email.isEmpty()) {
            emailLayout.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Please enter a valid email");
            isValid = false;
        } else {
            emailLayout.setError(null);
        }

        // Validate username
        String username = signupUsername.getText().toString().trim();
        if (username.isEmpty()) {
            usernameLayout.setError("Username is required");
            isValid = false;
        } else if (username.length() < 4) {
            usernameLayout.setError("Username must be at least 4 characters");
            isValid = false;
        } else {
            usernameLayout.setError(null);
        }

        // Validate password
        String password = signupPassword.getText().toString().trim();
        if (password.isEmpty()) {
            passwordLayout.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            passwordLayout.setError(null);
        }

        // Validate terms checkbox
        if (!termsCheckbox.isChecked()) {
            Toast.makeText(this, "Please agree to the Terms of Service", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    /**
     * Toggle loading state
     */
    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            signupButton.setEnabled(!isLoading);
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
        runOnUiThread(() -> {
            try {
                AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
                builder.setTitle("No Internet Connection");
                builder.setMessage("You need to be online to sign up. Please check your internet connection and try again.");
                builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                builder.setCancelable(false);
                AlertDialog dialog = builder.create();
                dialog.show();
            } catch (Exception e) {
                // If dialog fails, fall back to Toast
                Toast.makeText(SignupActivity.this,
                        "No internet connection. You need to be online to sign up.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
