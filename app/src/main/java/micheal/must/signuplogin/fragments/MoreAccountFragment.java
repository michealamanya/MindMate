package micheal.must.signuplogin.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.bumptech.glide.Glide;

import micheal.must.signuplogin.MainActivity;
import micheal.must.signuplogin.R;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;

public class MoreAccountFragment extends Fragment {

    private static final String TAG = "MoreAccountFragment";
    private ShapeableImageView profileImageView;
    private MaterialButton btnProfile, btnChangePassword, btnNotifications, btnDeleteAccount, btnLogout;
    private FirebaseAuth mAuth;
    private SignInClient oneTapClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_more_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        oneTapClient = Identity.getSignInClient(getContext());

        initViews(view);
        loadUserProfile();
        setupClickListeners();
    }

    private void initViews(View view) {
        profileImageView = view.findViewById(R.id.iv_profile);
        btnProfile = view.findViewById(R.id.btn_profile);
        btnChangePassword = view.findViewById(R.id.btn_change_password);
//        btnNotifications = view.findViewById(R.id.btn_notification_settings);
        btnDeleteAccount = view.findViewById(R.id.btn_delete_account);
        btnLogout = view.findViewById(R.id.btn_logout);
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && profileImageView != null && user.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.default_profile)
                    .into(profileImageView);
        }
    }

    private void setupClickListeners() {
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> showProfileDialog());
        }
        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> 
                Toast.makeText(getContext(), "Change password feature coming soon", Toast.LENGTH_SHORT).show());
        }
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> 
                Toast.makeText(getContext(), "Notification settings", Toast.LENGTH_SHORT).show());
        }
        if (btnDeleteAccount != null) {
            btnDeleteAccount.setOnClickListener(v -> showDeleteAccountConfirmation());
        }
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> showSignOutConfirmation());
        }
    }

    private void showProfileDialog() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String profileText = "Name: " + (user.getDisplayName() != null ? user.getDisplayName() : "—") +
                "\nEmail: " + (user.getEmail() != null ? user.getEmail() : "—");

        new AlertDialog.Builder(getContext())
                .setTitle("Profile")
                .setMessage(profileText)
                .setPositiveButton("Close", null)
                .show();
    }

    private void showDeleteAccountConfirmation() {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone. All your data will be permanently removed from our system.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        Toast.makeText(getContext(), "Deleting account...", Toast.LENGTH_SHORT).show();

        // Delete user data from Firebase Realtime Database
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        userRef.removeValue().addOnSuccessListener(aVoid -> {
            Log.d(TAG, "User data deleted from database");
            deleteUserAuthAccount(user);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error deleting user data: " + e.getMessage());
            Toast.makeText(getContext(), "Error deleting account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void deleteUserAuthAccount(FirebaseUser user) {
        user.delete().addOnSuccessListener(aVoid -> {
            Log.d(TAG, "User account deleted from Firebase Auth");
            mAuth.signOut();
            oneTapClient.signOut().addOnCompleteListener(task -> {
                Toast.makeText(getContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                navigateToLogin();
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error deleting user account: " + e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("credential")) {
                Toast.makeText(getContext(), "Please sign in again to delete your account", Toast.LENGTH_LONG).show();
                showReauthenticationDialog(user);
            } else {
                Toast.makeText(getContext(), "Error deleting account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showReauthenticationDialog(FirebaseUser user) {
        new AlertDialog.Builder(getContext())
                .setTitle("Reauthentication Required")
                .setMessage("Your session has expired. Please sign in again to delete your account.")
                .setPositiveButton("OK", (dialog, which) -> {
                    mAuth.signOut();
                    navigateToLogin();
                })
                .setCancelable(false)
                .show();
    }

    private void showSignOutConfirmation() {
        new AlertDialog.Builder(getContext())
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out", (dialog, which) -> performSignOut())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performSignOut() {
        mAuth.signOut();
        oneTapClient.signOut().addOnCompleteListener(task -> {
            Toast.makeText(getContext(), "Signed out successfully", Toast.LENGTH_SHORT).show();
            navigateToLogin();
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}
