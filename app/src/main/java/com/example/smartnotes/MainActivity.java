package com.example.smartnotes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NoteAdapter.OnNoteClickListener {

    Button btnAdd;

    EditText edtSearch;

    TextView txtEmpty;

    RecyclerView recyclerNotes;

    ArrayList<Note> noteList;

    ArrayList<Note> allNotes;

    NoteAdapter noteAdapter;

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        btnAdd = findViewById(R.id.btnAdd);

        edtSearch = findViewById(R.id.edtSearch);

        txtEmpty = findViewById(R.id.txtEmpty);

        recyclerNotes =
                findViewById(R.id.recyclerNotes);

        preferences =
                getSharedPreferences(
                        "SmartNotes",
                        MODE_PRIVATE
                );

        noteList =
                new ArrayList<>();

        allNotes =
                new ArrayList<>();

        recyclerNotes.setLayoutManager(
                new LinearLayoutManager(this)
        );

        noteAdapter =
                new NoteAdapter(
                        noteList,
                        this
                );

        recyclerNotes.setAdapter(
                noteAdapter
        );

        // Add Note

        btnAdd.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            AddNoteActivity.class
                    );

            startActivity(intent);
        });

        // Search

        edtSearch.addTextChangedListener(
                new android.text.TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        searchNotes(
                                s.toString()
                        );
                    }

                    @Override
                    public void afterTextChanged(
                            android.text.Editable s) {
                    }
                }
        );

        loadNotes();
    }

    @Override
    protected void onResume() {

        super.onResume();

        loadNotes();
    }

    // Load notes

    private void loadNotes() {

        allNotes.clear();

        String data =
                preferences.getString(
                        "notes",
                        "[]"
                );

        try {

            JSONArray notesArray =
                    new JSONArray(data);

            for (int i = 0;
                 i < notesArray.length();
                 i++) {

                JSONObject object =
                        notesArray.getJSONObject(i);

                String title =
                        object.getString(
                                "title"
                        );

                String content =
                        object.getString(
                                "content"
                        );

                String date = object.optString(
                        "date",
                        ""
                );

                allNotes.add(
                        new Note(
                                title,
                                content,
                                date
                        )
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        searchNotes(
                edtSearch.getText().toString()
        );
    }

    // Search

    private void searchNotes(
            String searchText) {

        noteList.clear();

        String search =
                searchText
                        .toLowerCase()
                        .trim();

        for (Note note : allNotes) {

            if (
                    note.title
                            .toLowerCase()
                            .contains(search)
                            ||
                            note.content
                                    .toLowerCase()
                                    .contains(search)
            ) {

                noteList.add(note);
            }
        }

        noteAdapter.notifyDataSetChanged();

        if (noteList.isEmpty()) {

            txtEmpty.setVisibility(
                    View.VISIBLE
            );

            if (search.isEmpty()) {

                txtEmpty.setText(
                        "No notes yet\nTap + to create your first note ✨"
                );

            } else {

                txtEmpty.setText(
                        "No notes found 🔍"
                );
            }

        } else {

            txtEmpty.setVisibility(
                    View.GONE
            );
        }
    }

    // Tap note = Edit

    @Override
    public void onNoteClick(int position) {

        Note selectedNote =
                noteList.get(position);

        int realPosition =
                allNotes.indexOf(
                        selectedNote
                );

        Intent intent =
                new Intent(
                        MainActivity.this,
                        AddNoteActivity.class
                );

        intent.putExtra(
                "position",
                realPosition
        );

        startActivity(intent);
    }

    // Long press = Delete

    @Override
    public void onNoteLongClick(
            int position) {

        Note selectedNote =
                noteList.get(position);

        int realPosition =
                allNotes.indexOf(
                        selectedNote
                );

        new AlertDialog.Builder(this)

                .setTitle("Delete Note")

                .setMessage(
                        "Are you sure you want to delete this note?"
                )

                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> {

                            deleteNote(
                                    realPosition
                            );
                        }
                )

                .setNegativeButton(
                        "Cancel",
                        null
                )

                .show();
    }

    // Delete note

    private void deleteNote(
            int position) {

        try {

            String data =
                    preferences.getString(
                            "notes",
                            "[]"
                    );

            JSONArray notesArray =
                    new JSONArray(data);

            notesArray.remove(position);

            preferences.edit()
                    .putString(
                            "notes",
                            notesArray.toString()
                    )
                    .apply();

            loadNotes();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}