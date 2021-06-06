/*
    @author Furkan Kırmızıoğlu
*/

package com.furkan.locateyourfriends;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DatabaseReference;

import static android.content.Context.LOCATION_SERVICE;

public class Utility {

    public static final String CHANNEL_1_ID = "channel1";
    public static final String USERS = "Users";
    public static final String CODE = "code";
    public static final String IS_SHARING = "isSharing";
    public static final String LAT = "lat";
    public static final String LNG = "lng";
    public static final String FRIENDS = "friends";
    public static final String IMAGE_URL = "imageUrl";


    public boolean checkInternetConnection(Activity activity, String alertText) {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting())
            return true;
        else {
            internetAlert(activity, alertText);
            return false;
        }
    }

    private void internetAlert(final Activity activity, String alertText) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
        builder.setCancelable(false);
        builder.setIcon(R.drawable.wifi_off);
        builder.setTitle(activity.getResources().getString(R.string.login_alert_text));
        builder.setMessage(alertText);
        builder.setNegativeButton(activity.getResources().getString(R.string.login_alert_negative_text), null);
        builder.setPositiveButton(activity.getResources().getString(R.string.login_alert_positive_text), (paramDialogInterface, paramInt) -> activity.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)));
        builder.show();
    }

    public boolean checkGPSConnection(Activity activity) {
        LocationManager locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) return true;
        else {
            alertGPS(activity);
            return false;
        }
    }

    private void alertGPS(final Activity activity) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setMessage(activity.getResources().getString(R.string.user_location_alert_text))
                .setCancelable(false)
                .setPositiveButton(activity.getResources().getString(R.string.user_location_alert_positive_text), (dialog, id) -> {
                    Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    activity.startActivity(callGPSSettingIntent);
                });
        alertDialogBuilder.setNegativeButton(activity.getResources().getString(R.string.user_location_alert_negative_text),
                (dialog, id) -> dialog.cancel());
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    public void exitAlert(Activity activity, final DatabaseReference reference, final String user_uid) {

    }

    public BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        assert vectorDrawable != null;
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void requestFocus(View view, Activity activity) {
        if (view.requestFocus()) {
            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        } else
            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}
