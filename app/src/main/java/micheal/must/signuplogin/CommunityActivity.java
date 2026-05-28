package micheal.must.signuplogin;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import micheal.must.signuplogin.adapters.CommunityViewPagerAdapter;

public class CommunityActivity extends AppCompatActivity {

    private static final String TAG = "CommunityActivity";
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "onCreate() called");
        setContentView(R.layout.activity_community);
        Log.d(TAG, "Layout set: activity_community");

        try {
            viewPager = findViewById(R.id.viewPager);
            tabLayout = findViewById(R.id.tabLayout);
            btnBack = findViewById(R.id.btn_back);
            
            Log.d(TAG, "After findViewById: vp2=" + (viewPager != null) + ", tab=" + (tabLayout != null) + ", btn=" + (btnBack != null));
        } catch (Exception e) {
            Log.e(TAG, "Error in findViewById", e);
            return;
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        if (viewPager != null && tabLayout != null) {
            try {
                CommunityViewPagerAdapter adapter = new CommunityViewPagerAdapter(this);
                viewPager.setAdapter(adapter);

                new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                    if (position == 0) tab.setText("Posts");
                    else if (position == 1) tab.setText("Groups");
                }).attach();
                
                Log.d(TAG, "✓ ViewPager2 and TabLayout setup successful");
            } catch (Exception e) {
                Log.e(TAG, "Error setting up ViewPager2", e);
            }
        } else {
            Log.e(TAG, "CRITICAL: ViewPager2 or TabLayout is null!");
        }
    }
}
