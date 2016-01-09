package com.mjchs.beaconApp.activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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


    private SharedPreferences settings;

    private BeaconListAdapter adapter;
    private List<Beacon> beacons;

    private AppClass globState;
    private DataModel model;

    private Button toggleRanging;

    @Override
    protected int getLayoutResId()
    {
        return R.layout.main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        settings = getSharedPreferences(AppClass.PREFS_FILE, 0);
        globState = (AppClass)this.getApplication();
        model =  globState.getModel();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        toggleRanging = (Button) toolbar.findViewById(R.id.toggle_ranging);
        //check if bluetooth etc is on
        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        //Start our background ranging service
        boolean rangingEnabled = settings.getBoolean(AppClass.RANG_SET, true);

        if (rangingEnabled)
        {
            startService(new Intent(this, RangingService.class));
            updateUIRangingOn();
        }
        else
        {
            stopService(new Intent(ListBeacons.this, RangingService.class));
            updateUIRangingOff();
        }

        toggleRanging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //stop service
                if (isRanging(RangingService.class))
                {
                    stopService(new Intent(ListBeacons.this, RangingService.class));
                    updateUIRangingOff();
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(AppClass.RANG_SET, false);
                    editor.commit();
                }
                else
                {
                    startService(new Intent(ListBeacons.this, RangingService.class));
                    updateUIRangingOn();
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(AppClass.RANG_SET, true);
                    editor.commit();
                }
            }
        });

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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                beacons = model.getBeaconList();
                Log.d(TAG, "THE UPDATE IS IN");
                toolbar.setTitle("#Beacons " + beacons.size());
                adapter.replaceWith(beacons);
            }
        });
    }

    private boolean isRanging(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }

        return false;
    }

    private void updateUIRangingOn()
    {
        toggleRanging.setText("Set OFF");
        toolbar.setSubtitle("Ranging is running");
        toolbar.setSubtitleTextColor(Color.parseColor("#78AB46"));
    }

    private void updateUIRangingOff()
    {
        toggleRanging.setText("Set ON");
        toolbar.setSubtitle("Ranging not running");
        toolbar.setSubtitleTextColor(Color.parseColor("#CC0000"));
    }
}
