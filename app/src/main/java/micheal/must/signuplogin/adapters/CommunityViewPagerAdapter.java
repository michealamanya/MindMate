package micheal.must.signuplogin.adapters;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import micheal.must.signuplogin.fragments.PostsFragment;
import micheal.must.signuplogin.fragments.GroupsFragment;

public class CommunityViewPagerAdapter extends FragmentStateAdapter {

    public CommunityViewPagerAdapter(@NonNull AppCompatActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PostsFragment();
            case 1:
                return new GroupsFragment();
            default:
                return new PostsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
