# Mobile_Dimensioning_Samples
The Zebra Parcel Dimensioning solution provides fast and accurate point-and-shoot dimensioning using an integrated time of flight sensor (ToF) with no external snap-on sensors required - [Zebra Dimensioning (zebra.com)](https://www.zebra.com/us/en/products/software/mobile-computers/zebra-dimensioning.html).

## Java
### ParcelMDClientSampleApp
This sample demonstrates the Zebra recommended way of using the Mobile Parcel Dimensioning API in a Line of Business Application (LOB). This sample app is targeted for Java Development Environment. Use this as a reference to build your Application.
Note: This sample has been updated to work with Mobile Parcel 1.4.0.0, which removes the requirement for an API token. If you are working with an earlier release (or still want to use the API token), you can enable it by setting "REQUIRE_TOKEN = true" in app/build.gradle.
Tested with:
Android Studio Koala

### MDIntegrationApp
This sample demonstrates multiple ways to integrate Mobile Dimensioning into an existing customer environment. The application provides multiple ways to launch dimensioning and provide the dimensioning data to the customer environment.
See [Detailed Documentation](Java/MDIntegrationApp/README.md) here. Tested with: Android Studio Koala

## Enterprise Browser
### ParcelMDClientSampleApp
This sample demonstrates the Zebra recommended way of using the Mobile Parcel Dimensioning API from a web application running within Zebra Enterprise Browser. Use this as a reference to build your Application.
See [Detailed Documentation](EnterpriseBrowser/ParcelMDClientSampleApp/README.md) here. Tested with: Enterprise Browser 5.0.0.13

## Velocity
This sample uses the Mobile Parcel Dimensioning API within Ivanti Velocity Client emulation (e.g. IBM 5250/3270 and VT100/220). Use this as a reference to build your Application.
See [Detailed Documentation](Velocity/README.md) here.

## Xamarin
### ParcelMDClientSampleApp
This sample demonstrates the zebra recommended way of using the Mobile Parcel Dimensioning API in a Line of Business Application (LOB) targeted for the Xamarin Development Enviroment. Use this as a reference to build your Application.

Tested with:
Microsoft Visual Studio Professional 2022 Version 17.2.1
Xamarin   17.2.0.174
Xamarin Designer   17.2.0.244
Xamarin.Android SDK   12.3.0.3

# Notes
Find [Parcel API Documentation](https://techdocs.zebra.com/mobile-parcel/latest/guide/api/) here.
