/**
 * @author Furkan Kırmızıoğlu on 2020
 * @project Locate Your Friends
 */

package com.furkan.locateyourfriends;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class UserLocationMainActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener, LocationListener {

    private AppBarConfiguration mAppBarConfiguration;

    public static final String CHANNEL_1_ID = "channel1", USERS = "Users", CODE = "code", IS_SHARING = "isSharing", LAT = "lat", LNG = "lng", FRIENDS = "friends";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 15;
    final List<User> userArrayList = new ArrayList<>();
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private GoogleMap mMap;
    private DatabaseReference databaseReference;
    private LatLng latLngDefault = new LatLng(41.0049823, 28.7319909);
    private boolean locationPermissionGranted = true;
    private Location lastKnownLocation;

    private TextView tv_username, tv_user_email;
    private ImageView iv_user_image;
    private NavigationView navigationView;
    private FusedLocationProviderClient fusedLocationProviderClient;

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
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child(USERS);
        getCurrentUser(databaseReference);
        if (!Utility.checkInternetConnection(this, getResources().getString(R.string.login_alert_text)))
            return;

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        setMenuItems();
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        // Prompt the user for permission.
        getLocationPermission();
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
    }

    private void getLocationPermission() {

        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
        }
    }

    private void getDeviceLocation() {

        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(latLngDefault, 7));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (lastKnownLocation == null) {
            Toast.makeText(getApplicationContext(), R.string.user_location_error, Toast.LENGTH_LONG).show();
        } else {
            databaseReference.child(firebaseUser.getUid()).child(LAT).setValue(lastKnownLocation.getLatitude());
            databaseReference.child(firebaseUser.getUid()).child(LNG).setValue(lastKnownLocation.getLongitude());
            getFriendLocation();
        }
    }

    //Calculates distance between two GPS coordinates.
    private String calculateDistance(LatLng b, LatLng a) {
        double distance = SphericalUtil.computeDistanceBetween(b, a);
        if (distance < 1000)
            return Math.round(distance) + " " + getResources().getString(R.string.user_location_meter);
        else
            return Math.round(distance / 1000) + " " + getResources().getString(R.string.user_location_km);
    }


    // ------MENU ITEMS & ACTIONS------

    // Fetches information of current user.
    private void getCurrentUser(DatabaseReference reference) {
        //Retrieving user information from database.
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.child(firebaseUser.getUid()).getValue(User.class);
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
    private void setMenuItems() {
        Menu menu = navigationView.getMenu();
        menu.clear();
        addMyItems(menu);
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

    // Copies UserUID to clipboard.
    public void getMyInviteCode() {
        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawers();
        databaseReference.child(firebaseUser.getUid()).child(CODE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userCode = dataSnapshot.getValue(String.class);
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Invite Code", getResources().getString(R.string.user_invite_code_prefix) + " " + userCode);
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
        LatLng latLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.user_gps_whatsApp_text) + latLng.latitude + "," + latLng.longitude + ",17z");
        intent.setPackage("com.whatsapp");
        startActivity(intent);
    }

    // Sign out.
    public void signOut() {
        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawers();
        if (firebaseUser != null) {
            databaseReference.child(firebaseUser.getUid()).child(IS_SHARING).setValue(false);
            firebaseAuth.signOut();
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
    public void sharingSettings(MenuItem item) {
        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawers();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getResources().getString(R.string.user_sharing_settings_alert_title))
                .setCancelable(false)
                .setNegativeButton(getResources().getString(R.string.user_sharing_disable), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        databaseReference.child(firebaseUser.getUid()).child(IS_SHARING).setValue(false);
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.user_sharing_disabled), Toast.LENGTH_SHORT);
                    }
                })
                .setNeutralButton(getResources().getString(R.string.user_sharing_nothing), null)
                .setPositiveButton(getResources().getString(R.string.user_sharing_enable), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        databaseReference.child(firebaseUser.getUid()).child(IS_SHARING).setValue(true);
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.user_sharing_enabled), Toast.LENGTH_SHORT);
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }


    // ------SERVICES RUNNING ON ACTIVITY------

    //Action when user pressed return button.
    public void onBackPressed() {
        Utility.exitAlert(this, databaseReference, firebaseUser.getUid());
    }

    public void createNotificationChannels(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(CHANNEL_1_ID, "Channel 1", NotificationManager.IMPORTANCE_HIGH);
            channel1.setDescription("Users which are closer to me");
            NotificationManager manager = activity.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }
    }

    private void sendChannelOne(String name_surname, String phoneNumber) {
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
    private void markLocation(User user) {
        LatLng position = new LatLng(user.lat, user.lng);
        mMap.addMarker(new MarkerOptions()
                .icon(Utility.bitmapDescriptorFromVector(UserLocationMainActivity.this, R.drawable.friends_marker))
                .position(position)
                .title(user.name + " " + user.surname + " " + getResources().getString(R.string.user_location_distance) + " " + calculateDistance(position, new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()))));
    }

    private void getFriendLocation() {
        getFriendList();
        for (int i = 0; i < userArrayList.size(); i++) {
            Query query = databaseReference.orderByChild(CODE).equalTo(userArrayList.get(i).code);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            User friend = ds.getValue(User.class);
                            LatLng position = new LatLng(friend.lat, friend.lng);
                            if (friend.isSharing) {
                                markLocation(friend);
                                long distance = Math.round(SphericalUtil.computeDistanceBetween(position, new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude())));
                                while (distance < 100 && distance > 90) {
                                    sendChannelOne(friend.name + " " + friend.surname, friend.phoneNumber);
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

    private void getFriendList() {
        userArrayList.clear();
        Query query = databaseReference.child(firebaseUser.getUid()).child(FRIENDS);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        User friend = ds.getValue(User.class);
                        userArrayList.add(friend);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /*// Brings user closer to chosen location.
    private void moveToCurrentLocation(LatLng currentLocation) {
        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawers();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
    }*/

}