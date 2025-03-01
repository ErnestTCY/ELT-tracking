package com.example.grpasg;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherAlertPopup {

    private static boolean isPopupShown = false; // To prevent frequent popups
    private static final String WEATHER_API_KEY = "02ff5b7c3d93873cae757880df67218b"; // Replace with your API key
    private static final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather";

    private static long lastWeatherAlertTime = 0; // Tracks the last time the popup was shown
    private static final long ALERT_DELAY = 15 * 60 * 1000; // 15 minutes in milliseconds

    public static void checkAndShowWeatherAlert(Context context, double latitude, double longitude) {
        long currentTime = System.currentTimeMillis();

        // Check if the delay period has passed
        if (currentTime - lastWeatherAlertTime < ALERT_DELAY) {
            Log.d("WeatherAlertPopup", "Alert delay not met. Skipping weather check.");
            return; // Skip if the last alert was shown less than 15 minutes ago
        }

        // Build the weather API URL with latitude and longitude
        String url = WEATHER_API_URL + "?lat=" + latitude + "&lon=" + longitude + "&appid=" + WEATHER_API_KEY;

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Parse weather data
                        JSONArray weatherArray = response.getJSONArray("weather");
                        if (weatherArray.length() > 0) {
                            JSONObject weatherObject = weatherArray.getJSONObject(0);
                            String weatherDescription = weatherObject.getString("description");

                            Log.d("WeatherAlertPopup", "Weather Description: " + weatherDescription);

                            // Check for hazardous conditions
                            if (isHazardousWeather(weatherDescription)) {
                                lastWeatherAlertTime = currentTime; // Update the last alert time
                                showWeatherAlert(context, weatherDescription, latitude, longitude);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Failed to parse weather data.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("WeatherAlertPopup", "Volley error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e("WeatherAlertPopup", "HTTP Status Code: " + error.networkResponse.statusCode);
                        Log.e("WeatherAlertPopup", "Error Data: " + new String(error.networkResponse.data));
                    }
                    Toast.makeText(context, "Failed to fetch weather data. Check internet or API configuration.", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(jsonObjectRequest);
    }


    private static boolean isHazardousWeather(String description) {
        description = description.toLowerCase().trim();
        return description.contains("thunderstorm") || description.contains("rain") ||
                description.contains("snow") || description.contains("wind") ||
                description.contains("storm") || description.contains("hail");
    }


    private static void showWeatherAlert(Context context, String weatherCondition, double latitude, double longitude) {
        if (isPopupShown) return; // Prevent showing multiple alerts
        isPopupShown = true;

        // Create a Dialog
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        // Inflate the custom popup layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View popupView = inflater.inflate(R.layout.popup_weather_alert, null);

        // Set the layout in the dialog
        dialog.setContentView(popupView);

        // Set weather condition message
        TextView alertMessage = popupView.findViewById(R.id.alert_message);
        alertMessage.setText("Hazardous Weather Detected: " + weatherCondition);

        // Set up Dismiss button
        Button dismissButton = popupView.findViewById(R.id.dismiss_button);
        dismissButton.setOnClickListener(v -> {
            // Play sound effect
            MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.clicked);
            mediaPlayer.start();
            isPopupShown = false;
            dialog.dismiss(); // Close the popup
            
        });

        // Show the popup
        dialog.show();
    }


}
