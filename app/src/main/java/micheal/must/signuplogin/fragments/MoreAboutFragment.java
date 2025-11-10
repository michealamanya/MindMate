package micheal.must.signuplogin.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import micheal.must.signuplogin.R;

public class MoreAboutFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_more_about, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // About App Section
        setupAboutSection(view);

        // Contact & Social Media Section
        setupContactSection(view);

        // Help & Support Section
        setupHelpSection(view);
    }

    private void setupAboutSection(View view) {
        TextView tvAppInfo = view.findViewById(R.id.tv_app_info);
        String aboutText = "<b>MindMate v1.1</b><br><br>" +
                "Your Personal Mental Wellness Companion<br><br>" +
                "MindMate is designed to help you track your mood, practice mindfulness, " +
                "join supportive communities, and improve your overall mental well-being.<br><br>" +
                "<b>Developed by Micheal Amanya</b><br>" +
                "© 2025 All Rights Reserved";
        tvAppInfo.setText(fromHtmlCompat(aboutText));
    }

    private void setupContactSection(View view) {
        LinearLayout contactContainer = view.findViewById(R.id.contact_container);

        // WhatsApp Button
        MaterialButton btnWhatsApp = view.findViewById(R.id.btn_whatsapp);
        btnWhatsApp.setOnClickListener(v -> openWhatsApp("+256740470116"));

        // Email Button
        MaterialButton btnEmail = view.findViewById(R.id.btn_email);
        btnEmail.setOnClickListener(v -> sendEmail("amanyamicheal770@gmail.com"));

        // Twitter Button
        MaterialButton btnTwitter = view.findViewById(R.id.btn_twitter);
        btnTwitter.setOnClickListener(v -> openURL("https://x.com/amanyamicheal_a"));

        // GitHub Button
        MaterialButton btnGitHub = view.findViewById(R.id.btn_github);
        btnGitHub.setOnClickListener(v -> openURL("https://github.com/michealamanya"));

        // LinkedIn Button
        MaterialButton btnLinkedIn = view.findViewById(R.id.btn_linkedin);
        btnLinkedIn.setOnClickListener(v -> openURL("https://www.linkedin.com/in/amanya-micheal-778a9234a"));
    

        // Instagram Button
        MaterialButton btnInstagram = view.findViewById(R.id.btn_instagram);
        btnInstagram.setOnClickListener(v -> openURL("https://www.instagram.com/amanya.micheal.770/"));
    }

    private void setupHelpSection(View view) {
        MaterialButton btnPrivacy = view.findViewById(R.id.btn_privacy_policy);
        btnPrivacy.setOnClickListener(v -> openURL("https://sites.google.com/view/mindmate-privacy-policy/home"));

        MaterialButton btnTerms = view.findViewById(R.id.btn_terms_of_service);
        btnTerms.setOnClickListener(v -> openURL("https://sites.google.com/view/mindmate-terms-of-service/home"));

        MaterialButton btnFAQ = view.findViewById(R.id.btn_faq);
        btnFAQ.setOnClickListener(v -> showFAQ());
    }

    private void openWhatsApp(String phoneNumber) {
        try {
            String url = "https://wa.me/" + phoneNumber.replaceAll("[^0-9]", "");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmail(String email) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        intent.putExtra(Intent.EXTRA_SUBJECT, "MindMate Support");
        try {
            startActivity(Intent.createChooser(intent, "Send Email"));
        } catch (Exception e) {
            Toast.makeText(getContext(), "No email client found", Toast.LENGTH_SHORT).show();
        }
    }

    private void openSocialMedia(String platform, String handle) {
        String url = "";
        String packageName = "";

        switch (platform) {
            case "twitter":
                url = "https://twitter.com/" + handle;
                packageName = "com.twitter.android";
                break;
            case "instagram":
                url = "https://instagram.com/" + handle;
                packageName = "com.instagram.android";
                break;
            case "linkedin":
                url = "https://linkedin.com/in/" + handle;
                packageName = "com.linkedin.android";
                break;
        }

        try {
            // Try to open in native app first
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setPackage(packageName);
            startActivity(intent);
        } catch (Exception e) {
            // Fallback to browser
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (Exception e2) {
                Toast.makeText(getContext(), "Could not open " + platform, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openURL(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Could not open link", Toast.LENGTH_SHORT).show();
        }
    }

    private void showFAQ() {
        String faqText = "<b>Frequently Asked Questions</b><br><br>" +
                "<b>Q: How do I create an account?</b><br>" +
                "A: Tap 'Create Account' on the login screen and fill in your details.<br><br>" +
                "<b>Q: How do I join a group?</b><br>" +
                "A: Navigate to the Discover section, find a group, and tap 'Join'.<br><br>" +
                "<b>Q: How do I reset my password?</b><br>" +
                "A: Tap 'Forgot Password' on the login screen and follow the instructions.<br><br>" +
                "<b>Q: Is my data secure?</b><br>" +
                "A: Yes, we use Firebase for secure authentication and data storage.";

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("FAQ")
                .setMessage(fromHtmlCompat(faqText))
                .setPositiveButton("OK", null)
                .create()
                .show();
    }

    @SuppressWarnings("deprecation")
    private Spanned fromHtmlCompat(String html) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }
}
