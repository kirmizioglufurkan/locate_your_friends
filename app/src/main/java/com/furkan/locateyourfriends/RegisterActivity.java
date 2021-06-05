/**
 * @author Furkan Kırmızıoğlu on 2020
 * @project Locate Your Friends
 */
package com.furkan.locateyourfriends;

import android.app.ProgressDialog;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

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
    private String register_username;
    private String register_email;
    private String register_password;
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
        btnRegister.setOnClickListener(RegisterActivity.this);
        imgBack = findViewById(R.id.img_register_back);
        imgBack.setOnClickListener(RegisterActivity.this);
        auth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(RegisterActivity.this);
        Intent intent = getIntent();
        if (intent != null) {
            etUsername.setText(intent.getStringExtra("username"));
            etEmail.setText(intent.getStringExtra("email"));
        }
    }

    private void goToLoginActivity() {
        Animation animation_bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
        imgBack.startAnimation(animation_bounce);
        startActivity(new Intent(RegisterActivity.this, WelcomeActivity.class));
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_register:
                register();
                break;
            case R.id.img_register_back:
                goToLoginActivity();
                finish();
                break;
        }
    }

    //Checking input fields and calls goToGalleryActivity.
    private void register() {
        if (!checkUsername()) return;
        if (!checkEmail()) return;
        if (!checkPassword()) return;
        if (!utility.checkInternetConnection(this, getResources().getString(R.string.register_alert_text)))
            return;

        layoutUsername.setErrorEnabled(false);
        layoutEmail.setErrorEnabled(false);
        layoutPassword.setErrorEnabled(false);
        layoutPasswordConfirm.setErrorEnabled(false);
        goToGalleryActivity();
    }

    private boolean checkUsername() {
        if (etUsername.getText().toString().trim().isEmpty()) {
            layoutUsername.setErrorEnabled(true);
            layoutUsername.setError(getResources().getString(R.string.register_username_null_error));
            return false;
        }
        layoutUsername.setErrorEnabled(false);
        return true;
    }

    private boolean checkEmail() {
        String emailTemp = etEmail.getText().toString().trim();
        if (emailTemp.isEmpty()) {
            layoutEmail.setErrorEnabled(true);
            layoutEmail.setError(getResources().getString(R.string.register_email_null_error));
            requestFocus(etEmail);
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailTemp).matches()) {
            layoutEmail.setErrorEnabled(true);
            layoutEmail.setError(getResources().getString(R.string.register_email_wrong_error));
            etEmail.setError("Valid Input Required");
            requestFocus(etEmail);
            return false;
        }
        layoutEmail.setErrorEnabled(false);
        return true;
    }

    private boolean checkPassword() {
        if (etPassword.getText().toString().trim().isEmpty()) {
            layoutPassword.setError(getResources().getString(R.string.register_password_null_error));
            requestFocus(etPassword);
            return false;
        }
        if (etPasswordConfirm.getText().toString().trim().isEmpty()) {
            layoutPasswordConfirm.setError(getResources().getString(R.string.register_password_confirm_null_error));
            requestFocus(etPasswordConfirm);
            return false;
        }
        if (!etPassword.getText().toString().trim().equals(etPasswordConfirm.getText().toString().trim())) {
            layoutPassword.setError(getResources().getString(R.string.register_password_confirm_not_match_error));
            layoutPasswordConfirm.setError(getResources().getString(R.string.register_password_confirm_not_match_error));
            requestFocus(etPasswordConfirm);
            return false;
        }
        layoutPassword.setErrorEnabled(false);
        layoutPasswordConfirm.setErrorEnabled(false);
        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        } else
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    //Fetch username, email, password and goes to GalleryActivity.
    private void goToGalleryActivity() {
        register_username = layoutUsername.getEditText().getText().toString().trim();
        register_email = layoutEmail.getEditText().getText().toString().trim();
        register_password = layoutPassword.getEditText().getText().toString().trim();
        dialog.setMessage(getResources().getString(R.string.register_check_information));
        dialog.show();
        dialog.setCancelable(false);
        auth.fetchSignInMethodsForEmail(register_email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if (task.isSuccessful()) {
                    dialog.dismiss();
                    boolean check = !task.getResult().getSignInMethods().isEmpty();
                    //Is e-mail already registered?
                    if (!check) {
                        Intent intent = new Intent(RegisterActivity.this, ProfileActivity.class);
                        intent.putExtra("username", register_username);
                        intent.putExtra("email", register_email);
                        intent.putExtra("password", register_password);
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
            }
        });
    }
}