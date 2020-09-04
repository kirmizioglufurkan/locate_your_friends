/**
 * @author Furkan Kırmızıoğlu on 2020
 * @project Locate Your Friends
 */

package com.furkan.locateyourfriends;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static com.furkan.locateyourfriends.Utility.CODE;
import static com.furkan.locateyourfriends.Utility.IMAGE_URL;

public class InviteCodeActivity extends AppCompatActivity implements View.OnClickListener {

    private String username;
    private String email;
    private String password;
    private String name;
    private String surname;
    private String phoneNumber;
    private String date;
    private String code;
    private String userId;

    private Uri imageUri;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference reference;
    private StorageReference storageReference;
    private ProgressDialog dialog;
    private PinView pwInviteCode;
    private Button btnInviteCode;
    private Utility utility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_code);
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        storageReference = FirebaseStorage.getInstance().getReference().child("User_Images");
        pwInviteCode = findViewById(R.id.pw_invite_code);
        btnInviteCode = findViewById(R.id.btn_invite_code);
        btnInviteCode.setOnClickListener(this);
        dialog = new ProgressDialog(this);
        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("username");
            email = intent.getStringExtra("email");
            password = intent.getStringExtra("password");
            name = intent.getStringExtra("name");
            surname = intent.getStringExtra("surname");
            phoneNumber = intent.getStringExtra("phoneNumber");
            date = intent.getStringExtra("date");
            code = intent.getStringExtra("code");
            imageUri = intent.getParcelableExtra("imageUri");
            pwInviteCode.setText(code);
        }

        utility = new Utility();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_invite_code:
                checkAndRegister();
                break;
        }
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
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //Insert values in realtime database
                    User registerUser = new User(username, email, password, name, surname, phoneNumber, code, false, 0, 0, "na");
                    user = auth.getCurrentUser();
                    userId = user.getUid();
                    reference.child(userId).setValue(registerUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //Storage profile image to firebase
                                StorageReference sr = storageReference.child(user.getUid() + ".jpg");
                                sr.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> taskSnapshot) {
                                        if (taskSnapshot.isSuccessful()) {
                                            String image_download_path = taskSnapshot.getResult().getMetadata().getReference().getDownloadUrl().toString();
                                            reference.child(user.getUid()).child(IMAGE_URL).setValue(image_download_path).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        storageReference.child(user.getUid() + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri uri) {
                                                                reference.child(user.getUid()).child(IMAGE_URL).setValue(uri.toString());
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                reference.child(user.getUid()).child(IMAGE_URL).setValue(IMAGE_URL);
                                                            }
                                                        });
                                                        dialog.dismiss();
                                                        reference.child(user.getUid()).child(CODE).setValue(code);
                                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.invite_code_successful), Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(InviteCodeActivity.this, LoginActivity.class);
                                                        intent.putExtra("email", email);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            } else {
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.invite_code_failed), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

}