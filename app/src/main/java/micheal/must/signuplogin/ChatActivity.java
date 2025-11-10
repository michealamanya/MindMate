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

import micheal.must.signuplogin.fragments.ChatBotFragment;
import micheal.must.signuplogin.fragments.ChatHistoryFragment;
import micheal.must.signuplogin.fragments.ChatResourcesFragment;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ChatPagerAdapter pagerAdapter;

    // Tab titles
    private final String[] tabTitles = {"Chat", "History", "Resources"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

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
                getSupportActionBar().setTitle("Chat");
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

        pagerAdapter = new ChatPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        if (tabLayout != null) {
            new TabLayoutMediator(tabLayout, viewPager, (tab, position) ->
                    tab.setText(tabTitles[position])
            ).attach();
        }
    }

    // ViewPager adapter to manage fragments
    private class ChatPagerAdapter extends FragmentStateAdapter {

        public ChatPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            try {
                switch (position) {
                    case 0:
                        return new ChatBotFragment();
                    case 1:
                        return new ChatHistoryFragment();
                    case 2:
                        return new ChatResourcesFragment();
                    default:
                        return new ChatBotFragment();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error creating fragment at position " + position + ": " + e.getMessage(), e);
                return new ChatBotFragment();
            }
        }

        @Override
        public int getItemCount() {
            return tabTitles.length;
        }
    }
}