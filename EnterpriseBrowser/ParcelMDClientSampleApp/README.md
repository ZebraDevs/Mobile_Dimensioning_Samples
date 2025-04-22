# ParcelMDClientSampleApp
This sample demonstrates the Zebra recommended way of using the Mobile Parcel Dimensioning API from a web application running within Zebra Enterprise Browser. Use this as a reference to build your Application.
Note: This sample has been updated to work with Mobile Parcel 1.4.0.0, which removes the requirement for an API token. If you are working with an earlier release (or still want to use the API token), you can enable it by setting "enableToken = true" in [dimensionSample.js](dimensionSample.js).

## Requirements
- This application requires Enterprise Browser 5.0.0.13 or later to be installed. See [here](https://www.zebra.com/us/en/support-downloads/software/mobile-computer-software/enterprise-browser.html).

## Limitations
- Enterprise Browser 5.0.0.13 does not support the equivalent of startForegroundService, only startService (EB.Intent.START_SERVICE). This means that the ENABLE API will not start the dimensioning service but will only work if the service is already running. This may be resolved in a future update, but in practice this should not be a big problem because the service starts automatically.
- Enterprise Browser 5.0.0.13 only supports the PendingIntent callback for the sendBroadcast (EB.Intent.BROADCAST) type of Intent. This means that you can get a response for the GET_DIMENSION / GET_DIMENSION_PARAMETER / SET_DIMENSION_PARAMETER / DISABLE_DIMENSION APIs, but not for the ENABLE_DIMENSION API, which must be called via startService. This means the application will not be able to see if the ENABLE API fails. This may be resolved in a future update.

## Installation
- [Config.xml](Config.xml) – This is the Enterprise Browser configuration. The only changes from the defaults are to enable DebugMode, enable usedwforscanning, and set the StartPage to dimensionSample.html. Copy this file to /Internal shared storage/Android/data/com.zebra.mdna.enterprisebrowser/.
- [dimensionSample.css](dimensionSample.css) – This is the application stylesheet. Copy this file to /Internal shared storage/Android/data/com.zebra.mdna.enterprisebrowser/.
- [dimensionSample.html](dimensionSample.html) – This is the application HTML content. Copy this file to /Internal shared storage/Android/data/com.zebra.mdna.enterprisebrowser/.
- [dimensionSample.js](dimensionSample.js) – This is the application JavaScript code. It uses EB API to generate the API token (if configured) and call the dimensioning API. Copy this file to /Internal shared storage/Android/data/com.zebra.mdna.enterprisebrowser/.
- [EnterpriseBrowserAllowList.pdf](EnterpriseBrowserAllowList.pdf) – This PDF contains StageNow barcodes to add Enterprise Browser to the allow list for calling the dimensioning API. This is only required if you are using the API token. See Note above. If the API token is required, open the StageNow application on the device and scan the barcodes to enable token generation.
