package com.example.notepad;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditNoteActivity extends AppCompatActivity {

    EditText etTitle, etDescription;
    Switch switchReminder;
    Button btnPickDate, btnPickTime, btnUpdate;
    TextView tvReminderTime;

    SharedPrefManager pref;
    NoteDatabaseHelper db;
    Calendar reminderCalendar;
    int noteId;
    String currentReminder = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        etTitle = findViewById(R.id.etEditTitle);
        etDescription = findViewById(R.id.etEditDescription);
        switchReminder = findViewById(R.id.switchEditReminder);
        btnPickDate = findViewById(R.id.btnEditPickDate);
        btnPickTime = findViewById(R.id.btnEditPickTime);
        tvReminderTime = findViewById(R.id.tvEditReminderTime);
        btnUpdate = findViewById(R.id.btnUpdateNote);

        pref = new SharedPrefManager(this);
        db = new NoteDatabaseHelper(this);
        reminderCalendar = Calendar.getInstance();

        noteId = getIntent().getIntExtra("id", -1);
        loadNoteData();

        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnPickDate.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            btnPickTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        btnPickDate.setOnClickListener(v -> {
            int y = reminderCalendar.get(Calendar.YEAR);
            int m = reminderCalendar.get(Calendar.MONTH);
            int d = reminderCalendar.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                reminderCalendar.set(Calendar.YEAR, year);
                reminderCalendar.set(Calendar.MONTH, month);
                reminderCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateReminderText();
            }, y, m, d).show();
        });

        btnPickTime.setOnClickListener(v -> {
            int h = reminderCalendar.get(Calendar.HOUR_OF_DAY);
            int min = reminderCalendar.get(Calendar.MINUTE);
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                reminderCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                reminderCalendar.set(Calendar.MINUTE, minute);
                reminderCalendar.set(Calendar.SECOND, 0);
                updateReminderText();
            }, h, min, false).show();
        });

        btnUpdate.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDescription.getText().toString().trim();
            String newReminder = switchReminder.isChecked() ? formatReminder(reminderCalendar) : "";

            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            db.updateNote(noteId, title, desc, newReminder);

            if (!newReminder.isEmpty()) {
                setAlarm(noteId, reminderCalendar, title, desc);
            }

            Toast.makeText(this, "Note updated!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void loadNoteData() {
        Cursor c = db.getNoteById(noteId);
        if (c != null && c.moveToFirst()) {
            etTitle.setText(c.getString(2)); // Assuming title is in column index 2
            etDescription.setText(c.getString(3)); // Assuming desc is index 3
            currentReminder = c.getString(4); // Assuming reminder is index 4

            if (currentReminder != null && !currentReminder.isEmpty()) {
                switchReminder.setChecked(true);
                btnPickDate.setVisibility(View.VISIBLE);
                btnPickTime.setVisibility(View.VISIBLE);
                tvReminderTime.setText("Reminder: " + currentReminder);

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    Date date = sdf.parse(currentReminder);
                    if (date != null) {
                        reminderCalendar.setTime(date);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateReminderText() {
        tvReminderTime.setText("Reminder: " + formatReminder(reminderCalendar));
    }

    private String formatReminder(Calendar c) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(c.getTime());
    }

    private void setAlarm(int noteId, Calendar cal, String title, String desc) {
        Intent i = new Intent(this, ReminderReceiver.class);
        i.putExtra("noteTitle", title);
        i.putExtra("noteDescription", desc);

        PendingIntent pi = PendingIntent.getBroadcast(
                this,
                noteId,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            am.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        }
    }
}
