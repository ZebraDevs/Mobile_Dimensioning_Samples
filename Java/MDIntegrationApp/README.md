# MDIntegrationApp

This sample demonstrates multiple ways to integrate Mobile Dimensioning into an existing customer environment. The application provides multiple ways to launch dimensioning and provide the dimensioning data to the customer environment.

The basic functionality of the MDIntegrationApp is to allow/require entering a barcode before enabling a Start Dimension button. The application will display attribute buttons that can be used to mark the parcel with certain flags such as fragile or damaged. After dimensioning, a confirmation image is shown and an Upload button is displayed for the user to end the dimensioning session and start uploading the data to a remote server. Many of these features are [configurable](#configuration).

## Launching the Application

There are several ways to launch the MDIntegrationApp beyond the standard tapping on the icon on a home screen or application launcher:

### Normal application launch

If the MDIntegrationApp is launched via the application icon or the Recent Apps tool it will allow for multiple dimensioning sessions. The dimensioning output will be displayed on the screen and possibly transmitted by [HTTP Upload](#http-upload).

### Launch by URL

As defined in the [Android Manifest](app/src/main/AndroidManifest.xml), the MDIntegrationApp defines a scheme and host that allows the application to be launched via a URL in the form `dimensioning://`. For example, you can add the following to an HTML file:
```
<a href="dimensioning://">Start dimensioning</a>
```
Clicking on the link (or launching via JavaScript) will launch the MDIntegrationApp for a single dimensioning session. Once the dimensioning session is complete, the MDIntegrationApp will close, returning control to the web browser.
Standard web browsers, such as Chrome, allow for this method of launching MDIntegration app but do not provide a method for returning dimensioning data directly to the web browser. See [HTTP Upload](#http-upload) for a method to upload the dimension data output to a server for later retrieval.
Note: Zebra [Enterprise Browser](https://techdocs.zebra.com/enterprise-browser/latest/guide/about/) has the ability to send broadcast Intents and receive responses via PendingIntents, so it can directly use the Mobile Dimensioning [Intent API](https://techdocs.zebra.com/mobile-parcel/latest/guide/about/).

### Launch Activity

As defined in the [Android Manifest](app/src/main/AndroidManifest.xml), the MDIntegrationApp defines a `com.sample.mdintegrationapp.GET_DIMENSION` intent that can be used to launch the application via `startActivity`, `startActivityForResult`, `ActivityResultLauncher`, or similar method. If the MDIntegrationApp is launched via this Intent, it will launch for a single dimensioning session. Once the dimensioning session is complete, the MDIntegrationApp will close, returning control to the launching application.
See [Launch Activity For Result](#launch-activity-for-result) for details on the dimensioning data output.

## Dimensioning Data Output

By default, the MDIntegrationApp will display the dimensioning results on the screen but will not output the dimensioning data. There are two methods for outputting the dimensioning data for integration into the customer environment:

### HTTP Upload

If enabled in the configuration, the MDIntegrationApp will upload the dimensioning output to a web server when a dimension session completes. The MDIntegrationApp will disable dimensioning until it receives and displays the response from the server or times out. See [Basic Configuration](#basic-configuration) for details on configuration of this feature.
The data format used by the MDIntegrationApp is JSON, as defined in [DimensioningResult.java](app/src/main/java/com/sample/mdintegrationapp/DimensioningResult.java). If a customer would like to modify the data format, the [DimensioningResult.java](app/src/main/java/com/sample/mdintegrationapp/DimensioningResult.java) class should be modified.
The HTTP Upload feature is designed as an example of how a customer could design an application to transmit each dimension session to a server via HTTP / HTTPS / TCP/IP or other network protocol. Using another network protocol would require modifying [HttpPublisher.java](app/src/main/java/com/sample/mdintegrationapp/publisher/HttpPublisher.java) and related code.
Note: If enabled in the [configuration](#basic-configuration), the MDIntegrationApp will upload the dimensioning output regardless of how the application is launched.

### Launch Activity For Result

As mentioned in [Launch Activity](#launch-activity), the MDIntegrationApp can be launched by Intent. When the dimensioning session is complete, the [MainActivity](app/src/main/java/com/sample/mdintegrationapp/MainActivity.java) will call `setResult()` to send an Intent containing a JSON representation of a [DimensioningResult](app/src/main/java/com/sample/mdintegrationapp/DimensioningResult.java) object back (without image) to the launching application. The confirmation image is not included due to size limitations of Android. If a customer would like to modify the data format, the [DimensioningResult.java](app/src/main/java/com/sample/mdintegrationapp/DimensioningResult.java) class should be modified.

## Configuration

The MDIntegrationApp is configured via text files located in the private application storage found in `/sdcard/Android/data/com.sample.mdintegrationapp/files/`. These files can be updated manually or via an MDM.

### Basic Configuration

Basic configuration for the MDIntegrationApp can be found in the `config.txt` file located in the application storage. A sample can be found [here](config/config.txt).
- `imperial_units` - If `true`, dimensioning results will be in inches, otherwise results will be in centimeters.
- `allow_empty_barcode` - If `true`, the app will allow dimensioning without first requiring a barcode / ID.
- `automatic_dim` - If `true`, the app will automatically start dimensioning when dimensioning is allowed without displaying the Start Dimensioning button.
- `automatic_upload` - If `true`, the app will automatically finish a dimensioning session after dimensioning and start data output without displaying the Upload button.
- `url` - If defined, this parameter enables [HTTP upload](#http-upload) to the defined URL.
- `report_image` - If `true`, the app will include a base64 encoded JPEG image in the HTTP upload.
- `username` - Username for HTTP basic authentication.
- `password` - Password for HTTP basic authentication.
- `connect_timeout` - Connection timeout in ms.
- `read_timeout` - Read timeout in ms.
- `num_retries` - Number of retry attempts before failing data upload.
- `retry_delay` - Delay in ms between data upload retries.

### Attributes Configuration

The MDIntegrationApp includes a feature to optionally add attributes to the application UI and dimensioning output. The attribute values are included in the [DimensioningResult](app/src/main/java/com/sample/mdintegrationapp/DimensioningResult.java) when a dimensioning session completes. These attributes are defined in a JSON array in `attributes.json`. Each of the (up to four) attributes is defined with labels for when the attribute is on (`labelOn`) and off (`labelOff`). Only defined attributes will be displayed in the UI. A sample can be found [here](config/attributes.json).
```
[
  {
    "labelOn": "Battery",
    "labelOff": "Battery"
  },
  {
    "labelOn": "Fragile",
    "labelOff": "Fragile"
  },
  {
    "labelOn": "Hazardous",
    "labelOff": "Hazardous"
  },
  {
    "labelOn": "Damaged",
    "labelOff": "Damaged"
  }
]
```
