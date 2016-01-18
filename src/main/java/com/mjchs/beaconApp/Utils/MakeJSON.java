package com.mjchs.beaconApp.Utils;

import android.provider.Settings;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.Utils;
import com.mjchs.beaconApp.AppClass;
import com.mjchs.beaconApp.Inference.Inference;
import com.mjchs.beaconApp.services.Sensors;

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
    private static final String SENSORID = "sensor_id";
    private static final String USERID = "user_id";

    public static JSONObject makeJSONAllBeacons(List<Beacon> beaconList)
    {
        JSONObject obj = new JSONObject();

        try
        {
            obj.put(SENSORID, Sensors.PROXIMITY);
            obj.put(USERID, AppClass.userID);
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
            obj.put(USERID, AppClass.userID);
            obj.put("inRoom", room);
        }
        catch (JSONException ex)
        {
            Log.d(TAG, ex.getMessage());
        }

        return obj;
    }

    public static JSONObject makeJSONTemp(Float temp, Beacon b)
    {
        JSONObject obj = new JSONObject();

        try
        {
            obj.put(SENSORID, Sensors.TEMP);
            obj.put("major", b.getMajor());
            obj.put("minor", b.getMinor());
            obj.put(USERID, AppClass.userID);
            obj.put("value", temp);

        } catch (JSONException ex) {
            Log.d(TAG, ex.getMessage());
        }

        return obj;
    }
}
