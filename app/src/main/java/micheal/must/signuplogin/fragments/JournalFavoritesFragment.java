package micheal.must.signuplogin.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import micheal.must.signuplogin.R;
import micheal.must.signuplogin.adapters.JournalEntryAdapter;
import micheal.must.signuplogin.models.JournalEntry;

public class JournalFavoritesFragment extends Fragment {

    private static final String TAG = "JournalFavoritesFragment";
    private RecyclerView rvFavorites;
    private TextView tvEmptyState;
    private List<JournalEntry> favorites = new ArrayList<>();
    private JournalEntryAdapter adapter;
    private DatabaseReference entriesRef;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_journal_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) return;

        rvFavorites = view.findViewById(R.id.rv_favorites);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);

        entriesRef = FirebaseDatabase.getInstance().getReference()
                .child("journal_entries").child(userId);

        adapter = new JournalEntryAdapter(favorites, this::onEntryActionClick);
        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFavorites.setAdapter(adapter);

        loadFavorites();
    }

    private void loadFavorites() {
        entriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favorites.clear();

                for (DataSnapshot entrySnapshot : snapshot.getChildren()) {
                    try {
                        JournalEntry entry = entrySnapshot.getValue(JournalEntry.class);
                        if (entry != null && entry.isFavorite() && !entry.isArchived()) {
                            entry.setEntryId(entrySnapshot.getKey());
                            favorites.add(entry);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing entry: " + e.getMessage());
                    }
                }

                favorites.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                adapter.notifyDataSetChanged();

                // Show empty state if no favorites
                if (favorites.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    rvFavorites.setVisibility(View.GONE);
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                    rvFavorites.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error: " + error.getMessage());
            }
        });
    }

    private void onEntryActionClick(JournalEntry entry, String action) {
        DatabaseReference entryRef = entriesRef.child(entry.getEntryId());

        if ("favorite".equals(action)) {
            entryRef.child("isFavorite").setValue(false);
        } else if ("archive".equals(action)) {
            entryRef.child("isArchived").setValue(true);
        }
    }
}
