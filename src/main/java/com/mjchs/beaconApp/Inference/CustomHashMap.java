package com.mjchs.beaconApp.Inference;

import com.estimote.sdk.Beacon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by mjchs on 11-12-2015.
 */
public class CustomHashMap
{
    private HashMap<Integer, Integer> hash = new HashMap<Integer, Integer>();
    private List<Beacon> mBeacons = null;
    int k = -1;

    public CustomHashMap(List<Beacon> list, int k)
    {
        this.k = k;
        mBeacons = list;
    }

    public void put(Integer key)
    {
        if (!hash.containsKey(key))
            hash.put(key, 1);
        else
            hash.put(key, hash.get(key) + 1);
    }

    private List<Map.Entry<Integer, Integer>> isUnique(Set<Map.Entry<Integer, Integer>> s, Integer val)
    {
        List<Map.Entry<Integer, Integer>> l = new ArrayList<>();

        for (Map.Entry<Integer, Integer> m : s)
        {
            if (m.getValue().intValue() == val.intValue())
                l.add(m);
        }

        return l;
    }

    private int takeBest(List<Map.Entry<Integer, Integer>> l)
    {
        double w = Integer.MIN_VALUE;
        int bestRoom = -1;

        for (Map.Entry<Integer, Integer> m : l)
        {
            int aggrRSSI = 0;
            for (int j = 0; j != k; ++j)
            {
                Beacon b = mBeacons.get(j);

                if (b.getMajor() == m.getKey().intValue())
                {
                    aggrRSSI += b.getRssi();
                }
            }

            double meanRSSI = aggrRSSI / m.getValue().intValue();
            if (meanRSSI > w)
            {
                w = meanRSSI;
                bestRoom = m.getKey().intValue();
            }
        }

        return bestRoom;
    }

    public int getMax()
    {
        Set<Map.Entry<Integer, Integer>> s = hash.entrySet();

        Integer max = 0;
        Integer room = -1;

        for (Map.Entry<Integer, Integer> m : s)
        {
            if (m.getValue().intValue() > max)
            {
                max = m.getValue().intValue();
                room = m.getKey().intValue();
            }
        }

        List<Map.Entry<Integer, Integer>> l = isUnique(s, max);
        if (l.size() <= 1)
            return room;

        return takeBest(l);
    }
}
