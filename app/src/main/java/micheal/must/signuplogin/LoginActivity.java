package micheal.must.signuplogin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText loginUsername, loginPassword;
    private TextInputLayout passwordLayout;
    private MaterialButton loginButton;
    private TextView toSignup;
    private TextView forgotPassword;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private CheckBox rememberMe;
    private MaterialButton biometricLoginButton;

    // For biometric authentication
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    // --- new: loading dialog ---
    private ProgressDialog loadingDialog;

    // Constants for SharedPreferences
    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_REMEMBER = "remember";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private int loginAttempts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Bind UI elements
        loginUsername = findViewById(R.id.login_username);
        loginPassword = findViewById(R.id.login_password);
        passwordLayout = findViewById(R.id.password_layout);
        loginButton = findViewById(R.id.login_button);
        toSignup = findViewById(R.id.to_signup);
        forgotPassword = findViewById(R.id.forgot_password);

        // New UI elements
        progressBar = findViewById(R.id.login_progress);
        rememberMe = findViewById(R.id.remember_me);
        biometricLoginButton = findViewById(R.id.biometric_login_button);

        // Handle system window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Remember Me functionality
        loadSavedCredentials();

        // Login button click
        loginButton.setOnClickListener(v -> {
            if (!validateUsername() || !validatePassword()) return;

            boolean isConnected = isNetworkAvailable();
            if (!isConnected) {
                showNetworkErrorDialog();
                return;
            }

            if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
                showMaxAttemptsReachedDialog();
                return;
            }

            loginAttempts++;
            showLoadingDialog("Authenticating...");
            checkUser();
        });

        // Redirect to signup activity
        toSignup.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignupActivity.class)));

        // Forgot password functionality
        forgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

        // Setup biometric authentication
        setupBiometricLogin();
    }

    private void setupBiometricLogin() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isBiometricEnabled = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false);

        if (biometricLoginButton != null) {
            biometricLoginButton.setVisibility(isBiometricEnabled ? View.VISIBLE : View.GONE);

            if (isBiometricEnabled) {
                executor = ContextCompat.getMainExecutor(this);
                biometricPrompt = new BiometricPrompt(this, executor,
                        new BiometricPrompt.AuthenticationCallback() {
                            @Override
                            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                                super.onAuthenticationSucceeded(result);

                                SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                                String savedUsername = prefs.getString(KEY_USERNAME, "");
                                String savedPassword = prefs.getString(KEY_PASSWORD, "");

                                if (!savedUsername.isEmpty() && !savedPassword.isEmpty()) {
                                    loginUsername.setText(savedUsername);
                                    loginPassword.setText(savedPassword);
                                    showLoadingDialog("Logging in...");
                                    checkUser();
                                } else {
                                    Toast.makeText(LoginActivity.this,
                                            "No saved credentials found", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                                super.onAuthenticationError(errorCode, errString);
                                Toast.makeText(LoginActivity.this,
                                        "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onAuthenticationFailed() {
                                super.onAuthenticationFailed();
                                Toast.makeText(LoginActivity.this,
                                        "Authentication failed", Toast.LENGTH_SHORT).show();
                            }
                        });

                promptInfo = new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Biometric Login")
                        .setSubtitle("Log in using your biometric credential")
                        .setNegativeButtonText("Cancel")
                        .build();

                biometricLoginButton.setOnClickListener(v ->
                        biometricPrompt.authenticate(promptInfo));
            }
        }
    }

    private void loadSavedCredentials() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isRemembered = prefs.getBoolean(KEY_REMEMBER, false);

        if (rememberMe != null) {
            rememberMe.setChecked(isRemembered);

            if (isRemembered) {
                String savedUsername = prefs.getString(KEY_USERNAME, "");
                String savedPassword = prefs.getString(KEY_PASSWORD, "");

                if (loginUsername != null) loginUsername.setText(savedUsername);
                if (loginPassword != null) loginPassword.setText(savedPassword);
            }
        }
    }

    private void saveCredentials() {
        if (rememberMe != null && rememberMe.isChecked()) {
            SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
            editor.putBoolean(KEY_REMEMBER, true);
            editor.putString(KEY_USERNAME, loginUsername.getText().toString().trim());
            editor.putString(KEY_PASSWORD, loginPassword.getText().toString().trim());
            editor.putBoolean(KEY_BIOMETRIC_ENABLED, true); // Enable biometric for next time
            editor.apply();
        } else {
            // Clear saved credentials
            clearSavedCredentials();
        }
    }

    private void clearSavedCredentials() {
        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(KEY_REMEMBER, false);
        editor.putString(KEY_USERNAME, "");
        editor.putString(KEY_PASSWORD, "");
        editor.putBoolean(KEY_BIOMETRIC_ENABLED, false);
        editor.apply();
    }

    /** Show loading dialog */
    private void showLoadingDialog(String message) {
        if (loadingDialog == null) {
            loadingDialog = new ProgressDialog(LoginActivity.this);
            loadingDialog.setTitle("MindMate");
            loadingDialog.setMessage(message);
            loadingDialog.setCancelable(false);
            loadingDialog.show();
        } else {
            loadingDialog.setMessage(message);
            loadingDialog.show();
        }
    }

    /** Dismiss loading dialog */
    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            loginButton.setEnabled(!isLoading);
        }
    }

    private void showMaxAttemptsReachedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Too Many Attempts")
                .setMessage("You've reached the maximum number of login attempts. Please try again later.")
                .setPositiveButton("OK", (dialog, which) -> {
                    loginAttempts = 0;
                })
                .setCancelable(false)
                .show();
    }

    /** Validate username input */
    private boolean validateUsername() {
        String usernameInput = loginUsername.getText().toString().trim();
        if (usernameInput.isEmpty()) {
            loginUsername.setError("Username is required");
            return false;
        } else {
            loginUsername.setError(null);
            return true;
        }
    }

    /** Validate password input */
    private boolean validatePassword() {
        String passwordInput = loginPassword.getText().toString().trim();
        if (passwordInput.isEmpty()) {
            loginPassword.setError("Password is required");
            return false;
        } else if (passwordInput.length() < 6) {
            loginPassword.setError("Password must be at least 6 characters");
            return false;
        } else {
            loginPassword.setError(null);
            return true;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        System.out.println("Network connection status: " + (isConnected ? "Connected" : "Disconnected"));

        return isConnected;
    }

    private void showNetworkErrorDialog() {
        runOnUiThread(() -> {
            try {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("No Internet Connection");
                builder.setMessage("You need to be online to log in. Please check your internet connection and try again.");
                builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                builder.setCancelable(false);
                AlertDialog dialog = builder.create();
                dialog.show();
            } catch (Exception e) {
                Toast.makeText(LoginActivity.this,
                        "No internet connection. You need to be online to log in.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Check if user exists and authenticate */
    private void checkUser() {
        String userUsername = loginUsername.getText().toString().trim();
        String userPassword = loginPassword.getText().toString().trim();

        if (userUsername.contains("@")) {
            authenticateWithFirebase(userUsername, userPassword);
        } else {
            lookupEmailAndAuthenticate(userUsername, userPassword);
        }
    }

    private void authenticateWithFirebase(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    dismissLoadingDialog();
                    if (task.isSuccessful()) {
                        loginAttempts = 0;
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveCredentials();
                        showLoadingDialog("Loading dashboard...");
                        proceedToDashboard(user, user.getDisplayName(), email);
                    } else {
                        handleAuthError(task.getException());
                    }
                });
    }

    private void handleAuthError(Exception exception) {
        String errorMessage = "Authentication failed";

        if (exception instanceof FirebaseAuthInvalidUserException) {
            errorMessage = "No user found with this email";
            loginUsername.setError(errorMessage);
            loginUsername.requestFocus();
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = "Invalid password";
            loginPassword.setError(errorMessage);
            loginPassword.requestFocus();
        } else if (exception != null) {
            errorMessage = exception.getMessage();
        }

        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void lookupEmailAndAuthenticate(String username, String password) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query query = reference.orderByChild("username").equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    dismissLoadingDialog();
                    loginUsername.setError("User does not exist");
                    loginUsername.requestFocus();
                    return;
                }

                DataSnapshot userSnapshot = snapshot.getChildren().iterator().next();
                String passwordFromDB = userSnapshot.child("password").getValue(String.class);
                String nameFromDB = userSnapshot.child("name").getValue(String.class);
                String emailFromDB = userSnapshot.child("email").getValue(String.class);

                if (passwordFromDB != null && passwordFromDB.equals(password)) {
                    loginAttempts = 0;
                    saveCredentials();

                    if (emailFromDB != null && !emailFromDB.isEmpty()) {
                        createFirebaseAuthSession(emailFromDB, password, nameFromDB, username);
                    } else {
                        String generatedEmail = username + "@mindmate.app";
                        createFirebaseAuthSession(generatedEmail, password, nameFromDB, username);
                    }
                } else {
                    dismissLoadingDialog();
                    loginPassword.setError("Invalid credentials");
                    loginPassword.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dismissLoadingDialog();
                Toast.makeText(LoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createFirebaseAuthSession(String email, String password, String name, String username) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        showLoadingDialog("Loading dashboard...");
                        proceedToDashboard(user, name, username);
                    } else {
                        createNewFirebaseUser(email, password, name, username);
                    }
                });
    }

    private void createNewFirebaseUser(String email, String password, String name, String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        showLoadingDialog("Loading dashboard...");
                        proceedToDashboard(user, name, username);
                    } else {
                        dismissLoadingDialog();
                        Toast.makeText(LoginActivity.this,
                                "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void proceedToDashboard(FirebaseUser user, String name, String username) {
        if (user != null) {
            com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        dismissLoadingDialog();
                        if (documentSnapshot.exists() && "admin".equals(documentSnapshot.getString("role"))) {
                            Toast.makeText(LoginActivity.this, "Welcome Admin, " + name, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, micheal.must.signuplogin.admin.AdminDashboardActivity.class);
                            intent.putExtra("username", username);
                            intent.putExtra("name", name);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Welcome, " + name, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                            intent.putExtra("username", username);
                            intent.putExtra("name", name);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        dismissLoadingDialog();
                        Toast.makeText(LoginActivity.this, "Welcome, " + name, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                        intent.putExtra("username", username);
                        intent.putExtra("name", name);
                        startActivity(intent);
                        finish();
                    });
        } else {
            dismissLoadingDialog();
            Toast.makeText(LoginActivity.this, "Authentication error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissLoadingDialog();
    }
}