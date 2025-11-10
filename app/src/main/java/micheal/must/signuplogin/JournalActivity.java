package micheal.must.signuplogin;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import micheal.must.signuplogin.fragments.JournalArchiveFragment;
import micheal.must.signuplogin.fragments.JournalEntriesFragment;
import micheal.must.signuplogin.fragments.JournalFavoritesFragment;

public class JournalActivity extends AppCompatActivity {

    private static final String TAG = "JournalActivity";
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private JournalPagerAdapter pagerAdapter;

    // Tab titles
    private final String[] tabTitles = {"My Entries", "Favorites", "Archive"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        try {
            initViews();
            setupToolbar();
            setupViewPager();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Journal");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager() {
        if (viewPager == null) {
            Log.e(TAG, "ViewPager2 is null");
            return;
        }

        pagerAdapter = new JournalPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        if (tabLayout != null) {
            new TabLayoutMediator(tabLayout, viewPager, (tab, position) ->
                    tab.setText(tabTitles[position])
            ).attach();
        }
    }

    // ViewPager adapter to manage fragments
    private class JournalPagerAdapter extends FragmentStateAdapter {

        public JournalPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            try {
                switch (position) {
                    case 0:
                        return new JournalEntriesFragment();
                    case 1:
                        return new JournalFavoritesFragment();
                    case 2:
                        return new JournalArchiveFragment();
                    default:
                        return new JournalEntriesFragment();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error creating fragment at position " + position + ": " + e.getMessage(), e);
                return new JournalEntriesFragment();
            }
        }

        @Override
        public int getItemCount() {
            return tabTitles.length;
        }
    }
}
