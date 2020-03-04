package com.example.ps.googlemap.GMap;


import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

public interface ILocationService {

    void addMarker(LatLng latLng, String marker);

    void removeMarker(Marker marker);

    void drawOnMap();

    void drawPolyLine(List<LatLng> list);

    void changeCamera(LatLng latLng);

}
