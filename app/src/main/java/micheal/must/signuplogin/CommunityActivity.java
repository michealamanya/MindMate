package micheal.must.signuplogin;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import micheal.must.signuplogin.fragments.AllPostsFragment;
import micheal.must.signuplogin.fragments.CreatePostDialogFragment;
import micheal.must.signuplogin.fragments.CreateGroupDialogFragment;
import micheal.must.signuplogin.fragments.DiscoverFragment;
import micheal.must.signuplogin.fragments.MyGroupsFragment;

public class CommunityActivity extends AppCompatActivity {

    private static final String TAG = "CommunityActivity";
    private Toolbar toolbar;

    private TabLayout tabLayout;
    private RecyclerView rvCommunityPosts;
    private ViewPager2 viewPager;
    private FloatingActionButton fabNewPost;
    private FloatingActionButton fabCreateGroup;
    private CommunityPagerAdapter pagerAdapter;

    // Tab titles
    private final String[] tabTitles = {"All Posts", "My Groups", "Discover"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        try {
            initViews();
            setupToolbar();
            setupViewPager();
            setupFabs();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        fabNewPost = findViewById(R.id.fab_new_post);
        fabCreateGroup = findViewById(R.id.fab_create_group);
        rvCommunityPosts = findViewById(R.id.rv_community_posts);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Community");
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

        pagerAdapter = new CommunityPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        if (tabLayout != null) {
            new TabLayoutMediator(tabLayout, viewPager, (tab, position) ->
                    tab.setText(tabTitles[position])
            ).attach();
            
            // Set white text color for tab labels
            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                if (tab != null && tab.getCustomView() != null) {
                    android.widget.TextView tabLabel = (android.widget.TextView) tab.getCustomView();
                    tabLabel.setTextColor(android.graphics.Color.WHITE);
                }
            }
        }

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                // Tab 0: All Posts - show post FAB
                // Tab 1: My Groups - show group FAB
                // Tab 2: Discover - hide both FABs
                if (position == 0) {
                    if (fabNewPost != null) fabNewPost.show();
                    if (fabCreateGroup != null) fabCreateGroup.hide();
                } else if (position == 1) {
                    if (fabNewPost != null) fabNewPost.hide();
                    if (fabCreateGroup != null) fabCreateGroup.show();
                } else {
                    if (fabNewPost != null) fabNewPost.hide();
                    if (fabCreateGroup != null) fabCreateGroup.hide();
                }
            }
        });
    }

    private void setupFabs() {
        if (fabNewPost != null) {
            fabNewPost.setOnClickListener(view -> showCreatePostDialog());
        }

        if (fabCreateGroup != null) {
            fabCreateGroup.setOnClickListener(view -> showCreateGroupDialog());
            fabCreateGroup.hide();
        }
    }

    private void showCreatePostDialog() {
        try {
            CreatePostDialogFragment dialogFragment = new CreatePostDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "create_post_dialog");
        } catch (Exception e) {
            Log.e(TAG, "Error showing create post dialog: " + e.getMessage(), e);
        }
    }

    private void showCreateGroupDialog() {
        try {
            CreateGroupDialogFragment dialogFragment = new CreateGroupDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "create_group_dialog");
        } catch (Exception e) {
            Log.e(TAG, "Error showing create group dialog: " + e.getMessage(), e);
        }
    }

    // ViewPager adapter to manage fragments
    private class CommunityPagerAdapter extends FragmentStateAdapter {

        public CommunityPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            try {
                switch (position) {
                    case 0:
                        AllPostsFragment allPostsFragment = new AllPostsFragment();
                        // Set text color to white for All Posts fragment
                        Bundle args = new Bundle();
                        args.putInt("textColor", android.graphics.Color.WHITE);
                        allPostsFragment.setArguments(args);
                        return allPostsFragment;
                    case 1:
                        return new MyGroupsFragment();
                    case 2:
                        return new DiscoverFragment();
                    default:
                        return new AllPostsFragment();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error creating fragment at position " + position + ": " + e.getMessage(), e);
                return new AllPostsFragment();
            }
        }

        @Override
        public int getItemCount() {
            return tabTitles.length;
        }
    }
}
