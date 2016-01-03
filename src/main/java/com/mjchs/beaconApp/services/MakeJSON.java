package com.mjchs.beaconApp.services;

import android.provider.Settings;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.Utils;
import com.mjchs.beaconApp.Inference.Inference;

import org.joda.time.DateTime;
import org.json.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import android.provider.Settings.Secure;

/**
 * Created by mjchs on 29-12-2015.
 */
public class MakeJSON
{
    public final static String TAG = "MakeJSON";

    public static JSONObject makeJSONAllBeacons(List<Beacon> beaconList, String android_id)
    {
        JSONObject obj = new JSONObject();

        try
        {
            obj.put("sensor_id", Sensors.PROXIMITY);
            obj.put("user_id", android_id);
            JSONArray foundBeacons = new JSONArray();

            for (Beacon b : beaconList)
            {
                JSONObject beaconDat = new JSONObject();
                beaconDat.put("major", b.getMajor());
                beaconDat.put("minor", b.getMinor());
                beaconDat.put("rssi", b.getRssi());
                beaconDat.put("proximity_zone", Utils.computeProximity(b).name());
                beaconDat.put("proximity_distance", Utils.computeAccuracy(b));
                foundBeacons.put(beaconDat);
                //TODO temperature and movement
            }
            obj.put("beacons", foundBeacons);
            //obj.put("beacons", foundBeacons);
            //obj.put("instance_id", )
        }
        catch (JSONException ex)
        {
            Log.d(TAG, ex.getMessage());
        }

        return obj;
    }

    public static JSONObject makeJSONInference(int room)
    {
        JSONObject obj = new JSONObject();

        try
        {
            obj.put("user_id", "AUser");
            obj.put("inRoom", room);
        }
        catch (JSONException ex)
        {
            Log.d(TAG, ex.getMessage());
        }

        return obj;
    }

}
