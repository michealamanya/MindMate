package micheal.must.signuplogin.adapters;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import micheal.must.signuplogin.fragments.ChatBotFragment;
import micheal.must.signuplogin.fragments.ResourcesFragment;
import micheal.must.signuplogin.fragments.AssistanceFragment;

public class ChatViewPagerAdapter extends FragmentStateAdapter {

    public ChatViewPagerAdapter(@NonNull AppCompatActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ChatBotFragment();
            case 1:
                return new ResourcesFragment();
            case 2:
                return new AssistanceFragment();
            default:
                return new ChatBotFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
