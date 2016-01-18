package com.mjchs.beaconApp.Inference;

import android.util.Log;

import com.estimote.sdk.Beacon;

import java.util.List;

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
            roomCount.put(room);
        }

        return roomCount.getMax();
    }
}
