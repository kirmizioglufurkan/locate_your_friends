/*
    @author Furkan Kırmızıoğlu
*/

package com.furkan.locateyourfriends;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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

import static com.furkan.locateyourfriends.Utility.CHANNEL_1_ID;
import static com.furkan.locateyourfriends.Utility.CODE;
import static com.furkan.locateyourfriends.Utility.FRIENDS;
import static com.furkan.locateyourfriends.Utility.IS_SHARING;
import static com.furkan.locateyourfriends.Utility.LAT;
import static com.furkan.locateyourfriends.Utility.LNG;
import static com.furkan.locateyourfriends.Utility.USERS;

public class UserLocationMainActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener, LocationListener {

    private AppBarConfiguration mAppBarConfiguration;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 15;

    private final List<Friendship> friendshipArrayList = new ArrayList<>();
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private GoogleMap mMap;
    private DatabaseReference databaseReference;
    private final LatLng latLngDefault = new LatLng(41.0049823, 28.7319909);
    private boolean locationPermissionGranted = true;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;

    private User loggedUser;

    private TextView tv_username;
    private TextView tv_user_email;
    private ImageView iv_user_image;
    private NavigationView navigationView;

    private final Utility utility = new Utility();

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
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow).setOpenableLayout(drawer).build();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(UserLocationMainActivity.this);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        createNotificationChannels(UserLocationMainActivity.this);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child(USERS);
        getCurrentUser(databaseReference);
        if (!utility.checkInternetConnection(this, getResources().getString(R.string.login_alert_text)))
            return;

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        loggedUser = new User();
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

    private void getDeviceLocation() {

        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, task -> {
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
                });
            }
        } catch (SecurityException e) {
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

    @Override
    public void onLocationChanged(Location location) {
        if (lastKnownLocation == null) {
            Toast.makeText(getApplicationContext(), R.string.user_location_error, Toast.LENGTH_LONG).show();
        } else {
            loggedUser.setLat(lastKnownLocation.getLatitude());
            loggedUser.setLng(lastKnownLocation.getLongitude());
            databaseReference.child(firebaseUser.getUid()).child(LAT).setValue(loggedUser.getLat());
            databaseReference.child(firebaseUser.getUid()).child(LNG).setValue(loggedUser.getLng());
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
                    loggedUser = dataSnapshot.child(firebaseUser.getUid()).getValue(User.class);
                    tv_username.setText(loggedUser.getName() + " " + loggedUser.getSurname());
                    tv_user_email.setText(loggedUser.getEmail());
                    Picasso.get().load(loggedUser.getImageUrl()).into(iv_user_image);
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

        final SubMenu sideMenu = menu.addSubMenu(getResources().getString(R.string.user_friends));
        addFriendsItems(sideMenu);
    }

    private void addMyItems(Menu menu) {
        final SubMenu myMenu = menu.addSubMenu(R.string.user_me);
        myMenu.add(getResources().getString(R.string.add_friend_text)).setIcon(R.drawable.add_friend).setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(UserLocationMainActivity.this, AddFriendActivity.class);
            startActivity(intent);
            return true;
        });
        myMenu.add(getResources().getString(R.string.user_get_invite_code)).setIcon(R.drawable.copy_clipboard).setOnMenuItemClickListener(item -> {
            getMyInviteCode();
            return true;
        });
        myMenu.add(getResources().getString(R.string.user_gps_whatsApp)).setIcon(R.drawable.share_on_whatsapp).setOnMenuItemClickListener(item -> {
            shareOnWhatsApp();
            return true;
        });

        myMenu.add(getResources().getString(R.string.user_sign_out)).setIcon(R.drawable.sign_out).setOnMenuItemClickListener(item -> {
            signOut();
            return true;
        });
    }

    private void addFriendsItems(final SubMenu sideMenu) {
        if (friendshipArrayList.isEmpty()) {
            sideMenu.addSubMenu(getResources().getString(R.string.user_friends_null_error));
            return;
        }
        sideMenu.clear();
        for (int i = 0; i < friendshipArrayList.size(); i++) {
            Query query = databaseReference.orderByChild(CODE).equalTo(friendshipArrayList.get(i).getCode());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            final User itemUser = ds.getValue(User.class);
                            sideMenu.add(itemUser.getName() + " " + itemUser.getSurname())
                                    .setIcon(R.drawable.explore)
                                    .setOnMenuItemClickListener(item -> {
                                        if (itemUser.isSharing()) {
                                            LatLng location = new LatLng(itemUser.getLat(), itemUser.getLng());
                                            moveToCurrentLocation(location);
                                            onNavigationItemSelected(item);
                                        } else
                                            Toast.makeText(getApplicationContext(), itemUser.getName() + " " + itemUser.getSurname() + " " + getResources().getString(R.string.user_friend_location_null_error), Toast.LENGTH_SHORT).show();
                                        return true;
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

    // Copies InviteCode to clipboard.
    public void getMyInviteCode() {
        try {
            DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
            mDrawerLayout.closeDrawers();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Invite Code", getResources().getString(R.string.user_invite_code_prefix) + " " + loggedUser.getCode());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.user_invite_code_copied), Toast.LENGTH_SHORT).show();
        } catch (NullPointerException exception) {
            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    // Shares instant location on WhatsApp.
    public void shareOnWhatsApp() {
        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawers();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.user_gps_whatsApp_text) + loggedUser.getLat() + "," + loggedUser.getLng() + ",17z");
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
                .setNegativeButton(getResources().getString(R.string.user_sharing_disable), (dialog, which) -> {
                    databaseReference.child(firebaseUser.getUid()).child(IS_SHARING).setValue(false);
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.user_sharing_disabled), Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton(getResources().getString(R.string.user_sharing_nothing), null)
                .setPositiveButton(getResources().getString(R.string.user_sharing_enable), (dialog, id) -> {
                    databaseReference.child(firebaseUser.getUid()).child(IS_SHARING).setValue(true);
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.user_sharing_enabled), Toast.LENGTH_SHORT).show();
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }


    // ------RUNNING SERVICES ON ACTIVITY------

    //Action when user pressed return button.
    public void onBackPressed() {
        utility.exitAlert(this, databaseReference, firebaseUser.getUid());
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

    private void getFriendLocation() {
        getFriendList();
        for (int i = 0; i < friendshipArrayList.size(); i++) {
            Query query = databaseReference.orderByChild(CODE).equalTo(friendshipArrayList.get(i).getCode());
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            User friend = ds.getValue(User.class);
                            LatLng position = new LatLng(friend.getLat(), friend.getLng());
                            if (friend.isSharing()) {
                                markLocation(friend);
                                long distance = Math.round(SphericalUtil.computeDistanceBetween(position, new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude())));
                                while (distance < 100 && distance > 90) {
                                    sendChannelOne(friend.getName() + " " + friend.getSurname(), friend.getPhoneNumber());
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

    private void markLocation(User user) {
        LatLng position = new LatLng(user.getLat(), user.getLng());
        mMap.addMarker(new MarkerOptions()
                .icon(utility.bitmapDescriptorFromVector(UserLocationMainActivity.this, R.drawable.friends_marker))
                .position(position)
                .title(user.getName() + " " + user.getSurname() + " " +
                        getResources().getString(R.string.user_location_distance) + " " +
                        calculateDistance(position, new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()))));
    }

    private void getFriendList() {
        friendshipArrayList.clear();
        Query query = databaseReference.child(firebaseUser.getUid()).child(FRIENDS);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Friendship friend = ds.getValue(Friendship.class);
                        friendshipArrayList.add(friend);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

}