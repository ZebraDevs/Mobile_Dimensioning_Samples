package com.sample.dimensionapp;

import android.app.Application;
import android.content.Intent;

/**
 * Class used to capture application crashes.
 */
public class ApplicationDimensioningClient extends Application
{
    private static final String TAG = ApplicationDimensioningClient.class.getSimpleName();

    @Override
    public void onCreate()
    {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(this::handleUncaughtException);
    }

    /**
     * To report the crashes to SDK using Intent.
     *
     * @param thread thread to capture crashes
     */
    public void handleUncaughtException(Thread thread, Throwable e)
    {
        Log.d(TAG, "handleUncaughtException start :");
        if (DimensioningClientApp.token != null && !DimensioningClientApp.token.isEmpty())
        {
            Intent intent = new Intent();
            intent.setAction(DimensioningConstants.INTENT_ACTION_DISABLE_DIMENSION);
            intent.setPackage(DimensioningConstants.ZEBRA_DIMENSIONING_PACKAGE);
            intent.putExtra(DimensioningConstants.APPLICATION_PACKAGE, getPackageName());
            intent.putExtra(DimensioningConstants.TOKEN, DimensioningClientApp.token);
            sendBroadcast(intent);
        }
        Log.d(TAG, "handleUncaughtException stop :");
    }
}