
# Zebra Parcel Dimensioning integration with Velocity 
Use Mobile Parcel API from Zebra within Ivanti Velocity Client emulation (e.g. IBM 5250/3270 and VT100/220).
API Reference: [Mobile Parcel API - TechDocs (zebra.com)](https://techdocs.zebra.com/mobile-parcel/latest/guide/api/)


## Introduction

Ivanti Velocity is an Android client that can connect to Telnet hosts (including IBM 5250/3270 and VT100/220) - [Velocity (ivanti.com)](https://help.ivanti.com/wl/help/en_US/Velocity/2.0.0/admin/velocityConsoleHelp.htm).

The Zebra Parcel Dimensioning solution provides fast and accurate point-and-shoot dimensioning using an integrated time of flight sensor (ToF) with no external snap-on sensors required - [Zebra Dimensioning (zebra.com)](https://www.zebra.com/gb/en/products/software/mobile-computers/zebra-dimensioning.html).

This project contains all the resources needed to make the Zebra Parcel Dimensioning solution run within Velocity client emulation.


### Preview
<img src="https://user-images.githubusercontent.com/101400857/230152772-c4f8fd3a-af5f-409b-9f7b-6e9c5c27bc27.png" width=30% height=30%> <img src="https://user-images.githubusercontent.com/101400857/230152798-1abe75ca-07a6-49e3-99e5-f5e74b891ee0.png" width=30% height=30%> <img src="https://user-images.githubusercontent.com/101400857/230152819-7db6d14e-62a0-435c-975a-48e4f9e7c543.png" width=30% height=30%>

## Requirements

 - One of the following Zebra Devices with integrated ToF sensor:
	 - TC53 (Premium only) 
	 - TC58 (Premium only)
- Velocity Client installed in the Zebra device (available on PlayStore: https://play.google.com/store/apps/details?id=com.wavelink.velocity)


## Setup

 1. Build and install on the TC53/58 the [MDIntegration app](https://github.com/ZebraDevs/Mobile_Dimensioning_Samples/tree/main/Java/MDIntegrationApp). This middleware allows velocity to use Zebra Mobile Parcel API.
 2. Copy the profile **velocity-profile-zebra-dimensioning.wldep** attached to this project to the following path of your device: */sdcard/Android/data/com.wavelink.velocity/files/*
 3. Open Velocity app in your device and select "ZebraDimensioning" host profile.
 4. Tap the "Capture Volume" button
 5. Get parcel dimensioning!

### Code

Source code for the middleware apk is available here: https://github.com/ZebraDevs/Mobile_Dimensioning_Samples/tree/main/Java/MDIntegrationApp

Velocity Script used to invoke the middleware is attached to this repo: https://github.com/ZebraDevs/Mobile_Dimensioning_Samples/blob/22dd3245c49d677a674af34188a9a00288b6476f/Velocity%20-%20All%20Touch%20TE/velocity-script-lauch-zebra-dimensioning.js

Along the full Velocity Console project: https://github.com/ZebraDevs/Mobile_Dimensioning_Samples/blob/22dd3245c49d677a674af34188a9a00288b6476f/Velocity%20-%20All%20Touch%20TE/Zebra%20Dimensioning%20-%20Velocity%20Console%20Profile.zip

# License
MIT
