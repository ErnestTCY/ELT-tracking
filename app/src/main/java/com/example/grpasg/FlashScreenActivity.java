package com.example.grpasg;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.example.grpasg.R;
import android.media.MediaPlayer;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FlashScreenActivity extends AppCompatActivity {

    private static final int SPLASH_SCREEN_DURATION = 3900; // 3 seconds
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_screen);

        auth = FirebaseAuth.getInstance();

        // Add animations
        TextView splashText = findViewById(R.id.splash_text);
        ImageView splashImage = findViewById(R.id.splash_image);

        // Load animations
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation scaleUpAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_up);

        // Start animations
        splashText.startAnimation(fadeInAnimation);
        splashImage.startAnimation(scaleUpAnimation);

        // Play sound effect
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.splash_sound); // Add splash_sound.mp3 to res/raw folder
        mediaPlayer.start();
        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = auth.getCurrentUser();

            if (currentUser != null && currentUser.isEmailVerified()) {
                // Check if the user's email is verified
                startActivity(new Intent(FlashScreenActivity.this, MainActivity.class));
            } else {
                // No user is signed in or email is not verified, navigate to LoginActivity
                startActivity(new Intent(FlashScreenActivity.this, LoginActivity.class));
            }
            finish(); // Close FlashScreenActivity
        }, SPLASH_SCREEN_DURATION);
    }


}
