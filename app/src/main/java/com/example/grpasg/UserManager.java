package com.example.grpasg;

import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class UserManager {

    private static UserManager instance;
    private FirebaseUser firebaseUser;
    private GoogleSignInAccount googleSignInAccount;

    // Private constructor to ensure singleton pattern
    private UserManager() {}

    // Get the singleton instance
    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    // Store Firebase user info
    public void setFirebaseUser(FirebaseUser user) {
        this.firebaseUser = user;
    }

    public FirebaseUser getFirebaseUser() {
        return firebaseUser;
    }

    // Store Google sign-in info
    public void setGoogleSignInAccount(GoogleSignInAccount account) {
        this.googleSignInAccount = account;
    }

    public GoogleSignInAccount getGoogleSignInAccount() {
        return googleSignInAccount;
    }
}
