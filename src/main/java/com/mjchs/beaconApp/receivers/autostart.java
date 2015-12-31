package com.mjchs.beaconApp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mjchs.beaconApp.services.RangingService;

/**
 * Created by mjchs on 31-12-2015.
 */
public class autostart extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, RangingService.class);
        context.startService(i);
        Log.i("com.mjchs.beaconApp.receivers.autostart", "started");
    }
}
