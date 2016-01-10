package com.mjchs.beaconApp;

import android.app.Application;
import android.content.SharedPreferences;

import com.mjchs.beaconApp.Model.DataModel;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.EstimoteSDK;

import java.util.UUID;

/**
 * Created by mjchs on 4-12-2015.
 */
public class AppClass extends Application
{
    public static final String PREFS_FILE = "prefs_file";
    public static final String SET_RANG = "rangingEnabled";
    public static final String SET_USER = "userid";
    public static final UUID BUUID = UUID.fromString("316CDCA2-8FE3-446F-8864-D7D2DA69A3F4");
    public static String userID;

    private DataModel mModel;
    private BeaconManager mManager;

    @Override
    public void onCreate()
    {
        super.onCreate();

        EstimoteSDK.initialize(getApplicationContext(), "occudet-hr9", "8dcb10d402b34332b723208a022ebf34");
        SharedPreferences settings = getSharedPreferences(PREFS_FILE, 0);
        userID = settings.getString(SET_USER, "default");

        mManager = new BeaconManager(getApplicationContext());
        mModel = new DataModel();
    }

    public DataModel getModel()
    {
        return mModel;
    }

    public BeaconManager getManager()
    {
        return mManager;
    }
}
