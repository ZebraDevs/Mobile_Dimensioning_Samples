using System;

namespace XamarinDimensionSample.Droid
{
    internal class ConstantUtils
    {
        public const String INTENT_ACTION_SDK_APPLICATION_CRASH = "com.zebra.dimensioning.APPLICATION_CRASH";
        public const String INTENT_ACTION_ENABLE_DIMENSION = "com.zebra.dimensioning.ENABLE_DIMENSION";
        public const String INTENT_ACTION_DISABLE_DIMENSION = "com.zebra.dimensioning.DISABLE_DIMENSION";
        public const String INTENT_ACTION_GET_DIMENSION = "com.zebra.parceldimensioning.GET_DIMENSION";
        public const String INTENT_ACTION_GET_DIMENSION_PARAMETER = "com.zebra.parceldimensioning.GET_DIMENSION_PARAMETER";
        public const String INTENT_ACTION_SET_DIMENSION_PARAMETER = "com.zebra.parceldimensioning.SET_DIMENSION_PARAMETER";

        public const String READY_LENGTH = "READY_LENGTH";
        public const String READY_WIDTH = "READY_WIDTH";
        public const String READY_HEIGHT = "READY_HEIGHT";
        public const String DIMENSIONING_UNIT = "DIMENSIONING_UNIT";

        public const String ENABLE_EXTRA_KEY = "MODULE";
        public const String ENABLE_EXTRA_VALUE = "parcel";
        public const String CMP_PACKAGE = "com.zebra.dimensioning";
        public const String CALLBACK_RESPONSE = "CALLBACK_RESPONSE";

        public const String RESULT_CODE = "RESULT_CODE";
        public const String RESULT_MESSAGE = "RESULT_MESSAGE";

        public const String LENGTH_STATUS = "LENGTH_STATUS";
        public const String WIDTH_STATUS = "WIDTH_STATUS";
        public const String HEIGHT_STATUS = "HEIGHT_STATUS";
        public const String LENGTH = "LENGTH";
        public const String WIDTH = "WIDTH";
        public const String HEIGHT = "HEIGHT";
        public const String BUNDLE_VERSION = "BUNDLE_VERSION";
        public const String FRAMEWORK_VERSION = "FRAMEWORK_VERSION";
        public const String SERVICE_VERSION = "SERVICE_VERSION";
        public const String IN = "in";
        public const String INCH = "Inch";
        public const String CM = "CM";
        public const String BOX_ID = "BOX_ID";
        public const String TIMESTAMP = "TIMESTAMP";
        public const String SUPPORTED_UNITS = "SUPPORTED_UNITS";
        public const String REGULATORY_APPROVAL = "REGULATORY_APPROVAL";
        public const String READY_STATUS = "READY_STATUS";
        public const String PARCEL_ID = "PARCEL_ID";
        public const String API_TOKEN = "TOKEN";
        public const String PACKAGE_NAME = "APPLICATION_PACKAGE";
        public const String IMAGE = "IMAGE";
        public const String REPORT_IMAGE = "REPORT_IMAGE";

        public const int REQUEST_CODE = 100;
        public const int SUCCESS = 0;
        public const int FAIL = 1;
        public const int ERROR = 2;
        public const int CANCELED = 3;

        public const String IN_RANGE = "InRange";
        public const String BELOW_RANGE = "BelowRange";
        public const String ABOVE_RANGE = "AboveRange";

        public const String NO_DIM = "NoDim";
        public const String SERVICE_IDENTIFIER = "delegation-zebra-com.zebra.mobiledimensioning-Enable";
    }
}