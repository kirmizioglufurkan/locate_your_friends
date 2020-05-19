package com.furkan.locateyourfriends;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth auth;

    private Button btnLogin; private TextView goToRegister; private ImageView imgBack;
    private TextInputLayout emailLayout, passwordLayout;
    private EditText etEmail, etPassword;
    private String user_email, user_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(this);
        goToRegister = findViewById(R.id.tv_login_goToRegister);
        goToRegister.setOnClickListener(this);
        imgBack = findViewById(R.id.img_login_back);
        imgBack.setOnClickListener(this);
        emailLayout = findViewById(R.id.til_login_email);
        passwordLayout = findViewById(R.id.til_login_password);
        etEmail = findViewById(R.id.et_login_email);
        etPassword = findViewById(R.id.et_login_password);
        auth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        if(intent != null)
            emailLayout.getEditText().setText(intent.getStringExtra("email"));
    }

    //Authorizes to Firebase.
    private void login() {
        if (!checkEmail()) return;
        if (!checkPassword()) return;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connectivityManager.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            user_email = emailLayout.getEditText().getText().toString();
            user_password = passwordLayout.getEditText().getText().toString();
            auth.signInWithEmailAndPassword(user_email, user_password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(LoginActivity.this, UserLocationMainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_wrong_info), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setCancelable(false);
            builder.setIcon(R.drawable.wifi_off);
            builder.setTitle(getResources().getString(R.string.register_alert_title));
            builder.setMessage(getResources().getString(R.string.register_alert_text));
            builder.setNegativeButton(getResources().getString(R.string.register_alert_negative_text), null);
            builder.setPositiveButton(getResources().getString(R.string.register_alert_positive_text), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    LoginActivity.this.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            });
            builder.show();
        }
    }

    // Actions for every possible click.
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.btn_login: { login(); } break;
            case R.id.tv_login_goToRegister: { startActivity(new Intent(LoginActivity.this,RegisterActivity.class)); finish(); } break;
            case R.id.img_login_back: {
                Animation animation = AnimationUtils.loadAnimation(this, R.anim.bounce);
                imgBack.startAnimation(animation);
                startActivity(new Intent(LoginActivity.this,WelcomeActivity.class)); finish();
            } break;
        }
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
        }
    }

}
