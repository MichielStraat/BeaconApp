package com.mjchs.beaconApp.Tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.goebl.david.Webb;

import org.json.JSONObject;

/** Class that creates a AsyncTask and sends with this task the beaconData to the server
 * Created by mjchs on 11-12-2015.
 */
public class SendBeaconData extends  AsyncTask<JSONObject, Integer, String>
{
    public final static String TAG = "SendBeaconData";
    private String endPoint;


    public SendBeaconData(String endPoint)
    {
        this.endPoint = endPoint;
    }

    @Override
    protected String doInBackground(JSONObject... params)
    {
        JSONObject result = null;

        try
        {
            Webb webb = Webb.create();
            result = webb.post(endPoint)
                    .useCaches(false)
                    .body(params[0])
                    .asJsonObject()
                    .getBody();
        }
        catch (Exception e)
        {
            Log.d(TAG, e.getMessage());
        }

        if (result != null)
            return result.toString();

        return "Could not get Stats!";
    }

    @Override
    protected void onPostExecute(String s)
    {
        Log.d(TAG, "Result: " + s);
    }
}
