package com.example.grpasg;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.graphics.Bitmap;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class RunningActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private Button startButton, pauseButton, playButton, stopButton;
    private TextView timeLabel, averageSpeedLabel, distanceLabel, caloriesLabel;

    private boolean isTracking = false;
    private long startTime, pauseTime, totalTimePaused = 0;
    private ArrayList<LatLng> trackPoints = new ArrayList<>();
    private Polyline polyline;
    private Marker userMarker;

    private DatabaseReference databaseReference;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Handler timerHandler;
    private Runnable timerRunnable;
    private double totalDistance = 0; // Distance in kilometers
    private long elapsedTime = 0;    // Time in milliseconds
    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running);

        // Initialize Firebase Auth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        GoogleSignInAccount gAccount = GoogleSignIn.getLastSignedInAccount(this);

        // Check authentication and get userId
        if (user != null) {
            userId = user.getUid();
        } else if (gAccount != null) {
            // Use Google account to authenticate with Firebase
            AuthCredential credential = GoogleAuthProvider.getCredential(gAccount.getIdToken(), null);
            FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            if (firebaseUser != null) {
                                userId = firebaseUser.getUid();
                            }
                        }
                    });
        } else {
            // No authentication, redirect to login
            Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        // Initialize Views
        mapView = findViewById(R.id.map_view);
        startButton = findViewById(R.id.start_button);
        pauseButton = findViewById(R.id.pause_button);
        playButton = findViewById(R.id.play_button);
        stopButton = findViewById(R.id.stop_button);
        timeLabel = findViewById(R.id.time_label);
        averageSpeedLabel = findViewById(R.id.average_speed_label);
        distanceLabel = findViewById(R.id.distance_label);
        caloriesLabel = findViewById(R.id.calories_label);




        // Initialize Views for Navigation
        ImageView navCycling = findViewById(R.id.nav_cycling);
        ImageView navCommunity = findViewById(R.id.nav_community);
        ImageView navLeaderboard = findViewById(R.id.nav_leaderboard);
        ImageView navReminder = findViewById(R.id.nav_reminder);
        ImageView userProfile = findViewById(R.id.UserProfile);
        ImageView optionsMenu = findViewById(R.id.OptionsMenu);
        ImageView navWeather = findViewById(R.id.Weather);

        // Set up navigation
        NavigationCycling.setupNavigation(this, navCycling, navCommunity, navLeaderboard, navReminder, userProfile, navWeather);

        // Set up options menu
        NavigationCycling.setupOptionsMenu(this, optionsMenu);

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance()
                .getReference("history").child(userId).child("running");

        // Initialize FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // MapView setup
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Timer Handler
        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isTracking) {
                    long elapsedTime = System.currentTimeMillis() - startTime - totalTimePaused;
                    String formattedTime = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                            (elapsedTime / 3600000), // Hours
                            (elapsedTime / 60000) % 60, // Minutes
                            (elapsedTime / 1000) % 60); // Seconds

                    // Update UI with elapsed time
                    timeLabel.setText("Time: " + formattedTime);

                    // Update average speed
                    double averageSpeed = ((totalDistance/100) / (elapsedTime / 3600000.0)); // km/h
                    averageSpeedLabel.setText(String.format(Locale.getDefault(), "Average Speed: %.2f km/h", averageSpeed));

                    // Update calories burned
                    double caloriesBurned = totalDistance * 20; // Approximation
                    caloriesLabel.setText(String.format(Locale.getDefault(), "Calories: %.0f", caloriesBurned));

                    // Update distance
                    distanceLabel.setText(String.format(Locale.getDefault(), "KM: %.2f", totalDistance));

                    timerHandler.postDelayed(this, 1000);
                }
            }
        };


        // Start Button Listener
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Play sound effect
                MediaPlayer mediaPlayer = MediaPlayer.create(RunningActivity.this, R.raw.start);
                mediaPlayer.start();
                try {
                    Thread.sleep(4000);
                    startTracking();
                }catch(Exception e){
                    startTracking();
                }
            }
        });

        // Pause Button Listener
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Play sound effect
                MediaPlayer mediaPlayer = MediaPlayer.create(RunningActivity.this, R.raw.clicked);
                mediaPlayer.start();
                pauseTracking();
            }
        });


        // Play Button Listener
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Play sound effect
                MediaPlayer mediaPlayer = MediaPlayer.create(RunningActivity.this, R.raw.start);
                mediaPlayer.start();
                try {
                    Thread.sleep(4000);
                    resumeTracking();
                }catch(Exception e){
                    resumeTracking();
                }
            }
        });


        // Stop Button Listener
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Play sound effect
                MediaPlayer mediaPlayer = MediaPlayer.create(RunningActivity.this, R.raw.stop);
                mediaPlayer.start();
                stopTracking();
            }
        });


    }

    private void startTracking() {
        isTracking = true;
        startTime = System.currentTimeMillis();
        totalTimePaused = 0;
        trackPoints.clear();

        startButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.VISIBLE);
        stopButton.setVisibility(View.VISIBLE);

        timerHandler.post(timerRunnable);
        // Start receiving location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(
                    LocationRequest.create().setInterval(2500).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY),
                    locationCallback,
                    null
            );
        }
        startLocationUpdates();
    }

    private void pauseTracking() {
        isTracking = false;
        pauseTime = System.currentTimeMillis();

        pauseButton.setVisibility(View.GONE);
        playButton.setVisibility(View.VISIBLE);
    }

    private void resumeTracking() {
        isTracking = true;
        totalTimePaused += System.currentTimeMillis() - pauseTime;

        playButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.VISIBLE);

        timerHandler.post(timerRunnable);
    }

    private void stopTracking() {
        isTracking = false;
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime - totalTimePaused;

        // Stop receiving location updates
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

        // Save to Firebase
        saveToFirebase(elapsedTime, calculateDistance());

        // Reset UI
        resetUI();
        stopLocationUpdates();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    private void saveToFirebase(long elapsedTime, double distance) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String timestamp = dateFormat.format(new Date());

        // Create a HistoryEntry object
        HistoryEntry entry = new HistoryEntry();
        entry.setActivityType("running");
        entry.setCalories((int) calculateCalories()); // Convert calories to integer if needed
        entry.setDistance(distance);
        entry.setElapsedTime(formatElapsedTime(elapsedTime));
        entry.setElapsedTimeInMillis(elapsedTime);
        entry.setTimestamp(timestamp);

        // Save to Firebase
        DatabaseReference newEntryRef = databaseReference.push();
        newEntryRef.setValue(entry).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("Firebase", "Running data saved successfully");
            } else {
                Log.e("Firebase", "Failed to save running data: " + task.getException());
            }
        });

        // Update active challenges
        updateChallengeProgress(distance);
    }

    private void updateChallengeProgress(double newDistance) {
        DatabaseReference activeChallengesRef = FirebaseDatabase.getInstance()
            .getReference("challenges")
            .child("active")
            .child(userId);

        activeChallengesRef.orderByChild("activityType")
            .equalTo("jogging")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot challengeSnapshot : snapshot.getChildren()) {
                        String status = challengeSnapshot.child("status").getValue(String.class);
                        if ("active".equals(status)) {
                            // Check if challenge is still valid
                            Long endTime = challengeSnapshot.child("endTime").getValue(Long.class);
                            if (endTime != null && endTime > System.currentTimeMillis()) {
                                // Update progress
                                Double currentProgress = challengeSnapshot.child("progress")
                                    .getValue(Double.class);
                                Double target = challengeSnapshot.child("target")
                                    .getValue(Double.class);
                                
                                if (currentProgress != null && target != null) {
                                    double newProgress = currentProgress + newDistance;
                                    
                                    // Update progress
                                    challengeSnapshot.getRef()
                                        .child("progress")
                                        .setValue(newProgress);
                                    
                                    // Check if challenge completed
                                    if (newProgress >= target) {
                                        challengeSnapshot.getRef()
                                            .child("status")
                                            .setValue("completed");
                                        
                                        // Notify user
                                        showChallengeCompletedNotification(
                                            challengeSnapshot.child("title")
                                                .getValue(String.class));
                                    }
                                }
                            } else {
                                // Challenge expired
                                challengeSnapshot.getRef()
                                    .child("status")
                                    .setValue("expired");
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("RunningActivity", "Failed to update challenges: " + error.getMessage());
                }
            });
    }

    private void showChallengeCompletedNotification(String challengeTitle) {
        Toast.makeText(this, 
            "Challenge Completed: " + challengeTitle, 
            Toast.LENGTH_LONG).show();
        // You can implement more sophisticated notification here
    }

    private String formatElapsedTime(long elapsedTime) {
        return String.format(Locale.getDefault(), "%02d:%02d:%02d",
                (elapsedTime / 3600000), (elapsedTime / 60000) % 60, (elapsedTime / 1000) % 60);
    }




    private void resetUI() {
        trackPoints.clear();
        if (polyline != null) {
            polyline.remove();
        }

        timeLabel.setText("Time: --:--");
        averageSpeedLabel.setText("Average Speed: -- km/h");
        distanceLabel.setText("KM: --");
        caloriesLabel.setText("Calories: --");

        startButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.GONE);
        playButton.setVisibility(View.GONE);
        stopButton.setVisibility(View.GONE);
    }

    private double calculateDistance() {
        double totalDistance = 0;
        for (int i = 1; i < trackPoints.size(); i++) {
            totalDistance += distanceBetween(trackPoints.get(i - 1), trackPoints.get(i));
        }
        distanceLabel.setText(String.format(Locale.getDefault(), "KM: %.2f", totalDistance));
        return totalDistance;
    }

    private double calculateCalories() {
        double caloriesBurned = calculateDistance() * 50; // Approximation
        caloriesLabel.setText(String.format(Locale.getDefault(), "Calories: %.0f", caloriesBurned));
        return caloriesBurned;
    }

    private double distanceBetween(LatLng start, LatLng end) {
        double earthRadius = 6371000; // meters
        double dLat = Math.toRadians(end.latitude - start.latitude);
        double dLng = Math.toRadians(end.longitude - start.longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(start.latitude)) * Math.cos(Math.toRadians(end.latitude))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c / 1000; // Convert to kilometers
    }

    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        googleMap.setMyLocationEnabled(true);

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));

                if (userMarker == null) {
                    userMarker = googleMap.addMarker(new MarkerOptions()
                            .position(currentLatLng)
                            .title("You are here")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                }
            }
        });
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000); // 5 seconds
        locationRequest.setFastestInterval(2000); // 2 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (isTracking && locationResult != null) {
                for (Location location : locationResult.getLocations()) {
                    LatLng newPoint = new LatLng(location.getLatitude(), location.getLongitude());

                    // Calculate distance between consecutive points
                    if (!trackPoints.isEmpty()) {
                        totalDistance += distanceBetween(trackPoints.get(trackPoints.size() - 1), newPoint);

                        // Update the distance instantly
                        distanceLabel.setText(String.format(Locale.getDefault(), "KM: %.2f", totalDistance));
                    }

                    // Add point to the track
                    trackPoints.add(newPoint);

                    // Update polyline
                    if (polyline != null) {
                        polyline.setPoints(trackPoints);
                    }
                    // Call Weather Alert Popup
                    WeatherAlertPopup.checkAndShowWeatherAlert(RunningActivity.this, location.getLatitude(), location.getLongitude());
                }
            }
        }
    };


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        enableMyLocation();

        polyline = googleMap.addPolyline(new PolylineOptions().color(0xFF0000FF).width(5)); // Blue line
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
