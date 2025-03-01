package com.example.grpasg;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChallengesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChallengesAdapter adapter;
    private TabLayout tabLayout;
    private List<Challenge> currentChallenges;
    private DatabaseReference databaseReference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);

        // Get current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference();
        }

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.challengesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Initialize challenges list
        currentChallenges = new ArrayList<>();
        adapter = new ChallengesAdapter(currentChallenges);
        recyclerView.setAdapter(adapter);
        
        // Initialize TabLayout
        tabLayout = findViewById(R.id.tabLayout);
        TabLayout.Tab joggingTab = tabLayout.newTab().setIcon(R.drawable.jogging);
        TabLayout.Tab cyclingTab = tabLayout.newTab().setIcon(R.drawable.cycling1);
        tabLayout.addTab(joggingTab);
        tabLayout.addTab(cyclingTab);

        // Handle tab selection
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String activityType = tab.getPosition() == 0 ? "running" : "cycling";
                loadChallenges(activityType);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Initialize Navigation
        ImageView navCycling = findViewById(R.id.nav_cycling);
        ImageView navCommunity = findViewById(R.id.nav_community);
        ImageView navLeaderboard = findViewById(R.id.nav_leaderboard);
        ImageView navReminder = findViewById(R.id.nav_reminder);
        ImageView userProfile = findViewById(R.id.UserProfile);
        ImageView optionsMenu = findViewById(R.id.OptionsMenu);
        ImageView navWeather = findViewById(R.id.Weather);

        // Set up navigation
        Navigation.setupNavigation(this, navCycling, navCommunity, navLeaderboard, navReminder, userProfile, navWeather);
        Navigation.setupOptionsMenu(this, optionsMenu);

        // Load initial challenges (running)
        loadChallenges("running");
    }

    private void loadChallenges(String activityType) {
        DatabaseReference templatesRef = databaseReference.child("challenges").child("templates");
        
        // Convert "running" to "jogging" to match Firebase structure
        final String fbActivityType = activityType.equals("running") ? "jogging" : activityType;
        
        templatesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentChallenges.clear();
                
                // Load daily challenges
                DataSnapshot dailySnapshot = snapshot.child("daily").child(fbActivityType);
                if (dailySnapshot.exists()) {
                    Challenge dailyChallenge = createChallengeFromSnapshot(dailySnapshot);
                    if (dailyChallenge != null) {
                        currentChallenges.add(dailyChallenge);
                    }
                }

                // Load three-day challenges
                DataSnapshot threeDaySnapshot = snapshot.child("three_day").child(fbActivityType);
                if (threeDaySnapshot.exists()) {
                    Challenge threeDayChallenge = createChallengeFromSnapshot(threeDaySnapshot);
                    if (threeDayChallenge != null) {
                        currentChallenges.add(threeDayChallenge);
                    }
                }

                // Load weekly challenges
                DataSnapshot weeklySnapshot = snapshot.child("weekly").child(fbActivityType);
                if (weeklySnapshot.exists()) {
                    Challenge weeklyChallenge = createChallengeFromSnapshot(weeklySnapshot);
                    if (weeklyChallenge != null) {
                        currentChallenges.add(weeklyChallenge);
                    }
                }

                // Update the RecyclerView
                adapter.notifyDataSetChanged();
                
                // Debug log
                Log.d("ChallengesActivity", "Loaded " + currentChallenges.size() + 
                    " challenges for " + fbActivityType);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChallengesActivity.this, 
                    "Failed to load challenges: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Challenge createChallengeFromSnapshot(DataSnapshot snapshot) {
        try {
            String title = snapshot.child("title").getValue(String.class);
            String description = snapshot.child("description").getValue(String.class);
            int target = snapshot.child("target").getValue(Integer.class);
            String period = snapshot.child("period").getValue(String.class);
            int validityHours = snapshot.child("validityHours").getValue(Integer.class);

            // Set icon based on activity type
            String path = snapshot.getRef().getPath().toString();
            int iconResId = path.contains("jogging") ? 
                R.drawable.jogging : R.drawable.cycling1;

            return new Challenge(title, description, iconResId, 0, target, period);
        } catch (Exception e) {
            Log.e("ChallengesActivity", "Error creating challenge: " + e.getMessage());
            return null;
        }
    }

    public void activateChallenge(Challenge challenge, String period) {
        DatabaseReference activeRef = databaseReference
            .child("challenges")
            .child("active")
            .child(userId);

        String challengeId = activeRef.push().getKey();
        
        Map<String, Object> challengeData = new HashMap<>();
        challengeData.put("title", challenge.getTitle());
        challengeData.put("description", challenge.getDescription());
        challengeData.put("target", challenge.getGoal());
        challengeData.put("progress", 0);
        challengeData.put("startTime", ServerValue.TIMESTAMP);
        challengeData.put("status", "active");
        challengeData.put("activityType", tabLayout.getSelectedTabPosition() == 0 ? "jogging" : "cycling");
        challengeData.put("period", period);
        
        // Set end time using a separate update after getting the start time
        activeRef.child(challengeId).setValue(challengeData)
            .addOnSuccessListener(aVoid -> {
                // Get the actual start time and calculate end time
                activeRef.child(challengeId).child("startTime").get()
                    .addOnSuccessListener(dataSnapshot -> {
                        if (dataSnapshot.exists()) {
                            Long startTime = dataSnapshot.getValue(Long.class);
                            if (startTime != null) {
                                long endTime = startTime + (validityHours(period) * 3600000);
                                activeRef.child(challengeId).child("endTime").setValue(endTime);
                            }
                        }
                    });
                Toast.makeText(this, "Challenge activated!", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> Toast.makeText(this,
                "Failed to activate challenge", Toast.LENGTH_SHORT).show());
    }

    private long validityHours(String period) {
        switch (period) {
            case "daily": return 24;
            case "three_day": return 72;
            case "weekly": return 168;
            default: return 24;
        }
    }
} 