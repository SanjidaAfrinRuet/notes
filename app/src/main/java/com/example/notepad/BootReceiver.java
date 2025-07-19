package com.example.notepad;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            NoteDatabaseHelper db = new NoteDatabaseHelper(context);
            Cursor cursor = db.getAllNotes(); // Ensure this method returns all saved notes

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(0); // Assuming _id is column 0
                    String title = cursor.getString(2); // Assuming title is column 2
                    String desc = cursor.getString(3); // Assuming description is column 3
                    String reminderTime = cursor.getString(4); // Assuming reminder time is column 4

                    if (reminderTime != null && !reminderTime.isEmpty()) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                            Date date = sdf.parse(reminderTime);

                            if (date != null && date.after(new Date())) {
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(date);

                                Intent i = new Intent(context, ReminderReceiver.class);
                                i.putExtra("noteTitle", title);
                                i.putExtra("noteDescription", desc);

                                PendingIntent pi = PendingIntent.getBroadcast(
                                        context, id, i,
                                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                                );

                                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                                if (am != null) {
                                    am.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
                cursor.close();
            }
        }
    }
}
