package com.mjchs.beaconApp.Inference;

import android.util.Log;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.mjchs.beaconApp.AppClass;
import com.mjchs.beaconApp.activities.BaseAppActivity;
import com.mjchs.beaconApp.activities.ListBeacons;
import com.mjchs.beaconApp.activities.Sensors;

import java.util.List;
import android.widget.Toast;


/**
 *
 * Class will contain several methods to perform inference on a list of found beacons
 * Created by mjchs on 11-12-2015.
 */
public class Inference
{
    /**
     * The list of found beacons
     */
    private List<Beacon> mBeacons;

    public Inference(List<Beacon> beacons)
    {
        mBeacons = beacons;
    }

    /**
     * infer occupancy using KNN
     *
     * @param k how many beacons will be taken into account
     * @return The room number that is inferred \n when no inference could be made, returns -1
     */
    public int performKNN(int k)
    {
        if (k > mBeacons.size())
            k = mBeacons.size();

        CustomHashMap roomCount = new CustomHashMap(mBeacons, k);

        for (int j = 0; j != k; ++j)
        {
            //TODO: there is an assumption that we dont find beacons from other floors for now
            int room = mBeacons.get(j).getMajor();
            room /= 1000;
            Log.d("INFERENCE", "Beacon in room: " + room + " found");
            Toast.makeText(AppClass.getCont(), "Beacon in room: " + room + " found!", Toast.LENGTH_SHORT).show();
            roomCount.put(room);
        }

        return roomCount.getMax();
    }
}
