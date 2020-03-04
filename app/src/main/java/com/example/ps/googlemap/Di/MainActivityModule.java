package com.example.ps.googlemap.Di;

import android.content.Context;

import com.example.ps.googlemap.GMap.MainActivityLocationService;
import com.google.android.gms.maps.model.LatLng;

import dagger.Module;
import dagger.Provides;

@Module
public class MainActivityModule {

    private Context context;
    private LatLng latLng;

    public MainActivityModule(Context context) {
        this.context = context;
    }

    public MainActivityModule(Context context, LatLng latLng) {
        this.context = context;
        this.latLng = latLng;
    }


    @Provides
    MainActivityLocationService ProvideMainActivityLocationService() {
        return new MainActivityLocationService(context, latLng);
    }

}
