
package com.example.notepad;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Calendar;

public class AddNoteActivity extends AppCompatActivity {

    EditText etTitle, etDescription;
    Button btnSaveNote, btnPickDate, btnPickTime;
    Switch switchReminder;
    TextView tvReminderTime;

    Calendar reminderCalendar;
    NoteDatabaseHelper dbHelper;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        // Initialize views
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        btnSaveNote = findViewById(R.id.btnSaveNote);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickTime = findViewById(R.id.btnPickTime);
        switchReminder = findViewById(R.id.switchReminder);
        tvReminderTime = findViewById(R.id.tvReminderTime);

        dbHelper = new NoteDatabaseHelper(this);
        reminderCalendar = Calendar.getInstance();

        SharedPrefManager pref = new SharedPrefManager(this);
        username = pref.getUsername();

        // Ask for POST_NOTIFICATIONS permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                tvReminderTime.setText("Reminder: OFF");
            }
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
            }, h, min, true).show();
        });

        btnSaveNote.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDescription.getText().toString().trim();

            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Please enter title and description", Toast.LENGTH_SHORT).show();
                return;
            }

            long reminderTime = 0;
            String reminderStr = "";

            if (switchReminder.isChecked()) {
                reminderTime = reminderCalendar.getTimeInMillis();

                if (reminderTime <= System.currentTimeMillis()) {
                    Toast.makeText(this, "Please select a future time for the reminder", Toast.LENGTH_SHORT).show();
                    return;
                }

                setReminder(reminderTime, title, desc);
                reminderStr = android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", reminderTime).toString();
            }

            boolean saved = dbHelper.addNote(username, title, desc, reminderStr);

            if (saved) {
                Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error saving note", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateReminderText() {
        tvReminderTime.setText("Reminder set: " +
                android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", reminderCalendar));
    }

    private void setReminder(long timeInMillis, String title, String desc) {
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("noteTitle", title);
        intent.putExtra("noteDescription", desc);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }
    }
}
