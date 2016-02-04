package com.mjchs.beaconApp.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.estimote.sdk.MacAddress;
import com.estimote.sdk.cloud.model.BeaconInfo;
import com.estimote.sdk.connection.BeaconConnection;
import com.estimote.sdk.connection.Property;
import com.estimote.sdk.exception.EstimoteDeviceException;
import com.mjchs.beaconApp.AppClass;
import com.mjchs.beaconApp.Inference.Inference;
import com.mjchs.beaconApp.Model.DataModel;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.mjchs.beaconApp.Tasks.SendBeaconData;
import com.mjchs.beaconApp.Utils.MakeJSON;
import com.mjchs.beaconApp.activities.ListBeacons;

import org.json.JSONObject;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


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
    private BeaconConnection connection;
    private boolean isExtended = false;

    private static Region OUR_BEACONS = null;
    private static final int NOTCONST = 223;

    @Override
    public void onCreate()
    {
        super.onCreate();
        //The custom UUID that is made
        OUR_BEACONS = new Region("beacons", null, null, null);

        globState = (AppClass)this.getApplication();
        model =  globState.getModel();
        mBeaconManager = globState.getManager();
    }


    private class RangListener implements BeaconManager.RangingListener
    {
        @Override
        public void onBeaconsDiscovered(Region region, List<Beacon> list)
        {
            Log.d(TAG, "FROM SERVICE, BEACONS FOUND WITH RANGING: " + ListBeacons.stringRepList(list));

            //Set the data in the model
            model.setData(list);

            if (!list.isEmpty()) {

                try {
                        /*Send all beacon data to the server*/
                    JSONObject JSONAllBeacon = MakeJSON.makeJSONAllBeacons(list);
                    Log.d(TAG, JSONAllBeacon.toString());
                    new SendBeaconData("http://129.125.84.205:8086/bt/beacon").execute(JSONAllBeacon);

                    //optional: send inference too
                    int room = new Inference(list).performKNN(3);
                    JSONObject JSONInference = MakeJSON.makeJSONInference(room);
                    Log.d(TAG, JSONInference.toString());
                    new SendBeaconData("http://192.168.178.19/newapi/putEntry.php").execute(JSONInference);

                    if (isExtended) {
                        adjustScan(2000, 5000);
                        isExtended = false;
                    }
                } catch (Exception ex) {
                    Log.d(TAG, ex.getMessage());
                }
            }
            else
            {
                if (!isExtended) {
                    adjustScan(2000, 25000);
                    isExtended = true;
                }
            }
        }
    }


    private void adjustScan(final int scanningTime, final int waitingTime)
    {
        mBeaconManager.stopRanging(OUR_BEACONS);
        mBeaconManager.disconnect();
        mBeaconManager.setRangingListener(new RangListener());
        mBeaconManager.setForegroundScanPeriod(scanningTime, waitingTime);
        mBeaconManager.connect(new BeaconManager.ServiceReadyCallback()
        {
            @Override
            public void onServiceReady()
            {
                mBeaconManager.startRanging(OUR_BEACONS);
                Log.d(TAG, "Scan time adjusted!");
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
        adjustScan(2000, 5000);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.support.v7.appcompat.R.drawable.notification_template_icon_bg)
                .setContentTitle("BeaconApp")
                .setContentText("Ranging for beacons...")
                .setOngoing(true);

        Intent resultIntent = new Intent(this, ListBeacons.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ListBeacons.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTCONST, mBuilder.build());

        return Service.START_STICKY;
    }

    /**
     * Perform the necessary cleanup, however, avoid that service is killed by Android OS
     * to make free resources. This is an important service! (Service in foreground)
     */
    @Override
    public void onDestroy()
    {
        mBeaconManager.stopRanging(OUR_BEACONS);
        mBeaconManager.disconnect();

        //cancel the notification that the server was running
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTCONST);

        Log.d(TAG, "SERVICE HAS STOPPED");
        super.onDestroy();
    }
}
