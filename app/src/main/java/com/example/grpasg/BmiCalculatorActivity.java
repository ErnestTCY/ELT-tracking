package com.example.grpasg;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


import java.text.SimpleDateFormat;

public class BmiCalculatorActivity extends AppCompatActivity {

    private EditText weightInput;
    private EditText heightInput;
    private TextView bmiResultText,bmiTimestamp;

    private BmiDatabaseHelper databaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmicalculator);

        weightInput = findViewById(R.id.weight_input);
        heightInput = findViewById(R.id.height_input);
        bmiResultText = findViewById(R.id.bmi_result_text);
        bmiTimestamp = findViewById(R.id.bmi_timestamp);

        // Initialize Database Helper
        databaseHelper = new BmiDatabaseHelper(this);
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

        Button calculateButton = findViewById(R.id.calculate_button);

        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Play sound effect
                MediaPlayer mediaPlayer = MediaPlayer.create(BmiCalculatorActivity.this, R.raw.calculate);
                mediaPlayer.start();
                calculateAndSaveBMI();
            }
        });
    }

    private void calculateAndSaveBMI() {
        String weightStr = weightInput.getText().toString();
        String heightStr = heightInput.getText().toString();

        if (!weightStr.isEmpty() && !heightStr.isEmpty()) {
            float weight = Float.parseFloat(weightStr);
            float height = Float.parseFloat(heightStr) / 100; // Convert cm to meters

            float bmi = weight / (height * height);
            String category = getBMICategory(bmi);

            // Save BMI to SQLite
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            databaseHelper.saveBMI(bmi, category, timestamp);


            // Display result to user
            bmiResultText.setText(String.format("Your BMI is: %.2f\nCategory: %s\n", bmi, category));
            bmiTimestamp.setText(String.format("Saved at: %s", timestamp));
        } else {
            bmiResultText.setText("Please enter valid values.");
        }
    }

     private String getBMICategory(float bmi) {
        if (bmi < 18.5) {
            return "Underweight";
        } else if (bmi >= 18.5 && bmi < 24.9) {
            return "Normal weight";
        } else if (bmi >= 25 && bmi < 29.9) {
            return "Overweight";
        } else {
            return "Obese";
        }
    }
}
