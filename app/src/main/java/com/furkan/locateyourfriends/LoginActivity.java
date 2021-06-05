/*
    @author Furkan Kırmızıoğlu
*/
package com.furkan.locateyourfriends;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private Button btnLogin;
    private TextView goToRegister;
    private ImageView imgBack;
    private ProgressBar pbLogin;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private EditText etEmail;
    private EditText etPassword;
    private Utility utility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btnLogin = findViewById(R.id.btn_login);
        goToRegister = findViewById(R.id.tv_login_goToRegister);
        imgBack = findViewById(R.id.img_login_back);
        pbLogin = findViewById(R.id.progressbar_login);
        emailLayout = findViewById(R.id.til_login_email);
        passwordLayout = findViewById(R.id.til_login_password);
        etEmail = findViewById(R.id.et_login_email);
        etPassword = findViewById(R.id.et_login_password);
        pbLogin.setVisibility(View.INVISIBLE);
        auth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        if (intent != null)
            Objects.requireNonNull(emailLayout.getEditText()).setText(intent.getStringExtra("email"));
        utility = new Utility();
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnLogin.setOnClickListener(v -> login());

        goToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });

        imgBack.setOnClickListener(v -> {
            Animation animation = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.bounce);
            imgBack.startAnimation(animation);
            startActivity(new Intent(LoginActivity.this, WelcomeActivity.class));
            finish();
        });

    }

    //Authorizing to Firebase.
    private void login() {
        String email = Objects.requireNonNull(emailLayout.getEditText()).getText().toString().trim();
        String password = Objects.requireNonNull(passwordLayout.getEditText()).getText().toString().trim();

        if (!utility.emailNullCheck(email)) {
            emailLayout.setError(getResources().getString(R.string.register_email_null_error));
            utility.requestFocus(etEmail, LoginActivity.this);
            return;
        }
        if (!utility.emailFormatCheck(email)) {
            emailLayout.setError(getResources().getString(R.string.register_email_wrong_error));
            utility.requestFocus(etEmail, LoginActivity.this);
            return;
        }
        if (!utility.passwordNullCheck(password)) {
            passwordLayout.setError(getResources().getString(R.string.login_password_null_error));
            utility.requestFocus(etPassword, LoginActivity.this);
            return;
        }
        if (!utility.checkInternetConnection(this, getResources().getString(R.string.login_alert_text)))
            return;

        pbLogin.setVisibility(View.VISIBLE);
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                pbLogin.setVisibility(View.VISIBLE);
                startActivity(new Intent(LoginActivity.this, UserLocationMainActivity.class));
                finish();
            } else {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_wrong_info), Toast.LENGTH_SHORT).show();
                pbLogin.setVisibility(View.INVISIBLE);
            }
        });
    }
}
