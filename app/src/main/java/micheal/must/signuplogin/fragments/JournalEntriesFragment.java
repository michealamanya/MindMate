package micheal.must.signuplogin.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import micheal.must.signuplogin.R;
import micheal.must.signuplogin.adapters.JournalEntryAdapter;
import micheal.must.signuplogin.models.JournalEntry;

public class JournalEntriesFragment extends Fragment {

    private static final String TAG = "JournalEntriesFragment";
    private RecyclerView rvEntries;
    private FloatingActionButton fabNewEntry;
    private List<JournalEntry> entries = new ArrayList<>();
    private JournalEntryAdapter adapter;
    private DatabaseReference entriesRef;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_journal_entries, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(getContext(), "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        rvEntries = view.findViewById(R.id.rv_entries);
        fabNewEntry = view.findViewById(R.id.fab_new_entry);

        entriesRef = FirebaseDatabase.getInstance().getReference()
                .child("journal_entries").child(userId);

        // Setup RecyclerView
        adapter = new JournalEntryAdapter(entries, this::onEntryActionClick);
        rvEntries.setLayoutManager(new LinearLayoutManager(getContext()));
        rvEntries.setAdapter(adapter);

        // Setup FAB
        if (fabNewEntry != null) {
            fabNewEntry.setOnClickListener(v -> showNewEntryDialog());
        }

        // Load entries
        loadEntries();
    }

    private void loadEntries() {
        entriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                entries.clear();

                for (DataSnapshot entrySnapshot : snapshot.getChildren()) {
                    try {
                        JournalEntry entry = entrySnapshot.getValue(JournalEntry.class);
                        if (entry != null) {
                            entry.setEntryId(entrySnapshot.getKey());
                            // Only show non-archived entries
                            if (!entry.isArchived()) {
                                entries.add(entry);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing entry: " + e.getMessage());
                    }
                }

                // Sort by date (newest first)
                entries.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                adapter.notifyDataSetChanged();

                if (entries.isEmpty()) {
                    Toast.makeText(getContext(), "No entries yet. Create one!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading entries: " + error.getMessage());
                Toast.makeText(getContext(), "Failed to load entries", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNewEntryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("New Journal Entry");

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_journal_entry, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.et_entry_title);
        EditText etContent = dialogView.findViewById(R.id.et_entry_content);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            saveJournalEntry(title, content);
        }).setNegativeButton("Cancel", null).show();
    }

    private void saveJournalEntry(String title, String content) {
        DatabaseReference newEntryRef = entriesRef.push();

        Map<String, Object> entryData = new HashMap<>();
        entryData.put("title", title);
        entryData.put("content", content);
        entryData.put("createdAt", ServerValue.TIMESTAMP);
        entryData.put("isFavorite", false);
        entryData.put("isArchived", false);

        newEntryRef.setValue(entryData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Entry saved!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Entry saved successfully");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to save entry: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error saving entry: " + e.getMessage());
                });
    }

    private void onEntryActionClick(JournalEntry entry, String action) {
        DatabaseReference entryRef = entriesRef.child(entry.getEntryId());

        if ("favorite".equals(action)) {
            entryRef.child("isFavorite").setValue(!entry.isFavorite())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(),
                                entry.isFavorite() ? "Removed from favorites" : "Added to favorites",
                                Toast.LENGTH_SHORT).show();
                    });
        } else if ("archive".equals(action)) {
            entryRef.child("isArchived").setValue(true)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Entry archived", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
