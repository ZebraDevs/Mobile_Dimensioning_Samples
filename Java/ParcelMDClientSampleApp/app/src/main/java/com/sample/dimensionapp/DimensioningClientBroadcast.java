package com.sample.dimensionapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Class will receive SDK crash report.
 */
public class DimensioningClientBroadcast extends BroadcastReceiver
{
    private static final String TAG = DimensioningClientBroadcast.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(TAG, "onReceive() called.");
        String actionName = intent.getAction();

        if (actionName != null)
        {
            if (actionName.equals(DimensioningConstants.INTENT_ACTION_APPLICATION_CRASH))
            {
                Log.d(TAG, "onReceive() SDK crash");
                Toast.makeText(context.getApplicationContext(), String.valueOf(R.string.SDK_service_crashed), Toast.LENGTH_SHORT).show();
                Log.e(TAG, String.valueOf(R.string.SDK_service_crashed));
                Intent intentClean = new Intent(context, DimensioningClientApp.class);
                intentClean.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intentClean);
            }
        }
    }
}
