package com.mjchs.beaconApp.Model;

import com.estimote.sdk.Beacon;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * RangingService will manipulate data in DataModel, such that views can observe this model
 * and get the beacon data when model is changed.
 * Created by mjchs on 4-12-2015.
 */
public class DataModel extends Observable
{
    private List<Beacon> mList;

    public DataModel()
    {
        mList = new ArrayList<>();
    }

    public void setData(List<Beacon> updatedList)
    {
        mList = updatedList;
        setChanged();
        notifyObservers();
    }

    public List<Beacon> getBeaconList()
    {
        return mList;
    }
}
