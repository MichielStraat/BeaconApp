package com.mjchs.beaconApp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.estimote.sdk.SystemRequirementsChecker;
import com.mjchs.beaconApp.AppClass;
import com.mjchs.beaconApp.Model.DataModel;
import com.mjchs.beaconApp.R;
import com.mjchs.beaconApp.adapters.BeaconListAdapter;
import com.estimote.sdk.Beacon;
import com.mjchs.beaconApp.services.RangingService;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class ListBeacons extends BaseAppActivity implements Observer
{
    public static final String TAG = "ListBeaconActivity";
    public static final String EXTRA_BEACON = "beacon";

    /*Adapter will return views when we scroll through the listview*/
    private BeaconListAdapter adapter;
    private List<Beacon> beacons;

    private AppClass globState;
    private DataModel model;

    @Override
    protected int getLayoutResId()
    {
        return R.layout.main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        globState = (AppClass)this.getApplication();
        model =  globState.getModel();

        //check if bluetooth etc is on
        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        //Start our background ranging service
        Intent intent = new Intent(this, RangingService.class);
        startService(intent);

        adapter = new BeaconListAdapter(this);
        ListView listView = (ListView) findViewById(R.id.device_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*When a beacons is clicked, we will show its sensor values*/
                Beacon clickedBeacon = beacons.get(position);
                Intent i = new Intent(ListBeacons.this, Sensors.class);
                /*We put the beacon that is clicked in an extra*/
                i.putExtra(EXTRA_BEACON, clickedBeacon);
                startActivity(i);
            }
        });
    }

    /**
     * Method introduced for debugging purposes, gives a string representation of a Beacon list
     * @param list A list of beacons
     * @return
     */
    public static String stringRepList(List<Beacon> list)
    {
        StringBuilder builder = new StringBuilder();

        for (Beacon b : list) {
            builder.append(b.getMajor() + " ");
            builder.append(b.getRssi() + " ");
        }
        return builder.toString();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        model.addObserver(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        model.deleteObserver(this);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    public void update(Observable o, Object arg)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                beacons = model.getBeaconList();
                Log.d(TAG, "THE UPDATE IS IN");
                toolbar.setTitle("Beacons in proximity: " + beacons.size());
                toolbar.setSubtitle("Ranging is running");
                toolbar.setSubtitleTextColor(Color.parseColor("#78AB46"));
                adapter.replaceWith(beacons);
            }
        });
    }
}
