package com.furkan.locateyourfriends;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.santalu.maskedittext.MaskEditText;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class GalleryActivity extends AppCompatActivity implements View.OnClickListener {

    private String username,email,password, name, surname, phoneNumber;
    private EditText etName, etSurname;
    private MaskEditText etPhoneNumber;
    private CircleImageView circleImageView; private Uri resultUri;
    private Button btnGallery; private ImageView imgBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        circleImageView = findViewById(R.id.img_gallery_profile);

        etName = findViewById(R.id.et_gallery_name);
        etSurname = findViewById(R.id.et_gallery_surname);
        etPhoneNumber = findViewById(R.id.et_gallery_phone);
        btnGallery = findViewById(R.id.btn_gallery);
        btnGallery.setOnClickListener(this);
        imgBack = findViewById(R.id.img_gallery_back);
        imgBack.setOnClickListener(this);

        Intent intent = getIntent();
        if(intent != null) {
            username = intent.getStringExtra("username");
            email = intent.getStringExtra("email");
            password = intent.getStringExtra("password");
        }
    }

    public void selectImage(View v) {
        Intent i = new Intent();
        i.setAction(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        startActivityForResult(i,12);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 12 && resultCode == RESULT_OK && data!=null) {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                circleImageView.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_gallery: generateCode(v); break;
            case R.id.img_gallery_back: goBack(v); break;
            default:break;
        }
    }

    private void generateCode(View v) {
        Date myDate =  new Date();
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy--MM-dd hh:mm:ss a", Locale.getDefault());
        String date = format1.format(myDate);
        Random r = new Random();
        int n = 100000 + r.nextInt(900000);
        String code = String.valueOf(n);

        name = etName.getText().toString();
        surname = etSurname.getText().toString();
        phoneNumber = etPhoneNumber.getRawText().toString();
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.fade_in);
        btnGallery.startAnimation(animation);

        if(resultUri != null && name != null && surname != null && phoneNumber != null) {
            Intent intent = new Intent(GalleryActivity.this, InviteCodeActivity.class);
            intent.putExtra("username",username); intent.putExtra("email",email); intent.putExtra("password",password);
            intent.putExtra("name",name); intent.putExtra("surname",surname); intent.putExtra("phoneNumber", phoneNumber);
            intent.putExtra("date",date); intent.putExtra("isSharing","false");
            intent.putExtra("code",code); intent.putExtra("imageUri",resultUri);
            startActivity(intent); overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); finish();
        }
        else if (name.isEmpty() || surname.isEmpty() || phoneNumber.isEmpty()){
            Toast.makeText(getApplicationContext(), "Lütfen tüm alanları doldurunuz.",Toast.LENGTH_SHORT).show();}
        else {
            Toast.makeText(getApplicationContext(), "Lütfen profil resmi seçiniz.",Toast.LENGTH_SHORT).show(); }
    }

    private void goBack(View v){
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.bounce);
        imgBack.startAnimation(animation);
        Intent intent = new Intent(GalleryActivity.this, RegisterActivity.class);
        intent.putExtra("username",username);intent.putExtra("email",email);intent.putExtra("password",password);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

}
