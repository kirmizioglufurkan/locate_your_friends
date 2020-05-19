package com.furkan.locateyourfriends;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText email,password,username;
    private FirebaseAuth auth;
    private ProgressDialog dialog;
    private String register_username, register_email, register_password;
    private Button btnRegister; private ImageView imgBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = findViewById(R.id.et_register_name);
        email = findViewById(R.id.et_register_email);
        password = findViewById(R.id.et_register_password);
        btnRegister = findViewById(R.id.btn_register);
        imgBack = findViewById(R.id.img_register_back);
        btnRegister.setOnClickListener(this);
        imgBack.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(this);

        Intent intent = getIntent();
        if(intent!=null){
            username.setText(intent.getStringExtra("username"));
            email.setText(intent.getStringExtra("email"));
            password.setText(intent.getStringExtra("password"));
        }
    }

    //Fetch username, email, password and goes to GalleryActivity.
    private void goToGalleryActivity() {
        register_username = username.getText().toString();
        register_email = email.getText().toString();
        register_password = password.getText().toString();
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.fade_in);
        btnRegister.startAnimation(animation);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connectivityManager.getActiveNetworkInfo();
        if(activeInfo != null && activeInfo.isConnected()) {
            dialog.setMessage("Bilgileriniz kontrol ediliyor.");
            dialog.show();
            dialog.setCancelable(false);
            if (register_email.isEmpty() == false || register_username.isEmpty() == false || register_password.isEmpty() == false) {
                auth.fetchSignInMethodsForEmail(register_email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if (task.isSuccessful()) {
                            dialog.dismiss();
                            boolean check = !task.getResult().getSignInMethods().isEmpty();
                            //Is e-mail already registered?
                            if (!check) {
                                if (password.getText().toString().length() > 6) {
                                    Intent intent = new Intent(RegisterActivity.this, GalleryActivity.class);
                                    intent.putExtra("username", register_username);
                                    intent.putExtra("email", register_email);
                                    intent.putExtra("password", register_password);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                    finish();
                                } else if (register_password.length() < 6) {
                                    dialog.dismiss();
                                    Toast.makeText(getApplicationContext(), R.string.register_password_error, Toast.LENGTH_SHORT).show();
                                    password.setText("");
                                }
                            } else {
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), R.string.register_email_error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            } else {
                dialog.dismiss();
                Toast.makeText(getApplicationContext(),R.string.register_form_error,Toast.LENGTH_SHORT).show();
                email.setText(""); password.setText(""); username.setText("");
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
            builder.setCancelable(false);
            builder.setIcon(R.drawable.wifi_off);
            builder.setTitle(getResources().getString(R.string.register_alert_title));
            builder.setMessage(getResources().getString(R.string.register_alert_text));
            builder.setNegativeButton(getResources().getString(R.string.register_alert_negative_text), null);
            builder.setPositiveButton(getResources().getString(R.string.register_alert_positive_text), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) { RegisterActivity.this.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));}});
            builder.show();
        }
    }

    private void goToLoginActivity(){
        Animation animation_bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
        imgBack.startAnimation(animation_bounce);
        startActivity(new Intent(RegisterActivity.this, WelcomeActivity.class)); finish();
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.btn_register: goToGalleryActivity(); break;
            case R.id.img_register_back: goToLoginActivity(); finish(); break;
        }
    }
}