using Android.App;
using Android.Content;
using Android.Content.PM;
using Android.Graphics;
using Android.OS;
using Android.Util;
using Android.Views;
using Android.Widget;

using AndroidX.AppCompat.App;
using Com.Zebra.Security.Broadcastprotection;
using Java.Math;
using Boolean = System.Boolean;
using Exception = System.Exception;
using String = System.String;

namespace XamarinDimensionSample.Droid
{
    [Activity(Label = "@string/app_name", Theme = "@style/AppTheme.NoActionBar", MainLauncher = true)]
    public class MainActivity : AppCompatActivity, View.IOnClickListener, IOnInitCallback
    {
        private const string TAG = "MainActivity";
        private EditText mTxtLength, mTxtWidth, mTxtHeight, mBoxID;
        private Button mStartDimensioningButton, mScanBarcodeButton;
        private TextView mTextViewReset;
        private TextView mTextViewLengthIcon, mTextViewWidthIcon, mTextViewHeightIcon;
        private TextView mTextViewInch;
        private TextView mTextViewCm;
        private View mLayoutLength, mLayoutWidth, mLayoutHeight;
        private Context mContext = null;
        private String mUnit = ConstantUtils.INCH;
        private static BigDecimal mReadyLength, mReadyWidth, mReadyHeight;
        private BroadCastAuthenticator vIntentprotect = new BroadCastAuthenticator();
        private static String token = "";
        private Boolean mRetryingToken = false;

        private static Boolean mPersistValue = false;
        private static BigDecimal mPersistLength, mPersistWidth, mPersistHeight;
        private static String mPersistLengthStatus = "READY_STATUS";
        private static String mPersistWidthStatus = "READY_STATUS";
        private static String mPersistHeightStatus = "READY_STATUS";
        private static String mPersistUnit = null;

        protected override void OnCreate(Bundle savedInstanceState)
        {
            base.OnCreate(savedInstanceState);
            SetContentView(Resource.Layout.activity_main);
            if (token == null || token.Length == 0)
            {
                vIntentprotect.Initialize(ApplicationContext, this);
            }
            Initialization();
            mScanBarcodeButton.SetOnClickListener(this);
            mStartDimensioningButton.SetOnClickListener(this);
            mTextViewReset.SetOnClickListener(this);
            mTextViewCm.SetOnClickListener(this);
            mTextViewInch.SetOnClickListener(this);
            if (savedInstanceState != null)
            {
                mPersistValue = true;
            }
        }

        protected override void OnStart()
        {
            Log.Debug(TAG, "onStart()");
            SendIntentApi(ConstantUtils.INTENT_ACTION_ENABLE_DIMENSION, ConstantUtils.ENABLE_EXTRA_KEY, ConstantUtils.ENABLE_EXTRA_VALUE);
            base.OnStart();
        }

        public override void OnBackPressed()
        {
            mPersistValue = false;
            SendIntentApi(ConstantUtils.INTENT_ACTION_DISABLE_DIMENSION);
            base.OnBackPressed();
        }

        protected override void OnRestart()
        {
            Log.Debug(TAG, "onRestart()");
            mPersistValue = false;
            mBoxID.Text = "";
            base.OnRestart();
        }

        protected override void OnStop()
        {
            Log.Debug(TAG, "onStop()");
            SendIntentApi(ConstantUtils.INTENT_ACTION_DISABLE_DIMENSION);
            base.OnStop();
        }

        public void Initialization()
        {
            mContext = ApplicationContext;
            mTextViewReset = (TextView)FindViewById(Resource.Id.reset);
            mTxtLength = (EditText)FindViewById(Resource.Id.editTextLength);
            mTxtWidth = (EditText)FindViewById(Resource.Id.editTextWidth);
            mTxtHeight = (EditText)FindViewById(Resource.Id.editTextHeight);
            mTextViewLengthIcon = (TextView)FindViewById(Resource.Id.lengthTextViewIcon);
            mTextViewWidthIcon = (TextView)FindViewById(Resource.Id.widthTextViewIcon);
            mTextViewHeightIcon = (TextView)FindViewById(Resource.Id.heightTextViewIcon);
            mBoxID = (EditText)FindViewById(Resource.Id.editTextBoxID);
            mStartDimensioningButton = (Button)FindViewById(Resource.Id.button);
            mScanBarcodeButton = (Button)FindViewById(Resource.Id.buttonScanBarcode);
            mLayoutLength = FindViewById(Resource.Id.linearlayoutLength);
            mLayoutWidth = FindViewById(Resource.Id.linearlayoutWidth);
            mLayoutHeight = FindViewById(Resource.Id.linearlayoutHeight);
            mTextViewReset = (TextView)FindViewById(Resource.Id.reset);
            mTextViewInch = (TextView)FindViewById(Resource.Id.inch);
            mTextViewCm = (TextView)FindViewById(Resource.Id.cm);
        }

        public void OnClick(View v)
        {
            Log.Debug(TAG, "Button clicked: " + v.Id);
            switch (v.Id)
            {
                case Resource.Id.buttonScanBarcode: //"Scan Barcode" button pressed
                    mBoxID.FocusableInTouchMode = true;
                    mBoxID.RequestFocus();
                    mBoxID.Text = "";
                    Intent dwIntent = new Intent();
                    dwIntent.SetAction("com.symbol.datawedge.api.ACTION");
                    dwIntent.PutExtra("com.symbol.datawedge.api.SOFT_SCAN_TRIGGER", "START_SCANNING");
                    SendBroadcast(dwIntent);
                    Log.Debug(TAG, "Calling Start Scanning Intent action= " + dwIntent.Action);
                    break;
                case Resource.Id.button: //"Start Dimensioning" button pressed
                    String boxID = mBoxID.Text.ToString();
                    mBoxID.Text = "";
                    Log.Debug(TAG, "boxid is: " + boxID);
                    Log.Error(TAG, "Start Dimensioning");
                    SendIntentApi(ConstantUtils.INTENT_ACTION_GET_DIMENSION, ConstantUtils.BOX_ID, boxID);
                    break;
                case Resource.Id.reset:
                    Log.Error(TAG, "Reset");
                    mPersistValue = false;
                    SendIntentApi(ConstantUtils.INTENT_ACTION_GET_DIMENSION_PARAMETER);
                    Log.Debug(TAG, "Calling Get Dimension Intent ");
                    break;
                case Resource.Id.inch:
                    CallIntentApiForToggleSwitch(ConstantUtils.INCH);
                    mTextViewInch.SetBackgroundDrawable(mContext.Resources.GetDrawable(Resource.Drawable.selected_inch_bg, null));
                    mTextViewCm.SetBackgroundDrawable(mContext.Resources.GetDrawable(Resource.Drawable.unselected_cm_bg, null));
                    break;
                case Resource.Id.cm:
                    CallIntentApiForToggleSwitch(ConstantUtils.CM);
                    mTextViewInch.SetBackgroundDrawable(mContext.Resources.GetDrawable(Resource.Drawable.unselected_inch_bg, null));
                    mTextViewCm.SetBackgroundDrawable(mContext.Resources.GetDrawable(Resource.Drawable.selected_cm_bg, null));
                    break;
            }
        }

        /**
         * callIntentApiForToggleSwitch function is used to call set dimension parameter for selected unit switch.
         * @param selectedUnit It contain current value of toggle switch i.e. cm/inch
         */
        public void CallIntentApiForToggleSwitch(String selectedUnit)
        {
            mPersistValue = false;
            SendIntentApi(ConstantUtils.INTENT_ACTION_SET_DIMENSION_PARAMETER, ConstantUtils.DIMENSIONING_UNIT, selectedUnit);
            Log.Debug(TAG, "Calling Set Dimension Parameter Intent selectedUnit = " + selectedUnit);
        }

        public void SendIntentApi(String action, String extraKey, Boolean extraValue)
        {
            Bundle extras = new Bundle();
            extras.PutBoolean(extraKey, extraValue);
            SendIntentApi(action, extras);
        }

        public void SendIntentApi(String action, String extraKey, String extraValue)
        {
            Bundle extras = new Bundle();
            extras.PutString(extraKey, extraValue);
            SendIntentApi(action, extras);
        }

        public void SendIntentApi(String action)
        {
            SendIntentApi(action, null);
        }

        public void SendIntentApi(String action, Bundle extras)
        {
            if (token == null || token.Length == 0)
            {
                Log.Error(TAG, "Token is Null");
                return;
            }

            Log.Debug(TAG, "SendIntentApi " + action);
            Intent intent = new Intent();
            intent.SetAction(action);
            intent.SetPackage(ConstantUtils.CMP_PACKAGE);
            intent.PutExtra(ConstantUtils.PACKAGE_NAME, PackageName);
            intent.PutExtra(ConstantUtils.API_TOKEN, token);

            if (extras != null)
            {
                intent.PutExtras(extras);
            }

            PendingIntent lobPendingIntent = CreatePendingResult(ConstantUtils.REQUEST_CODE, new Intent(),
                    PendingIntentFlags.UpdateCurrent);

            intent.PutExtra(ConstantUtils.CALLBACK_RESPONSE, lobPendingIntent);

            if (intent.Action.Equals(ConstantUtils.INTENT_ACTION_ENABLE_DIMENSION))
            {
                StartForegroundService(intent);
            }
            else
            {
                SendBroadcast(intent);
            }
        }

        /**
         * onActivityResult handles the intent response from dimensioning and performs the corresponding action.
         *
         * @param requestCode is used as an identity for Client and Framework request and response intent communication.
         * @param resultCode is a response result of each intent.
         * @param intent Intent received from Framework.
         */
        protected override void OnActivityResult(int requestCode, Result resultCode, Intent intent)
        {
            base.OnActivityResult(requestCode, resultCode, intent);
            try
            {
                if (requestCode == ConstantUtils.REQUEST_CODE)
                {
                    if (intent != null)
                    {
                        String actionName = intent.Action;
                        int dimResultCode = intent.GetIntExtra(ConstantUtils.RESULT_CODE, ConstantUtils.FAIL);
                        String dimResultMessage = intent.GetStringExtra(ConstantUtils.RESULT_MESSAGE);
                        if (dimResultMessage == null)
                            dimResultMessage = "";
                        Log.Debug(TAG, "onActivityResult: " + actionName + ", " + dimResultCode + ", " + dimResultMessage);

                        if (!mRetryingToken && (dimResultCode == ConstantUtils.ERROR) && dimResultMessage.Equals("Access Denied"))
                        {
                            // The token expired (after 24 hours)
                            mRetryingToken = true;
                            GenerateToken();
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
                                        EnableStartDimensioningButton();
                                        if (mPersistUnit != null)
                                        {
                                            SendIntentApi(ConstantUtils.INTENT_ACTION_SET_DIMENSION_PARAMETER, ConstantUtils.DIMENSIONING_UNIT, mPersistUnit);
                                        }
                                        else
                                        {
                                            SendIntentApi(ConstantUtils.INTENT_ACTION_GET_DIMENSION_PARAMETER);
                                        }
                                    }
                                    else
                                    {
                                        Toast.MakeText(this, dimResultMessage, ToastLength.Short).Show();
                                    }
                                }
                                catch (Exception e)
                                {
                                    Log.Debug(TAG, "Exception : " + e);
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
                                        String lengthStatus = ConstantUtils.NO_DIM;
                                        String widthStatus = ConstantUtils.NO_DIM;
                                        String heightStatus = ConstantUtils.NO_DIM;

                                        Bundle extras = intent.Extras;
                                        if (extras.ContainsKey(ConstantUtils.DIMENSIONING_UNIT))
                                        {
                                            mUnit = intent.GetStringExtra(ConstantUtils.DIMENSIONING_UNIT);
                                        }
                                        if (extras.ContainsKey(ConstantUtils.LENGTH))
                                        {
                                            length = (BigDecimal)intent.GetSerializableExtra(ConstantUtils.LENGTH);
                                        }
                                        if (extras.ContainsKey(ConstantUtils.WIDTH))
                                        {
                                            width = (BigDecimal)intent.GetSerializableExtra(ConstantUtils.WIDTH);
                                        }
                                        if (extras.ContainsKey(ConstantUtils.HEIGHT))
                                        {
                                            height = (BigDecimal)intent.GetSerializableExtra(ConstantUtils.HEIGHT);
                                        }
                                        if (extras.ContainsKey(ConstantUtils.LENGTH_STATUS))
                                        {
                                            lengthStatus = intent.GetStringExtra(ConstantUtils.LENGTH_STATUS);
                                        }
                                        if (extras.ContainsKey(ConstantUtils.WIDTH_STATUS))
                                        {
                                            widthStatus = intent.GetStringExtra(ConstantUtils.WIDTH_STATUS);
                                        }
                                        if (extras.ContainsKey(ConstantUtils.HEIGHT_STATUS))
                                        {
                                            heightStatus = intent.GetStringExtra(ConstantUtils.HEIGHT_STATUS);
                                        }
                                        if (extras.ContainsKey(ConstantUtils.PARCEL_ID))
                                        {
                                            String parcelid = intent.GetStringExtra(ConstantUtils.PARCEL_ID);
                                            Log.Debug(TAG, "PARCEL_ID is : " + parcelid);
                                        }

                                        ShowDimensioningParameterStatus(length, width, height, lengthStatus, widthStatus, heightStatus, mUnit);
                                    }
                                    else
                                    {
                                        ShowDimensioningParameterStatus(mReadyLength, mReadyWidth, mReadyHeight,
                                                ConstantUtils.READY_STATUS, ConstantUtils.READY_STATUS, ConstantUtils.READY_STATUS, mUnit);
                                    }
                                    if ((dimResultCode != ConstantUtils.SUCCESS) && (dimResultCode != ConstantUtils.CANCELED))
                                    {
                                        Toast.MakeText(this, dimResultMessage, ToastLength.Short).Show();
                                    }
                                }
                                catch (Exception e)
                                {
                                    Log.Debug(TAG, "Exception : " + e);
                                }
                                break;

                            case ConstantUtils.INTENT_ACTION_GET_DIMENSION_PARAMETER:
                                try
                                {
                                    Bundle extras = intent.Extras;
                                    if (extras.ContainsKey(ConstantUtils.READY_LENGTH))
                                    {
                                        mReadyLength = (BigDecimal)intent.GetSerializableExtra(ConstantUtils.READY_LENGTH);
                                        Log.Debug(TAG, "Get Dimension Parameter Result mReadyLength: " + mReadyLength);
                                    }
                                    if (extras.ContainsKey(ConstantUtils.READY_WIDTH))
                                    {
                                        mReadyWidth = (BigDecimal)intent.GetSerializableExtra(ConstantUtils.READY_WIDTH);
                                        Log.Debug(TAG, "Get Dimension Parameter Result mReadyWidth: " + mReadyWidth);
                                    }
                                    if (extras.ContainsKey(ConstantUtils.READY_HEIGHT))
                                    {
                                        mReadyHeight = (BigDecimal)intent.GetSerializableExtra(ConstantUtils.READY_HEIGHT);
                                        Log.Debug(TAG, "Get Dimension Parameter Result mReadyHeight: " + mReadyHeight);
                                    }
                                    if (extras.ContainsKey(ConstantUtils.DIMENSIONING_UNIT))
                                    {
                                        mUnit = intent.GetStringExtra(ConstantUtils.DIMENSIONING_UNIT);
                                        Log.Debug(TAG, "Get Dimension Parameter Result mUnit : " + mUnit);
                                    }
                                    if (extras.ContainsKey(ConstantUtils.SUPPORTED_UNITS))
                                    {
                                        String[] mSupportedUnit = intent.GetStringArrayExtra(ConstantUtils.SUPPORTED_UNITS);
                                        UpdateUnitSwitch(mSupportedUnit);
                                    }
                                    if (mPersistValue && mPersistUnit != null)
                                    {
                                        ShowDimensioningParameterStatus(mPersistLength, mPersistWidth, mPersistHeight,
                                                mPersistLengthStatus, mPersistWidthStatus, mPersistHeightStatus, mPersistUnit);
                                    }
                                    else
                                    {
                                        ShowDimensioningParameterStatus(mReadyLength, mReadyWidth, mReadyHeight,
                                            ConstantUtils.READY_STATUS, ConstantUtils.READY_STATUS, ConstantUtils.READY_STATUS, mUnit);
                                    }
                                    if (dimResultCode != ConstantUtils.SUCCESS)
                                    {
                                        Toast.MakeText(this, dimResultMessage, ToastLength.Short).Show();
                                    }
                                }
                                catch (Exception e)
                                {
                                    Log.Debug(TAG, "Exception : " + e);
                                }
                                break;
                            case ConstantUtils.INTENT_ACTION_SET_DIMENSION_PARAMETER:
                                try
                                {
                                    if (dimResultCode == ConstantUtils.SUCCESS)
                                    {
                                        SendIntentApi(ConstantUtils.INTENT_ACTION_GET_DIMENSION_PARAMETER);
                                    }
                                    else
                                    {
                                        Toast.MakeText(this, dimResultMessage, ToastLength.Short).Show();
                                    }
                                }
                                catch (Exception e)
                                {
                                    Log.Debug(TAG, "Exception : " + e);
                                }
                                break;
                            case ConstantUtils.INTENT_ACTION_DISABLE_DIMENSION:
                                try
                                {
                                    if (dimResultCode != ConstantUtils.SUCCESS)
                                    {
                                        Toast.MakeText(this, dimResultMessage, ToastLength.Short).Show();
                                    }
                                }
                                catch (Exception e)
                                {
                                    Log.Debug(TAG, "Exception : " + e);
                                }
                                break;
                        }
                    }
                }
            }
            catch (Exception e)
            {
                Log.Debug(TAG, "Exception : " + e);
            }
        }

        /**
         * enableStartDimensioningButton function is used to Enable the START DIMENSION button.
         */
        public void EnableStartDimensioningButton()
        {
            RunOnUiThread(() =>
            {
                mStartDimensioningButton.Enabled = true;
                mStartDimensioningButton.Clickable = true;
                mStartDimensioningButton.Background.SetColorFilter(Color.ParseColor("#2185D5"), PorterDuff.Mode.SrcAtop);
            });
        }

        /**
         * showDimensioningParameterStatus function is used to display Dimensioning Result.
         *
         * @param length Length of Parcel.
         * @param width Width of Parcel.
         * @param height Height of Parcel.
         * @param mLengthStatus LengthStatus show if the length of parcel is in Allowed range.
         * @param mWidthStatus WidthStatus show if the width of parcel is in Allowed range.
         * @param mHeightStatus HeigthStatus show if the Height of parcel is in Allowed range.
         * @param unit unit determines whether dimensioning result is in CM or Inch.
         */
        public void ShowDimensioningParameterStatus(BigDecimal length, BigDecimal width,
                                                    BigDecimal height, String mLengthStatus,
                                                    String mWidthStatus, String mHeightStatus, String unit)
        {
            String uiUnit = (unit.Equals(ConstantUtils.INCH) ? ConstantUtils.IN : unit.ToLower());

            String dimensionLength = length.ToString() + uiUnit;
            mTxtLength.Text = dimensionLength;
            String dimensionWidth = width.ToString() + uiUnit;
            mTxtWidth.Text = dimensionWidth;
            String dimensionHeight = height.ToString() + uiUnit;
            mTxtHeight.Text = dimensionHeight;

            mPersistLength = length;
            mPersistWidth = width;
            mPersistHeight = height;
            mPersistLengthStatus = mLengthStatus;
            mPersistWidthStatus = mWidthStatus;
            mPersistHeightStatus = mHeightStatus;
            mPersistUnit = unit;
            mPersistValue = true;

            if (unit.Equals(ConstantUtils.CM))
            {
                mTextViewInch.SetBackgroundDrawable(mContext.Resources.GetDrawable(Resource.Drawable.unselected_inch_bg, null));
                mTextViewCm.SetBackgroundDrawable(mContext.Resources.GetDrawable(Resource.Drawable.selected_cm_bg, null));
            }
            else if (unit.Equals(ConstantUtils.INCH))
            {
                mTextViewInch.SetBackgroundDrawable(mContext.Resources.GetDrawable(Resource.Drawable.selected_inch_bg, null));
                mTextViewCm.SetBackgroundDrawable(mContext.Resources.GetDrawable(Resource.Drawable.unselected_cm_bg, null));
            }

            if (mWidthStatus.Equals(ConstantUtils.NO_DIM) || mHeightStatus.Equals(ConstantUtils.NO_DIM) ||
                    mLengthStatus.Equals(ConstantUtils.NO_DIM))
            {
                mTxtLength.Text = ConstantUtils.NO_DIM;
                mLayoutLength.SetBackgroundDrawable(mContext.Resources.GetDrawable(Resource.Drawable.red_background, null));
                mTextViewLengthIcon.SetCompoundDrawablesWithIntrinsicBounds(0, 0, Resource.Drawable.warning_red, 0);
                mTxtWidth.Text = ConstantUtils.NO_DIM;
                mLayoutWidth.SetBackgroundDrawable(mContext.Resources.GetDrawable(Resource.Drawable.red_background, null));
                mTextViewWidthIcon.SetCompoundDrawablesWithIntrinsicBounds(0, 0, Resource.Drawable.warning_red, 0);
                mTxtHeight.Text = ConstantUtils.NO_DIM;
                mLayoutHeight.SetBackgroundDrawable(mContext.Resources.GetDrawable(Resource.Drawable.red_background, null));
                mTextViewHeightIcon.SetCompoundDrawablesWithIntrinsicBounds(0, 0, Resource.Drawable.warning_red, 0);
            }
            else
            {
                if (mLengthStatus.Equals(ConstantUtils.ABOVE_RANGE) || mLengthStatus.Equals(ConstantUtils.BELOW_RANGE))
                {
                    mLayoutLength.SetBackgroundDrawable(mContext.Resources.GetDrawable(Resource.Drawable.orange_background, null));
                    mTextViewLengthIcon.SetCompoundDrawablesWithIntrinsicBounds(0, 0, Resource.Drawable.warning, 0);
                }
                else if (mLengthStatus.Equals(ConstantUtils.IN_RANGE))
                {
                    mLayoutLength.SetBackgroundDrawable(mContext.Resources.GetDrawable(Resource.Drawable.green_background, null));
                    mTextViewLengthIcon.SetCompoundDrawablesWithIntrinsicBounds(0, 0, Resource.Drawable.check, 0);
                }
                else if (mLengthStatus.Equals(ConstantUtils.READY_STATUS))
                {
                    mLayoutLength.SetBackgroundDrawable(mContext.Resources.GetDrawable(Resource.Drawable.box, null));
                    mTextViewLengthIcon.SetCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }

                if (mWidthStatus.Equals(ConstantUtils.ABOVE_RANGE) || mWidthStatus.Equals(ConstantUtils.BELOW_RANGE))
                {
                    mLayoutWidth.SetBackgroundDrawable(mContext.Resources.GetDrawable(Resource.Drawable.orange_background, null));
                    mTextViewWidthIcon.SetCompoundDrawablesWithIntrinsicBounds(0, 0, Resource.Drawable.warning, 0);
                }
                else if (mWidthStatus.Equals(ConstantUtils.IN_RANGE))
                {
                    mLayoutWidth.SetBackgroundDrawable(mContext.Resources.GetDrawable(Resource.Drawable.green_background, null));
                    mTextViewWidthIcon.SetCompoundDrawablesWithIntrinsicBounds(0, 0, Resource.Drawable.check, 0);
                }
                else if (mWidthStatus.Equals(ConstantUtils.READY_STATUS))
                {
                    mLayoutWidth.SetBackgroundDrawable(mContext.Resources.GetDrawable(Resource.Drawable.box, null));
                    mTextViewWidthIcon.SetCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }

                if (mHeightStatus.Equals(ConstantUtils.ABOVE_RANGE) || mHeightStatus.Equals(ConstantUtils.BELOW_RANGE))
                {
                    mLayoutHeight.SetBackgroundDrawable(mContext.Resources.GetDrawable(Resource.Drawable.orange_background, null));
                    mTextViewHeightIcon.SetCompoundDrawablesWithIntrinsicBounds(0, 0, Resource.Drawable.warning, 0);
                }
                else if (mHeightStatus.Equals(ConstantUtils.IN_RANGE))
                {
                    mLayoutHeight.SetBackgroundDrawable(mContext.Resources.GetDrawable(Resource.Drawable.green_background, null));
                    mTextViewHeightIcon.SetCompoundDrawablesWithIntrinsicBounds(0, 0, Resource.Drawable.check, 0);
                }
                else if (mHeightStatus.Equals(ConstantUtils.READY_STATUS))
                {
                    mLayoutHeight.SetBackgroundDrawable(mContext.Resources.GetDrawable(Resource.Drawable.box, null));
                    mTextViewHeightIcon.SetCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
            }
        }

        /**
         * UpdateUnitSwitch is used to update unit switch visibility.
         * @param supportedUnits It consist CM/Inch
         */
        public void UpdateUnitSwitch(String[] supportedUnits)
        {
            if (supportedUnits.Length <= 1)
            {
                mTextViewInch.Visibility = ViewStates.Gone;
                mTextViewCm.Visibility = ViewStates.Gone;
            }
            else
            {
                mTextViewInch.Visibility = ViewStates.Visible;
                mTextViewCm.Visibility = ViewStates.Visible;
            }
        }

        public void GenerateToken()
        {
            Log.Debug(TAG, "generateToken()");
            try
            {
                token = vIntentprotect.GetToken(ConstantUtils.SERVICE_IDENTIFIER);
                if (token != null && token.Length != 0)
                {
                    SendIntentApi(ConstantUtils.INTENT_ACTION_ENABLE_DIMENSION, ConstantUtils.ENABLE_EXTRA_KEY, ConstantUtils.ENABLE_EXTRA_VALUE);
                }
                else
                {
                    Log.Debug(TAG, "Token is empty or null");
                    RunOnUiThread(() =>
                    {
                        Toast.MakeText(mContext, "Access Denied", ToastLength.Short).Show();
                    });
                }
            }
            catch (Exception exception)
            {
                Log.Error(TAG, "generateToken", exception);
                RunOnUiThread(() =>
                {
                    Toast.MakeText(mContext, "Access Denied", ToastLength.Short).Show();
                });
            }
        }

        /**
         * BroadcastProtection initialized callback
         */
        public void OnInitialized()
        {
            Log.Debug(TAG, "BroadCastAuthenticator is successfully initialized");
            GenerateToken();
        }
    }
}