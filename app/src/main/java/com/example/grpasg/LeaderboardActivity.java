package com.example.grpasg;

import androidx.annotation.NonNull;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.grpasg.R;

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LeaderboardAdapter adapter;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        recyclerView = findViewById(R.id.leaderboardRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        tabLayout = findViewById(R.id.tabLayout);
        
        // Setup tabs with icons
        TabLayout.Tab joggingTab = tabLayout.newTab().setIcon(R.drawable.jogging);
        TabLayout.Tab cyclingTab = tabLayout.newTab().setIcon(R.drawable.cycling1);
        
        tabLayout.addTab(joggingTab);
        tabLayout.addTab(cyclingTab);
        
        // Initialize empty adapter
        adapter = new LeaderboardAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setupRealtimeLeaderboard();  // Reload data when tab changes
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Initial data load
        setupRealtimeLeaderboard();

        // Initialize Views for Navigation
        ImageView navCycling = findViewById(R.id.nav_cycling);
        ImageView navCommunity = findViewById(R.id.nav_community);
        ImageView navLeaderboard = findViewById(R.id.nav_leaderboard);
        ImageView navReminder = findViewById(R.id.nav_reminder);
        ImageView userProfile = findViewById(R.id.UserProfile);
        ImageView optionsMenu = findViewById(R.id.OptionsMenu);
        ImageView navWeather = findViewById(R.id.Weather);

        // Set up navigation
        Navigation.setupNavigation(this, navCycling, navCommunity, navLeaderboard, navReminder, userProfile, navWeather);

        // Set up options menu
        Navigation.setupOptionsMenu(this, optionsMenu);
    }

    private void setupRealtimeLeaderboard() {
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference("history");
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        
        historyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Double> userTotals = new HashMap<>();
                
                // Get current activity type based on selected tab
                String activityType = tabLayout.getSelectedTabPosition() == 0 ? "running" : "cycling";
                
                // Calculate totals for each user
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    double totalDistance = calculateUserDistance(userSnapshot, activityType);
                    if (totalDistance > 0) {  // Only add users who have activities
                        userTotals.put(userId, totalDistance);
                    }
                }
                
                // Get usernames and create leaderboard entries
                fetchUsernames(userTotals);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LeaderboardActivity.this, 
                    "Failed to load leaderboard", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUsernames(Map<String, Double> userTotals) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<LeaderboardEntry> entries = new ArrayList<>();
                
                for (Map.Entry<String, Double> total : userTotals.entrySet()) {
                    String userId = total.getKey();
                    Double distance = total.getValue();
                    String displayName = null;
                    
                    // Debug log
                    Log.d("LeaderboardDebug", "Processing user ID: " + userId);
                    
                    // Try getting current user's display name
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null && currentUser.getUid().equals(userId)) {
                        displayName = currentUser.getDisplayName();
                        Log.d("LeaderboardDebug", "Current user display name: " + displayName);
                    }
                    
                    // Try getting from users node
                    if (displayName == null || displayName.isEmpty()) {
                        DataSnapshot userSnapshot = snapshot.child(userId);
                        Log.d("LeaderboardDebug", "User exists in database: " + userSnapshot.exists());
                        if (userSnapshot.exists()) {
                            Log.d("LeaderboardDebug", "User data: " + userSnapshot.getValue());
                        }
                        
                        if (userSnapshot.exists() && userSnapshot.child("name").exists()) {
                            displayName = userSnapshot.child("name").getValue(String.class);
                            Log.d("LeaderboardDebug", "Found name in database: " + displayName);
                        }
                    }
                    
                    // Fallback to Player prefix
                    if (displayName == null || displayName.isEmpty()) {
                        displayName = "Player " + userId.substring(0, 4);
                        Log.d("LeaderboardDebug", "Using fallback name: " + displayName);
                    }
                    
                    entries.add(new LeaderboardEntry(displayName, distance, 0));
                }
                
                // Sort entries by distance
                Collections.sort(entries, (a, b) -> 
                    Double.compare(b.getDistance(), a.getDistance()));
                
                // Assign ranks
                for (int i = 0; i < entries.size(); i++) {
                    entries.get(i).setRank(i + 1);
                }
                
                // Update adapter
                adapter.updateEntries(entries);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LeaderboardActivity.this, 
                    "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private double calculateUserDistance(DataSnapshot userSnapshot, String activityType) {
        double totalDistance = 0;
        
        // Get activities of specified type
        DataSnapshot activitySnapshot = userSnapshot.child(activityType);
        
        // Sum up all distances
        for (DataSnapshot entrySnapshot : activitySnapshot.getChildren()) {
            HistoryEntry entry = entrySnapshot.getValue(HistoryEntry.class);
            if (entry != null) {
                totalDistance += entry.getDistance();
            }
        }
        
        return totalDistance;
    }
} 