package com.example.ps.googlemap.GMap;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.ps.googlemap.R;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.SphericalUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;


public class MainActivityLocationService implements OnMapReadyCallback, ILocationService, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private Context context;
    private Marker origin;
    private Marker destination;
    private boolean isOriginEnabled = true;
    private boolean isDestinationEnabled = false;
    private LatLng initLatLong;
    private GoogleMap.OnMarkerDragListener onMarkerDragListener;

    @Inject
    public MainActivityLocationService(Context context) {
        this.context = context;
        initLatLong = new LatLng(35.715298, 51.404343);
    }

    public MainActivityLocationService(Context context, LatLng latLngInit) {
        this.context = context;
        initLatLong = latLngInit;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

//        ArrayList<LatLng> MarkerPoints;
//        GoogleApiClient mGoogleApiClient;
//        LocationRequest mLocationRequest;

        mMap = googleMap;

        addMarker(initLatLong, "origin");

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(origin.getPosition()));
        mMap.setMyLocationEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin.getPosition(), 12));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                changeCamera(marker.getPosition());
            }
        });


        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Toast.makeText(context, "please turn on location service", Toast.LENGTH_SHORT).show();

                    LocationRequest locationRequest = LocationRequest.create();
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                            .addLocationRequest(locationRequest);

                    Task<LocationSettingsResponse> result =
                            LocationServices.getSettingsClient(context).checkLocationSettings(builder.build());


                    result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                            try {
                                LocationSettingsResponse response = task.getResult(ApiException.class);
                                // All location settings are satisfied. The client can initialize location
                                // requests here.
                            } catch (ApiException exception) {
                                switch (exception.getStatusCode()) {
                                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                        // Location settings are not satisfied. But could be fixed by showing the
                                        // user a dialog.
                                        try {
                                            // Cast to a resolvable exception.
                                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                                            // Show the dialog by calling startResolutionForResult(),
                                            // and check the result in onActivityResult().
                                            resolvable.startResolutionForResult(
                                                    (Activity) context,
                                                    LocationRequest.PRIORITY_HIGH_ACCURACY);
                                        } catch (IntentSender.SendIntentException e) {
                                            // Ignore the error.
                                        } catch (ClassCastException e) {
                                            // Ignore, should be an impossible error.
                                        }
                                        break;
                                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                        // Location settings are not satisfied. However, we have no way to fix the
                                        // settings so we won't show the dialog.
                                        break;
                                }
                            }
                        }
                    });
                } else {
                    return false;
                }
                return true;
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                addMarker(latLng, "origin");
            }
        });

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                if (isOriginEnabled && origin != null) {
                    origin.setPosition(mMap.getCameraPosition().target);
                } else if (!isOriginEnabled && isDestinationEnabled) {
                    destination.setPosition(mMap.getCameraPosition().target);
                }
            }
        });

        mMap.setOnMarkerClickListener(MainActivityLocationService.this);


    }


    @Override
    public void addMarker(LatLng latLng, String marker) {
        switch (marker) {
            case "origin":
                if (origin == null) {
                    origin = mMap.addMarker(new MarkerOptions()
                            .position(latLng).title("Tehran")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            .draggable(true)
                            .snippet("Population: 4,137,400")
                            .zIndex(10f));
                    changeCamera(latLng);
                } else {
                    origin.setPosition(latLng);
                    changeCamera(latLng);
                }
                break;
            case "destination":
                if (destination == null) {
                    destination = mMap.addMarker(new MarkerOptions()
                            .position(latLng).title("Sanandaj")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            .draggable(true)
                            .snippet("Population: 4,256,302")
                            .zIndex(100f));
                    changeCamera(latLng);
                } else {
                    destination.setPosition(latLng);
                    changeCamera(latLng);
                }
                break;

        }

    }

    @Override
    public void removeMarker(Marker marker) {
        marker.remove();
    }

    @Override
    public void drawOnMap() {

    }

    @Override
    public void drawPolyLine(List<LatLng> list) {

        PatternItem DOT = new Dot();
        PatternItem GAP = new Gap(10);

//
// Create a stroke pattern of a gap followed by a dot.
        final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);

        Polyline polyline;

        PolylineOptions options = new PolylineOptions()
                .width(10)
                .color(Color.GRAY)
                .clickable(true)
                .geodesic(false)//this will make line arc in long distance( if true )
                .jointType(JointType.ROUND)
                .endCap(new CustomCap(BitmapDescriptorFactory.fromResource(R.drawable.moreinfo_arrow_pressed)))
                .startCap(new CustomCap(BitmapDescriptorFactory.fromResource(R.drawable.moreinfo_arrow)))
                .pattern(PATTERN_POLYLINE_DOTTED);
        for (int z = 0; z < list.size(); z++) {
            LatLng point = list.get(z);
            options.add(point);
        }
        polyline = mMap.addPolyline(options);

    }


    public void showCurvedPolyline(LatLng p1, LatLng p2, double arc) {
//        Calculate distance and heading between two points
        double d = SphericalUtil.computeDistanceBetween(p1, p2);
        double h = SphericalUtil.computeHeading(p1, p2);


        //Midpoint position
        LatLng p = SphericalUtil.computeOffset(p1, d * 0.5, h);

        //Apply some mathematics to calculate position of the circle center
        double x = (1 - arc * arc) * d * 0.5 / (2 * arc);
        double r = (1 + arc * arc) * d * 0.5 / (2 * arc);

        LatLng c = SphericalUtil.computeOffset(p, x, h + 90.0);

        //Polyline options
        PolylineOptions options = new PolylineOptions();
        List<PatternItem> pattern = Arrays.<PatternItem>asList(new Dash(30), new Gap(20));

        //Calculate heading between circle center and two points
        double h1 = SphericalUtil.computeHeading(c, p1);
        double h2 = SphericalUtil.computeHeading(c, p2);

        //Calculate positions of points on circle border and add them to polyline options
        int numpoints = 100;
        double step = (h2 - h1) / numpoints;

        for (int i = 0; i < numpoints; i++) {
            LatLng pi = SphericalUtil.computeOffset(c, r, h1 + i * step);
            options.add(pi);
        }

        //Draw polyline
        mMap.addPolyline(options.width(10).color(Color.BLACK).geodesic(false).pattern(pattern));


        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        builder.include(p1);
        builder.include(p2);

        LatLngBounds bounds = builder.build();
        int padding = 400; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.moveCamera(cu);
        mMap.animateCamera(cu);




//        List<PatternItem> pattern2 = Arrays.<PatternItem>asList(new Dash(30), new Gap(20));
//        PolylineOptions popt = new PolylineOptions().add(p1).add(p2)
//                .width(10).color(Color.MAGENTA).pattern(pattern2)
//                .geodesic(true);
//        mMap.addPolyline(popt);
//
//        LatLngBounds.Builder builder = new LatLngBounds.Builder();
//
//        builder.include(p1);
//        builder.include(p2);
//
//        LatLngBounds bounds = builder.build();
//        int padding = 350; // offset from edges of the map in pixels
//        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
//        mMap.moveCamera(cu);
//        mMap.animateCamera(cu);
    }

    @Override
    public void changeCamera(LatLng latLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.equals(origin) && isOriginEnabled) {
            origin.setPosition(mMap.getCameraPosition().target);
            addMarker(mMap.getCameraPosition().target, "destination");
            isOriginEnabled = false;
            isDestinationEnabled = true;
        } else if (marker.equals(destination) && isDestinationEnabled) {
            destination.setPosition(mMap.getCameraPosition().target);
            isDestinationEnabled = false;

//            String str_origin = "origin=" + origin.getPosition().latitude + "," + origin.getPosition().longitude;
//            String str_dest = "destination=" + destination.getPosition().latitude + "," + destination.getPosition().longitude;
//            String sensor = "sensor=false";
//            String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + "key=AIzaSyDSw-S37OohI6N7BJSbrnVfV8MZcKAk7SU";
//            String output = "json";
//            String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
//
//            FetchUrl FetchUrl = new FetchUrl();
//            FetchUrl.execute(url);

            List<LatLng> list = new ArrayList<>();
            list.add(origin.getPosition());
            list.add(destination.getPosition());
            drawPolyLine(list);
            showCurvedPolyline(origin.getPosition(), destination.getPosition(), 2);
        }
        return false;
    }


    //direction service google
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            String data = "";

            try {
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);

        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                JSONParserTask parser = new JSONParserTask();
                Log.d("ParserTask", parser.toString());
                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = result.get(i);
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute", "onPostExecute lineoptions decoded");

            }
            if (lineOptions != null) {
                mMap.addPolyline(lineOptions);
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }
}
