package micheal.must.signuplogin;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import android.widget.ArrayAdapter;
import android.content.SharedPreferences;

public class ResourcesActivity extends AppCompatActivity {

    private static final String TAG = "ResourcesActivity";

    // UI Components
    private RecyclerView rvResources;
    private ProgressBar progressBar;
    private TextView tvErrorMessage;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ChipGroup resourceCategoryChips;

    // Data
    private List<MentalHealthResource> allResources = new ArrayList<>();
    private List<MentalHealthResource> filteredResources = new ArrayList<>();
    private ResourcesAdapter adapter;

    // Web scraping
    private final Executor executor = Executors.newSingleThreadExecutor();
    private String currentCategory = "all"; // Default category

    // Sources for mental health resources
    private final Map<String, String> resourceSources = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resources);

        // Initialize UI components
        initViews();

        // Setup resource sources
        setupResourceSources();

        // Setup RecyclerView
        adapter = new ResourcesAdapter(filteredResources);
        rvResources.setLayoutManager(new LinearLayoutManager(this));
        rvResources.setAdapter(adapter);

        // Setup category filters
        setupCategoryFilters();

        // Load user resources from preferences
        loadUserResources();

        // Load resources
        loadResources();
    }

    private void initViews() {
        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // Title is in the collapsing toolbar

        // Setup back button click listener
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });

        // Find views
        rvResources = findViewById(R.id.rv_resources);
        progressBar = findViewById(R.id.progress_bar);
        View loadingContainer = findViewById(R.id.loading_container);
        View errorCard = findViewById(R.id.error_card);
        tvErrorMessage = findViewById(R.id.tv_error_message);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        resourceCategoryChips = findViewById(R.id.resource_category_chips);

        // Setup retry button
        findViewById(R.id.btn_retry).setOnClickListener(v -> loadResources());

        // Setup FAB for adding new resources
        findViewById(R.id.fab_add_resource).setOnClickListener(v -> {
            showAddResourceDialog();
        });

        // Setup pull-to-refresh
        swipeRefreshLayout.setOnRefreshListener(this::loadResources);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.category_crisis,
                R.color.category_therapy,
                R.color.category_support_groups,
                R.color.category_self_help
        );
    }

    private void setupResourceSources() {
        // Add various mental health resource websites
        resourceSources.put("nimh", "https://www.nimh.nih.gov/health/find-help");
        resourceSources.put("mentalhealth_gov", "https://www.mentalhealth.gov/get-help");
        resourceSources.put("samhsa", "https://www.samhsa.gov/find-help/national-helpline");
        resourceSources.put("nami", "https://www.nami.org/help");
        resourceSources.put("mhanational", "https://www.mhanational.org/finding-help");
    }

    private void setupCategoryFilters() {
        // Create category chips dynamically
        String[] categories = {"All", "Crisis", "Support Groups", "Therapy", "Self-Help", "Youth"};

        for (String category : categories) {
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setCheckable(true);
            chip.setClickable(true);

            // Set "All" as default selected
            if (category.equalsIgnoreCase("All")) {
                chip.setChecked(true);
            }

            chip.setOnClickListener(v -> {
                // Uncheck all other chips
                for (int i = 0; i < resourceCategoryChips.getChildCount(); i++) {
                    Chip otherChip = (Chip) resourceCategoryChips.getChildAt(i);
                    if (otherChip != v) {
                        otherChip.setChecked(false);
                    }
                }

                // Apply filter
                currentCategory = category.toLowerCase();
                filterResources();
            });

            resourceCategoryChips.addView(chip);
        }
    }

    private void loadResources() {
        showLoading();
        allResources.clear();

        // First, try a basic connectivity test
        executor.execute(() -> {
            boolean canConnectToInternet = testBasicConnectivity();

            if (!canConnectToInternet) {
                Log.e(TAG, "Basic connectivity test failed");
                runOnUiThread(() -> {
                    Toast.makeText(this, "Internet connection test failed. Using offline resources.", Toast.LENGTH_LONG).show();
                    loadOfflineResources();
                    filterResources();
                    showContent();
                    swipeRefreshLayout.setRefreshing(false);
                });
                return;
            }

            // Continue with resource loading if connectivity test passes
            Log.d(TAG, "Basic connectivity test passed, continuing with resource loading");

            // Try a simpler, more reliable resource instead of scraping
            tryDirectResourceLoading();
        });
    }

    /**
     * Test if we can connect to a simple website
     */
    private boolean testBasicConnectivity() {
        try {
            Log.d(TAG, "Testing basic connectivity...");
            // Try to connect to Google - a reliable test site
            Document doc = Jsoup.connect("https://www.google.com")
                    .timeout(10000)
                    .get();
            Log.d(TAG, "Successfully connected to Google: " + (doc != null));
            return doc != null;
        } catch (Exception e) {
            Log.e(TAG, "Basic connectivity test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Try loading resources from more reliable sources directly
     */
    private void tryDirectResourceLoading() {
        try {
            Log.d(TAG, "Trying direct resource loading from reliable sources");

            // Add some resources that are more likely to be accessible
            addDirectNIMHResources();
            addDirectSAMHSAResources();

            // Try original scraping as fallback
            if (allResources.isEmpty()) {
                Log.d(TAG, "Direct loading failed, trying original scraping");
                tryOriginalScraping();
            } else {
                // Show resources that we loaded directly
                runOnUiThread(() -> {
                    filterResources();
                    showContent();
                    swipeRefreshLayout.setRefreshing(false);
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in direct resource loading: " + e.getMessage());
            loadOfflineResources();
            runOnUiThread(() -> {
                filterResources();
                showContent();
                swipeRefreshLayout.setRefreshing(false);
            });
        }
    }

    /**
     * Add predefined NIMH resources directly without scraping
     */
    private void addDirectNIMHResources() {
        // Add critical NIMH resources manually instead of scraping
        allResources.add(new MentalHealthResource(
                "NIMH - Help for Mental Illness",
                "If you or someone you know has a mental illness, there are ways to get help. Use these resources to find help for yourself, a friend, or a family member.",
                "https://www.nimh.nih.gov/health/find-help",
                "Support",
                "NIMH"
        ));

        allResources.add(new MentalHealthResource(
                "NIMH - Suicide Prevention",
                "If you or someone you know is in immediate distress or is thinking about hurting themselves, call the National Suicide Prevention Lifeline toll-free at 1-800-273-TALK (8255).",
                "https://www.nimh.nih.gov/health/topics/suicide-prevention",
                "Crisis",
                "NIMH"
        ));

        allResources.add(new MentalHealthResource(
                "NIMH - Mental Health Medications",
                "Information about mental health medications, side effects, and treatment considerations.",
                "https://www.nimh.nih.gov/health/topics/mental-health-medications",
                "Therapy",
                "NIMH"
        ));
    }

    /**
     * Add predefined SAMHSA resources directly without scraping
     */
    private void addDirectSAMHSAResources() {
        allResources.add(new MentalHealthResource(
                "SAMHSA's National Helpline",
                "SAMHSA's National Helpline is a free, confidential, 24/7, 365-day-a-year treatment referral and information service (in English and Spanish) for individuals and families facing mental and/or substance use disorders. Call 1-800-662-HELP (4357)",
                "https://www.samhsa.gov/find-help/national-helpline",
                "Crisis",
                "SAMHSA"
        ));

        allResources.add(new MentalHealthResource(
                "SAMHSA Treatment Locator",
                "Find treatment facilities confidentially and anonymously for mental and substance use disorders.",
                "https://findtreatment.samhsa.gov/",
                "Therapy",
                "SAMHSA"
        ));
    }

    /**
     * Try the original scraping approach
     */
    private void tryOriginalScraping() {
        boolean anySuccessfulScrapes = false;

        // Try simpler URLs that are more likely to work
        Map<String, String> simplifiedSources = new HashMap<>();
        simplifiedSources.put("nimh_home", "https://www.nimh.nih.gov");
        simplifiedSources.put("samhsa_home", "https://www.samhsa.gov");
        simplifiedSources.put("nami_home", "https://www.nami.org");

        // Try scraping from these simpler URLs
        for (Map.Entry<String, String> source : simplifiedSources.entrySet()) {
            try {
                Log.d(TAG, "Trying simplified scraping from: " + source.getValue());
                Document doc = Jsoup.connect(source.getValue())
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .timeout(10000)
                        .get();

                // If we got here, we at least connected to the site
                Log.d(TAG, "Successfully connected to " + source.getValue());

                // Try to extract some basic content
                Elements paragraphs = doc.select("p");
                if (!paragraphs.isEmpty()) {
                    Log.d(TAG, "Found " + paragraphs.size() + " paragraphs");

                    // Create a general resource about this site
                    String title = doc.title();
                    if (title.isEmpty()) title = source.getKey() + " Resource";

                    StringBuilder description = new StringBuilder();
                    // Take just the first few paragraphs
                    for (int i = 0; i < Math.min(3, paragraphs.size()); i++) {
                        String text = paragraphs.get(i).text();
                        if (text.length() > 30) { // Only add substantial paragraphs
                            description.append(text).append("\n\n");
                        }
                    }

                    if (description.length() > 0) {
                        String category = source.getKey().contains("nimh") ? "Support" :
                                source.getKey().contains("samhsa") ? "Crisis" : "Support Groups";

                        allResources.add(new MentalHealthResource(
                                title,
                                description.toString().trim(),
                                source.getValue(),
                                category,
                                extractDomainName(source.getValue())
                        ));

                        anySuccessfulScrapes = true;
                        Log.d(TAG, "Successfully created a resource from " + source.getValue());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error scraping from " + source.getValue() + ": " + e.getMessage());
            }
        }

        // Final result handling
        if (!anySuccessfulScrapes) {
            Log.d(TAG, "All scraping attempts failed. Using offline resources.");
            loadOfflineResources();
        }

        // Update UI
        runOnUiThread(() -> {
            filterResources();
            showContent();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void loadOfflineResources() {
        allResources.clear();

        // Crisis resources
        allResources.add(new MentalHealthResource(
                "National Suicide Prevention Lifeline",
                "The Lifeline provides 24/7, free and confidential support for people in distress, prevention and crisis resources. Call 1-800-273-8255.",
                "https://suicidepreventionlifeline.org",
                "Crisis",
                "Offline Resource"
        ));

        allResources.add(new MentalHealthResource(
                "Crisis Text Line",
                "Text HOME to 741741 to connect with a Crisis Counselor. Free 24/7 support.",
                "https://www.crisistextline.org",
                "Crisis",
                "Offline Resource"
        ));

        // Support Groups
        allResources.add(new MentalHealthResource(
                "NAMI Connection Recovery Support Group",
                "Free, peer-led support group for adults living with mental health conditions.",
                "https://www.nami.org/Support-Education/Support-Groups/NAMI-Connection",
                "Support Groups",
                "Offline Resource"
        ));

        // Therapy resources
        allResources.add(new MentalHealthResource(
                "Psychology Today Therapist Finder",
                "Find detailed listings for mental health professionals with reviews.",
                "https://www.psychologytoday.com/us/therapists",
                "Therapy",
                "Offline Resource"
        ));

        // Self-help resources
        allResources.add(new MentalHealthResource(
                "Calm App",
                "Meditation and sleep stories to help reduce stress and improve sleep quality.",
                "https://www.calm.com",
                "Self-Help",
                "Offline Resource"
        ));

        allResources.add(new MentalHealthResource(
                "Headspace",
                "Guided meditation and mindfulness techniques for daytime and bedtime.",
                "https://www.headspace.com",
                "Self-Help",
                "Offline Resource"
        ));

        // Youth resources
        allResources.add(new MentalHealthResource(
                "Teen Line",
                "A confidential hotline for teenagers to talk to other teenagers about their problems.",
                "https://teenlineonline.org",
                "Youth",
                "Offline Resource"
        ));
    }

    private List<MentalHealthResource> scrapeResourcesFromUrl(String url, String sourceId) throws IOException {
        List<MentalHealthResource> resources = new ArrayList<>();

        try {
            // Connect to website and get HTML document with increased timeout
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(15000) // Increase timeout to 15 seconds
                    .followRedirects(true)
                    .get();

            Log.d(TAG, "Successfully connected to " + url + " - HTML title: " + doc.title());

            // Different parsing strategies based on the source website
            switch (sourceId) {
                case "nimh":
                    resources.addAll(parseNIMHResources(doc));
                    break;
                case "mentalhealth_gov":
                    resources.addAll(parseMentalHealthGovResources(doc));
                    break;
                case "samhsa":
                    resources.addAll(parseSAMHSAResources(doc));
                    break;
                case "nami":
                    resources.addAll(parseNAMIResources(doc));
                    break;
                case "mhanational":
                    resources.addAll(parseMHAResources(doc));
                    break;
                default:
                    // Generic parsing for other websites
                    resources.addAll(parseGenericResources(doc, url));
                    break;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to " + url + ": " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error scraping " + url + ": " + e.getMessage(), e);
            throw new IOException("Error scraping content: " + e.getMessage(), e);
        }

        return resources;
    }

    // Specialized parsers for different websites

    private List<MentalHealthResource> parseNIMHResources(Document doc) {
        List<MentalHealthResource> resources = new ArrayList<>();

        try {
            // Example: Parse main resource sections
            Elements resourceSections = doc.select("div.field-items h2, div.field-items h3");

            for (Element heading : resourceSections) {
                String title = heading.text();
                String category = "Support";

                // Get the description (paragraphs following the heading)
                StringBuilder description = new StringBuilder();
                Element nextElement = heading.nextElementSibling();
                while (nextElement != null && !nextElement.tagName().equals("h2") && !nextElement.tagName().equals("h3")) {
                    if (nextElement.tagName().equals("p")) {
                        description.append(nextElement.text()).append("\n\n");
                    }
                    nextElement = nextElement.nextElementSibling();
                }

                // Get any links in this section
                Elements links = heading.parent().select("a[href]");
                String resourceUrl = links.isEmpty() ? "" : links.first().attr("abs:href");

                // Categorize based on title keywords
                if (title.toLowerCase().contains("crisis") || title.toLowerCase().contains("emergency")) {
                    category = "Crisis";
                } else if (title.toLowerCase().contains("support group") || title.toLowerCase().contains("peer")) {
                    category = "Support Groups";
                } else if (title.toLowerCase().contains("therapy") || title.toLowerCase().contains("counseling")) {
                    category = "Therapy";
                } else if (title.toLowerCase().contains("self-help") || title.toLowerCase().contains("self help")) {
                    category = "Self-Help";
                } else if (title.toLowerCase().contains("youth") || title.toLowerCase().contains("teen")
                        || title.toLowerCase().contains("child")) {
                    category = "Youth";
                }

                if (!title.isEmpty() && description.length() > 0) {
                    resources.add(new MentalHealthResource(
                            title,
                            description.toString().trim(),
                            resourceUrl,
                            category,
                            "NIMH"
                    ));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing NIMH resources: " + e.getMessage());
        }

        return resources;
    }

    // Additional specialized parsers would be implemented similarly

    private List<MentalHealthResource> parseMentalHealthGovResources(Document doc) {
        // Implementation similar to NIMH parser but tailored to MentalHealth.gov structure
        List<MentalHealthResource> resources = new ArrayList<>();

        try {
            Elements resourceElements = doc.select(".field-content");

            for (Element element : resourceElements) {
                Elements headings = element.select("h2, h3, h4");
                if (!headings.isEmpty()) {
                    String title = headings.first().text();
                    String description = "";

                    Elements paragraphs = element.select("p");
                    for (Element p : paragraphs) {
                        description += p.text() + "\n\n";
                    }

                    String resourceUrl = "";
                    Elements links = element.select("a");
                    if (!links.isEmpty()) {
                        resourceUrl = links.first().attr("abs:href");
                    }

                    String category = "Support";
                    // Determine category based on content
                    if (title.toLowerCase().contains("crisis") || description.toLowerCase().contains("emergency")) {
                        category = "Crisis";
                    } else if (title.toLowerCase().contains("group") || description.toLowerCase().contains("support group")) {
                        category = "Support Groups";
                    }

                    if (!title.isEmpty()) {
                        resources.add(new MentalHealthResource(
                                title,
                                description.trim(),
                                resourceUrl,
                                category,
                                "MentalHealth.gov"
                        ));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing MentalHealth.gov resources: " + e.getMessage());
        }

        return resources;
    }

    private List<MentalHealthResource> parseSAMHSAResources(Document doc) {
        // Implementation for SAMHSA website
        List<MentalHealthResource> resources = new ArrayList<>();

        try {
            // Get the main helpline information
            Elements helplineInfo = doc.select(".field--name-body");
            if (!helplineInfo.isEmpty()) {
                String title = "SAMHSA's National Helpline";
                StringBuilder description = new StringBuilder();

                for (Element paragraph : helplineInfo.select("p")) {
                    description.append(paragraph.text()).append("\n\n");
                }

                resources.add(new MentalHealthResource(
                        title,
                        description.toString().trim(),
                        "https://www.samhsa.gov/find-help/national-helpline",
                        "Crisis",
                        "SAMHSA"
                ));
            }

            // Get additional resources
            Elements additionalResources = doc.select(".accordion");
            for (Element resource : additionalResources) {
                String title = resource.select(".accordion-button").text();
                StringBuilder description = new StringBuilder();

                for (Element paragraph : resource.select(".accordion-content p")) {
                    description.append(paragraph.text()).append("\n\n");
                }

                String resourceUrl = "";
                Elements links = resource.select("a");
                if (!links.isEmpty()) {
                    resourceUrl = links.first().attr("abs:href");
                }

                String category = "Support";

                resources.add(new MentalHealthResource(
                        title,
                        description.toString().trim(),
                        resourceUrl,
                        category,
                        "SAMHSA"
                ));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing SAMHSA resources: " + e.getMessage());
        }

        return resources;
    }

    private List<MentalHealthResource> parseNAMIResources(Document doc) {
        // Implementation for NAMI website
        List<MentalHealthResource> resources = new ArrayList<>();

        try {
            Elements resourceCards = doc.select(".card");

            for (Element card : resourceCards) {
                String title = card.select(".card-title").text();
                String description = card.select(".card-text").text();

                String resourceUrl = "";
                Elements links = card.select("a");
                if (!links.isEmpty()) {
                    resourceUrl = links.first().attr("abs:href");
                }

                String category = "Support";
                if (title.toLowerCase().contains("crisis")) {
                    category = "Crisis";
                } else if (title.toLowerCase().contains("group")) {
                    category = "Support Groups";
                }

                if (!title.isEmpty()) {
                    resources.add(new MentalHealthResource(
                            title,
                            description,
                            resourceUrl,
                            category,
                            "NAMI"
                    ));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing NAMI resources: " + e.getMessage());
        }

        return resources;
    }

    private List<MentalHealthResource> parseMHAResources(Document doc) {
        // Implementation for Mental Health America website
        List<MentalHealthResource> resources = new ArrayList<>();

        try {
            Elements sections = doc.select(".field-item");

            for (Element section : sections) {
                Elements headings = section.select("h2, h3");

                for (Element heading : headings) {
                    String title = heading.text();
                    StringBuilder description = new StringBuilder();

                    // Get text from paragraphs following this heading
                    Element nextElement = heading.nextElementSibling();
                    while (nextElement != null && !nextElement.tagName().equals("h2") && !nextElement.tagName().equals("h3")) {
                        if (nextElement.tagName().equals("p")) {
                            description.append(nextElement.text()).append("\n\n");
                        }
                        nextElement = nextElement.nextElementSibling();
                    }

                    String resourceUrl = "";
                    Elements links = heading.parent().select("a");
                    if (!links.isEmpty()) {
                        resourceUrl = links.first().attr("abs:href");
                    }

                    String category = determineCategory(title, description.toString());

                    if (!title.isEmpty() && description.length() > 0) {
                        resources.add(new MentalHealthResource(
                                title,
                                description.toString().trim(),
                                resourceUrl,
                                category,
                                "Mental Health America"
                        ));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing MHA resources: " + e.getMessage());
        }

        return resources;
    }

    private List<MentalHealthResource> parseGenericResources(Document doc, String url) {
        List<MentalHealthResource> resources = new ArrayList<>();

        try {
            // Generic approach for unknown websites
            // 1. Look for main content sections
            Elements mainContent = doc.select("main, article, .content, #content");
            if (mainContent.isEmpty()) {
                mainContent = doc.select("body");
            }

            // 2. Find headings in the main content
            Elements headings = mainContent.select("h1, h2, h3, h4");

            for (Element heading : headings) {
                String title = heading.text();

                // Skip navigational elements or very short headings
                if (title.length() < 5 || isNavigationalElement(heading)) {
                    continue;
                }

                // Get description (paragraphs following the heading)
                StringBuilder description = new StringBuilder();
                Element nextElement = heading.nextElementSibling();
                int paragraphCount = 0;

                while (nextElement != null && paragraphCount < 3 &&
                        !nextElement.tagName().matches("h[1-4]")) {
                    if (nextElement.tagName().equals("p") && !nextElement.text().isEmpty()) {
                        description.append(nextElement.text()).append("\n\n");
                        paragraphCount++;
                    }
                    nextElement = nextElement.nextElementSibling();
                }

                // Only add if we found description content
                if (description.length() > 10) {
                    // Get any links in this section
                    Elements links = heading.parent().select("a[href]");
                    String resourceUrl = links.isEmpty() ? url : links.first().attr("abs:href");

                    String category = determineCategory(title, description.toString());
                    String source = extractDomainName(url);

                    resources.add(new MentalHealthResource(
                            title,
                            description.toString().trim(),
                            resourceUrl,
                            category,
                            source
                    ));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing generic resources: " + e.getMessage());
        }

        return resources;
    }

    private boolean isNavigationalElement(Element element) {
        // Check if element is likely part of navigation rather than content
        Element parent = element.parent();
        if (parent != null) {
            String parentClass = parent.className().toLowerCase();
            String parentId = parent.id().toLowerCase();
            return parentClass.contains("nav") ||
                    parentClass.contains("menu") ||
                    parentId.contains("nav") ||
                    parentId.contains("menu");
        }
        return false;
    }

    private String extractDomainName(String url) {
        try {
            String domain = url.replaceAll("https?://", "")
                    .replaceAll("www\\.", "");
            if (domain.contains("/")) {
                domain = domain.substring(0, domain.indexOf('/'));
            }
            return domain;
        } catch (Exception e) {
            return url;
        }
    }

    private String determineCategory(String title, String description) {
        String combinedText = (title + " " + description).toLowerCase();

        if (combinedText.contains("crisis") || combinedText.contains("emergency") ||
                combinedText.contains("hotline") || combinedText.contains("suicide") ||
                combinedText.contains("urgent")) {
            return "Crisis";
        } else if (combinedText.contains("group") || combinedText.contains("peer") ||
                combinedText.contains("support group")) {
            return "Support Groups";
        } else if (combinedText.contains("therapy") || combinedText.contains("counseling") ||
                combinedText.contains("therapist") || combinedText.contains("counselor")) {
            return "Therapy";
        } else if (combinedText.contains("self-help") || combinedText.contains("self help") ||
                combinedText.contains("app") || combinedText.contains("tool")) {
            return "Self-Help";
        } else if (combinedText.contains("youth") || combinedText.contains("teen") ||
                combinedText.contains("child") || combinedText.contains("adolescent")) {
            return "Youth";
        }

        return "Support";
    }

    private void filterResources() {
        filteredResources.clear();

        if (currentCategory.equalsIgnoreCase("all")) {
            filteredResources.addAll(allResources);
        } else {
            for (MentalHealthResource resource : allResources) {
                if (resource.getCategory().toLowerCase().equals(currentCategory)) {
                    filteredResources.add(resource);
                }
            }
        }

        // Update adapter
        adapter.notifyDataSetChanged();

        // Show message if no resources match filter
        if (filteredResources.isEmpty()) {
            tvErrorMessage.setText("No resources found for this category");
            tvErrorMessage.setVisibility(View.VISIBLE);
        } else {
            tvErrorMessage.setVisibility(View.GONE);
        }
    }

    private void showLoading() {
        findViewById(R.id.loading_container).setVisibility(View.VISIBLE);
        rvResources.setVisibility(View.GONE);
        findViewById(R.id.error_card).setVisibility(View.GONE);
    }

    private void showContent() {
        findViewById(R.id.loading_container).setVisibility(View.GONE);
        rvResources.setVisibility(View.VISIBLE);
        findViewById(R.id.error_card).setVisibility(View.GONE);
    }

    private void showError(String message) {
        findViewById(R.id.loading_container).setVisibility(View.GONE);
        rvResources.setVisibility(View.GONE);
        findViewById(R.id.error_card).setVisibility(View.VISIBLE);
        tvErrorMessage.setText(message);
    }

    /**
     * Shows dialog for adding a custom resource
     */
    private void showAddResourceDialog() {
        // Create dialog builder
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Add New Resource");

        // Inflate custom layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_resource, null);
        builder.setView(dialogView);

        // Find views in the dialog
        final com.google.android.material.textfield.TextInputEditText etTitle = 
            dialogView.findViewById(R.id.et_resource_title);
        final com.google.android.material.textfield.TextInputEditText etDescription = 
            dialogView.findViewById(R.id.et_resource_description);
        final com.google.android.material.textfield.TextInputEditText etUrl = 
            dialogView.findViewById(R.id.et_resource_url);
        final com.google.android.material.textfield.MaterialAutoCompleteTextView spinnerCategory = 
            dialogView.findViewById(R.id.spinner_category);

        // Set up category autocomplete
        String[] categories = {"Crisis", "Support Groups", "Therapy", "Self-Help", "Youth", "Support"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories);
        spinnerCategory.setAdapter(adapter);

        // Add buttons
        builder.setPositiveButton("Save", null); // We'll override this below
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Create and show the dialog
        final androidx.appcompat.app.AlertDialog dialog = builder.create();

        // Show the dialog
        dialog.show();

        // Override the positive button to validate before dismissing
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            // Validate inputs
            String title = etTitle.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String url = etUrl.getText().toString().trim();
            String category = spinnerCategory.getText().toString().trim();

            if (title.isEmpty()) {
                etTitle.setError("Title is required");
                return;
            }

            if (description.isEmpty()) {
                etDescription.setError("Description is required");
                return;
            }

            if (category.isEmpty()) {
                Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
                return;
            }

            // URL is optional but should be valid if provided
            if (!url.isEmpty() && !url.startsWith("http")) {
                url = "https://" + url;
            }

            // Add the new resource
            addUserResource(title, description, url, category);

            // Dismiss the dialog
            dialog.dismiss();
        });
    }

    /**
     * Adds a user-created resource to the list
     */
    private void addUserResource(String title, String description, String url, String category) {
        // Create new resource
        MentalHealthResource newResource = new MentalHealthResource(
                title,
                description,
                url,
                category,
                "User Added"
        );

        // Add to resources list
        allResources.add(0, newResource); // Add at the top

        // Apply filtering
        filterResources();

        // Scroll to top to show the new resource
        if (!filteredResources.isEmpty()) {
            rvResources.smoothScrollToPosition(0);
        }

        // Show success message
        Toast.makeText(this, "Resource added successfully", Toast.LENGTH_SHORT).show();

        // Save the user resources to preferences for persistence
        saveUserResources();
    }

    /**
     * Saves user-added resources to SharedPreferences
     */
    private void saveUserResources() {
        try {
            // Get user-added resources
            List<MentalHealthResource> userResources = allResources.stream()
                    .filter(r -> "User Added".equals(r.getSource()))
                    .collect(Collectors.toList());

            // Convert to JSON
            JSONArray jsonArray = new JSONArray();
            for (MentalHealthResource resource : userResources) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("title", resource.getTitle());
                jsonObject.put("description", resource.getDescription());
                jsonObject.put("url", resource.getUrl());
                jsonObject.put("category", resource.getCategory());
                jsonArray.put(jsonObject);
            }

            // Save to SharedPreferences
            SharedPreferences prefs = getSharedPreferences("resources_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("user_resources", jsonArray.toString());
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving user resources: " + e.getMessage());
        }
    }

    /**
     * Loads user-added resources from SharedPreferences
     */
    private void loadUserResources() {
        try {
            SharedPreferences prefs = getSharedPreferences("resources_prefs", MODE_PRIVATE);
            String userResourcesJson = prefs.getString("user_resources", null);

            if (userResourcesJson != null) {
                JSONArray jsonArray = new JSONArray(userResourcesJson);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    MentalHealthResource resource = new MentalHealthResource(
                            jsonObject.getString("title"),
                            jsonObject.getString("description"),
                            jsonObject.getString("url"),
                            jsonObject.getString("category"),
                            "User Added"
                    );

                    allResources.add(resource);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading user resources: " + e.getMessage());
        }
    }

    // Model class for mental health resources
    public static class MentalHealthResource {
        private final String title;
        private final String description;
        private final String url;
        private final String category;
        private final String source;

        public MentalHealthResource(String title, String description, String url, String category, String source) {
            this.title = title;
            this.description = description;
            this.url = url;
            this.category = category;
            this.source = source;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getUrl() {
            return url;
        }

        public String getCategory() {
            return category;
        }

        public String getSource() {
            return source;
        }
    }

    // Adapter for displaying mental health resources
    private static class ResourcesAdapter extends RecyclerView.Adapter<ResourcesAdapter.ViewHolder> {
        private final List<MentalHealthResource> resources;

        public ResourcesAdapter(List<MentalHealthResource> resources) {
            this.resources = resources;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_resource, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MentalHealthResource resource = resources.get(position);

            holder.tvTitle.setText(resource.getTitle());
            holder.tvDescription.setText(resource.getDescription());

            // Set category with appropriate color
            if (holder.tvCategory != null) {
                holder.tvCategory.setText(resource.getCategory());

                // Set color based on category
                int colorResId;
                switch (resource.getCategory().toLowerCase()) {
                    case "crisis":
                        colorResId = R.color.category_crisis;
                        break;
                    case "support groups":
                        colorResId = R.color.category_support_groups;
                        break;
                    case "therapy":
                        colorResId = R.color.category_therapy;
                        break;
                    case "self-help":
                        colorResId = R.color.category_self_help;
                        break;
                    case "youth":
                        colorResId = R.color.category_youth;
                        break;
                    default:
                        colorResId = R.color.category_support;
                        break;
                }

                // Apply color to chip and top bar
                int categoryColor = holder.itemView.getContext().getResources().getColor(colorResId);
                holder.tvCategory.setChipBackgroundColorResource(colorResId);

                // Also apply to the top color bar if it exists
                if (holder.categoryColorBar != null) {
                    holder.categoryColorBar.setBackgroundColor(categoryColor);
                }
            }

            if (holder.tvSource != null) {
                holder.tvSource.setText("Source: " + resource.getSource());
            }

            // Set up the visit button if available
            if (holder.btnVisitResource != null) {
                if (!resource.getUrl().isEmpty()) {
                    holder.btnVisitResource.setVisibility(View.VISIBLE);
                    holder.btnVisitResource.setOnClickListener(v -> {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(resource.getUrl()));
                            v.getContext().startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(v.getContext(), "Unable to open URL", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    holder.btnVisitResource.setVisibility(View.GONE);
                }
            }

            // Set click listener on the entire item
            holder.itemView.setOnClickListener(v -> {
                if (!resource.getUrl().isEmpty()) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(resource.getUrl()));
                        v.getContext().startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(v.getContext(), "Unable to open URL", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return resources.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle;
            TextView tvDescription;
            Chip tvCategory;
            TextView tvSource;
            View categoryColorBar;
            MaterialButton btnVisitResource;

            public ViewHolder(View itemView) {
                super(itemView);
                // Fix the ID references to match the actual layout file IDs
                tvTitle = itemView.findViewById(R.id.tv_resource_title);
                tvDescription = itemView.findViewById(R.id.tv_resource_description);
                tvCategory = itemView.findViewById(R.id.tv_resource_category);
                tvSource = itemView.findViewById(R.id.tv_resource_source);
                categoryColorBar = itemView.findViewById(R.id.category_color_bar);
                btnVisitResource = itemView.findViewById(R.id.btn_visit_resource);
            }
        }
    }
}
