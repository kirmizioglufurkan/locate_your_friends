/*
    @author Furkan Kırmızıoğlu
*/

package com.furkan.locateyourfriends;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chaos.view.PinView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

import static com.furkan.locateyourfriends.Utility.CODE;
import static com.furkan.locateyourfriends.Utility.IMAGE_URL;

public class InviteCodeActivity extends AppCompatActivity {

    private String username;
    private String email;
    private String password;
    private String name;
    private String surname;
    private String phoneNumber;
    private String code;
    private String userId;
    private Uri imageUri;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference reference;
    private StorageReference storageReference;
    private ProgressDialog dialog;
    private Button btnInviteCode;
    private Utility utility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_code);
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        storageReference = FirebaseStorage.getInstance().getReference().child("User_Images");
        PinView pwInviteCode = findViewById(R.id.pw_invite_code);
        btnInviteCode = findViewById(R.id.btn_invite_code);
        dialog = new ProgressDialog(this);
        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("username");
            email = intent.getStringExtra("email");
            password = intent.getStringExtra("password");
            name = intent.getStringExtra("name");
            surname = intent.getStringExtra("surname");
            phoneNumber = intent.getStringExtra("phoneNumber");
            code = intent.getStringExtra("code");
            pwInviteCode.setText(code);
        }
        imageUri = Uri.parse("https://mpng.subpng.com/20190623/ich/kisspng-computer-icons-clip-art-transparency-vector-graphi-un-buon-soggiorno-fattoria-di-macia-5d0f273ab57bb8.1831790115612741707434.jpg");
        utility = new Utility();
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnInviteCode.setOnClickListener(v -> checkAndRegister());
    }

    private void checkAndRegister() {
        if (!utility.checkInternetConnection(InviteCodeActivity.this, getResources().getString(R.string.invite_code_alert_text)))
            return;
        dialog.setMessage(getResources().getString(R.string.invite_code_progress));
        dialog.setCancelable(false);
        dialog.show();
        registerUser();
    }

    private void registerUser() {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                //Insert values in realtime database
                User registerUser = new User(username, email, password, name, surname, phoneNumber, code, false, 0, 0, "na");
                user = Objects.requireNonNull(auth.getCurrentUser());
                userId = Objects.requireNonNull(user.getUid());
                reference.child(userId).setValue(registerUser).addOnCompleteListener(task12 -> {
                    if (task12.isSuccessful()) {
                        //Storage profile image to firebase
                        StorageReference sr = storageReference.child(user.getUid() + ".jpg");
                        //TODO -> Fix the image issue.
                        sr.putFile(imageUri).addOnCompleteListener(taskSnapshot -> {
                            if (taskSnapshot.isSuccessful()) {
                                String image_download_path = Objects.requireNonNull(Objects.requireNonNull(taskSnapshot.getResult().getMetadata()).getReference()).getDownloadUrl().toString();
                                reference.child(user.getUid()).child(IMAGE_URL).setValue(image_download_path).addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        storageReference.child(user.getUid() + ".jpg").getDownloadUrl().addOnSuccessListener(uri -> reference.child(user.getUid()).child(IMAGE_URL).setValue(uri.toString())).addOnFailureListener(e -> reference.child(user.getUid()).child(IMAGE_URL).setValue(IMAGE_URL));
                                        dialog.dismiss();
                                        reference.child(user.getUid()).child(CODE).setValue(code);
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.invite_code_successful), Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(InviteCodeActivity.this, LoginActivity.class);
                                        intent.putExtra("email", email);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            }
                        });
                    } else {
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.invite_code_failed), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}