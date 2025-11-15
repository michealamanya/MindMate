package micheal.must.signuplogin.services;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class WebScrapingService {
    private static final String TAG = "WebScrapingService";

    /**
     * Scrape self-care tips from website
     */
    public static String scrapeSelfCareTips() {
        try {
            // Scrape from mindful.org
            Document doc = Jsoup.connect("https://www.mindful.org/self-care/")
                    .timeout(10000)
                    .userAgent("Mozilla/5.0")
                    .get();

            StringBuilder content = new StringBuilder("🧘 SELF-CARE PRACTICES\n\n");

            // Extract main content
            Elements paragraphs = doc.select("p");
            int count = 0;
            for (Element p : paragraphs) {
                String text = p.text().trim();
                if (!text.isEmpty() && text.length() > 50) {
                    content.append("• ").append(text).append("\n\n");
                    count++;
                    if (count >= 5) break;
                }
            }

            // Add fallback content if scraping yields little
            if (count < 3) {
                content.append("1. MEDITATION & MINDFULNESS\n")
                        .append("• 10-15 min daily meditation\n")
                        .append("• Body scan exercises\n")
                        .append("• Mindful breathing techniques\n\n")
                        .append("2. PHYSICAL ACTIVITY\n")
                        .append("• 30 min exercise daily\n")
                        .append("• Yoga or stretching\n")
                        .append("• Outdoor walks in nature\n\n")
                        .append("3. SLEEP HYGIENE\n")
                        .append("• 7-9 hours nightly\n")
                        .append("• Consistent sleep schedule\n")
                        .append("• Avoid screens before bed\n\n")
                        .append("4. NUTRITION\n")
                        .append("• Balanced, nutritious meals\n")
                        .append("• Stay hydrated (8+ glasses water)\n")
                        .append("• Limit caffeine & sugar");
            }

            Log.d(TAG, "✓ Scraped self-care tips");
            return content.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error scraping self-care: " + e.getMessage());
            return getDefaultSelfCareContent();
        }
    }

    /**
     * Scrape coping strategies from website
     */
    public static String scrapeCopingStrategies() {
        try {
            Document doc = Jsoup.connect("https://www.verywellmind.com/coping-strategies-for-anxiety-3024891")
                    .timeout(10000)
                    .userAgent("Mozilla/5.0")
                    .get();

            StringBuilder content = new StringBuilder("🎯 COPING STRATEGIES\n\n");

            Elements headers = doc.select("h2, h3");
            Elements paragraphs = doc.select("p");

            for (Element h : headers) {
                String headerText = h.text().trim();
                if (!headerText.isEmpty()) {
                    content.append(headerText).append(":\n");
                }
            }

            int count = 0;
            for (Element p : paragraphs) {
                String text = p.text().trim();
                if (!text.isEmpty() && text.length() > 40) {
                    content.append("• ").append(text).append("\n");
                    count++;
                    if (count >= 8) break;
                }
            }

            if (count < 3) {
                content = new StringBuilder(getDefaultCopingStrategiesContent());
            }

            Log.d(TAG, "✓ Scraped coping strategies");
            return content.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error scraping coping strategies: " + e.getMessage());
            return getDefaultCopingStrategiesContent();
        }
    }

    /**
     * Scrape wellness resources from website
     */
    public static String scrapeWellnessResources() {
        try {
            Document doc = Jsoup.connect("https://www.healthline.com/health/mental-health-resources")
                    .timeout(10000)
                    .userAgent("Mozilla/5.0")
                    .get();

            StringBuilder content = new StringBuilder("❤️ WELLNESS RESOURCES\n\n");

            Elements listItems = doc.select("li");
            int count = 0;
            for (Element li : listItems) {
                String text = li.text().trim();
                if (!text.isEmpty() && text.length() > 20) {
                    content.append("• ").append(text).append("\n");
                    count++;
                    if (count >= 12) break;
                }
            }

            if (count < 3) {
                content = new StringBuilder(getDefaultWellnessContent());
            }

            Log.d(TAG, "✓ Scraped wellness resources");
            return content.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error scraping wellness: " + e.getMessage());
            return getDefaultWellnessContent();
        }
    }

    /**
     * Scrape support groups from website
     */
    public static String scrapeSupportGroups() {
        try {
            Document doc = Jsoup.connect("https://www.samhsa.gov/find-help")
                    .timeout(10000)
                    .userAgent("Mozilla/5.0")
                    .get();

            StringBuilder content = new StringBuilder("👥 SUPPORT COMMUNITIES\n\n");

            Elements headers = doc.select("h2, h3");
            Elements paragraphs = doc.select("p");

            for (Element h : headers) {
                String text = h.text().trim();
                if (!text.isEmpty()) {
                    content.append(text).append("\n");
                }
            }

            int count = 0;
            for (Element p : paragraphs) {
                String text = p.text().trim();
                if (!text.isEmpty() && text.length() > 30) {
                    content.append("• ").append(text).append("\n");
                    count++;
                    if (count >= 10) break;
                }
            }

            if (count < 2) {
                content = new StringBuilder(getDefaultSupportGroupsContent());
            }

            Log.d(TAG, "✓ Scraped support groups");
            return content.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error scraping support groups: " + e.getMessage());
            return getDefaultSupportGroupsContent();
        }
    }

    /**
     * Scrape fitness guide from website
     */
    public static String scrapePhysicalHealth() {
        try {
            Document doc = Jsoup.connect("https://www.cdc.gov/physicalactivity/index.html")
                    .timeout(10000)
                    .userAgent("Mozilla/5.0")
                    .get();

            StringBuilder content = new StringBuilder("💪 PHYSICAL HEALTH GUIDE\n\n");

            Elements paragraphs = doc.select("p");
            int count = 0;
            for (Element p : paragraphs) {
                String text = p.text().trim();
                if (!text.isEmpty() && text.length() > 50) {
                    content.append("• ").append(text).append("\n\n");
                    count++;
                    if (count >= 6) break;
                }
            }

            if (count < 2) {
                content = new StringBuilder(getDefaultPhysicalHealthContent());
            }

            Log.d(TAG, "✓ Scraped physical health");
            return content.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error scraping physical health: " + e.getMessage());
            return getDefaultPhysicalHealthContent();
        }
    }

    // Default fallback content methods
    private static String getDefaultSelfCareContent() {
        return "🧘 SELF-CARE PRACTICES\n\n" +
                "1. MEDITATION & MINDFULNESS\n" +
                "• 10-15 min daily meditation\n" +
                "• Body scan exercises\n" +
                "• Mindful breathing (4-7-8 technique)\n\n" +
                "2. PHYSICAL ACTIVITY\n" +
                "• 30 min exercise daily\n" +
                "• Yoga or stretching\n" +
                "• Outdoor walks in nature\n\n" +
                "3. SLEEP HYGIENE\n" +
                "• 7-9 hours nightly\n" +
                "• Consistent sleep schedule\n" +
                "• Avoid screens before bed\n\n" +
                "4. NUTRITION\n" +
                "• Balanced, nutritious meals\n" +
                "• Stay hydrated (8+ glasses water)\n" +
                "• Limit caffeine & sugar\n\n" +
                "5. SOCIAL CONNECTION\n" +
                "• Quality time with loved ones\n" +
                "• Join community groups\n" +
                "• Share your feelings openly";
    }

    private static String getDefaultCopingStrategiesContent() {
        return "🎯 COPING STRATEGIES\n\n" +
                "FOR ANXIETY:\n" +
                "• Deep breathing (5-4-3-2-1 grounding)\n" +
                "• Progressive muscle relaxation\n" +
                "• Physical activity\n\n" +
                "FOR SADNESS:\n" +
                "• Reach out to friends/family\n" +
                "• Engage in meaningful activities\n" +
                "• Practice self-compassion\n\n" +
                "FOR ANGER:\n" +
                "• Take time-outs\n" +
                "• Physical exercise\n" +
                "• Creative expression\n\n" +
                "FOR STRESS:\n" +
                "• Break tasks into smaller steps\n" +
                "• Take regular breaks\n" +
                "• Practice relaxation techniques\n\n" +
                "GENERAL STRATEGIES:\n" +
                "• Keep a journal\n" +
                "• Maintain routines\n" +
                "• Seek professional help when needed";
    }

    private static String getDefaultWellnessContent() {
        return "❤️ WELLNESS RESOURCES\n\n" +
                "PHYSICAL WELLNESS:\n" +
                "• Regular exercise (150 min/week)\n" +
                "• Balanced nutrition\n" +
                "• Adequate sleep\n\n" +
                "MENTAL WELLNESS:\n" +
                "• Therapy/counseling\n" +
                "• Meditation apps\n" +
                "• Support groups\n\n" +
                "EMOTIONAL WELLNESS:\n" +
                "• Strong relationships\n" +
                "• Creative outlets\n" +
                "• Meaningful activities\n\n" +
                "SPIRITUAL WELLNESS:\n" +
                "• Yoga & mindfulness\n" +
                "• Nature connection\n" +
                "• Volunteer work\n\n" +
                "RECOMMENDED APPS:\n" +
                "• Headspace (meditation)\n" +
                "• Calm (sleep & relaxation)\n" +
                "• Insight Timer (free meditation)\n" +
                "• BetterHelp (therapy)";
    }

    private static String getDefaultSupportGroupsContent() {
        return "👥 SUPPORT COMMUNITIES\n\n" +
                "ONLINE SUPPORT:\n" +
                "• 7 Cups of Tea (emotional support)\n" +
                "• Reddit r/mentalhealth\n" +
                "• Discord mental health communities\n\n" +
                "CONDITION-SPECIFIC:\n" +
                "• NAMI (National Alliance on Mental Illness)\n" +
                "• DBSA (Depression & Bipolar Support)\n" +
                "• ADAA (Anxiety & Depression Association)\n\n" +
                "LOCAL GROUPS:\n" +
                "• AA/NA Meetings\n" +
                "• Therapy support groups\n" +
                "• Community mental health centers\n\n" +
                "PEER SUPPORT:\n" +
                "• Find local support groups\n" +
                "• University counseling centers\n" +
                "• Workplace EAP programs";
    }

    private static String getDefaultPhysicalHealthContent() {
        return "💪 PHYSICAL HEALTH GUIDE\n\n" +
                "EXERCISE TYPES:\n" +
                "• Cardio: 150 min/week (walking, running)\n" +
                "• Strength: 2 days/week\n" +
                "• Flexibility: yoga, stretching\n\n" +
                "NUTRITION TIPS:\n" +
                "• Eat colorful fruits & vegetables\n" +
                "• Choose whole grains\n" +
                "• Drink adequate water\n" +
                "• Limit processed foods\n\n" +
                "SLEEP IMPROVEMENT:\n" +
                "• Keep consistent schedule\n" +
                "• Cool, dark bedroom\n" +
                "• Avoid screens 1 hour before bed\n\n" +
                "STRESS REDUCTION:\n" +
                "• Regular exercise\n" +
                "• Meditation\n" +
                "• Adequate rest\n" +
                "• Social connection";
    }
}
