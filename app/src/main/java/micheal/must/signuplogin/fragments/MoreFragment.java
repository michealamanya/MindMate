package micheal.must.signuplogin.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.fragment.app.Fragment;
import micheal.must.signuplogin.MoreOptionsActivity;

public class MoreFragment extends Fragment {
    public MoreFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout ll = new LinearLayout(requireContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        ll.setPadding(pad, pad, pad, pad);

        Button btn = new Button(requireContext());
        btn.setText("Open Settings");
        btn.setOnClickListener(v -> startActivity(new Intent(requireContext(), MoreOptionsActivity.class)));
        ll.addView(btn);

        return ll;
    }
}
