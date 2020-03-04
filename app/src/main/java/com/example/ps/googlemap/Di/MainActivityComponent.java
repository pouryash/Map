package com.example.ps.googlemap.Di;

import com.example.ps.googlemap.GMap.MainActivity;
import com.example.ps.googlemap.GMap.MainActivityLocationService;

import dagger.Component;

@Component(modules = MainActivityModule.class)
public interface MainActivityComponent {

    MainActivityLocationService getMainactivityLocationService();

    void inject(MainActivity mainActivity);

}
