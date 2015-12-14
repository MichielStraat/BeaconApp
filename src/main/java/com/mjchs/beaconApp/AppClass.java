package com.mjchs.beaconApp;

import android.app.Application;

import com.mjchs.beaconApp.Model.DataModel;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.EstimoteSDK;

/**
 * Created by mjchs on 4-12-2015.
 */
public class AppClass extends Application
{
    private DataModel mModel;
    private BeaconManager mManager;

    @Override
    public void onCreate()
    {
        super.onCreate();

        EstimoteSDK.initialize(getApplicationContext(), "occudet-hr9",  "8dcb10d402b34332b723208a022ebf34");

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
