package com.furkan.locateyourfriends;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class UserLocationMainActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private AppBarConfiguration mAppBarConfiguration;

    public static final String CHANNEL_1_ID = "channel1", USERS = "Users", CODE = "code", IS_SHARING = "isSharing", LAT = "lat", LNG = "lng", FRIENDS = "friends";
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference reference;
    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest request;
    private String user_uid, user_code;
    private TextView tv_username, tv_user_email;
    private ImageView iv_user_image;
    private LatLng latLngUser;
    private LatLng mapDefault = new LatLng(41.0049823, 28.7319909);
    private NavigationView navigationView;
    final List<Friendship> friendshipArrayList = new ArrayList<>();
    private Utility utility = new Utility();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_location_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        tv_username = header.findViewById(R.id.tv_user_name);
        tv_user_email = header.findViewById(R.id.tv_user_email);
        iv_user_image = header.findViewById(R.id.iv_userimage);
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow).setDrawerLayout(drawer).build();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        createNotificationChannels(UserLocationMainActivity.this);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        user_uid = auth.getUid();
        reference = FirebaseDatabase.getInstance().getReference().child(USERS);
        getCurrentUser(reference);
        if (!utility.checkInternetConnection(this, getResources().getString(R.string.login_alert_text)))
            return;
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!utility.checkGPSConnection(UserLocationMainActivity.this)) return;
                moveToCurrentLocation(latLngUser);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    // ------NAVIGATION VIEW ITEMS------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user_location_main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // ------MAP & LOCATION SERVICES------

    //Gets location.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        client = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        client.connect();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapDefault, 7));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(7), 2000, null);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        request = new LocationRequest().create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(3000);
        LocationServices.FusedLocationApi.requestLocationUpdates(client, request, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            Toast.makeText(getApplicationContext(), R.string.user_location_error, Toast.LENGTH_LONG).show();
        } else {
            reference.child(user_uid).child(LAT).setValue(location.getLatitude());
            reference.child(user_uid).child(LNG).setValue(location.getLongitude());
            latLngUser = new LatLng(location.getLatitude(), location.getLongitude());
            setMenuItems(navigationView);
            mMap.clear();
            markUserLocation(mMap);
            getFriendsLocations(mMap, friendshipArrayList);
            getFriendsList();
        }
    }

    // Brings user closer to chosen location.
    private void moveToCurrentLocation(LatLng currentLocation) {
        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawers();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
    }

    //Calculates distance between two GPS coordinates.
    private String calculateDistance(LatLng b, LatLng a) {
        double distance = SphericalUtil.computeDistanceBetween(b, a);
        if (distance < 1000)
            return Math.round(distance) + " " + getResources().getString(R.string.user_location_meter);
        else
            return Math.round(distance / 1000) + " " + getResources().getString(R.string.user_location_km);
    }

    //Marker of user location.
    private void markUserLocation(GoogleMap maps) {
        mMap.addMarker(new MarkerOptions()
                .icon(utility.bitmapDescriptorFromVector(this, R.drawable.my_location))
                .position(latLngUser)
                .title(getResources().getString(R.string.user_location_here)));
    }


    // ------MENU ITEMS & ACTIONS------

    // Fetches information of current user.
    private void getCurrentUser(DatabaseReference reference) {
        //Retrieving user information from database.
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.child(user_uid).getValue(User.class);
                    tv_username.setText(user.name + " " + user.surname);
                    tv_user_email.setText(user.email);
                    Picasso.get().load(user.imageUrl).into(iv_user_image);
                } else {
                    tv_username.setText(getResources().getString(R.string.error));
                    tv_user_email.setText(getResources().getString(R.string.error));
                    Picasso.get().load(R.drawable.upload_profile).into(iv_user_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //Changes NavigationView - SideMenu items and its actions.
    private void setMenuItems(final NavigationView navView) {
        Menu menu = navView.getMenu();
        menu.clear();
        addMyItems(menu);
        addFriendsItems(menu);
    }

    private void addMyItems(Menu menu) {
        final SubMenu myMenu = menu.addSubMenu(R.string.user_me);
        myMenu.add(getResources().getString(R.string.add_friend_text)).setIcon(R.drawable.add_friend).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(UserLocationMainActivity.this, AddFriendActivity.class);
                startActivity(intent);
                return true;
            }
        });
        myMenu.add(getResources().getString(R.string.user_get_invite_code)).setIcon(R.drawable.copy_clipboard).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                getMyInviteCode();
                return true;
            }
        });
        myMenu.add(getResources().getString(R.string.user_gps_whatsApp)).setIcon(R.drawable.share_on_whatsapp).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                shareOnWhatsApp();
                return true;
            }
        });

        myMenu.add(getResources().getString(R.string.user_sign_out)).setIcon(R.drawable.sign_out).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                signOut();
                return true;
            }
        });
    }

    private void addFriendsItems(Menu menu) {
        final Menu sideMenu = menu.addSubMenu(getResources().getString(R.string.user_friends));
        sideMenu.clear();
        if (friendshipArrayList.isEmpty()) {
            sideMenu.addSubMenu(getResources().getString(R.string.user_friends_null_error));
            return;
        }
        for (int i = 0; i < friendshipArrayList.size(); i++) {
            Query query = reference.orderByChild(CODE).equalTo(friendshipArrayList.get(i).code);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            final User itemUser = ds.getValue(User.class);
                            sideMenu.add(itemUser.name + " " + itemUser.surname)
                                    .setIcon(R.drawable.explore)
                                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                        @Override
                                        public boolean onMenuItemClick(MenuItem item) {
                                            if (itemUser.isSharing) {
                                                LatLng location = new LatLng(itemUser.lat, itemUser.lng);
                                                moveToCurrentLocation(location);
                                                onNavigationItemSelected(item);
                                            } else
                                                Toast.makeText(getApplicationContext(), itemUser.name + " " + itemUser.surname + " " + getResources().getString(R.string.user_friend_location_null_error), Toast.LENGTH_SHORT).show();
                                            return true;
                                        }
                                    });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    // Copies UserUID to clipboard.
    public void getMyInviteCode() {
        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawers();
        reference.child(user_uid).child(CODE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    user_code = dataSnapshot.getValue(String.class);
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Invite Code", getResources().getString(R.string.user_invite_code_prefix) + " " + user_code);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.user_invite_code_copied), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    // Shares instant location on WhatsApp.
    public void shareOnWhatsApp() {
        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawers();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.user_gps_whatsApp_text) + latLngUser.latitude + "," + latLngUser.longitude + ",17z");
        intent.setPackage("com.whatsapp");
        startActivity(intent);
    }

    // Sign out.
    public void signOut() {
        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawers();
        if (user != null) {
            reference.child(user_uid).child(IS_SHARING).setValue(false);
            auth.signOut();
            startActivity(new Intent(UserLocationMainActivity.this, WelcomeActivity.class));
            finish();
        }
    }

    // Location Settings
    public void locationSettings(MenuItem m) {
        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawers();
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    // Sharing settings.
    public void sharing_settings(MenuItem item) {
        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawers();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getResources().getString(R.string.user_sharing_settings_alert_title))
                .setCancelable(false)
                .setNegativeButton(getResources().getString(R.string.user_sharing_disable), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        reference.child(user_uid).child(IS_SHARING).setValue(false);
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.user_sharing_disabled), Toast.LENGTH_SHORT);
                    }
                })
                .setNeutralButton(getResources().getString(R.string.user_sharing_nothing), null)
                .setPositiveButton(getResources().getString(R.string.user_sharing_enable), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        reference.child(user_uid).child(IS_SHARING).setValue(true);
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.user_sharing_enabled), Toast.LENGTH_SHORT);
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    // ------SERVICES RUNNING ON ACTIVITY------

    //Action when user pressed return button.
    public void onBackPressed() {
        utility.exitAlert(this, reference, user_uid);
    }

    public void createNotificationChannels(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(CHANNEL_1_ID, "Channel 1", NotificationManager.IMPORTANCE_HIGH);
            channel1.setDescription("Users which are closer to me");
            NotificationManager manager = activity.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }
    }

    private void sendToChannel1(String name_surname, String phoneNumber) {
        Intent phoneCall = new Intent(Intent.ACTION_CALL);
        phoneCall.setData(Uri.parse("tel:" + phoneNumber));
        PendingIntent phoneCallIntent = PendingIntent.getActivity(this, 0, phoneCall, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder myNotification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.explore)
                .setContentTitle(name_surname + " " + getResources().getString(R.string.user_notification_title))
                .setContentText(getResources().getString(R.string.user_notification_text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .addAction(R.drawable.phone, getResources().getString(R.string.user_notification_call), phoneCallIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, myNotification.build());
    }

    // ------ SURGERY ROOM------
    private void markLocations(GoogleMap maps, User user) {
        LatLng position = new LatLng(user.lat, user.lng);
        maps.addMarker(new MarkerOptions()
                .icon(utility.bitmapDescriptorFromVector(UserLocationMainActivity.this, R.drawable.friends_marker))
                .position(position)
                .title(user.name + " " + user.surname + " " + getResources().getString(R.string.user_location_distance) + " " + calculateDistance(position, latLngUser)));
    }

    private void getFriendsLocations(final GoogleMap maps, List<Friendship> list) {
        for (int i = 0; i < list.size(); i++) {
            Query query = reference.orderByChild(CODE).equalTo(list.get(i).code);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            User locationUser = ds.getValue(User.class);
                            LatLng position = new LatLng(locationUser.lat, locationUser.lng);
                            if (locationUser.isSharing) {
                                markLocations(maps, locationUser);
                                long distance = Math.round(SphericalUtil.computeDistanceBetween(position, latLngUser));
                                while (distance < 100 && distance > 90) {
                                    sendToChannel1(locationUser.name + " " + locationUser.surname, locationUser.phoneNumber);
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }

    private void getFriendsList() {
        friendshipArrayList.clear();
        Query query = reference.child(user_uid).child(FRIENDS);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Friendship friendship = ds.getValue(Friendship.class);
                        friendshipArrayList.add(friendship);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    // ------ SURGERY ROOM ENDS------
}