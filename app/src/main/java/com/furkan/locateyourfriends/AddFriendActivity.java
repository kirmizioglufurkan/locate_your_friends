package com.furkan.locateyourfriends;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.chaos.view.PinView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddFriendActivity extends AppCompatActivity implements View.OnClickListener {
    PinView pwInviteCode;
    ImageView imgBack;
    Button btnAddFriend;
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    User user = new User();
    Friendship friendship = new Friendship();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        btnAddFriend = findViewById(R.id.btn_add_friend);
        btnAddFriend.setOnClickListener(this);
        pwInviteCode = findViewById(R.id.pw_add_friend_code);
        imgBack = findViewById(R.id.img_add_friend_back);
        imgBack.setOnClickListener(this);
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_friend: {
                if (!submit()) return;
                setFriend();
            }
            case R.id.img_add_friend_back:
                goToUserLocationMainActivity();
                break;
        }
    }

    private void setFriend() {
        String inviteCode = pwInviteCode.getText().toString();
        reference.orderByChild("code").equalTo(inviteCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    user = ds.getValue(User.class);
                    friendship.setCode(user.code);
                    friendship.setName(user.name);
                    friendship.setSurname(user.surname);
                    reference.child(firebaseUser.getUid()).child("friends").push().setValue(friendship);
                    Toast.makeText(getApplicationContext(), friendship.name + " " + friendship.surname + " artık arkadaşınız :)", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private boolean submit() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connectivityManager.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected())
            return true;
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(AddFriendActivity.this);
            builder.setCancelable(false);
            builder.setIcon(R.drawable.wifi_off);
            builder.setTitle(getResources().getString(R.string.add_friend_alert_title));
            builder.setMessage(getResources().getString(R.string.add_friend_alert_text));
            builder.setNegativeButton(getResources().getString(R.string.add_friend_alert_negative_text), null);
            builder.setPositiveButton(getResources().getString(R.string.add_friend_alert_positive_text), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    AddFriendActivity.this.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            });
            builder.show();
            return false;
        }
    }

    private void goToUserLocationMainActivity() {
        Animation animation_bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
        imgBack.startAnimation(animation_bounce);
        startActivity(new Intent(AddFriendActivity.this, UserLocationMainActivity.class));
        finish();
    }
}
