package com.furkan.locateyourfriends;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth auth; private FirebaseUser user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null) {
            setContentView(R.layout.activity_splash);
            startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
            finish();
        } else {
            startActivity(new Intent(SplashActivity.this, UserLocationMainActivity.class));
            finish();
        }
    }
}