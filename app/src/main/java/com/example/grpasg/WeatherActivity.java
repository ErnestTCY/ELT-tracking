package com.example.grpasg;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;



import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class WeatherActivity extends AppCompatActivity {
    TextView city,temp,main,humidity,wind,realFeel,time;
    ImageView weatherImage;
    private FusedLocationProviderClient client;
    static int indexfor=5;
    static String lat;
    static String lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        city = findViewById(R.id.id_city);
        temp = findViewById(R.id.id_degree);
        main = findViewById(R.id.id_main);
        humidity = findViewById(R.id.id_humidity);
        wind = findViewById(R.id.id_wind);
        realFeel = findViewById(R.id.id_realfeel);
        weatherImage = findViewById(R.id.id_weatherImage);
        client = LocationServices.getFusedLocationProviderClient(this);
        time=findViewById(R.id.id_time);

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

        if (ActivityCompat.checkSelfPermission(WeatherActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(WeatherActivity.this, ACCESS_FINE_LOCATION)){

                ActivityCompat.requestPermissions(WeatherActivity.this,
                        new String[]{ACCESS_FINE_LOCATION}, 1);
            }else{
                ActivityCompat.requestPermissions(WeatherActivity.this,
                        new String[]{ACCESS_FINE_LOCATION}, 1);
            }
        }
        client.getLastLocation().addOnSuccessListener(WeatherActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location!=null){
                    double latitude=Math.round(location.getLatitude() * 100.0)/100.0;
                    lat= String.valueOf(latitude);

                    double longitude=Math.round(location.getLongitude() * 100.0)/100.0;
                    lon= String.valueOf(longitude);

                    WeatherByLatLon(lat,lon);
                }else{
                    WeatherByCityName("Malaysia");
                }

            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(WeatherActivity.this,
                            ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                        client.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {

                                    double latitude = Math.round(location.getLatitude() * 100.0) / 100.0;
                                    lat = String.valueOf(latitude);

                                    double longitude = Math.round(location.getLongitude() * 100.0) / 100.0;
                                    lon = String.valueOf(longitude);

                                    WeatherByLatLon(lat, lon);
                                }
                            }
                        });
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private static final String API_KEY = "02ff5b7c3d93873cae757880df67218b";
    private void WeatherByCityName(String city){
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder()
                .url("https://api.openweathermap.org/data/2.5/forecast?q="+city+"&appid="+API_KEY+"&units=metric")
                .get().build();
        StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            Response response=client.newCall(request).execute();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {

                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String data=response.body().string();
                    try {

                        JSONObject json=new JSONObject(data);
                        JSONObject city=json.getJSONObject("city");
                        JSONObject coord=city.getJSONObject("coord");
                        String lat =coord.getString("lat");
                        String lon=coord.getString("lon");

                        WeatherByLatLon(lat,lon);

                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            });
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    private void WeatherByLatLon(String lat,String lon){
        OkHttpClient client=new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.openweathermap.org/data/2.5/forecast?lat=" + lat + "&lon=" + lon + "&appid=" + API_KEY + "&units=metric")
                .get().build();
        StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            Response response=client.newCall(request).execute();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {

                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String data=response.body().string();
                    try {

                        JSONObject json=new JSONObject(data);
                        TextView[] forecast = new TextView[5];
                        TextView[] forecastTemp=new TextView[5];
                        ImageView[] forecastIcons=new ImageView[5];
                        IdAssign(forecast,forecastTemp,forecastIcons);

                        indexfor=5;
                        for (int i=0;i<forecast.length;i++){
                            forecastCal(forecast[i],forecastTemp[i],forecastIcons[i],indexfor,json);
                        }

                        JSONArray list=json.getJSONArray("list");
                        JSONObject objects = list.getJSONObject(0);
                        JSONArray array=objects.getJSONArray("weather");
                        JSONObject object=array.getJSONObject(0);

                        String description=object.getString("description");
                        String icons=object.getString("icon");

                        Date currentDate=new Date();
                        String dateString=currentDate.toString();
                        String[] dateSplit=dateString.split(" ");
                        String date=dateSplit[0]+", "+dateSplit[1] +" "+dateSplit[2];

                        JSONObject Main=objects.getJSONObject("main");
                        double temparature=Main.getDouble("temp");
                        String Temp=Math.round(temparature)+"°C";
                        double Humidity=Main.getDouble("humidity");
                        String hum=Math.round(Humidity)+"%";
                        double FeelsLike=Main.getDouble("feels_like");
                        String feelsValue=Math.round(FeelsLike)+"°";

                        JSONObject Wind=objects.getJSONObject("wind");
                        String windValue=Wind.getString("speed")+" "+"km/h";

                        JSONObject CityObject=json.getJSONObject("city");
                        String City=CityObject.getString("name");

                        setDataText(city,City);
                        setDataText(temp,Temp);
                        setDataText(main,description);
                        setDataImage(weatherImage,icons);
                        setDataText(time,date);
                        setDataText(humidity,hum);
                        setDataText(realFeel,feelsValue);
                        setDataText(wind,windValue);

                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            });
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void setDataText(final TextView text, final String value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }
    private void setDataImage(final ImageView ImageView, final String value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (value){
                    case "01d": ImageView.setImageDrawable(getResources().getDrawable(R.drawable.w01d)); break;
                    case "01n": ImageView.setImageDrawable(getResources().getDrawable(R.drawable.w01d)); break;
                    case "02d": ImageView.setImageDrawable(getResources().getDrawable(R.drawable.w02d)); break;
                    case "02n": ImageView.setImageDrawable(getResources().getDrawable(R.drawable.w02d)); break;
                    case "03d": ImageView.setImageDrawable(getResources().getDrawable(R.drawable.w03d)); break;
                    case "03n": ImageView.setImageDrawable(getResources().getDrawable(R.drawable.w03d)); break;
                    case "04d": ImageView.setImageDrawable(getResources().getDrawable(R.drawable.w04d)); break;
                    case "04n": ImageView.setImageDrawable(getResources().getDrawable(R.drawable.w04d)); break;
                    case "09d": ImageView.setImageDrawable(getResources().getDrawable(R.drawable.w09d)); break;
                    case "09n": ImageView.setImageDrawable(getResources().getDrawable(R.drawable.w09d)); break;
                    case "10d": ImageView.setImageDrawable(getResources().getDrawable(R.drawable.w10d)); break;
                    case "10n": ImageView.setImageDrawable(getResources().getDrawable(R.drawable.w10d)); break;
                    case "11d": ImageView.setImageDrawable(getResources().getDrawable(R.drawable.w11d)); break;
                    case "11n": ImageView.setImageDrawable(getResources().getDrawable(R.drawable.w11d)); break;
                    case "13d": ImageView.setImageDrawable(getResources().getDrawable(R.drawable.w13d)); break;
                    case "13n": ImageView.setImageDrawable(getResources().getDrawable(R.drawable.w13d)); break;
                    default:ImageView.setImageDrawable(getResources().getDrawable(R.drawable.w03d));

                }
            }
        });
    }


    private void forecastCal(TextView forecast, TextView forecastTemp, ImageView forecastIcons, int index, JSONObject json) throws JSONException {
        JSONArray list = json.getJSONArray("list");
        int forecastsDisplayed = 0; // To track how many forecasts have been processed
        Calendar calendar = Calendar.getInstance();
        long currentTimeMillis = calendar.getTimeInMillis(); // Current time in milliseconds

        // Define the next forecast times (3-hour intervals: 3, 6, 9, 12, 15 hours from now)
        long[] targetTimes = new long[5];
        for (int i = 0; i < 5; i++) {
            targetTimes[i] = currentTimeMillis + i * 3 * 60 * 60 * 1000; // Add 3-hour increments
        }

        for (int j = index; j < list.length(); j++) {
            JSONObject object = list.getJSONObject(j);

            // Parse the forecast time
            String dt = object.getString("dt_txt"); // Example: "2024-12-09 03:00:00"
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            try {
                Date forecastDate = sdf.parse(dt);
                if (forecastDate == null) continue;

                long forecastTimeMillis = forecastDate.getTime();

                // Find the next closest forecast to the target times
                if (forecastsDisplayed < 5 && forecastTimeMillis >= targetTimes[forecastsDisplayed]) {
                    // Extract forecast hour for display
                    String[] dateSplit = dt.split(" "); // Split into [ "2024-12-09", "03:00:00"]
                    String time = dateSplit[1].substring(0, 5); // Extract "03:00"

                    // Set the forecast time
                    setDataText(forecast, time);

                    // Extract temperature
                    JSONObject main = object.getJSONObject("main");
                    double temperature = main.getDouble("temp");
                    String temp = Math.round(temperature) + "°C";
                    setDataText(forecastTemp, temp);

                    // Extract weather icon
                    JSONArray weatherArray = object.getJSONArray("weather");
                    JSONObject weather = weatherArray.getJSONObject(0);
                    String icon = weather.getString("icon");
                    setDataImage(forecastIcons, icon);

                    forecastsDisplayed++; // Increment the counter for displayed forecasts
                    indexfor = j + 1; // Update the indexfor for the next call
                    return; // Exit after processing one forecast
                }
            } catch (ParseException e) {
                // Handle the ParseException (e.g., log it or notify the user)
                Log.e("forecastCal", "Error parsing forecast date: " + dt, e);
            }
        }
    }





    private void IdAssign(TextView[] forecast,TextView[] forecastTemp,ImageView[] forecastIcons){
        forecast[0]=findViewById(R.id.id_forecastDay1);
        forecast[1]=findViewById(R.id.id_forecastDay2);
        forecast[2]=findViewById(R.id.id_forecastDay3);
        forecast[3]=findViewById(R.id.id_forecastDay4);
        forecast[4]=findViewById(R.id.id_forecastDay5);
        forecastTemp[0]=findViewById(R.id.id_forecastTemp1);
        forecastTemp[1]=findViewById(R.id.id_forecastTemp2);
        forecastTemp[2]=findViewById(R.id.id_forecastTemp3);
        forecastTemp[3]=findViewById(R.id.id_forecastTemp4);
        forecastTemp[4]=findViewById(R.id.id_forecastTemp5);
        forecastIcons[0]=findViewById(R.id.id_forecastIcon1);
        forecastIcons[1]=findViewById(R.id.id_forecastIcon2);
        forecastIcons[2]=findViewById(R.id.id_forecastIcon3);
        forecastIcons[3]=findViewById(R.id.id_forecastIcon4);
        forecastIcons[4]=findViewById(R.id.id_forecastIcon5);

    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                String citySearched=data.getStringExtra("result");
                WeatherByCityName(citySearched);
            }
            if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }
}