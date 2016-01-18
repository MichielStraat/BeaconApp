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
    private HashMap<String, Integer> scanCounts = new HashMap<String,Integer>();
    private HashMap<String, BeaconConnection> connections;

    private static Region OUR_BEACONS = null;

    private static final int NOTCONST = 223;

    @Override
    public void onCreate()
    {
        super.onCreate();
        //The custom UUID that is made
        OUR_BEACONS = new Region("beacons", AppClass.BUUID, null, null);

        globState = (AppClass)this.getApplication();
        model =  globState.getModel();
        mBeaconManager = globState.getManager();

        mBeaconManager.setRangingListener(new BeaconManager.RangingListener()
        {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                Log.d(TAG, "FROM SERIVCE, BEACONS FOUND WITH RANGING: " + ListBeacons.stringRepList(list));

                //Set the data in the model
                model.setData(list);

                if (!list.isEmpty())
                {
                    removeAllBeaconsNotPresent(list);
                    for (Beacon b : list)
                    {
                        String macAddress = b.getMacAddress().toString();
                        if (scanCounts.containsKey(macAddress))
                        {
                            Integer count = scanCounts.get(macAddress);
                            scanCounts.put(macAddress, count + 1);
                            if (count + 1 == 2)
                            {
                                establishConnection(b);
                            }
                        }
                        else
                        {
                            scanCounts.put(macAddress, 1);
                        }
                    }

                    Log.d(TAG, scanCounts.toString());
                    try
                    {
                    /*Send all beacon data to the server*/
                        JSONObject JSONAllBeacon = MakeJSON.makeJSONAllBeacons(list);
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

                    mBeaconManager.setBackgroundScanPeriod(2000, 5000);
                    mBeaconManager.setForegroundScanPeriod(2000, 5000);
                }
                else
                {
                    scanCounts.clear();
                    closeConnections();
                   // connections.clear();
                    mBeaconManager.setBackgroundScanPeriod(2000, 25000);
                    mBeaconManager.setForegroundScanPeriod(2000, 25000);
                }
            }
        });

        mBeaconManager.setBackgroundScanPeriod(2000, 5000);
        mBeaconManager.setForegroundScanPeriod(2000, 5000);
    }

    private void establishConnection(final Beacon b)
    {
            connection = new BeaconConnection(this, b, new BeaconConnection.ConnectionCallback() {
            @Override
            public void onAuthorized(BeaconInfo beaconInfo) {

            }

            @Override
            public void onConnected(BeaconInfo beaconInfo) {
                connection.temperature().getAsync(new Property.Callback<Float>() {
                    @Override
                    public void onValueReceived(Float temp) {
                        JSONObject jsonTemp = MakeJSON.makeJSONTemp(temp, b);
                        Log.d(TAG, jsonTemp.toString());
                        new SendBeaconData().execute(jsonTemp);
                    }

                    @Override
                    public void onFailure() {

                    }
                });
            }

            @Override
            public void onAuthenticationError(EstimoteDeviceException e) {

            }

            @Override
            public void onDisconnected() {

            }
        });

     //   connections.put(b.getMacAddress().toString(), connection);
    }

    private void closeConnections()
    {
        Iterator it = connections.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry pair = (Map.Entry) it.next();
            BeaconConnection conn = (BeaconConnection) pair.getValue();
            conn.close();
        }
    }

    private boolean beaconPresent(String macAddress, List<Beacon> list)
    {
        for (Beacon b : list)
        {
            if (b.getMacAddress().toString().equals(macAddress))
                return true;
        }
        return false;
    }

    /**
     * Removes from scanCounts each beacon that is not in list
     * @param list
     */
    private void removeAllBeaconsNotPresent(List<Beacon> list)
    {
        Iterator it = scanCounts.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry pair = (Map.Entry) it.next();
            String macAddress = (String) pair.getKey();
            if (!beaconPresent(macAddress, list))
            {
                //remove beacon from scanCounts
                scanCounts.remove(macAddress);
                BeaconConnection conn = connections.get(macAddress);
                if (conn != null)
                    conn.close();
            }
        }
    }

    private void startScanning()
    {
        mBeaconManager.stopRanging(OUR_BEACONS);
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

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.support.v7.appcompat.R.drawable.notification_template_icon_bg)
                .setContentTitle("BeaconApp")
                .setContentText("Ranging for beacons...")
                .setOngoing(true);

        Intent restulIntent = new Intent(this, ListBeacons.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ListBeacons.class);
        stackBuilder.addNextIntent(restulIntent);
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
