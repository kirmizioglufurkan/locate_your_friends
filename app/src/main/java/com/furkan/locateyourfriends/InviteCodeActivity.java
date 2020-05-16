package com.furkan.locateyourfriends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class InviteCodeActivity extends AppCompatActivity implements View.OnClickListener {

    private String username, email, password, name, surname, phoneNumber, date, isSharing, code, userId; private Uri imageUri;
    private FirebaseAuth auth; private FirebaseUser user; private DatabaseReference reference; private StorageReference storageReference;
    private ProgressDialog dialog;
    private TextView tvInviteCode; private Button btnInviteCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_code);
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        storageReference = FirebaseStorage.getInstance().getReference().child("User_Images");
        tvInviteCode = findViewById(R.id.tv_invite_code_code_text);
        btnInviteCode = findViewById(R.id.btn_invite_code);
        btnInviteCode.setOnClickListener(this);
        dialog = new ProgressDialog(this);
        Intent intent = getIntent();
        if(intent != null) {
            username = intent.getStringExtra("username");
            email = intent.getStringExtra("email");
            password = intent.getStringExtra("password");
            name = intent.getStringExtra("name");
            surname = intent.getStringExtra("surname");
            phoneNumber = intent.getStringExtra("phoneNumber");
            date = intent.getStringExtra("date");
            isSharing = intent.getStringExtra("isSharing");
            code = intent.getStringExtra("code");
            imageUri = intent.getParcelableExtra("imageUri");
            tvInviteCode.setText(code);
        }
    }

    private void registerUser(View v) {
        dialog.setMessage("Kullanıcı bilgileriniz kaydediliyor...");
        dialog.show();
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            //Insert values in realtime database
                            CreateUser createUser = new CreateUser(username,email,password,name,surname, phoneNumber, code,"false","na","na","na");
                            user = auth.getCurrentUser(); userId = user.getUid();
                            reference.child(userId).setValue(createUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        //Storage images to firebase
                                        StorageReference sr = storageReference.child(user.getUid() + ".jpg");
                                        sr.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> taskSnapshot) {
                                                        if (taskSnapshot.isSuccessful()) {
                                                            String image_download_path = taskSnapshot.getResult().getMetadata().getReference().getDownloadUrl().toString();
                                                            reference.child(user.getUid()).child("imageUrl").setValue(image_download_path).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                reference.child(user.getUid()).child("code").setValue(user.getUid());
                                                                                dialog.dismiss();
                                                                                Toast.makeText(getApplicationContext(), "Başarıyla kaydedildi.", Toast.LENGTH_SHORT).show();
                                                                                Intent intent = new Intent(InviteCodeActivity.this, LoginActivity.class);
                                                                                intent.putExtra("email",email);
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
                                        Toast.makeText(getApplicationContext(),"Kayıt işlemi sırasında bir hata oluştu.",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                }); }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_invite_code: registerUser(v); break;
        }
    }
}
