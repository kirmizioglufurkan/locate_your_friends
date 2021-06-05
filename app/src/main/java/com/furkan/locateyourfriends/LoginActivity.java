/**
 * @author Furkan Kırmızıoğlu on 2020
 * @project Locate Your Friends
 */
package com.furkan.locateyourfriends;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth auth;
    private Button btnLogin;
    private TextView goToRegister;
    private ImageView imgBack;
    private ProgressBar pbLogin;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private EditText etEmail;
    private EditText etPassword;
    private String user_email;
    private String user_password;
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
        btnLogin.setOnClickListener(LoginActivity.this);
        goToRegister.setOnClickListener(LoginActivity.this);
        imgBack.setOnClickListener(LoginActivity.this);
        pbLogin.setVisibility(View.INVISIBLE);
        auth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        if (intent != null)
            emailLayout.getEditText().setText(intent.getStringExtra("email"));
        utility = new Utility();
    }


    // Actions for every possible click.
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login: {
                login();
            }
            break;
            case R.id.tv_login_goToRegister: {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
            break;
            case R.id.img_login_back: {
                Animation animation = AnimationUtils.loadAnimation(this, R.anim.bounce);
                imgBack.startAnimation(animation);
                startActivity(new Intent(LoginActivity.this, WelcomeActivity.class));
                finish();
            }
            break;
        }
    }

    //Authorizes to Firebase.
    private void login() {
        if (!checkEmail()) return;
        if (!checkPassword()) return;
        if (!utility.checkInternetConnection(this, getResources().getString(R.string.login_alert_text)))
            return;
        pbLogin.setVisibility(View.VISIBLE);
        user_email = etEmail.getText().toString().trim();
        user_password = etPassword.getText().toString().trim();
        auth.signInWithEmailAndPassword(user_email, user_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    pbLogin.setVisibility(View.VISIBLE);
                    startActivity(new Intent(LoginActivity.this, UserLocationMainActivity.class));
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_wrong_info), Toast.LENGTH_SHORT).show();
                    pbLogin.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private boolean checkEmail() {
        String emailUser = emailLayout.getEditText().getText().toString().trim();
        if (emailUser.isEmpty()) {
            emailLayout.setError(getResources().getString(R.string.register_email_null_error));
            requestFocus(etEmail);
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailUser).matches()) {
            emailLayout.setError(getResources().getString(R.string.register_email_wrong_error));
            requestFocus(etEmail);
            return false;
        }
        emailLayout.setErrorEnabled(false);
        return true;
    }

    private boolean checkPassword() {
        if (etPassword.getText().toString().trim().isEmpty()) {
            passwordLayout.setError(getResources().getString(R.string.login_password_null_error));
            requestFocus(etPassword);
            return false;
        }
        passwordLayout.setErrorEnabled(false);
        return true;
    }

    private void requestFocus(View v) {
        if (v.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        } else
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

}
