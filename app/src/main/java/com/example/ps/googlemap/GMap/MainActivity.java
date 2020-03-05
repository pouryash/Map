package com.example.ps.googlemap.GMap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import com.example.ps.googlemap.Di.DaggerMainActivityComponent;
import com.example.ps.googlemap.Di.MainActivityComponent;
import com.example.ps.googlemap.Di.MainActivityModule;
import com.example.ps.googlemap.R;
import com.example.ps.googlemap.RuntimePermissionsActivity;
import com.example.ps.googlemap.databinding.ActivityGmapBinding;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;
import javax.inject.Inject;

public class MainActivity extends RuntimePermissionsActivity implements LocationListener {

    private int IsGooglePlayServiceAvailable = 0;
    public static final int LOCATION_REQUEST = 100;
    private ActivityGmapBinding binding;
    private LocationManager mLocationManager;
    @Inject
    MainActivityLocationService mainActivityLocationService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGmapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();

        checkGooglePlayServiceAvailability();

    }

    private void init() {
        MainActivityComponent component = DaggerMainActivityComponent
                .builder()
                .mainActivityModule(new MainActivityModule(this))
                .build();
        component.inject(this);

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

    }

    public void checkGooglePlayServiceAvailability() {

        IsGooglePlayServiceAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if (IsGooglePlayServiceAvailable == ConnectionResult.SUCCESS) {
            this.requestAppPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
        } else {
            Toast.makeText(this, "service not available", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onPermissionsGranted(int requestCode) {
        if (requestCode == LOCATION_REQUEST) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(mainActivityLocationService);
        }
    }

    @Override
    public void onPermissionsDeny(int requestCode) {
        if (requestCode == LOCATION_REQUEST) {
            Snackbar.make(binding.root, "persmision needed to continue", Snackbar.LENGTH_LONG)
                    .setAction("retry", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            MainActivity.this.requestAppPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION}, 100);
                        }
                    })
                    .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
                    .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LocationRequest.PRIORITY_HIGH_ACCURACY:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        Log.i("TAG", "onActivityResult: GPS Enabled by user");
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                                    MainActivity.this);

                        }

                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Log.i("TAG", "onActivityResult: User rejected GPS request");

                        break;
                    default:
                        break;
                }
                break;
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        LatLng tehran = new LatLng(location.getLatitude(), location.getLongitude());
        mainActivityLocationService.changeCamera(tehran);
        mainActivityLocationService.addMarker(tehran, "origin");
        mLocationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
