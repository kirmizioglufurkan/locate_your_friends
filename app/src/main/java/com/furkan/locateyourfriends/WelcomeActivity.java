/*
    @author Furkan Kırmızıoğlu
*/

package com.furkan.locateyourfriends;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.karan.churi.PermissionManager.PermissionManager;

import java.util.ArrayList;

public class WelcomeActivity extends AppCompatActivity {

    private Button btnLogin;
    private TextView tvRegister;
    private PermissionManager permissionManager;
    private Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        btnLogin = findViewById(R.id.btn_welcome_login);
        tvRegister = findViewById(R.id.tv_welcome_goToRegister);
        permissionManager = new PermissionManager() {
        };
        permissionManager.checkAndRequestPermissions(WelcomeActivity.this);
        animation = AnimationUtils.loadAnimation(WelcomeActivity.this, R.anim.fade_in);
    }


    @Override
    protected void onResume() {
        super.onResume();
        btnLogin.setOnClickListener(v -> {
            btnLogin.startAnimation(animation);
            startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
            finish();
        });

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, RegisterActivity.class));
            finish();
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionManager.checkResult(requestCode, permissions, grantResults);
        ArrayList<String> denied_permissions = permissionManager.getStatus().get(0).denied;
        if (denied_permissions.isEmpty())
            Toast.makeText(getApplicationContext(), "Permissions granted!", Toast.LENGTH_SHORT).show();
    }
}


