package com.furkan.locateyourfriends;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth auth;

    private Button btnLogin; private TextView goToRegister; private ImageView imgBack;
    private EditText email,password;
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
        email = findViewById(R.id.et_login_email);
        password = findViewById(R.id.et_login_password);
        auth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        if(intent != null)
            email.setText(intent.getStringExtra("email"));
    }

    //Authorizes to Firebase.
    private void login() {
        user_email = email.getText().toString();
        user_password = password.getText().toString();
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        btnLogin.startAnimation(animation);

        if(user_email.isEmpty() || user_password.isEmpty()){
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_field_error), Toast.LENGTH_SHORT).show();
            email.setText(""); password.setText("");
        } else {
        auth.signInWithEmailAndPassword(user_email,user_password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            Intent intent = new Intent(LoginActivity.this,UserLocationMainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_wrong_info), Toast.LENGTH_SHORT).show();
                            email.setText("");
                            password.setText("");
                        }
                    }
                });
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
}
