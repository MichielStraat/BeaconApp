package com.mjchs.beaconApp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.mjchs.beaconApp.AppClass;
import com.mjchs.beaconApp.services.RangingService;

/**
 * Created by mjchs on 31-12-2015.
 */
public class autostart extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences settings = context.getSharedPreferences(AppClass.PREFS_FILE, 0);
        boolean rangingEnabled = settings.getBoolean(AppClass.RANG_SET, true);

        if (rangingEnabled)
        {
            context.startService(new Intent(context, RangingService.class));
            Log.i("autostart", "STARTED RANGING SERVICE AFTER BOOT UP");
        }

    }
}
