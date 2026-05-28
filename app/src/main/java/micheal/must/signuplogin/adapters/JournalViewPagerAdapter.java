package micheal.must.signuplogin.adapters;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import micheal.must.signuplogin.fragments.JournalEntryFragment;
import micheal.must.signuplogin.fragments.JournalEntriesFragment;

public class JournalViewPagerAdapter extends FragmentStateAdapter {

    public JournalViewPagerAdapter(@NonNull AppCompatActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new JournalEntryFragment();
            case 1:
                return new JournalEntriesFragment();
            default:
                return new JournalEntryFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
