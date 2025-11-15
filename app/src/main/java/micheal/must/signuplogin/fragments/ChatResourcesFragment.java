package micheal.must.signuplogin.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import micheal.must.signuplogin.R;
import micheal.must.signuplogin.services.WebScrapingService;

public class ChatResourcesFragment extends Fragment {

    private RecyclerView rvResources;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_resources, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvResources = view.findViewById(R.id.rv_resources);
        if (rvResources != null) {
            rvResources.setLayoutManager(new LinearLayoutManager(getContext()));
            loadResourcesList();
        }
    }

    /**
     * Load comprehensive resources list
     */
    private void loadResourcesList() {
        if (rvResources == null) return;
        
        LinearLayout resourcesContainer = new LinearLayout(requireContext());
        resourcesContainer.setOrientation(LinearLayout.VERTICAL);
        resourcesContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        resourcesContainer.setPadding(16, 16, 16, 16);

        // Add all resource sections
        addCrisisResourcesSection(resourcesContainer);
        addMentalHealthTipsSection(resourcesContainer);
        addSelfCareSection(resourcesContainer);
        addCopingStrategiesSection(resourcesContainer);
        addWellnessResourcesSection(resourcesContainer);
        addSupportGroupsSection(resourcesContainer);
        addPhysicalHealthSection(resourcesContainer);

        ResourcesAdapter adapter = new ResourcesAdapter(resourcesContainer);
        rvResources.setAdapter(adapter);
    }

    private void addCrisisResourcesSection(LinearLayout container) {
        TextView header = new TextView(requireContext());
        header.setText("🆘 Crisis Resources");
        header.setTextSize(18);
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        header.setTextColor(android.graphics.Color.parseColor("#FF6B6B"));
        header.setPadding(0, 0, 0, 12);
        container.addView(header);

        MaterialButton btn = new MaterialButton(requireContext());
        btn.setText("Immediate Crisis Help & Hotlines");
        btn.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        btn.setOnClickListener(v -> showCrisisResources());
        container.addView(btn);
    }

    private void addMentalHealthTipsSection(LinearLayout container) {
        TextView header = new TextView(requireContext());
        header.setText("\n💡 Mental Health Tips");
        header.setTextSize(16);
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        header.setTextColor(android.graphics.Color.parseColor("#2E7D32"));
        header.setPadding(0, 16, 0, 12);
        container.addView(header);

        MaterialButton btn = new MaterialButton(requireContext());
        btn.setText("Daily Mental Health Tips & Tricks");
        btn.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        btn.setOnClickListener(v -> showMentalHealthTips());
        container.addView(btn);
    }

    private void addSelfCareSection(LinearLayout container) {
        TextView header = new TextView(requireContext());
        header.setText("\n🧘 Self-Care Practices");
        header.setTextSize(16);
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        header.setTextColor(android.graphics.Color.parseColor("#1565C0"));
        header.setPadding(0, 16, 0, 12);
        container.addView(header);

        MaterialButton btn = new MaterialButton(requireContext());
        btn.setText("Self-Care Routines & Exercises");
        btn.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        btn.setOnClickListener(v -> showSelfCare());
        container.addView(btn);
    }

    private void addCopingStrategiesSection(LinearLayout container) {
        TextView header = new TextView(requireContext());
        header.setText("\n🎯 Coping Strategies");
        header.setTextSize(16);
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        header.setTextColor(android.graphics.Color.parseColor("#F57C00"));
        header.setPadding(0, 16, 0, 12);
        container.addView(header);

        MaterialButton btn = new MaterialButton(requireContext());
        btn.setText("Effective Coping Mechanisms");
        btn.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        btn.setOnClickListener(v -> showCopingStrategies());
        container.addView(btn);
    }

    private void addWellnessResourcesSection(LinearLayout container) {
        TextView header = new TextView(requireContext());
        header.setText("\n❤️ Wellness Resources");
        header.setTextSize(16);
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        header.setTextColor(android.graphics.Color.parseColor("#C2185B"));
        header.setPadding(0, 16, 0, 12);
        container.addView(header);

        MaterialButton btn = new MaterialButton(requireContext());
        btn.setText("Holistic Wellness & Health");
        btn.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        btn.setOnClickListener(v -> showWellnessResources());
        container.addView(btn);
    }

    private void addSupportGroupsSection(LinearLayout container) {
        TextView header = new TextView(requireContext());
        header.setText("\n👥 Support Groups");
        header.setTextSize(16);
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        header.setTextColor(android.graphics.Color.parseColor("#512DA8"));
        header.setPadding(0, 16, 0, 12);
        container.addView(header);

        MaterialButton btn = new MaterialButton(requireContext());
        btn.setText("Find Support Communities");
        btn.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        btn.setOnClickListener(v -> showSupportGroups());
        container.addView(btn);
    }

    private void addPhysicalHealthSection(LinearLayout container) {
        TextView header = new TextView(requireContext());
        header.setText("\n💪 Physical Health");
        header.setTextSize(16);
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        header.setTextColor(android.graphics.Color.parseColor("#00796B"));
        header.setPadding(0, 16, 0, 12);
        container.addView(header);

        MaterialButton btn = new MaterialButton(requireContext());
        btn.setText("Exercise & Fitness Guide");
        btn.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        btn.setOnClickListener(v -> showPhysicalHealth());
        container.addView(btn);
    }

    /**
     * Show crisis resources with implicit intents
     */
    private void showCrisisResources() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        LinearLayout mainLayout = new LinearLayout(requireContext());
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(32, 32, 32, 32);

        // Title
        TextView titleView = new TextView(requireContext());
        titleView.setText("🆘 Crisis Resources");
        titleView.setTextSize(22);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setTextColor(android.graphics.Color.parseColor("#FF6B6B"));
        titleView.setPadding(0, 0, 0, 16);
        mainLayout.addView(titleView);

        // Subtitle
        TextView subtitleView = new TextView(requireContext());
        subtitleView.setText("Available 24/7, Free & Confidential");
        subtitleView.setTextSize(14);
        subtitleView.setTextColor(android.graphics.Color.GRAY);
        subtitleView.setPadding(0, 0, 0, 24);
        mainLayout.addView(subtitleView);

        // Crisis info text
        TextView infoView = new TextView(requireContext());
        infoView.setText("If you're in crisis or having thoughts of self-harm, please reach out immediately:");
        infoView.setTextSize(14);
        infoView.setLineSpacing(8, 1.2f);
        infoView.setTextColor(android.graphics.Color.DKGRAY);
        infoView.setPadding(0, 0, 0, 16);
        mainLayout.addView(infoView);

        // Call button
        MaterialButton callBtn = new MaterialButton(requireContext());
        callBtn.setText("📞 Call 988 (Suicide & Crisis Lifeline)");
        callBtn.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        callBtn.setOnClickListener(v -> callCrisisHelpline());
        mainLayout.addView(callBtn);

        // Text button
        MaterialButton textBtn = new MaterialButton(requireContext());
        textBtn.setText("📱 Text 'HELLO' to 741741");
        textBtn.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        textBtn.setOnClickListener(v -> openTextLineImplicit());
        mainLayout.addView(textBtn);

        // Website button
        MaterialButton webBtn = new MaterialButton(requireContext());
        webBtn.setText("🌐 Visit 988lifeline.org");
        webBtn.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        webBtn.setOnClickListener(v -> openBrowserImplicit("https://988lifeline.org"));
        mainLayout.addView(webBtn);

        // International resources
        TextView internationalTitle = new TextView(requireContext());
        internationalTitle.setText("\n🌍 International Resources:");
        internationalTitle.setTextSize(16);
        internationalTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        internationalTitle.setPadding(0, 16, 0, 12);
        mainLayout.addView(internationalTitle);

        // International button
        MaterialButton internationalBtn = new MaterialButton(requireContext());
        internationalBtn.setText("Find Your Local Helpline");
        internationalBtn.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        internationalBtn.setOnClickListener(v -> openBrowserImplicit("https://findahelpline.com"));
        mainLayout.addView(internationalBtn);

        builder.setView(mainLayout)
                .setNegativeButton("Close", (dialog, which) -> dialog.dismiss())
                .setCancelable(true);

        builder.create().show();
    }

    private void callCrisisHelpline() {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:988"));
            startActivity(intent);
        } catch (Exception e) {
            android.widget.Toast.makeText(requireContext(), "Could not open dialer", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void openTextLineImplicit() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://www.crisistextline.org"));
            startActivity(intent);
        } catch (Exception e) {
            android.widget.Toast.makeText(requireContext(), "Could not open", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void openBrowserImplicit(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            android.widget.Toast.makeText(requireContext(), "Could not open browser", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void showMentalHealthTips() {
        String[] tips = {
            "Practice mindfulness - be present in the moment",
            "Journal your thoughts and feelings daily",
            "Limit social media - reduce comparison anxiety",
            "Practice gratitude - list 3 things you're grateful for",
            "Set healthy boundaries with others",
            "Take regular digital detoxes",
            "Practice positive self-talk",
            "Engage in hobbies you enjoy",
            "Practice deep breathing exercises",
            "Spend time in nature regularly"
        };
        showTipsDialog("💡 Mental Health Tips", tips);
    }

    private void showSelfCare() {
        showLoadingDialog("Loading Self-Care Tips...");
        
        new Thread(() -> {
            final String content = WebScrapingService.scrapeSelfCareTips();
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    dismissLoadingDialog();
                    showContentDialog("🧘 Self-Care Practices", content);
                });
            }
        }).start();
    }

    private void showCopingStrategies() {
        showLoadingDialog("Loading Coping Strategies...");
        
        new Thread(() -> {
            final String content = WebScrapingService.scrapeCopingStrategies();
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    dismissLoadingDialog();
                    showContentDialog("🎯 Coping Strategies", content);
                });
            }
        }).start();
    }

    private void showWellnessResources() {
        showLoadingDialog("Loading Wellness Resources...");
        
        new Thread(() -> {
            final String content = WebScrapingService.scrapeWellnessResources();
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    dismissLoadingDialog();
                    showContentDialog("❤️ Wellness Resources", content);
                });
            }
        }).start();
    }

    private void showSupportGroups() {
        showLoadingDialog("Loading Support Communities...");
        
        new Thread(() -> {
            final String content = WebScrapingService.scrapeSupportGroups();
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    dismissLoadingDialog();
                    showContentDialog("👥 Support Communities", content);
                });
            }
        }).start();
    }

    private void showPhysicalHealth() {
        showLoadingDialog("Loading Physical Health Guide...");
        
        new Thread(() -> {
            final String content = WebScrapingService.scrapePhysicalHealth();
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    dismissLoadingDialog();
                    showContentDialog("💪 Physical Health", content);
                });
            }
        }).start();
    }

    private void showTipsDialog(String title, String[] tips) {
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setItems(tips, null)
                .setPositiveButton("Close", (d, w) -> d.dismiss())
                .setCancelable(true)
                .show();
    }

    private void showContentDialog(String title, String content) {
        ScrollView scrollView = new ScrollView(requireContext());
        TextView textView = new TextView(requireContext());
        textView.setText(content);
        textView.setTextSize(14);
        textView.setLineSpacing(6, 1.3f);
        textView.setPadding(24, 24, 24, 24);
        textView.setTextColor(android.graphics.Color.DKGRAY);
        scrollView.addView(textView);

        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(scrollView)
                .setPositiveButton("Close", (d, w) -> d.dismiss())
                .setCancelable(true)
                .show();
    }

    private AlertDialog loadingDialog;

    private void showLoadingDialog(String message) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(android.view.Gravity.CENTER);
        layout.setPadding(32, 32, 32, 32);

        android.widget.ProgressBar progressBar = new android.widget.ProgressBar(requireContext());
        layout.addView(progressBar);

        TextView textView = new TextView(requireContext());
        textView.setText(message);
        textView.setTextSize(14);
        textView.setPadding(0, 16, 0, 0);
        textView.setGravity(android.view.Gravity.CENTER);
        layout.addView(textView);

        builder.setView(layout);
        builder.setCancelable(false);
        loadingDialog = builder.create();
        loadingDialog.show();
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    /**
     * Simple adapter to display resources layout
     */
    private static class ResourcesAdapter extends RecyclerView.Adapter<ResourcesAdapter.ViewHolder> {
        private final LinearLayout resourcesLayout;

        public ResourcesAdapter(LinearLayout layout) {
            this.resourcesLayout = layout;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(resourcesLayout);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            // Already bound in constructor
        }

        @Override
        public int getItemCount() {
            return 1; // Only one item - the resources layout
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(LinearLayout layout) {
                super(layout);
            }
        }
    }
}
