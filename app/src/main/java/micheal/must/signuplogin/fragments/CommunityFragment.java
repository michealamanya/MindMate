package micheal.must.signuplogin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

public class CommunityFragment extends Fragment {
    public CommunityFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView tv = new TextView(requireContext());
        tv.setText("Community");
        tv.setTextSize(20f);
        tv.setPadding(32, 32, 32, 32);
        return tv;
    }
}
