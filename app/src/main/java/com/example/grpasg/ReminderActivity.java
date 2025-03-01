package com.example.grpasg;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ReminderActivity extends AppCompatActivity {

    private TimePicker timePickerTop;

    private EditText descriptionInput; // Declare the input field

    private RecyclerView reminderRecyclerView;

    private List<String> reminders = new ArrayList<>();
    private ReminderAdapter adapter; // Custom adapter for RecyclerView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        timePickerTop = findViewById(R.id.time_picker_top);
        descriptionInput = findViewById(R.id.description_text);
        reminderRecyclerView = findViewById(R.id.reminder_recycler_view);

        // Initialize Views
        ImageView navRunning = findViewById(R.id.nav_running);
        ImageView navCommunity = findViewById(R.id.nav_community);
        ImageView navLeaderboard = findViewById(R.id.nav_leaderboard);
        ImageView navReminder = findViewById(R.id.nav_reminder);
        ImageView userProfile = findViewById(R.id.UserProfile);
        ImageView optionsMenu = findViewById(R.id.OptionsMenu);
        ImageView  navWeather =findViewById(R.id.Weather);

        // Set up navigation
        Navigation.setupNavigation(this, navRunning, navCommunity, navLeaderboard, navReminder, userProfile,navWeather);

        // Set up options menu
        Navigation.setupOptionsMenu(this, optionsMenu);
        // Set up RecyclerView
        adapter = new ReminderAdapter(reminders);
        reminderRecyclerView.setAdapter(adapter);
        reminderRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up icon click listeners
        ImageView iconCheckTyre = findViewById(R.id.icon_check_tyre);
        ImageView iconCheckBrake = findViewById(R.id.icon_check_brake);
        ImageView iconChangeAccessories = findViewById(R.id.icon_change_accessories);

        iconCheckTyre.setOnClickListener(v -> {
            // Play sound effect
            MediaPlayer mediaPlayer = MediaPlayer.create(ReminderActivity.this, R.raw.clicked);
            mediaPlayer.start();
            updateDescription("CHECK TYRE, ");
        });
        iconCheckBrake.setOnClickListener(v -> {
            // Play sound effect
            MediaPlayer mediaPlayer = MediaPlayer.create(ReminderActivity.this, R.raw.clicked);
            mediaPlayer.start();
            updateDescription("CHECK BRAKE, ");
        });
        iconChangeAccessories.setOnClickListener(v -> {
            // Play sound effect
            MediaPlayer mediaPlayer = MediaPlayer.create(ReminderActivity.this, R.raw.clicked);
            mediaPlayer.start();
            updateDescription("CHANGE ACCESSORIES, ");
        });

        // Set up button click listeners
        Button setReminderButton = findViewById(R.id.set_reminder_button);
        Button deleteButton = findViewById(R.id.delete_button);

        setReminderButton.setOnClickListener(v -> {
            // Play sound effect
            MediaPlayer mediaPlayer = MediaPlayer.create(ReminderActivity.this, R.raw.reminder);
            mediaPlayer.start();
           setReminder();
        });

        deleteButton.setOnClickListener(v -> {
            // Play sound effect
            MediaPlayer mediaPlayer = MediaPlayer.create(ReminderActivity.this, R.raw.clicked);
            mediaPlayer.start();
            deleteReminder();
        });
    }

    private void updateDescription(String text) {
        String existingText = descriptionInput.getText().toString(); // Get existing text
        if (!existingText.isEmpty()) {
            // Append the new text with a separator
            descriptionInput.setText(existingText + "\n" + text);
        } else {
            // If no text exists, just set the new text
            descriptionInput.setText(text);
        }
    }

    public void removeTriggeredReminder(String description) {
        reminders.removeIf(reminder -> reminder.contains(description));
        adapter.notifyDataSetChanged();
    }



    private void scheduleNotification(int hour, int minute, String description, int requestCode) {



        Intent intent = new Intent(this, ReminderBroadcastReceiver.class);
        intent.putExtra("description", description);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            long triggerTime = calendar.getTimeInMillis();
            if (System.currentTimeMillis() > triggerTime) {
                // If the time is in the past, schedule it for the next day
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                triggerTime = calendar.getTimeInMillis();
            }

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
            );

            Toast.makeText(this, "Reminder scheduled for " + hour + ":" + String.format("%02d", minute), Toast.LENGTH_SHORT).show();
        }
    }

    private void setReminder() {
        int hour = timePickerTop.getHour();
        int minute = timePickerTop.getMinute();

        String description = descriptionInput.getText().toString().trim();

        if (description.isEmpty()) {
            description = "No description provided";
        }

        String reminderTime = String.format(Locale.getDefault(), "Reminder set for %02d:%02d - %s", hour, minute, description);

        // Add reminder to the list
        reminders.add(reminderTime);
        adapter.notifyDataSetChanged();

        // Schedule notification
        int requestCode = reminders.size(); // Unique request code for each reminder
        scheduleNotification(hour, minute, description, requestCode);

        // Show confirmation dialog
        showAlertDialog(reminderTime);

        // Clear input field
        descriptionInput.setText("");
    }


    private void deleteReminder() {
        if (!reminders.isEmpty()) {
            reminders.remove(reminders.size() - 1); // Remove last reminder as an example
            adapter.notifyDataSetChanged(); // Update RecyclerView
            Toast.makeText(this, "Last Reminder Deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No reminders to delete", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAlertDialog(String message) {
        if (!isFinishing()) {
            new AlertDialog.Builder(this)
                    .setTitle("Reminder Set")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }

    }
}

