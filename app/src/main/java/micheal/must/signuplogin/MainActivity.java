package micheal.must.signuplogin;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.analytics.FirebaseAnalytics;

import android.os.Handler;
import android.os.Looper;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Button buttonSignup, buttonLogin, buttonGoogle;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private FirebaseAnalytics mFirebaseAnalytics;

    // Consent preference key and state
    private static final String PREF_CONSENT_SHARE = "pref_consent_share";
    private SharedPreferences sharedPrefs;
    private Boolean consentToShareData = null; // null = not decided / not remembered

    // --- new: fallback GoogleSignInClient and handler for timeout ---
    private GoogleSignInClient googleSignInClient;
    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private final long ONE_TAP_TIMEOUT_MS = 4000;

    // --- new: loading dialogs ---
    private ProgressDialog loadingDialog;
    private ProgressDialog accountSelectionDialog;

    // ActivityResultLauncher to replace startIntentSenderForResult
    private final ActivityResultLauncher<IntentSenderRequest> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            result -> {
                timeoutHandler.removeCallbacksAndMessages(null);
                dismissAccountSelectionDialog();

                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    try {
                        SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                        String idToken = credential.getGoogleIdToken();
                        if (idToken != null) {
                            showLoadingDialog("Signing in...");
                            firebaseAuthWithGoogle(idToken);
                            return;
                        } else {
                            Log.w(TAG, "One Tap returned no idToken, falling back to classic GoogleSign-In");
                        }
                    } catch (ApiException e) {
                        Log.e(TAG, "One Tap sign-in exception: ", e);
                        Toast.makeText(MainActivity.this, "One Tap sign-in failed, using fallback", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.w(TAG, "One Tap result cancelled or null, falling back");
                }

                // fallback to classic Google Sign-In if One Tap didn't provide token
                launchGoogleSignInIntent();
            });

    // --- new: launcher for classic Google Sign-In Intent ---
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            (ActivityResult result) -> {
                dismissAccountSelectionDialog();

                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    try {
                        com.google.android.gms.auth.api.signin.GoogleSignInAccount account =
                                GoogleSignIn.getSignedInAccountFromIntent(result.getData()).getResult(ApiException.class);
                        if (account != null) {
                            String idToken = account.getIdToken();
                            if (idToken != null) {
                                showLoadingDialog("Signing in...");
                                firebaseAuthWithGoogle(idToken);
                                return;
                            }
                        }
                        Toast.makeText(MainActivity.this, "Google Sign-In failed to return token", Toast.LENGTH_SHORT).show();
                    } catch (ApiException e) {
                        Log.e(TAG, "GoogleSignIn API Exception: ", e);
                        Toast.makeText(MainActivity.this, "Google Sign-In error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.w(TAG, "Classic Google Sign-In cancelled or no data");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Handle system window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize SharedPreferences for consent
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPrefs.contains(PREF_CONSENT_SHARE)) {
            consentToShareData = sharedPrefs.getBoolean(PREF_CONSENT_SHARE, false);
        }

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("users");

        // Initialize One Tap client
        oneTapClient = Identity.getSignInClient(this);

        // Configure One Tap Sign-In request
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.default_web_client_id)) // From google-services.json
                        .setFilterByAuthorizedAccounts(false) // Allow choosing other accounts
                        .build())
                .setAutoSelectEnabled(false)
                .build();

        // --- new: prepare classic GoogleSignInClient fallback (requestIdToken must use same web client id) ---
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize Firebase Analytics
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Bind UI elements
        buttonSignup = findViewById(R.id.button_signup);
        buttonLogin = findViewById(R.id.button_login);
        buttonGoogle = findViewById(R.id.button_google);

        // Set click listeners
        buttonSignup.setOnClickListener(v -> {
            if (isInternetAvailable()) {
                startActivity(new Intent(MainActivity.this, SignupActivity.class));
            } else {
                showNoInternetDialog();
            }
        });

        buttonLogin.setOnClickListener(v -> {
            if (isInternetAvailable()) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            } else {
                showNoInternetDialog();
            }
        });

        // UPDATED: show consent dialog then sign-in (fast path)
        buttonGoogle.setOnClickListener(v -> {
            if (isInternetAvailable()) {
                showConsentAndSignIn();
            } else {
                showNoInternetDialog();
            }
        });

        // Check if user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User already signed in, proceed to dashboard
            proceedToDashboard(currentUser);
        }
    }

    /**
     * Check if internet connection is available
     * @return true if internet is available, false otherwise
     */
    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        }
        return false;
    }

    /**
     * Show a dialog notifying the user of no internet connection
     * with an option to go to settings
     */
    private void showNoInternetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Internet Connection");
        builder.setMessage("Please connect to the internet to continue with login or signup.");
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        // Add "Go to Settings" button
        builder.setPositiveButton("Go to Settings", (dialog, which) -> {
            Intent settingsIntent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
            startActivity(settingsIntent);
        });

        // Add "Cancel" button
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Make it non-cancelable so user must make a choice
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /** Start Google One Tap sign-in using ActivityResultLauncher with timeout fallback */
    private void signInWithGoogle() {
        showAccountSelectionDialog();
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.METHOD, "one_tap");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);

        // Start One Tap
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this, result -> {
                    try {
                        // Cancel any previous fallback requests
                        timeoutHandler.removeCallbacksAndMessages(null);

                        IntentSenderRequest intentSenderRequest =
                                new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build();

                        // start One Tap; also schedule a timeout fallback just in case
                        signInLauncher.launch(intentSenderRequest);

                        // schedule fallback in case One Tap UI doesn't appear quickly
                        timeoutHandler.postDelayed(() -> {
                            Log.w(TAG, "One Tap timed out; launching classic Google Sign-In");
                            // cancel one-tap if possible (no direct cancel API) and fallback
                            launchGoogleSignInIntent();
                        }, ONE_TAP_TIMEOUT_MS);

                    } catch (Exception e) {
                        Log.e(TAG, "Google One Tap Sign-In failed to launch, falling back", e);
                        // immediate fallback
                        launchGoogleSignInIntent();
                    }
                })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "One Tap beginSignIn failed, using classic GoogleSignIn", e);
                    // immediate fallback
                    launchGoogleSignInIntent();
                });
    }

    // Launch classic Google Sign-In intent
    private void launchGoogleSignInIntent() {
        // Cancel any pending One Tap fallback callbacks
        timeoutHandler.removeCallbacksAndMessages(null);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    /**
     * Show consent dialog if needed, then start fast sign-in (classic Google intent).
     * If the user already remembered a choice, skip dialog and use stored value.
     */
    private void showConsentAndSignIn() {
        // If user already remembered a choice, just proceed
        if (consentToShareData != null) {
            // proceed to fast sign-in (classic account chooser)
            fastSignIn();
            return;
        }

        // Build a simple custom dialog with "Remember my choice" checkbox
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        // Message TextView
        android.widget.TextView tv = new android.widget.TextView(this);
        tv.setText("Do you allow the app to store your basic profile (name, email) for personalization? " +
                "You can choose to remember this preference.");
        layout.addView(tv);

        // Remember checkbox
        CheckBox cbRemember = new CheckBox(this);
        cbRemember.setText("Remember my choice");
        layout.addView(cbRemember);

        new AlertDialog.Builder(this)
                .setTitle("Share Profile Data?")
                .setView(layout)
                .setPositiveButton("Allow", (dialog, which) -> {
                    consentToShareData = true;
                    Bundle bundle = new Bundle();
                    bundle.putString("consent_status", "allowed");
                    mFirebaseAnalytics.logEvent("user_consent", bundle);
                    if (cbRemember.isChecked()) {
                        sharedPrefs.edit().putBoolean(PREF_CONSENT_SHARE, true).apply();
                    }
                    fastSignIn();
                })
                .setNegativeButton("Don't Allow", (dialog, which) -> {
                    consentToShareData = false;
                    Bundle bundle = new Bundle();
                    bundle.putString("consent_status", "denied");
                    mFirebaseAnalytics.logEvent("user_consent", bundle);
                    if (cbRemember.isChecked()) {
                        sharedPrefs.edit().putBoolean(PREF_CONSENT_SHARE, false).apply();
                    }
                    // Still sign in but won't save profile data
                    fastSignIn();
                })
                .setCancelable(true)
                .show();
    }

    /** Fast sign-in path: launch classic Google account chooser immediately */
    private void fastSignIn() {
        timeoutHandler.removeCallbacksAndMessages(null);
        showAccountSelectionDialog();
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    /** Authenticate with Firebase using Google ID token */
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserDataAndProceed(user);
                        }
                    } else {
                        dismissLoadingDialog();
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(MainActivity.this, "Firebase Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** Save user data conditionally and proceed to dashboard */
    private void saveUserDataAndProceed(FirebaseUser user) {
        // Only save profile data to RTDB if user consented
        if (Boolean.TRUE.equals(consentToShareData)) {
            databaseRef.child(user.getUid()).child("name").setValue(user.getDisplayName());
            databaseRef.child(user.getUid()).child("email").setValue(user.getEmail());
            databaseRef.child(user.getUid()).child("lastLogin").setValue(System.currentTimeMillis());
        } else {
            // Optionally write only lastLogin without PII, or skip entirely
            databaseRef.child(user.getUid()).child("lastLogin").setValue(System.currentTimeMillis());
        }

        // Continue with admin check and navigation
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("email", user.getEmail())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean isAdmin = false;
                    if (!querySnapshot.isEmpty()) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            if ("admin".equals(doc.getString("role"))) {
                                isAdmin = true;
                                break;
                            }
                        }
                    }
                    dismissLoadingDialog();
                    
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.METHOD, "google");
                    bundle.putString("user_role", isAdmin ? "admin" : "user");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
                    
                    if (isAdmin) {
                        Toast.makeText(MainActivity.this, "Welcome Admin, " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, micheal.must.signuplogin.admin.AdminDashboardActivity.class));
                    } else {
                        Toast.makeText(MainActivity.this, "Welcome, " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    dismissLoadingDialog();
                    Toast.makeText(MainActivity.this, "Welcome, " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                    finish();
                });
    }

    /** Navigate to dashboard or admin dashboard based on Firestore role */
    private void proceedToDashboard(FirebaseUser user) {
        if (user == null) {
            Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show();
            return;
        }
        showLoadingDialog("Loading dashboard...");
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    dismissLoadingDialog();
                    if (documentSnapshot.exists() && "admin".equals(documentSnapshot.getString("role"))) {
                        // User is admin, go to admin dashboard
                        Toast.makeText(MainActivity.this, "Welcome Admin, " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, micheal.must.signuplogin.admin.AdminDashboardActivity.class));
                        finish();
                    } else {
                        // Regular user, go to normal dashboard
                        Toast.makeText(MainActivity.this, "Welcome, " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    dismissLoadingDialog();
                    Toast.makeText(MainActivity.this, "Welcome, " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                    finish();
                });
    }

    /** Show loading dialog */
    private void showLoadingDialog(String message) {
        if (loadingDialog == null) {
            loadingDialog = new ProgressDialog(MainActivity.this);
            loadingDialog.setTitle("MindMate");
            loadingDialog.setCancelable(false);
            loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            loadingDialog.setIndeterminate(true);
            loadingDialog.setMessage(message);
            loadingDialog.show();
        } else {
            loadingDialog.setMessage(message);
            if (!loadingDialog.isShowing()) {
                loadingDialog.show();
            }
        }
    }

    /** Show account selection loading dialog */
    private void showAccountSelectionDialog() {
        if (accountSelectionDialog == null) {
            accountSelectionDialog = new ProgressDialog(MainActivity.this);
            accountSelectionDialog.setTitle("MindMate");
            accountSelectionDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            accountSelectionDialog.setIndeterminate(true);
            accountSelectionDialog.setMessage("Loading your Google accounts...");
            accountSelectionDialog.setCancelable(false);
            accountSelectionDialog.show();
        } else {
            if (!accountSelectionDialog.isShowing()) {
                accountSelectionDialog.show();
            }
        }
    }

    /** Dismiss loading dialog */
    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    /** Dismiss account selection dialog */
    private void dismissAccountSelectionDialog() {
        if (accountSelectionDialog != null && accountSelectionDialog.isShowing()) {
            accountSelectionDialog.dismiss();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in and update UI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            proceedToDashboard(currentUser);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissLoadingDialog();
        dismissAccountSelectionDialog();
        timeoutHandler.removeCallbacksAndMessages(null);
    }
}

