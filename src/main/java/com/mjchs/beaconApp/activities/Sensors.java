package com.mjchs.beaconApp.activities;


import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.mjchs.beaconApp.R;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.cloud.model.BeaconInfo;
import com.estimote.sdk.connection.BeaconConnection;
import com.estimote.sdk.connection.MotionState;
import com.estimote.sdk.connection.Property;
import com.estimote.sdk.exception.EstimoteDeviceException;

/**
 * Activity that shows temperature and movement status of a beacon.
 * Created by mjchs on 4-12-2015.
 */
public class Sensors extends BaseAppActivity
{
    public static final String TAG = "Sensors";
    private Handler tempHandler;

    /**
     * The beacon whose temp and movement we will measure for the application
     */
    private Beacon beacon;
    private BeaconConnection connection;

    /*Some GUI elements that will show temperature and motionstatus*/
    private TextView motionView;
    private TextView tempView;


    @Override
    protected int getLayoutResId()
    {
        return R.layout.sensor_activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /*first we get the beacon whose sensor values we want to read from the intent
        that started the activity
         */
        beacon = getIntent().getParcelableExtra(ListBeacons.EXTRA_BEACON);

        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        t.setTitle("Sensor: " + beacon.getMinor());
        motionView = (TextView) findViewById(R.id.motion);
        tempView = (TextView) findViewById(R.id.temperature);

        tempHandler = new Handler();
        /**We set up a connection to the beacon and authorize access to it, this will make sure
         * we can read the temperature measurements and the movement status of the beacon
         */
        Log.d(TAG, "ready to set up the connection to the beacon");
        connection = new BeaconConnection(this, beacon, new BeaconConnection.ConnectionCallback() {

            @Override
            public void onAuthorized(final BeaconInfo beaconInfo)
            {
                runOnUiThread(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        Toast.makeText(Sensors.this, "Autorhized to beacon: " + beaconInfo.minor,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            /**
             * when connection has been established and access is granted
             * @param beaconInfo
             */
            @Override
            public void onConnected(final BeaconInfo beaconInfo)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(Sensors.this, "Conntected to beacon: " + beaconInfo.minor,
                                Toast.LENGTH_SHORT).show();
                    }
                });

                /*Enabling motion detection on beacon*/
                connection.edit().set(connection.motionDetectionEnabled(), true).commit(new BeaconConnection.WriteCallback()
                {
                    @Override
                    public void onSuccess()
                    {
                        /*Read initial motion value of beacon*/
                        setMotionState(connection.motionState().get());
                        setTemp(connection.temperature().get());
                        /*set the motion listener that will keep us updated with motionstate*/
                        connection.setMotionListener(new motionListener());
                        periodicTempRead();
                    }

                    @Override
                    public void onError(EstimoteDeviceException e)
                    {

                    }
                });
            }

            @Override
            public void onAuthenticationError(EstimoteDeviceException e)
            {
            }

            @Override
            public void onDisconnected()
            {

            }
        });
    }

    /**
     * motionListener is implements the callback interface for motion listening that we will
     * use upon succesfull connection to beacon
     */
    private class motionListener implements Property.Callback<MotionState>
    {
        @Override
        public void onValueReceived(MotionState motionState)
        {
            setMotionState(motionState);
        }

        @Override
        public void onFailure()
        {

        }
    }

    /**
     * Method will update the motionstate label to show the user the motion state of the beacon
     * @param motionState the motionstate of the beacon
     */
    private void setMotionState(final MotionState motionState)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (motionState != null)
                {
                    if (motionState == MotionState.MOVING)
                        motionView.setText("Yes");
                    else
                        motionView.setText("No");
                }
                else
                {
                    motionView.setText("Motion status not available");
                }
            }
        });
    }

    private void setTemp(final Float val)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                tempView.setText(String.format("%.1f\u2103", val));
            }
        });
    }

    private void periodicTempRead()
    {
        connection.temperature().getAsync(new Property.Callback<Float>()
        {
            @Override
            public void onValueReceived(Float aFloat)
            {
                /*aFloat is the incoming temperature, now update in GUI*/
                setTemp(aFloat);
                tempHandler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        periodicTempRead();
                    }
                }, 2000);
            }

            @Override
            public void onFailure()
            {
                // do nothing
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (!connection.isConnected())
        {
            connection.authenticate();
        }
        else
        {
            connection.setMotionListener(new motionListener());
            periodicTempRead();
        }
    }

    @Override
    protected void onPause()
    {
        if (connection != null)
            connection.setMotionListener(null);
        tempHandler.removeCallbacks(null);
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        connection.close();
        super.onDestroy();
    }
}
