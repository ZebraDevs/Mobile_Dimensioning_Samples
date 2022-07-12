package com.sample.dimensionapp;

import static com.sample.dimensionapp.ConstantUtils.ENABLE_EXTRA_KEY;
import static com.sample.dimensionapp.ConstantUtils.ENABLE_EXTRA_VALUE;
import static com.sample.dimensionapp.ConstantUtils.NO_DIM;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.zebra.security.broadcastprotection.BroadCastAuthenticator;
import com.zebra.security.broadcastprotection.OnInitCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * DimensioningClientApp Activity.
 * This is the first Activity that is initiated when application is launched.
 */
public class DimensioningClientApp extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private static final String TAG = DimensioningClientApp.class.getSimpleName();

    private Context mContext = null;
    private EditText mTxtLength, mTxtWidth, mTxtHeight, mBoxID;
    private Button mStartDimensioningButton, mScanBarcodeButton;
    private TextView mTextViewInch;
    private TextView mTextViewCm;
    private TextView mTextViewReset;
    private TextView mTextViewLengthIcon, mTextViewWidthIcon, mTextViewHeightIcon;
    private CheckBox mReportImageCheckBox;
    private View mLayoutLength, mLayoutWidth, mLayoutHeight;

    private String mUnit = ConstantUtils.INCH;
    private BigDecimal mReadyLength, mReadyWidth, mReadyHeight;
    public static String mBundleVersion, mFrameworkVersion, mServiceVersion, mRegulatoryApproval;

    private ActionBarDrawerToggle actionBarDrawerToggle;
    private DrawerLayout drawerLayout;

    private final BroadCastAuthenticator vIntentprotect = new BroadCastAuthenticator();
    protected static String token = "";
    private boolean mRetryingToken;

    // Save values to display after orientation change
    private static boolean mPersistValue = false;
    private static BigDecimal mPersistLength, mPersistWidth, mPersistHeight;
    private static String mPersistLengthStatus = "READY_STATUS";
    private static String mPersistWidthStatus = "READY_STATUS";
    private static String mPersistHeightStatus = "READY_STATUS";
    private static String mPersistUnit = null;

    // Save UI elements when going to About screen
    private static boolean mPersistImage = false;
    private static String mPersistBoxId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Token authenticator initialization.
        if (token == null || token.isEmpty())
        {
            vIntentprotect.initialize(getApplicationContext(), new OnInitCallback()
            {
                @Override
                public void onInitialized()
                {
                    Log.d(TAG, "BroadCastAuthenticator is successfully initialized");
                    generateToken();
                }
            });
        }

        initialization();
        setDataWedgeProfile();

        mReportImageCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                                                        {
                                                            @Override
                                                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                                                            {
                                                                sendIntentApi(ConstantUtils.INTENT_ACTION_SET_DIMENSION_PARAMETER, ConstantUtils.REPORT_IMAGE, isChecked);
                                                            }
                                                        }
        );

        mTextViewInch.setOnClickListener(new View.OnClickListener()
        {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v)
            {
                callIntentApiForToggleSwitch(ConstantUtils.INCH);
                mTextViewInch.setBackground(mContext.getResources().getDrawable(R.drawable.selected_inch_bg, null));
                mTextViewCm.setBackground(mContext.getResources().getDrawable(R.drawable.unselected_cm_bg, null));
            }
        });

        mTextViewCm.setOnClickListener(new View.OnClickListener()
        {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v)
            {
                callIntentApiForToggleSwitch(ConstantUtils.CM);
                mTextViewInch.setBackground(mContext.getResources().getDrawable(R.drawable.unselected_inch_bg, null));
                mTextViewCm.setBackground(mContext.getResources().getDrawable(R.drawable.selected_cm_bg, null));
            }
        });

        mTextViewReset.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.e(TAG, getResources().getString(R.string.reset_state));
                mPersistValue = false;
                Log.d(TAG, "Calling Get Dimension Intent ");
                sendIntentApi(ConstantUtils.INTENT_ACTION_GET_DIMENSION_PARAMETER);
            }
        });

        mStartDimensioningButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String boxID = mBoxID.getText().toString();
                mBoxID.getText().clear();
                Log.d(TAG, "boxID is: " + boxID);
                Log.e(TAG, ConstantUtils.START_DIMENSIONING);
                sendIntentApi(ConstantUtils.INTENT_ACTION_GET_DIMENSION, ConstantUtils.BOX_ID, boxID);
            }
        });

        mScanBarcodeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent dwIntent = new Intent();
                dwIntent.setAction("com.symbol.datawedge.api.ACTION");
                dwIntent.putExtra("com.symbol.datawedge.api.SOFT_SCAN_TRIGGER", "START_SCANNING");
                sendBroadcast(dwIntent);
                Log.d(TAG, "Calling Start Scanning Intent action= " + dwIntent.getAction());
            }
        });

        if (savedInstanceState != null)
        {
            mPersistValue = true;
        }
    }

    public void generateToken()
    {
        Log.d(TAG, "generateToken()");
        try
        {
            token = vIntentprotect.getToken(ConstantUtils.SERVICE_IDENTIFIER);
            if (token != null && !token.isEmpty())
            {
                if (isDimensioningServiceAvailable())
                {
                    sendIntentApi(ConstantUtils.INTENT_ACTION_ENABLE_DIMENSION, ENABLE_EXTRA_KEY, ENABLE_EXTRA_VALUE);
                }
                else
                {
                    Log.e(TAG, getResources().getString(R.string.dimensioning_service_availability_check));
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            Toast.makeText(mContext, getResources().getString(R.string.dimensioning_service_availability_check), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            else
            {
                Log.d(TAG, "Token is empty or null");
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        Toast.makeText(mContext, "Access Denied", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        catch (Exception exception)
        {
            Log.e(TAG, "generateToken", exception);
            runOnUiThread(new Runnable()
            {
                public void run()
                {
                    Toast.makeText(mContext, "Access Denied", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // override the onOptionsItemSelected()
    // function to implement the item click listener callback
    // to open and close the navigation drawer when the icon is clicked
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            Log.d(TAG, "onOptionsItemSelected= " + item);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * onActivityResult handles the intent response from SDK and perform the action accordingly.
     *
     * @param requestCode is used as an identity for Client and SDK request and response intent communication.
     * @param resultCode  is a response result of each intent.
     * @param intent      Intent received from SDK.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);
        try
        {
            if (requestCode == ConstantUtils.REQUEST_CODE)
            {
                if (intent != null)
                {
                    String actionName = intent.getAction();
                    int dimResultCode = intent.getIntExtra(ConstantUtils.RESULT_CODE, ConstantUtils.FAIL);
                    String dimResultMessage = intent.getStringExtra(ConstantUtils.RESULT_MESSAGE);
                    if (dimResultMessage == null)
                        dimResultMessage = "";
                    Log.d(TAG, "onActivityResult: " + actionName + ", " + dimResultCode + ", " + dimResultMessage);

                    if (!mRetryingToken && (dimResultCode == ConstantUtils.ERROR) && dimResultMessage.equals("Access Denied"))
                    {
                        // The token expired (after 24 hours)
                        mRetryingToken = true;
                        generateToken();
                        return;
                    }
                    mRetryingToken = false;

                    switch (actionName)
                    {
                        case ConstantUtils.INTENT_ACTION_ENABLE_DIMENSION:
                            try
                            {
                                if (dimResultCode == ConstantUtils.SUCCESS)
                                {
                                    enableStartDimensioningButton();
                                    if (mPersistUnit != null)
                                    {
                                        sendIntentApi(ConstantUtils.INTENT_ACTION_SET_DIMENSION_PARAMETER,
                                                ConstantUtils.DIMENSIONING_UNIT, mPersistUnit);
                                    }
                                    else
                                    {
                                        sendIntentApi(ConstantUtils.INTENT_ACTION_GET_DIMENSION_PARAMETER);
                                    }
                                }
                                else
                                {
                                    Toast.makeText(this, dimResultMessage, Toast.LENGTH_SHORT).show();
                                }
                            }
                            catch (Exception e)
                            {
                                Log.d(TAG, "Exception : " + e);
                            }
                            break;

                        case ConstantUtils.INTENT_ACTION_GET_DIMENSION:
                            try
                            {
                                if (dimResultCode == ConstantUtils.SUCCESS)
                                {
                                    BigDecimal length = new BigDecimal(0);
                                    BigDecimal width = new BigDecimal(0);
                                    BigDecimal height = new BigDecimal(0);
                                    String lengthStatus = NO_DIM;
                                    String widthStatus = NO_DIM;
                                    String heightStatus = NO_DIM;
                                    Instant timestamp = Instant.EPOCH;

                                    Bundle extras = intent.getExtras();
                                    if (extras.containsKey(ConstantUtils.DIMENSIONING_UNIT))
                                    {
                                        mUnit = intent.getStringExtra(ConstantUtils.DIMENSIONING_UNIT);
                                    }
                                    if (extras.containsKey(ConstantUtils.LENGTH))
                                    {
                                        length = (BigDecimal) intent.getSerializableExtra(ConstantUtils.LENGTH);
                                    }
                                    if (extras.containsKey(ConstantUtils.WIDTH))
                                    {
                                        width = (BigDecimal) intent.getSerializableExtra(ConstantUtils.WIDTH);
                                    }
                                    if (extras.containsKey(ConstantUtils.HEIGHT))
                                    {
                                        height = (BigDecimal) intent.getSerializableExtra(ConstantUtils.HEIGHT);
                                    }
                                    if (extras.containsKey(ConstantUtils.LENGTH_STATUS))
                                    {
                                        lengthStatus = intent.getStringExtra(ConstantUtils.LENGTH_STATUS);
                                    }
                                    if (extras.containsKey(ConstantUtils.WIDTH_STATUS))
                                    {
                                        widthStatus = intent.getStringExtra(ConstantUtils.WIDTH_STATUS);
                                    }
                                    if (extras.containsKey(ConstantUtils.HEIGHT_STATUS))
                                    {
                                        heightStatus = intent.getStringExtra(ConstantUtils.HEIGHT_STATUS);
                                    }
                                    if (extras.containsKey(ConstantUtils.TIMESTAMP))
                                    {
                                        timestamp = (Instant) intent.getSerializableExtra(ConstantUtils.TIMESTAMP);
                                        Log.d(TAG, "Time stamp is : " + timestamp);
                                    }
                                    if (extras.containsKey(ConstantUtils.IMAGE))
                                    {
                                        Bitmap bitmapImage = (Bitmap) intent.getParcelableExtra(ConstantUtils.IMAGE);
                                        saveImage(bitmapImage, timestamp);
                                        Log.d(TAG, "bitmapImage is : " + bitmapImage);
                                    }
                                    if (extras.containsKey(ConstantUtils.PARCEL_ID))
                                    {
                                        String PARCEL_ID = intent.getStringExtra(ConstantUtils.PARCEL_ID);
                                        Log.d(TAG, "PARCEL_ID is : " + PARCEL_ID);
                                    }

                                    showDimensioningParameterStatus(length, width, height, lengthStatus, widthStatus, heightStatus, mUnit);
                                }
                                else
                                {
                                    showDimensioningParameterStatus(mReadyLength, mReadyWidth, mReadyHeight,
                                            ConstantUtils.READY_STATUS, ConstantUtils.READY_STATUS, ConstantUtils.READY_STATUS, mUnit);
                                }
                                if ((dimResultCode != ConstantUtils.SUCCESS) && (dimResultCode != ConstantUtils.CANCELED))
                                {
                                    Toast.makeText(this, dimResultMessage, Toast.LENGTH_SHORT).show();
                                }
                            }
                            catch (Exception e)
                            {
                                Log.d(TAG, "Exception : " + e);
                            }
                            break;

                        case ConstantUtils.INTENT_ACTION_GET_DIMENSION_PARAMETER:
                            try
                            {
                                Bundle extras = intent.getExtras();
                                if (extras.containsKey(ConstantUtils.READY_LENGTH))
                                {
                                    mReadyLength = (BigDecimal) intent.getSerializableExtra(ConstantUtils.READY_LENGTH);
                                    Log.d(TAG, "Get Dimension Parameter Result mReadyLength: " + mReadyLength);
                                }
                                if (extras.containsKey(ConstantUtils.READY_WIDTH))
                                {
                                    mReadyWidth = (BigDecimal) intent.getSerializableExtra(ConstantUtils.READY_WIDTH);
                                    Log.d(TAG, "Get Dimension Parameter Result mReadyWidth: " + mReadyWidth);
                                }
                                if (extras.containsKey(ConstantUtils.READY_HEIGHT))
                                {
                                    mReadyHeight = (BigDecimal) intent.getSerializableExtra(ConstantUtils.READY_HEIGHT);
                                    Log.d(TAG, "Get Dimension Parameter Result mReadyHeight: " + mReadyHeight);
                                }
                                if (extras.containsKey(ConstantUtils.DIMENSIONING_UNIT))
                                {
                                    mUnit = intent.getStringExtra(ConstantUtils.DIMENSIONING_UNIT);
                                    Log.d(TAG, "Get Dimension Parameter Result mUnit : " + mUnit);
                                }
                                if (extras.containsKey(ConstantUtils.BUNDLE_VERSION))
                                {
                                    mBundleVersion = intent.getStringExtra(ConstantUtils.BUNDLE_VERSION);
                                    Log.d(TAG, "Get Dimension Parameter Result mBundleVersion: " + mBundleVersion);
                                }
                                if (extras.containsKey(ConstantUtils.FRAMEWORK_VERSION))
                                {
                                    mFrameworkVersion = intent.getStringExtra(ConstantUtils.FRAMEWORK_VERSION);
                                    Log.d(TAG, "Get Dimension Parameter Result mFrameworkVersion: " + mFrameworkVersion);
                                }
                                if (extras.containsKey(ConstantUtils.SERVICE_VERSION))
                                {
                                    mServiceVersion = intent.getStringExtra(ConstantUtils.SERVICE_VERSION);
                                    Log.d(TAG, "Get Dimension Parameter Result mServiceVersion: " + mServiceVersion);
                                }
                                if (extras.containsKey(ConstantUtils.SUPPORTED_UNITS))
                                {
                                    String[] supportedUnits = intent.getStringArrayExtra(ConstantUtils.SUPPORTED_UNITS);
                                    updateUnitSwitch(supportedUnits);
                                    Log.d(TAG, "Get Dimension Parameter Result supportedUnits: " + Arrays.toString(supportedUnits));
                                }
                                if (extras.containsKey(ConstantUtils.REGULATORY_APPROVAL))
                                {
                                    mRegulatoryApproval = intent.getStringExtra(ConstantUtils.REGULATORY_APPROVAL);
                                    Log.d(TAG, "Get Dimension Parameter Result mRegulatoryApproval: " + mRegulatoryApproval);
                                }
                                if (extras.containsKey(ConstantUtils.REPORT_IMAGE))
                                {
                                    boolean reportImage = intent.getBooleanExtra(ConstantUtils.REPORT_IMAGE, false);
                                    Log.d(TAG, "Get Dimension Parameter Result reportImage: " + reportImage);
                                }
                                if (mPersistValue && mPersistUnit != null)
                                {
                                    showDimensioningParameterStatus(mPersistLength, mPersistWidth, mPersistHeight,
                                            mPersistLengthStatus, mPersistWidthStatus, mPersistHeightStatus, mPersistUnit);
                                }
                                else
                                {
                                    showDimensioningParameterStatus(mReadyLength, mReadyWidth, mReadyHeight,
                                            ConstantUtils.READY_STATUS, ConstantUtils.READY_STATUS, ConstantUtils.READY_STATUS, mUnit);
                                }
                                if (dimResultCode != ConstantUtils.SUCCESS)
                                {
                                    Toast.makeText(this, dimResultMessage, Toast.LENGTH_SHORT).show();
                                }
                            }
                            catch (Exception e)
                            {
                                Log.d(TAG, "Exception : " + e);
                            }
                            break;
                        case ConstantUtils.INTENT_ACTION_SET_DIMENSION_PARAMETER:
                            try
                            {
                                if (dimResultCode == ConstantUtils.SUCCESS)
                                {
                                    //Below Intent is called to update the ready values everytime the switch is toggled
                                    sendIntentApi(ConstantUtils.INTENT_ACTION_GET_DIMENSION_PARAMETER);
                                }
                                else
                                {
                                    Toast.makeText(this, dimResultMessage, Toast.LENGTH_SHORT).show();
                                }
                            }
                            catch (Exception e)
                            {
                                Log.d(TAG, "Exception : " + e);
                            }
                            break;
                        case ConstantUtils.INTENT_ACTION_DISABLE_DIMENSION:
                            try
                            {
                                if (dimResultCode != ConstantUtils.SUCCESS)
                                {
                                    Toast.makeText(this, dimResultMessage, Toast.LENGTH_SHORT).show();
                                }
                            }
                            catch (Exception e)
                            {
                                Log.d(TAG, "Exception : " + e);
                            }
                            break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            Log.d(TAG, "Exception : " + e);
        }
    }

    /**
     * Initialization of all Drawables & Drawer Layout.
     */
    public void initialization()
    {
        // drawer layout instance to toggle the menu icon
        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // to make the Navigation drawer icon always appear on the action bar
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        NavigationView mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        mContext = getApplicationContext();
        mTextViewReset = findViewById(R.id.reset);
        mTxtLength = findViewById(R.id.editTextLength);
        mTxtWidth = findViewById(R.id.editTextWidth);
        mTxtHeight = findViewById(R.id.editTextHeight);
        mTextViewInch = findViewById(R.id.in);
        mTextViewCm = findViewById(R.id.cm);
        mTextViewLengthIcon = findViewById(R.id.lengthTextViewIcon);
        mTextViewWidthIcon = findViewById(R.id.widthTextViewIcon);
        mTextViewHeightIcon = findViewById(R.id.heightTextViewIcon);
        mBoxID = findViewById(R.id.editTextBoxID);
        mStartDimensioningButton = findViewById(R.id.button);
        mScanBarcodeButton = findViewById(R.id.buttonScanBarcode);
        mLayoutLength = findViewById(R.id.linearlayoutLength);
        mLayoutWidth = findViewById(R.id.linearlayoutWidth);
        mLayoutHeight = findViewById(R.id.linearlayoutHeight);
        mReportImageCheckBox = findViewById(R.id.checkBoxReportImage);

        IntentFilter filter = new IntentFilter();
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(ConstantUtils.DATA_WEDGE_INTENT_ACTION_SCANNING_RESULT);
        registerReceiver(datawedgeBroadcastReceiver, filter);
    }

    /**
     * callIntentApiForToggleSwitch function is used to call set dimension parameter for selected unit switch.
     *
     * @param selectedUnit It contain current value of toggle switch i.e. cm/inch
     */
    public void callIntentApiForToggleSwitch(String selectedUnit)
    {
        Log.e(TAG, getResources().getString(R.string.state_changes));
        mPersistValue = false;
        sendIntentApi(ConstantUtils.INTENT_ACTION_SET_DIMENSION_PARAMETER,
                ConstantUtils.DIMENSIONING_UNIT, selectedUnit);
        Log.d(TAG, "Calling Set Dimension Parameter Intent selectedUnit = " + selectedUnit);
    }

    /**
     * showDimensioningParameterStatus function is used to display Dimensioning Result.
     *
     * @param length        Length of Parcel.
     * @param width         Width of Parcel.
     * @param height        Height of Parcel.
     * @param mLengthStatus LengthStatus show if the length of parcel is in Allowed range.
     * @param mWidthStatus  WidthStatus show if the width of parcel is in Allowed range.
     * @param mHeightStatus HeigthStatus show if the Height of parcel is in Allowed range.
     * @param unit          unit determines whether dimensioning result is in Metrics or Imperial format.
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    public void showDimensioningParameterStatus(final BigDecimal length, final BigDecimal width,
                                                final BigDecimal height, String mLengthStatus,
                                                String mWidthStatus, String mHeightStatus, String unit)
    {
        String uiUnit = (unit.equalsIgnoreCase(ConstantUtils.INCH) ? ConstantUtils.IN : unit.toLowerCase());

        String dimensionLength = length.toString() + uiUnit;
        mTxtLength.setText(dimensionLength);
        String dimensionWidth = width.toString() + uiUnit;
        mTxtWidth.setText(dimensionWidth);
        String dimensionHeight = height.toString() + uiUnit;
        mTxtHeight.setText(dimensionHeight);

        mPersistLength = length;
        mPersistWidth = width;
        mPersistHeight = height;
        mPersistLengthStatus = mLengthStatus;
        mPersistWidthStatus = mWidthStatus;
        mPersistHeightStatus = mHeightStatus;
        mPersistUnit = unit;
        mPersistValue = true;

        if (unit.equalsIgnoreCase(ConstantUtils.CM))
        {
            mTextViewInch.setBackground(mContext.getResources().getDrawable(R.drawable.unselected_inch_bg, null));
            mTextViewCm.setBackground(mContext.getResources().getDrawable(R.drawable.selected_cm_bg, null));
        }
        else if (unit.equalsIgnoreCase(ConstantUtils.INCH))
        {
            mTextViewInch.setBackground(mContext.getResources().getDrawable(R.drawable.selected_inch_bg, null));
            mTextViewCm.setBackground(mContext.getResources().getDrawable(R.drawable.unselected_cm_bg, null));
        }

        if (mWidthStatus.equalsIgnoreCase(NO_DIM) || mHeightStatus.equalsIgnoreCase(NO_DIM) ||
                mLengthStatus.equalsIgnoreCase(NO_DIM))
        {
            mTxtLength.setText(NO_DIM);
            mLayoutLength.setBackground(mContext.getResources().getDrawable(R.drawable.red_background, null));
            mTextViewLengthIcon.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.warning_red, 0);
            mTxtWidth.setText(NO_DIM);
            mLayoutWidth.setBackground(mContext.getResources().getDrawable(R.drawable.red_background, null));
            mTextViewWidthIcon.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.warning_red, 0);
            mTxtHeight.setText(NO_DIM);
            mLayoutHeight.setBackground(mContext.getResources().getDrawable(R.drawable.red_background, null));
            mTextViewHeightIcon.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.warning_red, 0);
        }
        else
        {
            if (mLengthStatus.equalsIgnoreCase(ConstantUtils.ABOVE_RANGE) || mLengthStatus.equalsIgnoreCase(ConstantUtils.BELOW_RANGE))
            {
                mLayoutLength.setBackground(mContext.getResources().getDrawable(R.drawable.orange_background, null));
                mTextViewLengthIcon.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.warning, 0);
            }
            else if (mLengthStatus.equalsIgnoreCase(ConstantUtils.IN_RANGE))
            {
                mLayoutLength.setBackground(mContext.getResources().getDrawable(R.drawable.green_background, null));
                mTextViewLengthIcon.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check, 0);
            }
            else if (mLengthStatus.equalsIgnoreCase(ConstantUtils.READY_STATUS))
            {
                mLayoutLength.setBackground(mContext.getResources().getDrawable(R.drawable.box, null));
                mTextViewLengthIcon.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }

            if (mWidthStatus.equalsIgnoreCase(ConstantUtils.ABOVE_RANGE) || mWidthStatus.equalsIgnoreCase(ConstantUtils.BELOW_RANGE))
            {
                mLayoutWidth.setBackground(mContext.getResources().getDrawable(R.drawable.orange_background, null));
                mTextViewWidthIcon.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.warning, 0);
            }
            else if (mWidthStatus.equalsIgnoreCase(ConstantUtils.IN_RANGE))
            {
                mLayoutWidth.setBackground(mContext.getResources().getDrawable(R.drawable.green_background, null));
                mTextViewWidthIcon.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check, 0);
            }
            else if (mWidthStatus.equalsIgnoreCase(ConstantUtils.READY_STATUS))
            {
                mLayoutWidth.setBackground(mContext.getResources().getDrawable(R.drawable.box, null));
                mTextViewWidthIcon.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }

            if (mHeightStatus.equalsIgnoreCase(ConstantUtils.ABOVE_RANGE) || mHeightStatus.equalsIgnoreCase(ConstantUtils.BELOW_RANGE))
            {
                mLayoutHeight.setBackground(mContext.getResources().getDrawable(R.drawable.orange_background, null));
                mTextViewHeightIcon.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.warning, 0);
            }
            else if (mHeightStatus.equalsIgnoreCase(ConstantUtils.IN_RANGE))
            {
                mLayoutHeight.setBackground(mContext.getResources().getDrawable(R.drawable.green_background, null));
                mTextViewHeightIcon.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check, 0);
            }
            else if (mHeightStatus.equalsIgnoreCase(ConstantUtils.READY_STATUS))
            {
                mLayoutHeight.setBackground(mContext.getResources().getDrawable(R.drawable.box, null));
                mTextViewHeightIcon.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
        }
    }

    /**
     * enableStartDimensioningButton function is used to Enable the START DIMENSION button.
     */
    public void enableStartDimensioningButton()
    {
        mStartDimensioningButton.setEnabled(true);
        mStartDimensioningButton.setClickable(true);
        mStartDimensioningButton.setBackgroundColor(Color.parseColor("#2185D5"));
    }

    /**
     * sendIntentApi Calling of each action is done in sendIntentApi.
     *
     * @param action     Intent action to be performed
     * @param extraKey   To be sent along with intent
     * @param extraValue To be sent along with intent
     */
    public void sendIntentApi(String action, String extraKey, boolean extraValue)
    {
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
    public void sendIntentApi(String action, String extraKey, String extraValue)
    {
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
    public void sendIntentApi(String action, Bundle extras)
    {
        if (token == null || token.isEmpty())
        {
            Log.e(TAG, "Token is Null");
            return;
        }

        Log.d(TAG, "sendIntentApi " + action);
        Intent intent = new Intent();
        intent.setAction(action);
        intent.setPackage(ConstantUtils.CMP_PACKAGE);
        intent.putExtra(ConstantUtils.PACKAGE_NAME, getPackageName());
        intent.putExtra(ConstantUtils.API_TOKEN, token);

        if (extras != null)
        {
            intent.putExtras(extras);
        }

        PendingIntent lobPendingIntent = createPendingResult(ConstantUtils.REQUEST_CODE, new Intent(),
                PendingIntent.FLAG_UPDATE_CURRENT);

        intent.putExtra(ConstantUtils.CALLBACK_RESPONSE, lobPendingIntent);

        if (intent.getAction().equals(ConstantUtils.INTENT_ACTION_ENABLE_DIMENSION))
        {
            startForegroundService(intent);
        }
        else
        {
            sendBroadcast(intent);
        }
    }

    /**
     * sendIntentApi Calling of each action is done in sendIntentApi.
     *
     * @param action Intent action to be performed
     */
    public void sendIntentApi(String action)
    {
        sendIntentApi(action, null);
    }

    /**
     * displayScanResult function is used to receive the scanned data from broadcast receiver and to display the scanned results.
     *
     * @param initiatingIntent It shows the Intent which initiate the scanning result.
     */
    private void displayScanResult(Intent initiatingIntent)
    {
        String decodedData = initiatingIntent.getStringExtra(ConstantUtils.DATA_WEDGE_INTENT_DATA_KEY);
        mBoxID.getText().clear();
        mBoxID.setText(decodedData);
    }


    private final BroadcastReceiver datawedgeBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            intent.getExtras();

            if (action.equals(ConstantUtils.DATA_WEDGE_INTENT_ACTION_SCANNING_RESULT))
            {
                try
                {
                    displayScanResult(intent);
                }
                catch (Exception e)
                {
                    Log.d(TAG, "Exception : " + e);
                }
            }
        }
    };

    //check dimensioning service is running or not
    private boolean isDimensioningServiceAvailable()
    {
        PackageManager pm = getPackageManager();
        @SuppressLint("QueryPermissionsNeeded") List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo applicationInfo : packages)
        {
            try
            {
                if (applicationInfo.packageName.equals(ConstantUtils.CMP_PACKAGE))
                {
                    Log.d(TAG, "checkInstalledApplications : packageName : " + applicationInfo.packageName);
                    return true;
                }
            }
            catch (Exception e)
            {
                Log.d(TAG, "checkInstalledApplications() exception : " + e);
            }
        }
        return false;
    }

    /**
     * onBackPressed function is used to send the DISABLE_DIMENSION_INTENT to SDK when user exits the app.
     */
    @Override
    public void onBackPressed()
    {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else
        {
            mPersistValue = false;
            sendIntentApi(ConstantUtils.INTENT_ACTION_DISABLE_DIMENSION);
            super.onBackPressed();
        }
    }

    /**
     * updateUnitSwitch is used to update unit switch visibility.
     *
     * @param supportedUnits It consist CM/Inch
     */
    public void updateUnitSwitch(String[] supportedUnits)
    {
        if (supportedUnits.length <= 1)
        {
            mTextViewInch.setVisibility(View.GONE);
            mTextViewCm.setVisibility(View.GONE);
        }
        else
        {
            mTextViewInch.setVisibility(View.VISIBLE);
            mTextViewCm.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart()
    {
        Log.d(TAG, "onStart()");
        sendIntentApi(ConstantUtils.INTENT_ACTION_ENABLE_DIMENSION, ENABLE_EXTRA_KEY, ENABLE_EXTRA_VALUE);
        if (mPersistImage)
        {
            mReportImageCheckBox.setChecked(mPersistImage);
            mPersistImage = false;
        }
        if (mPersistBoxId != null)
        {
            mBoxID.setText(mPersistBoxId);
            mPersistBoxId = null;
        }
        super.onStart();
    }

    @Override
    protected void onRestart()
    {
        Log.d(TAG, "onRestart()");
        mPersistValue = false;
        mReportImageCheckBox.setChecked(false);
        mBoxID.setText("");
        super.onRestart();
    }

    /**
     * onDestroy function is used to unregister the broadcast receiver.
     */
    @Override
    protected void onDestroy()
    {
        unregisterReceiver(datawedgeBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    protected void onStop()
    {
        Log.d(TAG, "onStop()");
        sendIntentApi(ConstantUtils.INTENT_ACTION_DISABLE_DIMENSION);
        super.onStop();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.nav_account:
                if (drawerLayout.isDrawerOpen(GravityCompat.START))
                {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                return true;
            case R.id.nav_settings:
                mPersistImage = mReportImageCheckBox.isChecked();
                mPersistBoxId = mBoxID.getText().toString();
                startActivity(new Intent(this, AboutPageActivity.class));
                finishAfterTransition();
                return true;
            default:
                return false;
        }
    }

    private void setDataWedgeProfile()
    {
        Bundle mainBundle = new Bundle();
        mainBundle.putString("PROFILE_NAME", ConstantUtils.DATA_WEDGE_PROFILE_NAME);
        mainBundle.putString("PROFILE_ENABLED", "true");
        mainBundle.putString("CONFIG_MODE", "CREATE_IF_NOT_EXIST");

        //PLUGIN_CONFIG BUNDLE PROPERTIES
        Bundle intentPluginConfig = new Bundle();
        intentPluginConfig.putString("PLUGIN_NAME", "INTENT");
        intentPluginConfig.putString("RESET_CONFIG", "true");

        Bundle keyStrokePluginConfig = new Bundle();
        keyStrokePluginConfig.putString("PLUGIN_NAME", "KEYSTROKE");
        keyStrokePluginConfig.putString("RESET_CONFIG", "true");

        Bundle intentConfigParams = new Bundle();
        intentConfigParams.putString("intent_output_enabled", "true");
        intentConfigParams.putString("intent_action", ConstantUtils.DATA_WEDGE_INTENT_ACTION_SCANNING_RESULT);
        intentConfigParams.putInt("intent_delivery", 2);

        intentPluginConfig.putBundle("PARAM_LIST", intentConfigParams);

        Bundle keystrokeConfigParams = new Bundle();
        keystrokeConfigParams.putString("keystroke_output_enabled", "false");

        keyStrokePluginConfig.putBundle("PARAM_LIST", keystrokeConfigParams);

        ArrayList configList = new ArrayList<Bundle>();
        configList.add(intentPluginConfig);
        configList.add(keyStrokePluginConfig);

        mainBundle.putParcelableArrayList("PLUGIN_CONFIG", configList);

        Intent setConfigIntent = new Intent();
        setConfigIntent.setAction("com.symbol.datawedge.api.ACTION");
        setConfigIntent.putExtra("com.symbol.datawedge.api.SET_CONFIG", mainBundle);

        sendBroadcast(setConfigIntent);

        Intent switchProfileIntent = new Intent();
        switchProfileIntent.setAction("com.symbol.datawedge.api.ACTION");
        switchProfileIntent.putExtra("com.symbol.datawedge.api.SWITCH_TO_PROFILE", ConstantUtils.DATA_WEDGE_PROFILE_NAME);

        sendBroadcast(switchProfileIntent);
    }

    /**
     * saveImage is used to save image in internal storage.
     *
     * @param bitmapImage It consist image in bitmat format
     * @param mTimeStamp  It consist timestamp
     */
    private String saveImage(Bitmap bitmapImage, Instant mTimeStamp)
    {
        File imageDir = new File(getApplicationContext().getExternalFilesDir("Image").getAbsolutePath());
        if (!imageDir.exists())
        {
            imageDir.mkdirs();
        }

        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                .withZone(ZoneId.systemDefault());
        File filePath = new File(imageDir, "Image_" + DATE_TIME_FORMATTER.format(mTimeStamp) + ".PNG");
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(filePath);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            Log.d(TAG, "saveImage() : " + filePath);
            MediaStore.Images.Media.insertImage(getContentResolver(), String.valueOf(filePath),
                    "Image_" + DATE_TIME_FORMATTER.format(mTimeStamp) + ".PNG", null);
        }
        catch (Exception e)
        {
            Log.e(TAG, "saveImage", e);
        }
        finally
        {
            try
            {
                if (fos != null)
                {
                    fos.close();
                }
            }
            catch (IOException e)
            {
                Log.e(TAG, "saveImage", e);
            }
        }
        return imageDir.getAbsolutePath();
    }
}
