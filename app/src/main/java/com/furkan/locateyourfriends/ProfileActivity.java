/*
    @author Furkan Kırmızıoğlu
*/
package com.furkan.locateyourfriends;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.santalu.maskedittext.MaskEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class ProfileActivity extends AppCompatActivity {

    private String username;
    private String email;
    private String password;
    private String code;

    private TextInputLayout nameLayout;
    private TextInputLayout surnameLayout;

    private Utility utility;

    private EditText etName;
    private EditText etSurname;
    private MaskEditText maskPhoneNumber;
    // private Uri resultUri;
    private Button btnProfile;
    private ImageView imgBack;
    private final Date myDate = new Date();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        // CircleImageView circleImageView = findViewById(R.id.img_profile_picture);

        nameLayout = findViewById(R.id.til_profile_name);
        surnameLayout = findViewById(R.id.til_profile_surname);
        etName = findViewById(R.id.et_profile_name);
        etSurname = findViewById(R.id.et_profile_surname);
        maskPhoneNumber = findViewById(R.id.masket_profile_phone);
        btnProfile = findViewById(R.id.btn_profile);
        imgBack = findViewById(R.id.img_profile_back);

        utility = new Utility();

        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("username");
            email = intent.getStringExtra("email");
            password = intent.getStringExtra("password");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnProfile.setOnClickListener(v -> saveProfileInfo());
        imgBack.setOnClickListener(v -> goBack());
    }

    private void saveProfileInfo() {
        String name = etName.getText().toString();
        String surname = etSurname.getText().toString();
        String phoneNumber = Objects.requireNonNull(maskPhoneNumber.getRawText());
        if (!checkName(name)) return;
        if (!checkSurname(surname)) return;
        generateCode();
        Intent intent = new Intent(ProfileActivity.this, InviteCodeActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("email", email);
        intent.putExtra("password", password);
        intent.putExtra("name", name);
        intent.putExtra("surname", surname);
        intent.putExtra("phoneNumber", phoneNumber);
        intent.putExtra("isSharing", "false");
        intent.putExtra("code", code);
        //intent.putExtra("imageUri", resultUri);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    private boolean checkName(String name) {
        if (name.isEmpty()) {
            nameLayout.setErrorEnabled(true);
            nameLayout.setError(getResources().getString(R.string.gallery_name_null_error));
            utility.requestFocus(etName, ProfileActivity.this);
            return false;
        }
        return true;
    }

    private boolean checkSurname(String surname) {
        if (surname.isEmpty()) {
            surnameLayout.setErrorEnabled(true);
            surnameLayout.setError(getResources().getString(R.string.gallery_surname_null_error));
            utility.requestFocus(etSurname, ProfileActivity.this);
            return false;
        }
        return true;
    }


    //TODO -> Fix the image issue.

//    public void selectImage(View v) {
//        Intent i = new Intent();
//        i.setAction(Intent.ACTION_GET_CONTENT);
//        i.setType("image/*");
//        startActivityForResult(i, 12);
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
//            final Uri resultUri = UCrop.getOutput(data);
//        } else if (resultCode == UCrop.RESULT_ERROR) {
//            final Throwable cropError = UCrop.getError(data);
//        }
//    }

    private void generateCode() {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy--MM-dd hh:mm:ss a", Locale.getDefault());
        format1.format(myDate);
        Random r = new Random();
        int n = 100000 + r.nextInt(900000);
        code = String.valueOf(n);
    }

    private void goBack() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.bounce);
        imgBack.startAnimation(animation);
        Intent intent = new Intent(ProfileActivity.this, RegisterActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("email", email);
        intent.putExtra("password", password);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

}