package com.example.grpasg;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private List<HistoryEntry> historyEntries = new ArrayList<>();
    private boolean isRunningMode = true;

    FirebaseDatabase database = FirebaseDatabase.getInstance("https://elt-tracking-app-default-rtdb.asia-southeast1.firebasedatabase.app/");
    DatabaseReference databaseReference = database.getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize UserManager
        UserManager userManager = UserManager.getInstance();

        // Retrieve user info
        FirebaseUser firebaseUser = userManager.getFirebaseUser();
        GoogleSignInAccount googleSignInAccount = userManager.getGoogleSignInAccount();

        // Redirect to login if no user is logged in
        if (firebaseUser == null && googleSignInAccount == null) {
            redirectToLogin();
            return;
        }

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

        recyclerView = findViewById(R.id.history_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(historyEntries);
        recyclerView.setAdapter(adapter);
        // Firebase reference
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("history").child(userId);

        // Initialize Views
        LinearLayout runningContainer = findViewById(R.id.running_container);
        LinearLayout cyclingContainer = findViewById(R.id.cycling_container);
        View underlineRun = findViewById(R.id.underline_run);
        View underlineCycle = findViewById(R.id.underline_cycle);


        // Set up click listeners for toggle buttons
        runningContainer.setOnClickListener(v -> {
            // Play sound effect
            MediaPlayer mediaPlayer = MediaPlayer.create(HistoryActivity.this, R.raw.clicked);
            mediaPlayer.start();
            // Show underline for running and hide for cycling
            underlineRun.setVisibility(View.VISIBLE);
            underlineCycle.setVisibility(View.INVISIBLE);

            // Load running history
            loadHistoryData("running");
        });

        cyclingContainer.setOnClickListener(v -> {
            // Play sound effect
            MediaPlayer mediaPlayer = MediaPlayer.create(HistoryActivity.this, R.raw.clicked);
            mediaPlayer.start();
            // Show underline for cycling and hide for running
            underlineCycle.setVisibility(View.VISIBLE);
            underlineRun.setVisibility(View.INVISIBLE);

            // Load cycling history
            loadHistoryData("cycling");
        });

        // Set default state
        underlineRun.setVisibility(View.VISIBLE);
        underlineCycle.setVisibility(View.INVISIBLE);
        // Load default data
        loadHistoryData("running");
    }



    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    private void loadHistoryData(String activityType) {
        databaseReference.child(activityType)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        historyEntries.clear();
                        for (DataSnapshot entrySnapshot : snapshot.getChildren()) {
                            try {
                                HistoryEntry entry = entrySnapshot.getValue(HistoryEntry.class);
                                if (entry != null) {
                                    historyEntries.add(entry);
                                }
                            } catch (DatabaseException e) {
                                Log.e("HistoryActivity", "Error parsing entry: ", e);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HistoryActivity.this, "Failed to load history", Toast.LENGTH_SHORT).show();
                    }
                });
    }





}
