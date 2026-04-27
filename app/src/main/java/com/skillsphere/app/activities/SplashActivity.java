package com.skillsphere.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.skillsphere.app.R;
import com.skillsphere.app.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(this::checkUserStatus, 2000);
    }

    private void checkUserStatus() {
        // Use Firebase Auth as source of truth, not just SharedPrefs
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            SessionManager.getInstance(this).logout(); // clear stale prefs
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }
}
