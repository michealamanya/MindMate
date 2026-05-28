package micheal.must.signuplogin.adapters;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import micheal.must.signuplogin.fragments.ResourcesFragment;
import micheal.must.signuplogin.fragments.MentalHealthFragment;
import micheal.must.signuplogin.fragments.WellnessFragment;

public class ResourcesViewPagerAdapter extends FragmentStateAdapter {

    public ResourcesViewPagerAdapter(@NonNull AppCompatActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ResourcesFragment();
            case 1:
                return new MentalHealthFragment();
            case 2:
                return new WellnessFragment();
            default:
                return new ResourcesFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
