package com.furkan.locateyourfriends;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class UserLocationMainActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private AppBarConfiguration mAppBarConfiguration;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference reference;
    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest request;

    private String user_name, user_surname, user_uid;
    private TextView tv_username, tv_user_email; private ImageView iv_user_image;
    private LatLng latLngUser; private LatLng mapDefault = new LatLng(41.0049823, 28.7319909);
    private NavigationView navigationView;

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
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow).setDrawerLayout(drawer).build();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        auth = FirebaseAuth.getInstance();user = auth.getCurrentUser();user_uid = auth.getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(user_uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    user_name = dataSnapshot.child("name").getValue(String.class);
                    user_surname = dataSnapshot.child("surname").getValue(String.class);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        reference.child(user_uid).child("isSharing").setValue(true);
        currentUserInformation(reference);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveToCurrentLocation(latLngUser);
            }
        });

    }

    @Override
    protected void onDestroy() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Users");
        dbRef.child(user_uid).child("isSharing").setValue(false);
        super.onDestroy();
        finish();
    }

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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        request = new LocationRequest().create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(5000);
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
            Toast.makeText(getApplicationContext(), "Konumunuz bulunamadı.", Toast.LENGTH_LONG).show();
        } else {
            double latTemp = location.getLatitude(); double lngTemp = location.getLongitude();
            reference.child(user_uid).child("lat").setValue(latTemp); reference.child(user_uid).child("lng").setValue(lngTemp);

            mMap.clear();
            latLngUser = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .icon(bitmapDescriptorFromVector(this, R.drawable.my_marker))
                    .position(latLngUser)
                    .title(user_name + " " + user_surname + " şu an burada."));
            retrieveOtherUsersLocations(mMap);
            setMenuItem(reference, navigationView);

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(latLngUser);
            LatLngBounds bounds = builder.build();


        }
    }

    // Brings user closer to current location.
    private void moveToCurrentLocation(LatLng currentLocation) {
        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawers();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
    }

    // Copies UserUID to clipboard.
    public void getMyInviteCode() {
        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawers();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Davet Kodu", user.getUid());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getApplicationContext(), "Kodunuz kopyalandı.", Toast.LENGTH_SHORT).show();
    }

    // Shares instant location on WhatsApp.
    public void shareOnWhatsApp() {
        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawers();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "Şu an buradayım: " + "https://www.google.com/maps/@" + latLngUser.latitude + "," + latLngUser.longitude + ",17z");
        intent.setPackage("com.whatsapp");
        startActivity(intent);
    }

    //Sign out.
    public void signOut() {
        if (user != null) {
            reference.child(user_uid).child("isSharing").setValue(false);
            auth.signOut();
            startActivity(new Intent(UserLocationMainActivity.this, WelcomeActivity.class));
            finish();
        }
    }

    // Goes to Location Settings
    public void locationSettings(MenuItem m) {
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawers();
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    // Fetches other users locations. (need to optimize)
    private void retrieveOtherUsersLocations(final GoogleMap mMap) {
        Query query = reference.orderByChild("isSharing").equalTo(true);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "Users" node with all children with isSharing = true.
                    for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                            final LatLng latLngTemp = new LatLng(ds.child("lat").getValue(Double.class), ds.child("lng").getValue(Double.class));
                            mMap.addMarker(new MarkerOptions()
                                    .icon(bitmapDescriptorFromVector(UserLocationMainActivity.this, R.drawable.friends_marker))
                                    .position(latLngTemp)
                                    .title(ds.child("name").getValue(String.class) +  " " + ds.child("surname").getValue(String.class) + " - Uzaklık: " + distanceCalculator(latLngTemp, latLngUser)));
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    // Fetches information of current user.
    private void currentUserInformation(DatabaseReference reference){
        //Retrieving user information from database.
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tv_username.setText(dataSnapshot.child(user_uid).child("name").getValue(String.class)  + " " + dataSnapshot.child(user_uid).child("surname").getValue(String.class));
                tv_user_email.setText(dataSnapshot.child(user_uid).child("email").getValue(String.class));
                Picasso.get().load(dataSnapshot.child(user_uid).child("imageUrl").getValue(String.class)).into(iv_user_image);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //Changes NavigationView - SideMenu items.
    private void setMenuItem(DatabaseReference refMenuItem, final NavigationView navView) {
        Menu menu = navView.getMenu();
        menu.clear();
        menu.add(R.string.my_location).setIcon(R.drawable.my_location).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) { moveToCurrentLocation(latLngUser);return true; }});
        menu.add(R.string.get_invite_code).setIcon(R.drawable.copy_clipboard).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) { getMyInviteCode();return true; }});
        menu.add(R.string.share_on_whatsApp).setIcon(R.drawable.share_on_whatsapp).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) { shareOnWhatsApp();return true; }});
        menu.add(R.string.sign_out).setIcon(R.drawable.sign_out).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) { signOut();return true; }});

        final Menu sideMenu = menu.addSubMenu("Çevrimiçi Kullanıcılar");
        Query query = refMenuItem.orderByChild("isSharing").equalTo(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(final DataSnapshot ds : dataSnapshot.getChildren()){
                        sideMenu.add(ds.child("name").getValue(String.class) + " " + ds.child("surname").getValue(String.class))
                                .setIcon(R.drawable.explore).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        LatLng latLngTemp = new LatLng(ds.child("lat").getValue(Double.class), ds.child("lng").getValue(Double.class));
                                        moveToCurrentLocation(latLngTemp);
                                        onNavigationItemSelected(item);
                                        return true;
                                    }
                                });
                    }
                } else {
                    sideMenu.add("Şu anda hiç kimse çevrimiçi değil.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //Calculates distance between two GPS coordinates.
    private String distanceCalculator(LatLng b, LatLng a){
        double distance = SphericalUtil.computeDistanceBetween(b,a);
        if(distance < 1000) return distance + " metre.";
        else return Math.round(distance / 1000) + " km.";
    }

    //Action when user pressed return button.
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserLocationMainActivity.this);
        builder.setTitle("Uyarı");
        builder.setIcon(R.drawable.sign_out);
        builder.setMessage("Uygulamadan çıkmak istiyor musunuz?");
        builder.setNegativeButton("Hayır", null);
        builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        reference.child(user_uid).child("isSharing").setValue(false);
                        UserLocationMainActivity.this.finish();
                        System.exit(0); }});
        builder.show();
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable background = ContextCompat.getDrawable(context, R.drawable.ic_map_pin_filled_blue_48dp);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

}