package com.furkan.locateyourfriends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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
        dialog.setMessage("Bilgileriniz kontrol ediliyor.");
        dialog.show();

        register_username = username.getText().toString();
        register_email = email.getText().toString();
        register_password = password.getText().toString();
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.fade_in);
        btnRegister.startAnimation(animation);

        if(register_email.isEmpty() == false || register_username.isEmpty() == false || register_password.isEmpty() == false) {
            auth.fetchSignInMethodsForEmail(register_email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                  if(task.isSuccessful()) {
                      dialog.dismiss();
                      boolean check = !task.getResult().getSignInMethods().isEmpty();
                      //E-mail daha önceden kayıtlı mı?
                      if (!check) {
                          if (password.getText().toString().length() > 6) {
                              Intent intent = new Intent(RegisterActivity.this, GalleryActivity.class);
                              intent.putExtra("username", register_username);
                              intent.putExtra("email",register_email);
                              intent.putExtra("password",register_password);
                              startActivity(intent);
                              overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                              finish();
                          } else if (register_password.length() < 6) {
                              dialog.dismiss();
                              Toast.makeText(getApplicationContext(),"Şifre minimum 6 karakterden oluşmalıdır.",Toast.LENGTH_SHORT).show();
                              password.setText("");
                          }
                      } else {
                          dialog.dismiss();
                          Toast.makeText(getApplicationContext(),"Bu e-mail zaten kayıtlı.",Toast.LENGTH_SHORT).show();
                      }
                  } }
            });
        }
        else {
            dialog.dismiss();
            Toast.makeText(getApplicationContext(),"Lütfen tüm alanları doldurunuz.",Toast.LENGTH_SHORT).show();
            email.setText(""); password.setText(""); username.setText("");
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