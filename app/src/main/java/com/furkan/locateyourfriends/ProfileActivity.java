/**
 * @author Furkan Kırmızıoğlu on 2020
 * @project Locate Your Friends
 */
package com.furkan.locateyourfriends;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.santalu.maskedittext.MaskEditText;
import com.yalantis.ucrop.UCrop;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private String username;
    private String email;
    private String password;
    private String name;
    private String surname;
    private String phoneNumber;
    private String code;

    private TextInputLayout nameLayout;
    private TextInputLayout surnameLayout;
    private TextInputLayout phoneNumberLayout;

    private EditText etName;
    private EditText etSurname;
    private MaskEditText maskPhoneNumber;
    private CircleImageView circleImageView;
    private Uri resultUri;
    private Button btnProfile;
    private ImageView imgBack;
    private final Date myDate = new Date();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        circleImageView = findViewById(R.id.img_profile_picture);

        nameLayout = findViewById(R.id.til_profile_name);
        surnameLayout = findViewById(R.id.til_profile_surname);
        phoneNumberLayout = findViewById(R.id.til_profile_phone);
        etName = findViewById(R.id.et_profile_name);
        etSurname = findViewById(R.id.et_profile_surname);
        maskPhoneNumber = findViewById(R.id.masket_profile_phone);
        btnProfile = findViewById(R.id.btn_profile);
        btnProfile.setOnClickListener(this);
        imgBack = findViewById(R.id.img_profile_back);
        imgBack.setOnClickListener(this);

        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("username");
            email = intent.getStringExtra("email");
            password = intent.getStringExtra("password");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_profile:
                saveProfileInfo();
                break;
            case R.id.img_profile_back:
                goBack();
                break;
            default:
                break;
        }
    }

    private void saveProfileInfo() {
        if (!checkName()) return;
        if (!checkSurname()) return;
        if (!checkPhoneNumber()) return;
        name = etName.getText().toString();
        surname = etSurname.getText().toString();
        phoneNumber = maskPhoneNumber.getRawText();
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
        intent.putExtra("imageUri", resultUri);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    private boolean checkName() {
        if (etName.getText().toString().trim().isEmpty()) {
            nameLayout.setErrorEnabled(true);
            nameLayout.setError(getResources().getString(R.string.gallery_name_null_error));
            requestFocus(etName);
            return false;
        }
        nameLayout.setErrorEnabled(false);
        return true;
    }

    private boolean checkSurname() {
        if (etSurname.getText().toString().trim().isEmpty()) {
            surnameLayout.setErrorEnabled(true);
            surnameLayout.setError(getResources().getString(R.string.gallery_surname_null_error));
            requestFocus(etSurname);
            return false;
        }
        surnameLayout.setErrorEnabled(false);
        return true;
    }

    private boolean checkPhoneNumber() {
        if (maskPhoneNumber.getRawText().trim().isEmpty()) {
            phoneNumberLayout.setErrorEnabled(true);
            phoneNumberLayout.setError(getResources().getString(R.string.gallery_phone_null_error));
            requestFocus(maskPhoneNumber);
            return false;
        }
        if (maskPhoneNumber.getRawText().trim().length() != 12 | !Patterns.PHONE.matcher(maskPhoneNumber.getRawText()).matches()) {
            phoneNumberLayout.setErrorEnabled(true);
            phoneNumberLayout.setError(getResources().getString(R.string.gallery_phone_wrong_error));
            requestFocus(maskPhoneNumber);
            return false;
        }
        phoneNumberLayout.setErrorEnabled(false);
        return true;
    }

    public void selectImage(View v) {
        Intent i = new Intent();
        i.setAction(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        startActivityForResult(i, 12);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        }
    }


    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

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