/*
    @author Furkan Kırmızıoğlu
*/
package com.furkan.locateyourfriends;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout layoutUsername;
    private TextInputLayout layoutEmail;
    private TextInputLayout layoutPassword;
    private TextInputLayout layoutPasswordConfirm;
    private EditText etUsername;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etPasswordConfirm;
    private FirebaseAuth auth;
    private ProgressDialog dialog;
    private Button btnRegister;
    private ImageView imgBack;
    private final Utility utility = new Utility();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        layoutUsername = findViewById(R.id.til_register_name);
        layoutEmail = findViewById(R.id.til_register_email);
        layoutPassword = findViewById(R.id.til_register_password);
        layoutPasswordConfirm = findViewById(R.id.til_register_password_confirm);
        etUsername = findViewById(R.id.et_register_username);
        etEmail = findViewById(R.id.et_register_email);
        etPassword = findViewById(R.id.et_register_password);
        etPasswordConfirm = findViewById(R.id.et_register_password_confirm);
        btnRegister = findViewById(R.id.btn_register);
        imgBack = findViewById(R.id.img_register_back);
        auth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(RegisterActivity.this);
        Intent intent = getIntent();
        if (intent != null) {
            etUsername.setText(intent.getStringExtra("username"));
            etEmail.setText(intent.getStringExtra("email"));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnRegister.setOnClickListener(v -> register());
        imgBack.setOnClickListener(v -> {
            Animation animation_bounce = AnimationUtils.loadAnimation(RegisterActivity.this, R.anim.bounce);
            imgBack.startAnimation(animation_bounce);
            startActivity(new Intent(RegisterActivity.this, WelcomeActivity.class));
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(RegisterActivity.this, WelcomeActivity.class));
        finish();
    }

    //Checks input fields and calls goToGalleryActivity.
    private void register() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String passwordConfirm = etPasswordConfirm.getText().toString().trim();

        if (username.isEmpty()) {
            layoutUsername.setErrorEnabled(true);
            layoutUsername.setError(getResources().getString(R.string.register_username_null_error));
        }
        if (!utility.emailNullCheck(email)) {
            layoutEmail.setErrorEnabled(true);
            layoutEmail.setError(getResources().getString(R.string.register_email_null_error));
            utility.requestFocus(etEmail, RegisterActivity.this);
            return;
        }
        if (!utility.emailFormatCheck(email)) {
            layoutEmail.setErrorEnabled(true);
            layoutEmail.setError(getResources().getString(R.string.register_email_wrong_error));
            etEmail.setError("Valid Input Required");
            utility.requestFocus(etEmail, RegisterActivity.this);
            return;
        }
        if (!checkPassword(password, passwordConfirm)) return;
        if (!utility.checkInternetConnection(RegisterActivity.this, getResources().getString(R.string.register_alert_text)))
            return;

        goToGalleryActivity(username, email, password);
    }

    private boolean checkPassword(String password, String passwordConfirm) {
        if (password.isEmpty()) {
            layoutPassword.setError(getResources().getString(R.string.register_password_null_error));
            utility.requestFocus(etPassword, RegisterActivity.this);
            return false;
        }
        if (passwordConfirm.isEmpty()) {
            layoutPasswordConfirm.setError(getResources().getString(R.string.register_password_confirm_null_error));
            utility.requestFocus(etPasswordConfirm, RegisterActivity.this);
            return false;
        }
        if (!password.equals(passwordConfirm)) {
            layoutPassword.setError(getResources().getString(R.string.register_password_confirm_not_match_error));
            layoutPasswordConfirm.setError(getResources().getString(R.string.register_password_confirm_not_match_error));
            utility.requestFocus(etPasswordConfirm, RegisterActivity.this);
            return false;
        }
        return true;
    }

    //Fetches username, email, password information and goes to GalleryActivity.
    private void goToGalleryActivity(String username, String email, String password) {
        dialog.setMessage(getResources().getString(R.string.register_check_information));
        dialog.show();
        dialog.setCancelable(false);
        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                dialog.dismiss();
                boolean check = !Objects.requireNonNull(task.getResult().getSignInMethods()).isEmpty();
                //Is e-mail already registered?
                if (!check) {
                    Intent intent = new Intent(RegisterActivity.this, ProfileActivity.class);
                    intent.putExtra("username", username);
                    intent.putExtra("email", email);
                    intent.putExtra("password", password);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                } else {
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.register_email_duplicate_error), Toast.LENGTH_SHORT).show();
                    etPassword.getText().clear();
                    etPasswordConfirm.getText().clear();
                }
            }
        });
    }
}