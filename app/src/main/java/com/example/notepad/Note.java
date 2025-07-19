package com.example.notepad;

public class Note {
    private int id;
    private String username;
    private String title;
    private String description;
    private String reminderTime;
    private int color;

    public Note(int id, String username, String title, String description, String reminderTime, int color) {
        this.id = id;
        this.username = username;
        this.title = title;
        this.description = description;
        this.reminderTime = reminderTime;
        this.color = color;
    }

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getReminderTime() { return reminderTime; }
    public int getColor() { return color; }

    // Setters (optional)
    public void setColor(int color) { this.color = color; }
}
