package com.app.talkzy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.app.talkzy.Fragment.ProfileFragment;
import com.app.talkzy.VideoChat.VideoCallActivity;
import com.app.talkzy.database.UsersDatabase;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.opentok.android.Session;

import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.EasyPermissions;

public class Locator {
    private static LocationManager locationManager;
    private static Location location;
    private static String countryName = "Not defined";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void connectingLocationService(Activity context) {
        getCountry(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static void getCountry(Activity context) {
        requestLocationPermission();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            ProfileFragment.setCountry(countryName);  //NOT DEFINED!!!!!!!!!!!!
            return;
        }

        try {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                try {
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(
                            location.getLatitude(), location.getLongitude(), 1
                    );
                    String countryName = addresses.get(0).getCountryName();
                    if(countryName != null) {
                        Locator.countryName = countryName;
                        System.out.println(countryName);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            ProfileFragment.setCountry(Locator.countryName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ProfileFragment.setCountry(Locator.countryName);
    }

    private static void requestLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION };
        if(!EasyPermissions.hasPermissions(MainActivity.mainActivity, perms)) {
            EasyPermissions.requestPermissions(MainActivity.mainActivity, "Need Location Permissions... ", 124, perms);
        }
    }
}
