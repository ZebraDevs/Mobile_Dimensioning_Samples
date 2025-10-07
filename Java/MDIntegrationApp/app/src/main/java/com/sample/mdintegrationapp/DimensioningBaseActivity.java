package com.sample.mdintegrationapp;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class DimensioningBaseActivity extends AppCompatActivity {
    private static final String TAG = DimensioningBaseActivity.class.getSimpleName();
    static boolean mIsDimensionServiceEnabling = false;
    static boolean mIsDimensionServiceEnabled = false;
    static boolean mAutomaticTrigger = false;
    static final int REQUEST_CODE = 100;
    static final long ENABLE_RETRY_DELAY = 500L;
    static final int MAX_ENABLE_RETRIES = 3;
    static int mEnableRetryCount = 0;
    protected Button mStartDimensioningButton;
    private Toast mToastMsg = null;

    @Override
    protected void onStop() {
        // When switching app activities, callbacks are finish(), then onStart(), then onStop().
        // So, send DISABLE in finish() rather than onStop().
        // Also, don't send DISABLE when onStop() called due to configuration (orientation) change.
        Log.d(TAG, "onStop()");
        if (!isChangingConfigurations() && !isFinishing())
            sendIntentApi(DimensioningConstants.INTENT_ACTION_DISABLE_DIMENSION);
        super.onStop();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart()");
        mEnableRetryCount = 0;
        super.onStart();
    }

    public void finish() {
        // When switching app activities, callbacks are finish(), then onStart(), then onStop().
        // So, send DISABLE in finish() rather than onStop().
        sendIntentApi(DimensioningConstants.INTENT_ACTION_DISABLE_DIMENSION);
        Log.d(TAG, "finish()");
        super.finish();
    }

    /**
     * sendIntentApi Calling of each action is done in sendIntentApi.
     *
     * @param action     Intent action to be performed
     * @param extraKey   To be sent along with intent
     * @param extraValue To be sent along with intent
     */
    public void sendIntentApi(String action, String extraKey, boolean extraValue) {
        Bundle extras = new Bundle();
        extras.putBoolean(extraKey, extraValue);
        sendIntentApi(action, extras);
    }

    /**
     * sendIntentApi Calling of each action is done in sendIntentApi.
     *
     * @param action     Intent action to be performed
     * @param extraKey   To be sent along with intent
     * @param extraValue To be sent along with intent
     */
    public void sendIntentApi(String action, String extraKey, String extraValue) {
        Bundle extras = new Bundle();
        extras.putString(extraKey, extraValue);
        sendIntentApi(action, extras);
    }

    /**
     * sendIntentApi Calling of each action is done in sendIntentApi.
     *
     * @param action Intent action to be performed
     * @param extras To be sent along with intent
     */
    public void sendIntentApi(String action, Bundle extras) {
        Log.d(TAG, "sendIntentApi " + action);
        Intent intent = new Intent();
        intent.setAction(action);
        intent.setPackage(DimensioningConstants.ZEBRA_DIMENSIONING_PACKAGE);

        if (extras != null) {
            intent.putExtras(extras);
        }

        PendingIntent lobPendingIntent = createPendingResult(REQUEST_CODE, new Intent(),
                PendingIntent.FLAG_UPDATE_CURRENT);

        intent.putExtra(DimensioningConstants.CALLBACK_RESPONSE, lobPendingIntent);

        if (intent.getAction().equals(DimensioningConstants.INTENT_ACTION_ENABLE_DIMENSION)) {
            mIsDimensionServiceEnabling = true;
            mIsDimensionServiceEnabled = false;
            startForegroundService(intent);
        } else {
            if (intent.getAction().equals(DimensioningConstants.INTENT_ACTION_DISABLE_DIMENSION)) {
                disableStartDimensioningButton();
                mIsDimensionServiceEnabling = false;
                mIsDimensionServiceEnabled = false;
                intent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            }
            sendBroadcast(intent);
        }
    }

    /**
     * sendIntentApi Calling of each action is done in sendIntentApi.
     *
     * @param action Intent action to be performed
     */
    public void sendIntentApi(String action) {
        sendIntentApi(action, null);
    }

    /**
     * isDimensioningServiceAvailable Checks if dimensioning service is installed.
     */
    public boolean isDimensioningServiceAvailable() {
        PackageManager pm = getPackageManager();
        @SuppressLint("QueryPermissionsNeeded") List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo applicationInfo : packages) {
            try {
                if (applicationInfo.packageName.equals(DimensioningConstants.ZEBRA_DIMENSIONING_PACKAGE)) {
                    Log.d(TAG, "checkInstalledApplications : packageName : " + applicationInfo.packageName);
                    return true;
                }
            } catch (Exception e) {
                Log.d(TAG, "checkInstalledApplications() exception : " + e);
            }
        }
        return false;
    }

    public void enableDimensioning() {
        if (isDimensioningServiceAvailable()) {
            sendIntentApi(DimensioningConstants.INTENT_ACTION_ENABLE_DIMENSION, DimensioningConstants.MODULE, DimensioningConstants.PARCEL_MODULE);
        } else {
            Log.e(TAG, "Dimensioning service not available");
            showToast(getResources().getString(R.string.dimensioning_service_availability_check));
        }
    }

    /**
     * enableStartDimensioningButton function is used to Enable the START DIMENSION button.
     */
    public void enableStartDimensioningButton() {
        if (mAutomaticTrigger) {
            mStartDimensioningButton.callOnClick();
        } else {
            mStartDimensioningButton.setEnabled(true);
            mStartDimensioningButton.setClickable(true);
            mStartDimensioningButton.setBackgroundColor(getColor(R.color.blue));
        }
    }

    /**
     * disableStartDimensioningButton function is used to Disable the START DIMENSION button.
     */
    public void disableStartDimensioningButton() {
        mStartDimensioningButton.setEnabled(false);
        mStartDimensioningButton.setClickable(false);
        mStartDimensioningButton.setBackgroundColor(getColor(R.color.dark_gray));
    }

    public void showToast(String s) {
        runOnUiThread(() ->
        {
            if (mToastMsg != null)
                mToastMsg.cancel();
            mToastMsg = Toast.makeText(this, s, Toast.LENGTH_SHORT);
            mToastMsg.show();
        });
    }
}