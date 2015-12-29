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

/**
 * Created by mjchs on 29-12-2015.
 */
public class MakeJSON
{
    public final static String TAG = "MakeJSON";

    public static JSONObject makeJSONAllBeacons(List<Beacon> beaconList)
    {
        JSONObject obj = new JSONObject();
        //TODO look at sensor id, we shouldnt add a timestamp here (?)
        try
        {
            obj.put("sensor_id", "1");
            obj.put("user_id", "AUser");
            JSONArray foundBeacons = new JSONArray();

            for (Beacon b : beaconList)
            {
                JSONObject beaconDat = new JSONObject();
                beaconDat.put("instance_id", b.getMajor());
                beaconDat.put("rssi", b.getRssi());
                beaconDat.put("proximity_zone", Utils.computeProximity(b).name());
                beaconDat.put("proximity_distance", Utils.computeAccuracy(b));
                foundBeacons.put(beaconDat.toString());
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
