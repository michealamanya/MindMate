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

public class JournalArchiveFragment extends Fragment {

    private static final String TAG = "JournalArchiveFragment";
    private RecyclerView rvArchive;
    private TextView tvEmptyState;
    private List<JournalEntry> archived = new ArrayList<>();
    private JournalEntryAdapter adapter;
    private DatabaseReference entriesRef;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_journal_archive, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) return;

        rvArchive = view.findViewById(R.id.rv_archive);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);

        entriesRef = FirebaseDatabase.getInstance().getReference()
                .child("journal_entries").child(userId);

        adapter = new JournalEntryAdapter(archived, this::onEntryActionClick);
        rvArchive.setLayoutManager(new LinearLayoutManager(getContext()));
        rvArchive.setAdapter(adapter);

        loadArchived();
    }

    private void loadArchived() {
        entriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                archived.clear();

                for (DataSnapshot entrySnapshot : snapshot.getChildren()) {
                    try {
                        JournalEntry entry = entrySnapshot.getValue(JournalEntry.class);
                        if (entry != null && entry.isArchived()) {
                            entry.setEntryId(entrySnapshot.getKey());
                            archived.add(entry);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing entry: " + e.getMessage());
                    }
                }

                archived.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                adapter.notifyDataSetChanged();

                // Show empty state if no archived entries
                if (archived.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    rvArchive.setVisibility(View.GONE);
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                    rvArchive.setVisibility(View.VISIBLE);
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
            entryRef.child("isFavorite").setValue(!entry.isFavorite());
        } else if ("archive".equals(action)) {
            entryRef.child("isArchived").setValue(false); // Unarchive
        }
    }
}
