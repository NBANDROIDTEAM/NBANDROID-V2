[![Build Status](https://travis-ci.org/NBANDROIDTEAM/NBANDROID-V2.svg?branch=master)](https://travis-ci.org/NBANDROIDTEAM/NBANDROID-V2)
![](http://server.arsi.sk:8080/hit/counter.svg)
![](http://server.arsi.sk:8080/hit/current.svg)

# NBANDROID-V2
Apache NetBeans Android support plugin

This project aims to stabilize and update the discontinued NetBeans IDE plugin NBANDROID.

The current version supports Apache NetBeans 10 and 11, and CoolBeans.

Requirement: Java 10, 11, or 12.

**Many thanks to Radim Kubacki, the creator of the first NBANDROID!**</br>

## How to Install NBANDROID-V2

First you need to install a special plugin. It detects the version of your ANB / CoolBeans and installs the NBANDROID Update Center

http://server.arsi.sk/nbandroid_loader/updates.xml

![](https://user-images.githubusercontent.com/22594510/56475800-cf253800-648d-11e9-8ff9-7912460ecfad.png)

![](https://user-images.githubusercontent.com/22594510/56475807-ed8b3380-648d-11e9-80c7-d40fbfed842a.png)

![](https://user-images.githubusercontent.com/22594510/56475731-bec08d80-648c-11e9-814c-6af46f1d406e.png)

You can then install NBANDROID
![](https://user-images.githubusercontent.com/22594510/56475757-1c54da00-648d-11e9-8eb0-fec1e5505826.png)

## Known Issues

In a fresh ANB installation, the first time you open the Android Project, the project structure is not displayed correctly.

solution - close and reopen the project

## AVD Manager
![](https://user-images.githubusercontent.com/22594510/56445471-b259fa80-62fd-11e9-838e-ee6625081369.png)

![](https://user-images.githubusercontent.com/22594510/56473325-0f72bf00-646a-11e9-83a2-c755c7f743e2.png)

## Layout Preview Support
![Layout](https://user-images.githubusercontent.com/22594510/52371231-433a6d00-2a55-11e9-87d6-8ee9246c4168.png)

## Color Preview Support
![Color preview](https://user-images.githubusercontent.com/22594510/50722224-20224380-10cc-11e9-8a0a-90e2106b3c9d.png)

![Color preview](https://user-images.githubusercontent.com/22594510/50724036-f9253b00-10e6-11e9-92d0-c092ec9ed1f4.png)

![Color preview](https://user-images.githubusercontent.com/22594510/50724463-30e3b100-10ee-11e9-8d71-97dd83a3a357.png)

To use Android Color preview support you must first uninstall netbeans-color-codes-preview plugin if you have it installed.

And install the modified version of netbeans-color-codes-preview plugin from http://server.arsi.sk/ascp/ or add an update center 
http://server.arsi.sk/ascp/updates.xml. 

I'm waiting for PR to accept. Current source codes are here https://github.com/arsi-apli/netbeans-color-codes-preview. 

And I added basic support for Java, Color constants and RGB Color values.
![Color preview](https://user-images.githubusercontent.com/22594510/50656806-ab98b900-0f94-11e9-9d14-890c3303c7b7.png)


## Current Project Status
### What works:
* Layout preview
* Code Assistance
* Android XML Code Completion support
* Color preview support
* Google, Bintray jcenter and Maven dependency browser
* Build
* Build Signed APK + Key Store Manager
* Run
* Debug
* Install APK
* ADB Shell Terminal
* Switch ADB device to Wifi/USB
* Android emulator
* SDK Platform manager
* SDK Tools manager
* AVD Manager

### What does not work yet:
* SDK Manager - Update sites configuration
* we currently have no Visual Layout Editor 
* and many other things

