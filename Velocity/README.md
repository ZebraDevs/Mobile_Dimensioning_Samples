# Zebra Parcel Dimensioning integration with Velocity

## Introduction

Ivanti Velocity is an Android client that can connect to Telnet hosts (including IBM 5250/3270 and VT100/220) - [Velocity (ivanti.com)](https://help.ivanti.com/wl/help/en_US/Velocity/2.0.0/admin/velocityConsoleHelp.htm).

This project contains all the resources needed to make the Zebra Parcel Dimensioning solution run within Velocity client emulation.


### Preview
<img src="https://user-images.githubusercontent.com/101400857/230152772-c4f8fd3a-af5f-409b-9f7b-6e9c5c27bc27.png" width=30% height=30%> <img src="https://user-images.githubusercontent.com/101400857/230152798-1abe75ca-07a6-49e3-99e5-f5e74b891ee0.png" width=30% height=30%> <img src="https://user-images.githubusercontent.com/101400857/230152819-7db6d14e-62a0-435c-975a-48e4f9e7c543.png" width=30% height=30%>

## Setup

 1. Install the Velocity Client on the [supported](https://www.zebra.com/us/en/support-downloads/software/mobile-computer-software/zebra-dimensioning-mobile-parcel.html) Zebra device (available on PlayStore: https://play.google.com/store/apps/details?id=com.wavelink.velocity)
 2. Build and install the [MDIntegration app](https://github.com/ZebraDevs/Mobile_Dimensioning_Samples/tree/main/Java/MDIntegrationApp). This application functions as middleware to allow Velocity to use the Zebra Mobile Parcel API.
 3. Copy the profile [velocity-profile-zebra-dimensioning.wldep](velocity-profile-zebra-dimensioning.wldep) to the following path of your device: */sdcard/Android/data/com.wavelink.velocity/files/*
 4. Open Velocity app in your device and select the "ZebraDimensioning" host profile.
 5. Tap the "Capture Volume" button
 6. Get parcel dimensioning!

### Code

* Source code for the MDIntegration middleware application is available here: https://github.com/ZebraDevs/Mobile_Dimensioning_Samples/tree/main/Java/MDIntegrationApp
* The Velocity project [Zebra Dimensioning - Velocity Console Profile.zip](<Zebra Dimensioning - Velocity Console Profile.zip>) can be imported directly into Velocity Console.

# License
MIT
