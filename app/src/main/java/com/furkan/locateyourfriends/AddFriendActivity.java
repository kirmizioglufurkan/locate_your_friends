/*
    @author Furkan Kırmızıoğlu
*/

package com.furkan.locateyourfriends;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chaos.view.PinView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class AddFriendActivity extends AppCompatActivity {
    private PinView pwInviteCode;
    private ImageView imgBack;
    private Button btnAddFriend;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private User friend = new User();
    private Utility utility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        btnAddFriend = findViewById(R.id.btn_add_friend);
        pwInviteCode = findViewById(R.id.pw_add_friend_code);
        imgBack = findViewById(R.id.img_add_friend_back);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        utility = new Utility();

    }

    @Override
    protected void onResume() {
        super.onResume();
        btnAddFriend.setOnClickListener(v -> {
            String inviteCode = Objects.requireNonNull(pwInviteCode.getText()).toString();
            if (utility.checkInternetConnection(AddFriendActivity.this, getResources().getString(R.string.add_friend_alert_text)))
                return;
            if (!validationCheck(inviteCode)) return;
            setFriend(inviteCode);
        });

        imgBack.setOnClickListener(v -> {
            goToUserLocationMainActivity();
            finish();
        });

    }

    private void setFriend(String inviteCode) {
        reference.orderByChild("code").equalTo(inviteCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        friend = ds.getValue(User.class);
                        reference.child(firebaseUser.getUid()).child("friends").push().setValue(friend);
                        Toast.makeText(getApplicationContext(), friend.getName() + " " + friend.getSurname() + getResources().getString(R.string.add_friend_successful), Toast.LENGTH_SHORT).show();
                    }
                } else
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.add_friend_failed), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        Objects.requireNonNull(pwInviteCode.getText()).clear();
    }

    private void goToUserLocationMainActivity() {
        Animation animation_bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
        imgBack.startAnimation(animation_bounce);
        startActivity(new Intent(AddFriendActivity.this, UserLocationMainActivity.class));
        finish();
    }

    private boolean validationCheck(String codeText) {
        if (codeText.isEmpty()) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.add_friend_null_error), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

}
