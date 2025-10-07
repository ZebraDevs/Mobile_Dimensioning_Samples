package com.sample.mdintegrationapp;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.sample.mdintegrationapp.Config.ALLOW_EMPTY_BARCODE;
import static com.sample.mdintegrationapp.Config.AUTOMATIC_DIM;
import static com.sample.mdintegrationapp.Config.AUTOMATIC_UPLOAD;
import static com.sample.mdintegrationapp.Config.CONNECT_TIMEOUT;
import static com.sample.mdintegrationapp.Config.NUM_RETRIES;
import static com.sample.mdintegrationapp.Config.PASSWORD;
import static com.sample.mdintegrationapp.Config.READ_TIMEOUT;
import static com.sample.mdintegrationapp.Config.REPORT_IMAGE;
import static com.sample.mdintegrationapp.Config.RETRY_DELAY;
import static com.sample.mdintegrationapp.Config.URL;
import static com.sample.mdintegrationapp.Config.USERNAME;
import static com.sample.mdintegrationapp.Constants.DIMENSIONING_RESULT_INTENT_ACTION_GET_DIMENSION;
import static com.sample.mdintegrationapp.Constants.DIMENSIONING_RESULT_INTENT_EXTRA_DIMENSIONING_RESULT;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.sample.mdintegrationapp.publisher.HttpPublisher;
import com.sample.mdintegrationapp.publisher.HttpRequest;
import com.sample.mdintegrationapp.publisher.HttpRequestResponseListener;
import com.sample.mdintegrationapp.publisher.HttpResponse;
import com.sample.mdintegrationapp.publisher.PublisherSettings;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends DimensioningBaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private View mMainLayout;
    private ProgressBar mProgressBar;
    private ImageView mResultImageView;
    private EditText mBarcodeEditText;
    private Button mUploadButton;
    private ToggleButton mAttribute1Button, mAttribute2Button, mAttribute3Button, mAttribute4Button;
    public static final Gson mGson = new Gson();
    private static Bitmap mBitmapImage = null;
    private static String mURL = null;
    private DimensioningResult mDimensioningResult;
    private boolean mHideUI = false;
    private boolean mTriggerAfterID = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        initialization(); //Init UI elements

        new Thread(() ->
        {
            Config.setConfigDirectory(getExternalFilesDir(null) + File.separator);

            Config.init();
            initializePublisher();

            try {
                parseAttributesFile();
            } catch (IOException e) {
                Log.e(TAG, "Failed to load attributes file", e);
            }
            mTriggerAfterID = (Boolean.parseBoolean(Config.getSetting(AUTOMATIC_DIM)) && !Boolean.parseBoolean(Config.getSetting(ALLOW_EMPTY_BARCODE)));
            mAutomaticTrigger = mHideUI = (Boolean.parseBoolean(Config.getSetting(AUTOMATIC_DIM)) && Boolean.parseBoolean(Config.getSetting(ALLOW_EMPTY_BARCODE)));
            if (!mHideUI)
                showUI();
        }).start(); //Init Config
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(Constants.UPLOAD_BUTTON_IS_VISIBLE, mUploadButton.getVisibility() == VISIBLE);
        savedInstanceState.putBoolean(Constants.START_BUTTON_IS_ENABLED, mStartDimensioningButton.isEnabled());
        if (mDimensioningResult != null)
            mDimensioningResult.setImage(null); // Can't fit BASE64-encoded image in Bundle
        savedInstanceState.putString(Constants.DIMENSIONING_RESULT, mGson.toJson(mDimensioningResult));
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        boolean isUploadButtonVisible = savedInstanceState.getBoolean(Constants.UPLOAD_BUTTON_IS_VISIBLE);
        boolean isStartButtonEnabled = savedInstanceState.getBoolean(Constants.START_BUTTON_IS_ENABLED);
        mDimensioningResult = mGson.fromJson(savedInstanceState.getString(Constants.DIMENSIONING_RESULT), DimensioningResult.class);
        mUploadButton.setVisibility(isUploadButtonVisible ? VISIBLE : GONE);
        if (isStartButtonEnabled)
            enableStartDimensioningButton();
        else
            disableStartDimensioningButton();
        setBackgroundImage(mBitmapImage);
    }

    /**
     * onDestroy function is used to unregister the broadcast receiver.
     */
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        unregisterReceiver(datawedgeBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart()");
        enableDimensioning();
        super.onStart();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            setDataWedgeProfile();
        }
    }

    /**
     * Initialization of all Drawables
     */
    public void initialization() {
        mMainLayout = findViewById(R.id.main);
        mProgressBar = findViewById(R.id.progress_bar);
        mResultImageView = findViewById(R.id.result_imageview);
        mBarcodeEditText = findViewById(R.id.edittext_barcode);
        mStartDimensioningButton = findViewById(R.id.button_start_dimensioning);
        mUploadButton = findViewById(R.id.button_upload);
        mAttribute1Button = findViewById(R.id.button_attribute_1);
        mAttribute2Button = findViewById(R.id.button_attribute_2);
        mAttribute3Button = findViewById(R.id.button_attribute_3);
        mAttribute4Button = findViewById(R.id.button_attribute_4);

        mStartDimensioningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String parcelID = mBarcodeEditText.getText().toString();
                setBackgroundImage(null);
                mUploadButton.setVisibility(GONE);
                Log.d(TAG, "parcelID is: " + parcelID);
                Log.d(TAG, "Start Dimensioning");
                if (mIsDimensionServiceEnabled) {
                    sendIntentApi(DimensioningConstants.INTENT_ACTION_GET_DIMENSION, DimensioningConstants.PARCEL_ID, parcelID);
                }
                mBarcodeEditText.setEnabled(false);
                disableStartDimensioningButton();
            }
        });

        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleUpload();
            }
        });

        mBarcodeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!mIsDimensionServiceEnabled || Boolean.parseBoolean(Config.getSetting(Config.ALLOW_EMPTY_BARCODE)))
                    return; //Ignore barcode text change if ENABLE intent response not received
                if (charSequence.length() == 0)
                    disableStartDimensioningButton();
                else
                    enableStartDimensioningButton();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mBarcodeEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (mTriggerAfterID && mBarcodeEditText.isEnabled()) {
                    if (v.getText().length() != 0) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                            if (event == null || !event.isShiftPressed()) {
                                // Set mBarcodeEditText disabled to prevent multiple triggers
                                mBarcodeEditText.setEnabled(false);
                                mStartDimensioningButton.callOnClick();
                            }
                        }
                    }
                }
                return false;
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(Constants.DATA_WEDGE_INTENT_ACTION_SCANNING_RESULT);
        registerReceiver(datawedgeBroadcastReceiver, filter, RECEIVER_EXPORTED);
    }

    private void initializePublisher() {
        if (mURL == null && !TextUtils.isEmpty(Config.getSetting(URL))) {
            mURL = Config.getSetting(URL);

            PublisherSettings settings = new PublisherSettings();
            if (Config.getSetting(RETRY_DELAY) != null)
                settings.setRetryDelay(Long.parseLong(Config.getSetting(RETRY_DELAY)));
            if (Config.getSetting(NUM_RETRIES) != null)
                settings.setNumRetries(Integer.parseInt(Config.getSetting(NUM_RETRIES)));
            if (Config.getSetting(CONNECT_TIMEOUT) != null)
                settings.setConnectTimeout(Integer.parseInt(Config.getSetting(CONNECT_TIMEOUT)));
            if (Config.getSetting(READ_TIMEOUT) != null)
                settings.setReadTimeout(Integer.parseInt(Config.getSetting(READ_TIMEOUT)));
            if (!TextUtils.isEmpty(Config.getSetting(USERNAME)))
                settings.setUsername(Config.getSetting(USERNAME));
            if (!TextUtils.isEmpty(Config.getSetting(PASSWORD)))
                settings.setPassword(Config.getSetting(PASSWORD));
            HttpPublisher.setPublisherSettings(settings);
        }
    }

    private void handleUpload() {
        runOnUiThread(() -> {
            mUploadButton.setVisibility(View.GONE);
            disableStartDimensioningButton();
            List<Boolean> attributes = Arrays.asList(mAttribute1Button.isChecked(), mAttribute2Button.isChecked(),
                    mAttribute3Button.isChecked(), mAttribute4Button.isChecked());
            mDimensioningResult.setAttributes(attributes);
            mAttribute1Button.setChecked(false);
            mAttribute2Button.setChecked(false);
            mAttribute3Button.setChecked(false);
            mAttribute4Button.setChecked(false);
            mBarcodeEditText.getText().clear();

            boolean convertBitmap = Boolean.parseBoolean(Config.getSetting(REPORT_IMAGE)) && mURL != null && mBitmapImage != null;
            new Thread(() ->
            { //Run slow bitmap operation on separate thread
                //Only convert the image if needed
                String action = getIntent().getAction();
                if (action.equals(DIMENSIONING_RESULT_INTENT_ACTION_GET_DIMENSION))
                    //startActivityForResult will receive JSON string of DimensioningResult without image (too big)
                    setResult(RESULT_OK, new Intent().putExtra(DIMENSIONING_RESULT_INTENT_EXTRA_DIMENSIONING_RESULT, mGson.toJson(mDimensioningResult)));
                if (convertBitmap)
                    mDimensioningResult.setImage("data:image/jpeg;base64," + convertBitmapToBase64(mBitmapImage));
                else
                    mDimensioningResult.setImage(null);
                setBackgroundImage(null);
                if (mURL != null)
                    publishDimensioningResult(mDimensioningResult);
                else
                    completeHandleUpload(true);
            }).start();
        });
    }

    private void publishDimensioningResult(DimensioningResult result) {
        showToast(getString(R.string.uploading_data));
        HttpRequest request = new HttpRequest();
        request.setUrl(mURL);
        // Use Gson to create a JSON string from the DimensioningResult object
        // The JSON content can be changed by modifying the DimensioningResult class code
        request.setData(mGson.toJson(result));
        request.setContentType("application/json");
        HttpPublisher.sendRequest(request, new HttpRequestResponseListener() {
            @Override
            public void onHttpRequestResponse(HttpResponse response) {
                runOnUiThread(() -> {
                    boolean success = false;
                    Log.d(TAG, "HTTP response " + response.getResponseCode());
                    if (response.getResponseCode() == 0)
                        showToast(response.getResponse());
                    else if (response.getResponseCode() >= 200 && response.getResponseCode() <= 299) {
                        success = true;
                        showToast("Upload success " + response.getResponseCode());
                    } else
                        showToast("Upload failure " + response.getResponseCode());
                    completeHandleUpload(success);
                });
            }
        });
    }

    private void completeHandleUpload(boolean success) {
        runOnUiThread(() -> {
            String action = getIntent().getAction();
            if (success && action.equals(DIMENSIONING_RESULT_INTENT_ACTION_GET_DIMENSION))
                //Return to calling app if launched with startActivityForResult
                finish();
            else if (success && !action.equals(Intent.ACTION_MAIN))
                // Return to calling app if launched by web browser Intent
                moveTaskToBack(true);
            else if (success && mIsDimensionServiceEnabled &&
                    Boolean.parseBoolean(Config.getSetting(Config.ALLOW_EMPTY_BARCODE)) || mBarcodeEditText.getText().length() != 0)
                enableStartDimensioningButton();
            else if (!success) {
                mUploadButton.setVisibility(VISIBLE);
                if (mHideUI) {
                    showUI();
                    mHideUI = false;
                }
            }
        });
    }

    /**
     * onActivityResult handles the intent response from SDK and perform the action accordingly.
     *
     * @param requestCode is used as an identity for Client and SDK request and response intent communication.
     * @param resultCode  is a response result of each intent.
     * @param intent      Intent received from SDK.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        try {
            if (requestCode == REQUEST_CODE) {
                if (intent != null) {
                    String actionName = intent.getAction();
                    int dimResultCode = intent.getIntExtra(DimensioningConstants.RESULT_CODE, DimensioningConstants.FAILURE);
                    String dimResultMessage = intent.getStringExtra(DimensioningConstants.RESULT_MESSAGE);
                    if (dimResultMessage == null)
                        dimResultMessage = "";
                    Log.d(TAG, "onActivityResult: " + actionName + ", " + dimResultCode + ", " + dimResultMessage);

                    switch (actionName) {
                        case DimensioningConstants.INTENT_ACTION_ENABLE_DIMENSION:
                            try {
                                if (!mIsDimensionServiceEnabling) {
                                    // We ignore ENABLE response when no longer enabling
                                } else if (dimResultCode == DimensioningConstants.SUCCESS) {
                                    mIsDimensionServiceEnabled = true;
                                    sendIntentApi(DimensioningConstants.INTENT_ACTION_GET_DIMENSION_PARAMETER);
                                } else if (dimResultCode == DimensioningConstants.FAILURE && mEnableRetryCount < MAX_ENABLE_RETRIES) {
                                    disableStartDimensioningButton();
                                    mEnableRetryCount++;
                                    new Handler(Looper.getMainLooper()).postDelayed(() ->
                                    {
                                        if (mIsDimensionServiceEnabling)
                                            sendIntentApi(DimensioningConstants.INTENT_ACTION_ENABLE_DIMENSION, DimensioningConstants.MODULE,
                                                    DimensioningConstants.PARCEL_MODULE);
                                    }, ENABLE_RETRY_DELAY);
                                } else {
                                    showToast(dimResultMessage);
                                    disableStartDimensioningButton();
                                }
                            } catch (Exception e) {
                                Log.d(TAG, "Exception : " + e);
                            }
                            break;

                        case DimensioningConstants.INTENT_ACTION_GET_DIMENSION:
                            try {
                                if (dimResultCode == DimensioningConstants.SUCCESS || (dimResultCode == DimensioningConstants.CANCELED && mHideUI && Boolean.parseBoolean(Config.getSetting(AUTOMATIC_UPLOAD, "false")))) {
                                    mDimensioningResult = new DimensioningResult();
                                    Bundle extras = intent.getExtras();
                                    if (extras.containsKey(DimensioningConstants.DIMENSIONING_UNIT)) {
                                        String unit = intent.getStringExtra(DimensioningConstants.DIMENSIONING_UNIT);
                                        mDimensioningResult.setDimensionUnit((unit.equals(DimensioningConstants.CM) ? unit.toLowerCase() : "in"));
                                    }
                                    if (extras.containsKey(DimensioningConstants.LENGTH)) {
                                        mDimensioningResult.setLength((intent.getSerializableExtra(DimensioningConstants.LENGTH)).toString());
                                    }
                                    if (extras.containsKey(DimensioningConstants.WIDTH)) {
                                        mDimensioningResult.setWidth((intent.getSerializableExtra(DimensioningConstants.WIDTH)).toString());
                                    }
                                    if (extras.containsKey(DimensioningConstants.HEIGHT)) {
                                        mDimensioningResult.setHeight((intent.getSerializableExtra(DimensioningConstants.HEIGHT)).toString());
                                    }
                                    if (extras.containsKey(DimensioningConstants.LENGTH_STATUS)) {
                                        mDimensioningResult.setLengthStatus(intent.getStringExtra(DimensioningConstants.LENGTH_STATUS));
                                    }
                                    if (extras.containsKey(DimensioningConstants.WIDTH_STATUS)) {
                                        mDimensioningResult.setWidthStatus(intent.getStringExtra(DimensioningConstants.WIDTH_STATUS));
                                    }
                                    if (extras.containsKey(DimensioningConstants.HEIGHT_STATUS)) {
                                        mDimensioningResult.setHeightStatus(intent.getStringExtra(DimensioningConstants.HEIGHT_STATUS));
                                    }
                                    if (extras.containsKey(DimensioningConstants.TIMESTAMP)) {
                                        mDimensioningResult.setTimestamp(intent.getSerializableExtra(DimensioningConstants.TIMESTAMP).toString());
                                        Log.d(TAG, "Time stamp is : " + mDimensioningResult.getTimestamp());
                                    }
                                    if (extras.containsKey(DimensioningConstants.IMAGE)) {
                                        // Can't fit BASE64-encoded image in Bundle that is used for screen rotation (onSaveInstanceState)
                                        // So won't include image in mDimensioningResult until mUploadButton onClick
                                        setBackgroundImage(intent.getParcelableExtra(DimensioningConstants.IMAGE));
                                        Log.d(TAG, "bitmapImage is : " + mBitmapImage);
                                    }
                                    if (extras.containsKey(DimensioningConstants.MESSAGE)) {
                                        String MESSAGE = intent.getStringExtra(DimensioningConstants.MESSAGE);
                                        Log.d(TAG, "MESSAGE is : " + MESSAGE);
                                    }
                                    if (extras.containsKey(DimensioningConstants.PARCEL_ID)) {
                                        // This is broken in 2.0.0.0 release, so we can pull from edit field
                                        String parcelId = intent.getStringExtra(DimensioningConstants.PARCEL_ID);
                                        if (parcelId == null) {
                                            parcelId = mBarcodeEditText.getText().toString();
                                        }
                                        mDimensioningResult.setBarcode(parcelId);
                                        Log.d(TAG, "PARCEL_ID is : " + mDimensioningResult.getBarcode());
                                    }

                                    if (Boolean.parseBoolean(Config.getSetting(AUTOMATIC_UPLOAD, "false")))
                                        handleUpload();
                                    else
                                        mUploadButton.setVisibility(VISIBLE);

                                    if (mHideUI && !Boolean.parseBoolean(Config.getSetting(AUTOMATIC_UPLOAD, "false"))) {
                                        showUI();
                                        mHideUI = false;
                                    }
                                } else if (dimResultCode == DimensioningConstants.CANCELED) {
                                    if (Boolean.parseBoolean(Config.getSetting(AUTOMATIC_DIM)))
                                        mBarcodeEditText.getText().clear();
                                    if (Boolean.parseBoolean(Config.getSetting(Config.ALLOW_EMPTY_BARCODE)) || mBarcodeEditText.getText().length() != 0)
                                        enableStartDimensioningButton();
                                }
                                if ((dimResultCode == DimensioningConstants.SUCCESS) || (dimResultCode == DimensioningConstants.CANCELED)) {
                                    mBarcodeEditText.setEnabled(true);
                                } else {
                                    showToast(dimResultMessage);
                                    sendIntentApi(DimensioningConstants.INTENT_ACTION_DISABLE_DIMENSION, DimensioningConstants.MODULE, DimensioningConstants.PARCEL_MODULE);
                                }
                            } catch (Exception e) {
                                Log.d(TAG, "Exception : " + e);
                            }
                            break;

                        case DimensioningConstants.INTENT_ACTION_GET_DIMENSION_PARAMETER:
                            try {
                                if (dimResultCode == DimensioningConstants.SUCCESS) {
                                    Bundle extras = intent.getExtras();
                                    Bundle params = new Bundle();
                                    String unit = new String();
                                    if (extras.containsKey(DimensioningConstants.READY_LENGTH)) {
                                        BigDecimal readyLength = (BigDecimal) intent.getSerializableExtra(DimensioningConstants.READY_LENGTH);
                                        Log.d(TAG, "Get Dimension Parameter Result ReadyLength: " + readyLength);
                                    }
                                    if (extras.containsKey(DimensioningConstants.READY_WIDTH)) {
                                        BigDecimal readyWidth = (BigDecimal) intent.getSerializableExtra(DimensioningConstants.READY_WIDTH);
                                        Log.d(TAG, "Get Dimension Parameter Result ReadyWidth: " + readyWidth);
                                    }
                                    if (extras.containsKey(DimensioningConstants.READY_HEIGHT)) {
                                        BigDecimal readyHeight = (BigDecimal) intent.getSerializableExtra(DimensioningConstants.READY_HEIGHT);
                                        Log.d(TAG, "Get Dimension Parameter Result ReadyHeight: " + readyHeight);
                                    }
                                    if (extras.containsKey(DimensioningConstants.DIMENSIONING_UNIT)) {
                                        unit = intent.getStringExtra(DimensioningConstants.DIMENSIONING_UNIT);
                                        Log.d(TAG, "Get Dimension Parameter Result Unit : " + unit);
                                    }
                                    if (extras.containsKey(DimensioningConstants.BUNDLE_VERSION)) {
                                        String bundleVersion = intent.getStringExtra(DimensioningConstants.BUNDLE_VERSION);
                                        Log.d(TAG, "Get Dimension Parameter Result BundleVersion: " + bundleVersion);
                                    }
                                    if (extras.containsKey(DimensioningConstants.FRAMEWORK_VERSION)) {
                                        String frameworkVersion = intent.getStringExtra(DimensioningConstants.FRAMEWORK_VERSION);
                                        Log.d(TAG, "Get Dimension Parameter Result FrameworkVersion: " + frameworkVersion);
                                    }
                                    if (extras.containsKey(DimensioningConstants.SERVICE_VERSION)) {
                                        String serviceVersion = intent.getStringExtra(DimensioningConstants.SERVICE_VERSION);
                                        Log.d(TAG, "Get Dimension Parameter Result ServiceVersion: " + serviceVersion);
                                    }
                                    if (extras.containsKey(DimensioningConstants.SUPPORTED_UNITS)) {
                                        List supportedUnits = Arrays.asList(intent.getStringArrayExtra(DimensioningConstants.SUPPORTED_UNITS));
                                        if (Boolean.parseBoolean(Config.getSetting(Config.IMPERIAL_UNITS))) {
                                            if (!supportedUnits.contains(DimensioningConstants.INCH)) {
                                                showToast("Dimensioning does not support Imperial units");
                                            } else if (!unit.equals(DimensioningConstants.INCH)) {
                                                params.putString(DimensioningConstants.DIMENSIONING_UNIT, DimensioningConstants.INCH);
                                            }
                                        } else {
                                            if (!supportedUnits.contains(DimensioningConstants.CM)) {
                                                showToast("Dimensioning does not support Metric units");
                                            } else if (!unit.equals(DimensioningConstants.CM)) {
                                                params.putString(DimensioningConstants.DIMENSIONING_UNIT, DimensioningConstants.CM);
                                            }
                                        }
                                        Log.d(TAG, "Get Dimension Parameter Result supportedUnits: " + supportedUnits);
                                    }
                                    if (extras.containsKey(DimensioningConstants.REGULATORY_APPROVAL)) {
                                        String regulatoryApproval = intent.getStringExtra(DimensioningConstants.REGULATORY_APPROVAL);
                                        Log.d(TAG, "Get Dimension Parameter Result RegulatoryApproval: " + regulatoryApproval);
                                    }
                                    if (extras.containsKey(DimensioningConstants.REPORT_IMAGE)) {
                                        boolean reportImage = intent.getBooleanExtra(DimensioningConstants.REPORT_IMAGE, false);
                                        if (!reportImage) { //always report image
                                            params.putBoolean(DimensioningConstants.REPORT_IMAGE, true);
                                        }
                                        Log.d(TAG, "Get Dimension Parameter Result reportImage: " + reportImage);
                                    }
                                    if (params.isEmpty()) {
                                        if (Boolean.parseBoolean(Config.getSetting(Config.ALLOW_EMPTY_BARCODE)) || mBarcodeEditText.getText().length() != 0)
                                            enableStartDimensioningButton();
                                    } else {
                                        sendIntentApi(DimensioningConstants.INTENT_ACTION_SET_DIMENSION_PARAMETER, params);
                                    }
                                } else {
                                    showToast(dimResultMessage);
                                    sendIntentApi(DimensioningConstants.INTENT_ACTION_DISABLE_DIMENSION, DimensioningConstants.MODULE, DimensioningConstants.PARCEL_MODULE);
                                }
                            } catch (Exception e) {
                                Log.d(TAG, "Exception : " + e);
                            }
                            break;
                        case DimensioningConstants.INTENT_ACTION_SET_DIMENSION_PARAMETER:
                            try {
                                if (dimResultCode == DimensioningConstants.SUCCESS) {
                                    //Below Intent is called to update the ready values everytime the switch is toggled
                                    if (mIsDimensionServiceEnabled) {
                                        sendIntentApi(DimensioningConstants.INTENT_ACTION_GET_DIMENSION_PARAMETER);
                                    }
                                } else {
                                    showToast(dimResultMessage);
                                    if (mIsDimensionServiceEnabled) {
                                        sendIntentApi(DimensioningConstants.INTENT_ACTION_DISABLE_DIMENSION, DimensioningConstants.MODULE, DimensioningConstants.PARCEL_MODULE);
                                    }
                                }
                            } catch (Exception e) {
                                Log.d(TAG, "Exception : " + e);
                            }
                            break;
                        case DimensioningConstants.INTENT_ACTION_DISABLE_DIMENSION:
                            try {
                                if (dimResultCode != DimensioningConstants.SUCCESS) {
                                    showToast(dimResultMessage);
                                }
                            } catch (Exception e) {
                                Log.d(TAG, "Exception : " + e);
                            }
                            break;
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception : " + e);
        }
    }

    private void setBackgroundImage(Bitmap bitmap) {
        mBitmapImage = bitmap;
        runOnUiThread(() -> mResultImageView.setBackground(new BitmapDrawable(getResources(), mBitmapImage)));
    }

    private static String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT).replaceAll("\n", "");
    }

    private final BroadcastReceiver datawedgeBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            intent.getExtras();

            if (action.equals(Constants.DATA_WEDGE_INTENT_ACTION_SCANNING_RESULT)) {
                try {
                    displayScanResult(intent);
                } catch (Exception e) {
                    Log.d(TAG, "Exception : " + e);
                }
            }
        }
    };

    /**
     * displayScanResult function is used to receive the scanned data from broadcast receiver and to display the scanned results.
     *
     * @param initiatingIntent It shows the Intent which initiate the scanning result.
     */
    private void displayScanResult(Intent initiatingIntent) {
        String decodedData = initiatingIntent.getStringExtra(Constants.DATA_WEDGE_INTENT_DATA_KEY);
        if (mBarcodeEditText.isEnabled()) {
            mBarcodeEditText.getText().clear();
            mBarcodeEditText.setText(decodedData);
            if (mTriggerAfterID) {
                // Set mBarcodeEditText disabled to prevent multiple triggers
                mBarcodeEditText.setEnabled(false);
                mStartDimensioningButton.callOnClick();
            }
        }
    }

    private void setDataWedgeProfile() {
        Bundle mainBundle = new Bundle();
        mainBundle.putString("PROFILE_NAME", Constants.DATA_WEDGE_PROFILE_NAME);
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
        intentConfigParams.putString("intent_action", Constants.DATA_WEDGE_INTENT_ACTION_SCANNING_RESULT);
        intentConfigParams.putInt("intent_delivery", 2);

        intentPluginConfig.putBundle("PARAM_LIST", intentConfigParams);

        Bundle keystrokeConfigParams = new Bundle();
        keystrokeConfigParams.putString("keystroke_output_enabled", "false");

        keyStrokePluginConfig.putBundle("PARAM_LIST", keystrokeConfigParams);

        ArrayList<Bundle> configList = new ArrayList<>();
        configList.add(intentPluginConfig);
        configList.add(keyStrokePluginConfig);

        mainBundle.putParcelableArrayList("PLUGIN_CONFIG", configList);

        Intent setConfigIntent = new Intent();
        setConfigIntent.setAction("com.symbol.datawedge.api.ACTION");
        setConfigIntent.putExtra("com.symbol.datawedge.api.SET_CONFIG", mainBundle);

        sendBroadcast(setConfigIntent);

        Intent switchProfileIntent = new Intent();
        switchProfileIntent.setAction("com.symbol.datawedge.api.ACTION");
        switchProfileIntent.putExtra("com.symbol.datawedge.api.SWITCH_TO_PROFILE", Constants.DATA_WEDGE_PROFILE_NAME);

        sendBroadcast(switchProfileIntent);
    }

    private void parseAttributesFile() throws IOException {
        // Read attributes from file as JSON
        String file = Config.getConfigDirectory() + Constants.ATTRIBUTES_FILE_PATH;
        File attributesFile = new File(file);
        Log.i(TAG, "Loading attributes File: " + attributesFile.getAbsolutePath());
        if (attributesFile == null || !attributesFile.exists()) {
            throw new FileNotFoundException("Could not find attributes File: " + file);
        }

        InputStream inputStream = new FileInputStream(attributesFile);
        byte[] buffer = new byte[inputStream.available()];
        inputStream.read(buffer);
        inputStream.close();

        // Convert Byte[] to JSONObject
        Log.i(TAG, "Parsing attributes File...");
        updateAttributeNames(mGson.fromJson(new String(buffer, "UTF-8"), Attribute[].class));
        Log.i(TAG, "Parse Completed Successfully");
    }

    private void updateAttributeNames(Attribute[] attributes) {
        for (int i = 0; i < attributes.length; i++) {
            ToggleButton tb = getAttributeToggleButtonById(i);
            final Attribute attribute = attributes[i];
            if (tb == null)
                break;
            runOnUiThread(() ->
            {
                tb.setTextOn(attribute.getLabelOn());
                tb.setTextOff(attribute.getLabelOff());
                tb.setText(tb.isChecked() ? tb.getTextOn() : tb.getTextOff()); //necessary to instantaneously update
                tb.setVisibility(VISIBLE);
            });
        }
    }

    private ToggleButton getAttributeToggleButtonById(int id) {
        switch (id) {
            case 0:
                return mAttribute1Button;
            case 1:
                return mAttribute2Button;
            case 2:
                return mAttribute3Button;
            case 3:
                return mAttribute4Button;
            default:
                return null;
        }
    }

    private void showUI() {
        runOnUiThread(() ->
        {
            Log.d(TAG, "showUI");
            mMainLayout.setVisibility(VISIBLE);
            mProgressBar.setVisibility(GONE);
        });
    }
}