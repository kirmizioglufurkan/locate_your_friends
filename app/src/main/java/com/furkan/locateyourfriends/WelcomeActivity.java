/**
 * @author Furkan Kırmızıoğlu on 2020
 * @project Locate Your Friends
 */

package com.furkan.locateyourfriends;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.karan.churi.PermissionManager.PermissionManager;

import java.util.ArrayList;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnLogin;
    private TextView tvRegister;
    private PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        btnLogin = findViewById(R.id.btn_welcome_login);
        tvRegister = findViewById(R.id.tv_welcome_goToRegister);
        btnLogin.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
        permissionManager = new PermissionManager() {
        };
        permissionManager.checkAndRequestPermissions(this);
    }

    //Start LoginActivity or RegisterActivity.
    @Override
    public void onClick(View v) {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        switch (v.getId()) {
            case R.id.btn_welcome_login: {
                btnLogin.startAnimation(animation);
                startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
                finish();
                break;
            }
            case R.id.tv_welcome_goToRegister: {
                startActivity(new Intent(WelcomeActivity.this, RegisterActivity.class));
                finish();
                break;
            }
            default:
                break;
        }
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


