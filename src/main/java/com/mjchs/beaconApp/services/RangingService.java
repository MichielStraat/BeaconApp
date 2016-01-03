package com.mjchs.beaconApp.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mjchs.beaconApp.AppClass;
import com.mjchs.beaconApp.Inference.Inference;
import com.mjchs.beaconApp.Model.DataModel;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.mjchs.beaconApp.Tasks.SendBeaconData;
import com.mjchs.beaconApp.activities.ListBeacons;

import org.joda.time.DateTime;
import org.json.JSONObject;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;


/**
 * Service that will do ranging for beacons. Also service must continue ranging for beacons
 * at all time, with a reliable interval
 * Created by mjchs on 28-11-2015.
 */

public class RangingService extends Service
{
    public static final String TAG = "BeaconRangingService";
    private AppClass globState;
    private DataModel model;
    private BeaconManager mBeaconManager;

    private static Region OUR_BEACONS = null;

    @Override
    public void onCreate()
    {
        super.onCreate();
        //The custom UUID that is made
        OUR_BEACONS = new Region("beacons", null, null, null);

        globState = (AppClass)this.getApplication();
        model =  globState.getModel();
        mBeaconManager = globState.getManager();

        mBeaconManager.setRangingListener(new BeaconManager.RangingListener()
        {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list)
            {
                Log.d(TAG, "FROM SERIVCE, BEACONS FOUND WITH RANGING: " + ListBeacons.stringRepList(list));

                //Set the data in the model
                model.setData(list);

                try
                {
                    /*Send all beacon data to the server*/
                    String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                            Settings.Secure.ANDROID_ID);
                    JSONObject JSONAllBeacon = MakeJSON.makeJSONAllBeacons(list, android_id);
                    Log.d(TAG, JSONAllBeacon.toString());
                    new SendBeaconData().execute(JSONAllBeacon);

                    //optional: send inference too
                    int room = new Inference(list).performKNN(3);
                    JSONObject JSONInference = MakeJSON.makeJSONInference(room);
                    Log.d(TAG, JSONInference.toString());
                    new SendBeaconData().execute(JSONInference);
                }
                catch (Exception ex)
                {
                    Log.d(TAG, ex.getMessage());
                }
            }
        });

        mBeaconManager.setBackgroundScanPeriod(2000, 1000);
        mBeaconManager.setForegroundScanPeriod(2000, 1000);
    }

    private void startScanning()
    {
        mBeaconManager.connect(new BeaconManager.ServiceReadyCallback()
        {
            @Override
            public void onServiceReady()
            {
                mBeaconManager.startRanging(OUR_BEACONS);
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        startScanning();
        return Service.START_STICKY;
    }

    /**
     * Perform the necessary cleanup, however, avoid that service is killed by Android OS
     * to make free resources. This is an important service! (Service in foreground)
     */
    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }
}
