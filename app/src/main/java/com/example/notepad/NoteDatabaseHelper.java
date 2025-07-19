package com.example.notepad;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;

import java.util.Random;

public class NoteDatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "notes.db";
    private static final int DB_VERSION = 2;

    public NoteDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Users table
        db.execSQL("CREATE TABLE IF NOT EXISTS users(" +
                "username TEXT PRIMARY KEY, " +
                "password TEXT)");

        // Notes table with username to differentiate notes by user
        db.execSQL("CREATE TABLE IF NOT EXISTS notes(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, " +
                "title TEXT, " +
                "description TEXT, " +
                "reminderTime TEXT, " +
                "color INTEGER DEFAULT 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS notes");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    // User registration
    public boolean userExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username = ?", new String[]{username});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public void insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO users (username, password) VALUES (?, ?)", new Object[]{username, password});
    }

    public boolean validateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username = ? AND password = ?", new String[]{username, password});
        boolean valid = cursor.moveToFirst();
        cursor.close();
        return valid;
    }

    // Add note for a specific user
    public boolean addNote(String username, String title, String desc, String reminder) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            int[] colors = {
                    Color.parseColor("#FFCDD2"), Color.parseColor("#F8BBD0"),
                    Color.parseColor("#E1BEE7"), Color.parseColor("#D1C4E9"),
                    Color.parseColor("#BBDEFB"), Color.parseColor("#B2DFDB"),
                    Color.parseColor("#DCEDC8"), Color.parseColor("#FFF9C4"),
                    Color.parseColor("#FFECB3"), Color.parseColor("#D7CCC8")
            };
            int randomColor = colors[new Random().nextInt(colors.length)];

            db.execSQL("INSERT INTO notes(username, title, description, reminderTime, color) VALUES(?,?,?,?,?)",
                    new Object[]{username, title, desc, reminder, randomColor});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all notes for a specific user
    public Cursor getNotesByUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM notes WHERE username = ?", new String[]{username});
    }

    // Delete note by ID
    public void deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM notes WHERE id = ?", new Object[]{id});
    }

    // Get single note by ID
    public Cursor getNoteById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM notes WHERE id = ?", new String[]{String.valueOf(id)});
    }

    // Get last note ID
    public int getLastNoteId() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT MAX(id) FROM notes", null);
        int lastId = 0;
        if (c.moveToFirst()) {
            lastId = c.getInt(0);
        }
        c.close();
        return lastId;
    }

    // Update existing note
    public void updateNote(int id, String title, String desc, String reminder) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE notes SET title = ?, description = ?, reminderTime = ? WHERE id = ?",
                new Object[]{title, desc, reminder, id});
    }

    // Optional: get all notes (e.g. for debugging, not used in production)
    public Cursor getAllNotes() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM notes", null);
    }
}
