package com.example.notepad;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView notesRecyclerView;
    EditText etSearch;
    Button btnAddNote;
    ImageView btnDashboard;
    NoteAdapter adapter;
    NoteDatabaseHelper db;
    SharedPrefManager pref;
    String username;
    ArrayList<Note> userNotes = new ArrayList<>();
    AlertDialog dashboardDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        etSearch = findViewById(R.id.etSearch);
        btnAddNote = findViewById(R.id.btnAddNote);
        btnDashboard = findViewById(R.id.btnDashboard);

        // Initialize SharedPrefManager and get current user
        pref = new SharedPrefManager(this);
        username = pref.getUsername();

        if (username == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize DB and adapter
        db = new NoteDatabaseHelper(this);
        adapter = new NoteAdapter(this, userNotes);
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notesRecyclerView.setAdapter(adapter);

        // Load notes for the logged-in user
        loadNotes();

        // Add new note
        btnAddNote.setOnClickListener(v -> {
            startActivity(new Intent(this, AddNoteActivity.class));
        });

        // Dashboard icon click
        btnDashboard.setOnClickListener(v -> showDashboard());

        // Search bar logic
        etSearch.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNotes(s.toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void afterTextChanged(Editable s) {}
        });
    }

    // Load only current user's notes from DB
    private void loadNotes() {
        userNotes.clear();
        Cursor c = db.getNotesByUser(username);
        while (c.moveToNext()) {
            int id = c.getInt(c.getColumnIndexOrThrow("id"));
            String title = c.getString(c.getColumnIndexOrThrow("title"));
            String desc = c.getString(c.getColumnIndexOrThrow("description"));
            String time = c.getString(c.getColumnIndexOrThrow("reminderTime"));
            int color = c.getInt(c.getColumnIndexOrThrow("color"));
            userNotes.add(new Note(id, username, title, desc, time, color));
        }
        adapter.updateList(userNotes);
    }

    // Filter search results
    private void filterNotes(String keyword) {
        ArrayList<Note> filtered = new ArrayList<>();
        for (Note note : userNotes) {
            if (note.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                    note.getDescription().toLowerCase().contains(keyword.toLowerCase())) {
                filtered.add(note);
            }
        }
        adapter.updateList(filtered);
    }

    // Show dashboard with options
    private void showDashboard() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Dashboard")
                .setMessage("Logged in as: " + username)
                .setPositiveButton("Logout", (dialog, which) -> {
                    pref.logout(); // clear stored username
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("Switch Account", (dialog, which) -> {
                    pref.logout(); // clear stored username
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNeutralButton("Close", null);

        dashboardDialog = builder.create();
        dashboardDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes(); // Reload notes when returning
    }
}
