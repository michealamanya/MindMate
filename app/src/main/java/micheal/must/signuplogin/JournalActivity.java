package micheal.must.signuplogin;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import micheal.must.signuplogin.adapters.JournalViewPagerAdapter;

public class JournalActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        JournalViewPagerAdapter adapter = new JournalViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator((TabLayout) tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("New Entry");
                            break;
                        case 1:
                            tab.setText("My Entries");
                            break;
                    }
                }).attach();
    }
}
