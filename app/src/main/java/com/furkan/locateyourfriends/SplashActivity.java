package com.furkan.locateyourfriends;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;

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
            if (!checkConnection()) return;
            startActivity(new Intent(SplashActivity.this, UserLocationMainActivity.class));
            finish();
        }
    }

    private boolean checkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connectivityManager.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected())
            return true;
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
            builder.setCancelable(false);
            builder.setIcon(R.drawable.wifi_off);
            builder.setTitle(getResources().getString(R.string.login_alert_title));
            builder.setMessage(getResources().getString(R.string.login_alert_text));
            builder.setNegativeButton(getResources().getString(R.string.login_alert_negative_text), null);
            builder.setPositiveButton(getResources().getString(R.string.login_alert_positive_text), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    SplashActivity.this.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            });
            builder.show();
            return false;
        }
    }
}