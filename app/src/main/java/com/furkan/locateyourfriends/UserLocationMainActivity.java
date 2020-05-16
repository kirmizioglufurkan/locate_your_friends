package com.furkan.locateyourfriends;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    private FirebaseAuth auth; private FirebaseUser user; private DatabaseReference reference;
    private GoogleMap mMap; private GoogleApiClient client;
    private LocationRequest request;

    private String user_name, user_surname, user_email, user_uid, user_imageUrl;
    private TextView tv_username,tv_useremail; private ImageView iv_userimage;

    private LatLng latLngStart;
    private LatLng corlu = new LatLng(41.1539589,27.8126231);
    private LatLng muhendislik = new LatLng(41.6334751,26.6217317);
    private LatLng mapDefault = new LatLng(41.0049823,28.7319909);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_location_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        tv_username = header.findViewById(R.id.tv_user_name);
        tv_useremail = header.findViewById(R.id.tv_user_email);
        iv_userimage = header.findViewById(R.id.iv_userimage);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        user_uid = auth.getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");

        //Retrieving user information from database.
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user_name = dataSnapshot.child(user_uid).child("name").getValue(String.class);
                user_surname = dataSnapshot.child(user_uid).child("surname").getValue(String.class);
                user_email = dataSnapshot.child(user_uid).child("email").getValue(String.class);
                user_imageUrl = dataSnapshot.child(user_uid).child("imageUrl").getValue(String.class);

                tv_username.setText(user_name  + " " + user_surname);
                tv_useremail.setText(user_email);
                Picasso.get().load(user_imageUrl).into(iv_userimage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

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

    //Getting location.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        client = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        client.connect();

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapDefault,7));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(7), 2000, null);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        request = new LocationRequest().create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(3000);
        LocationServices.FusedLocationApi.requestLocationUpdates(client,request,this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //Locating user in the Google Maps.
    @Override
    public void onLocationChanged(Location location) {
        if(location == null) {
            Toast.makeText(getApplicationContext(), "Konumunuz bulunamadı.",Toast.LENGTH_LONG).show();
        } else {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(corlu)
                    .title("Cansu Diricanlı şu an burada.")
                    //.icon(vectorToBitmap(R.drawable.friends_marker, Color.parseColor("#D73534")))
            );

            mMap.addMarker(new MarkerOptions().position(muhendislik)
                    .title("Mühendislik Fakültesi şu an burada.")
            //.icon(vectorToBitmap(R.drawable.friends_marker, Color.parseColor("#D73534")))
            );


            reference.child(user_uid).child("lat").setValue(location.getLatitude()).toString();
            reference.child(user_uid).child("lng").setValue(location.getLongitude()).toString();

            latLngStart = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLngStart)
                    .title(user_name + " şu an burada.")
                    //.icon(vectorToBitmap(R.drawable.ic_gps_fixed_black_24dp, Color.parseColor("#D73534")))
            );
        }
    }

    //Zoom to the current location.
    private void moveToCurrentLocation(LatLng currentLocation) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
    }

    //Side menu functions.
    public void goToMyLocation(MenuItem m){
        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawers();
        moveToCurrentLocation(latLngStart);
    }

    public void getMyInviteCode(MenuItem m){
        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawers();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Davet Kodu", user.getUid());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getApplicationContext(),"Kodunuz kopyalandı.", Toast.LENGTH_SHORT).show();
    }

    public void shareOnWhatsapp(MenuItem m) {
        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawers();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,"Şu an buradayım: " + "https://www.google.com/maps/@" + latLngStart.latitude + "," + latLngStart.longitude + ",17z");
        intent.setPackage("com.whatsapp");
        startActivity(intent);
    }

    public void signOut(MenuItem m) {
        if(user != null) {
            auth.signOut();
            startActivity(new Intent(UserLocationMainActivity.this, WelcomeActivity.class));
            finish();
        }
    }

    public void locationSettings(MenuItem m){
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawers();
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    public void goToCansu(MenuItem m){
        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawers();
        moveToCurrentLocation(corlu);
    }

    public void goToFaculty(MenuItem m){
        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawers();
        moveToCurrentLocation(muhendislik);;
    }

}
