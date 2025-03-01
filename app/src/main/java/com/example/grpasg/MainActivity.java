package com.example.grpasg; // Replace with your package name

import static android.app.ProgressDialog.show;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.grpasg.AddDummyData;


public class MainActivity extends AppCompatActivity {

    private TextView userName, userEmail, userBmi;
    private ImageView userProfilePic, gifCycling, gifRunning, gifCommunity, gifLeaderboard;
    private ImageView navRunning, navCommunity, navLeaderboard, navReminder;
    private ImageView optionsMenu;
    private FirebaseAuth auth;

    GoogleSignInClient gClient;
    GoogleSignInOptions gOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // Call the dummy data method (for testing purposes)
        //AddDummyData.addDummyData();
        // Initialize Views
        userProfilePic = findViewById(R.id.profile_pic);
        userName = findViewById(R.id.user_name);
        userEmail = findViewById(R.id.user_email);
        userBmi = findViewById(R.id.user_bmi);
        gifCycling = findViewById(R.id.gif_cycling);
        gifRunning = findViewById(R.id.gif_running);
        gifCommunity = findViewById(R.id.gif_community);
        gifLeaderboard = findViewById(R.id.gif_Leaderboard);

        Button logout = findViewById(R.id.btn_logout);
        Button btnBmiCalculator = findViewById(R.id.btn_bmi_calculator);

        // Load GIFs using Glide
        Glide.with(this).asGif().load(R.drawable.cycling).into(gifCycling);
        Glide.with(this).asGif().load(R.drawable.running).into(gifRunning);
        Glide.with(this).asGif().load(R.drawable.community).into(gifCommunity);
        Glide.with(this).asGif().load(R.drawable.leaderboard).into(gifLeaderboard);

        gOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gClient = GoogleSignIn.getClient(this, gOptions);

        // Initialize Firebase Auth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        GoogleSignInAccount gAccount = GoogleSignIn.getLastSignedInAccount(this);


        // Pass user information to UserManager
        UserManager userManager = UserManager.getInstance();

        if (user != null) {
            userManager.setFirebaseUser(user);
            FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        if (gAccount != null) {
            userManager.setGoogleSignInAccount(gAccount);
            // Display user info
            String userNames = gAccount.getDisplayName();
            String userEmails = gAccount.getEmail();
            String photoUrl = gAccount.getPhotoUrl() != null ? gAccount.getPhotoUrl().toString() : null;

            // Update UI
            userName.setText("Name: " + (userNames != null ? userNames : "No Name"));
            userEmail.setText("Email: " + (userEmails != null ? userEmails : "No Email"));
            userBmi = findViewById(R.id.user_bmi);
            if (photoUrl != null) {
                Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_launcher_foreground) // Fallback image
                        .into(userProfilePic);
            }
        }

        // Button Click Listeners
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Play sound effect
                MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.bye);
                mediaPlayer.start();
                gClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        finish();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        Toast.makeText(MainActivity.this, "Log-out Successful", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        btnBmiCalculator.setOnClickListener(v -> redirectToPage(BmiCalculatorActivity.class));

        // Load BMI data
        loadUserBmi();
        ImageView navRunning = findViewById(R.id.nav_running);
        ImageView navCommunity = findViewById(R.id.nav_community);
        ImageView navLeaderboard = findViewById(R.id.nav_leaderboard);
        ImageView navReminder = findViewById(R.id.nav_reminder);
        ImageView userProfile = findViewById(R.id.UserProfile);
        ImageView  navWeather =findViewById(R.id.Weather);
        // Set up navigation
        Navigation.setupNavigation(this, navRunning, navCommunity, navLeaderboard, navReminder, userProfile,navWeather);

        // Initialize the options menu ImageView
        optionsMenu = findViewById(R.id.OptionsMenu);

        // Set up a click listener for the ImageView
        optionsMenu.setOnClickListener(view -> {
            // Create a PopupMenu
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
            popupMenu.getMenuInflater().inflate(R.menu.options_menu, popupMenu.getMenu());

            // Define variables for menu item IDs
            int menuHistoryId = R.id.menu_history;

            // Handle menu item clicks using if-else
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId(); // Get the clicked item's ID

                if (itemId == menuHistoryId) {
                    // Navigate to HistoryActivity
                    redirectToPage(HistoryActivity.class);
                    return true;

                } else {
                    return false; // No matching menu item
                }
            });

            // Show the PopupMenu
            popupMenu.show();
        });
    }


    private void loadUserBmi() {
        BmiDatabaseHelper databaseHelper = new BmiDatabaseHelper(this);
        Cursor cursor = databaseHelper.getBMI();

        if (cursor != null && cursor.moveToFirst()) {
            // Use constants from BmiDatabaseHelper to access column indices
            float bmiValue = cursor.getFloat(cursor.getColumnIndex(BmiDatabaseHelper.COLUMN_BMI_VALUE));
            String category = cursor.getString(cursor.getColumnIndex(BmiDatabaseHelper.COLUMN_CATEGORY));
            String timestamp = cursor.getString(cursor.getColumnIndex(BmiDatabaseHelper.COLUMN_TIMESTAMP));

            // Display BMI data
            userBmi.setText(String.format("BMI: %.2f\nCategory: %s\nUpdated: %s", bmiValue, category, timestamp));

            cursor.close();
        } else {
            // No BMI data found
            userBmi.setText("BMI: Not available");
        }
    }


    /**
     * Redirect to another page.
     *
     * @param activityClass Activity class to navigate to.
     */
    private void redirectToPage(Class<?> activityClass) {
        // Play sound effect
        MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.clicked);
        mediaPlayer.start();
        Intent intent = new Intent(MainActivity.this, activityClass);
        startActivity(intent);
    }


    /**
     * Create options menu in the top-right corner.
     */

}
