package com.example.smartnotes;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddNoteActivity extends AppCompatActivity {

    EditText edtNoteTitle;
    EditText edtNoteContent;

    Button btnSave;
    Button btnReminder;

    int position = -1;

    long reminderTime = -1;

    private static final int NOTIFICATION_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_note);

        edtNoteTitle =
                findViewById(R.id.edtNoteTitle);

        edtNoteContent =
                findViewById(R.id.edtNoteContent);

        btnSave =
                findViewById(R.id.btnSave);

        btnReminder =
                findViewById(R.id.btnReminder);

        position =
                getIntent().getIntExtra(
                        "position",
                        -1
                );

        // Ask notification permission
        requestNotificationPermission();

        // Load existing note
        if (position != -1) {

            loadNote();
        }

        // Reminder button
        btnReminder.setOnClickListener(v -> {

            showDatePicker();

        });

        // Save button
        btnSave.setOnClickListener(v -> {

            saveNote();

        });
    }

    private void requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.TIRAMISU) {

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        NOTIFICATION_PERMISSION_CODE
                );
            }
        }
    }

    private void loadNote() {

        SharedPreferences preferences =
                getSharedPreferences(
                        "SmartNotes",
                        MODE_PRIVATE
                );

        String data =
                preferences.getString(
                        "notes",
                        "[]"
                );

        try {

            JSONArray notesArray =
                    new JSONArray(data);

            JSONObject note =
                    notesArray.getJSONObject(
                            position
                    );

            edtNoteTitle.setText(
                    note.getString("title")
            );

            edtNoteContent.setText(
                    note.getString("content")
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void showDatePicker() {

        Calendar calendar =
                Calendar.getInstance();

        DatePickerDialog datePickerDialog =
                new DatePickerDialog(
                        this,

                        (view, year, month, day) -> {

                            Calendar selectedDate =
                                    Calendar.getInstance();

                            selectedDate.set(
                                    year,
                                    month,
                                    day
                            );

                            showTimePicker(
                                    selectedDate
                            );
                        },

                        calendar.get(
                                Calendar.YEAR
                        ),

                        calendar.get(
                                Calendar.MONTH
                        ),

                        calendar.get(
                                Calendar.DAY_OF_MONTH
                        )
                );

        datePickerDialog.show();
    }

    private void showTimePicker(
            Calendar selectedDate) {

        Calendar calendar =
                Calendar.getInstance();

        TimePickerDialog timePickerDialog =
                new TimePickerDialog(
                        this,

                        (view, hour, minute) -> {

                            selectedDate.set(
                                    Calendar.HOUR_OF_DAY,
                                    hour
                            );

                            selectedDate.set(
                                    Calendar.MINUTE,
                                    minute
                            );

                            selectedDate.set(
                                    Calendar.SECOND,
                                    0
                            );

                            selectedDate.set(
                                    Calendar.MILLISECOND,
                                    0
                            );

                            reminderTime =
                                    selectedDate
                                            .getTimeInMillis();

                            scheduleReminder();
                        },

                        calendar.get(
                                Calendar.HOUR_OF_DAY
                        ),

                        calendar.get(
                                Calendar.MINUTE
                        ),

                        false
                );

        timePickerDialog.show();
    }

    private void scheduleReminder() {

        if (reminderTime <=
                System.currentTimeMillis()) {

            Toast.makeText(
                    this,
                    "Please select a future time",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String title =
                edtNoteTitle
                        .getText()
                        .toString()
                        .trim();

        String content =
                edtNoteContent
                        .getText()
                        .toString()
                        .trim();

        if (title.isEmpty()) {

            edtNoteTitle.setError(
                    "Enter a title first"
            );

            return;
        }

        // Check exact alarm permission
        AlarmManager alarmManager =
                (AlarmManager)
                        getSystemService(
                                ALARM_SERVICE
                        );

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.S) {

            if (!alarmManager
                    .canScheduleExactAlarms()) {

                Toast.makeText(
                        this,
                        "Allow Alarms & reminders for Smart Notes",
                        Toast.LENGTH_LONG
                ).show();

                Intent settingsIntent =
                        new Intent(
                                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                Uri.parse(
                                        "package:" +
                                                getPackageName()
                                )
                        );

                startActivity(
                        settingsIntent
                );

                return;
            }
        }

        // Notification intent
        Intent intent =
                new Intent(
                        this,
                        ReminderReceiver.class
                );

        intent.putExtra(
                "title",
                title
        );

        intent.putExtra(
                "content",
                content
        );

        int requestCode =
                (int) System.currentTimeMillis();

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        this,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        // Set exact alarm

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.M) {

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
            );

        } else {

            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
            );
        }

        Toast.makeText(
                this,
                "Reminder set successfully! ⏰",
                Toast.LENGTH_LONG
        ).show();
    }

    private void saveNote() {

        String title =
                edtNoteTitle
                        .getText()
                        .toString()
                        .trim();

        String content =
                edtNoteContent
                        .getText()
                        .toString()
                        .trim();

        if (title.isEmpty()) {

            edtNoteTitle.setError(
                    "Enter a title"
            );

            return;
        }

        String currentDate =
                new SimpleDateFormat(
                        "dd MMM yyyy • hh:mm a",
                        Locale.getDefault()
                ).format(new Date());

        SharedPreferences preferences =
                getSharedPreferences(
                        "SmartNotes",
                        MODE_PRIVATE
                );

        String oldNotes =
                preferences.getString(
                        "notes",
                        "[]"
                );

        try {

            JSONArray notesArray =
                    new JSONArray(oldNotes);

            if (position == -1) {

                JSONObject note =
                        new JSONObject();

                note.put(
                        "title",
                        title
                );

                note.put(
                        "content",
                        content
                );

                note.put(
                        "date",
                        currentDate
                );

                notesArray.put(note);

                Toast.makeText(
                        this,
                        "Note saved!",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                JSONObject note =
                        notesArray.getJSONObject(
                                position
                        );

                note.put(
                        "title",
                        title
                );

                note.put(
                        "content",
                        content
                );

                note.put(
                        "date",
                        currentDate
                );

                Toast.makeText(
                        this,
                        "Note updated!",
                        Toast.LENGTH_SHORT
                ).show();
            }

            preferences.edit()
                    .putString(
                            "notes",
                            notesArray.toString()
                    )
                    .apply();

            finish();

        } catch (Exception e) {

            e.printStackTrace();

            Toast.makeText(
                    this,
                    "Something went wrong",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}