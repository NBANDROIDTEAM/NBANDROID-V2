[![Build Status](https://travis-ci.org/NBANDROIDTEAM/NBANDROID-V2.svg?branch=master)](https://travis-ci.org/NBANDROIDTEAM/NBANDROID-V2)

# NBANDROID-V2
NetBeans Android support plugin

This project aims to stabilize and update the discontinued NetBeans IDE plugin NBANDROID.
The current version supports NetBeans 8.1 and 8.2

**Many thanks to Radim Kubacki, the creator of the first NBANDROID!**</br>

## How to install NBANDROID-V2

**Gradle Support plugin is no longer needed**

The compiled version of the last commit is here:<br>


NetBeans 8.1 http://server.arsi.sk/nbandroid81/

NetBeans 8.2 http://server.arsi.sk/nbandroid82/

Or add an update center

NetBeans 8.1 http://server.arsi.sk/nbandroid81/updates.xml

NetBeans 8.2 http://server.arsi.sk/nbandroid82/updates.xml

Last plugin version which needs Gradle Support plugin is here:<br>


NetBeans 8.1 http://server.arsi.sk/nbandroid81_last_ext_gradle/

NetBeans 8.2 http://server.arsi.sk/nbandroid82_last_ext_gradle/


![Install](https://user-images.githubusercontent.com/22594510/50820918-2fa0c700-132e-11e9-9cc3-dbdf49bb17b4.png)
![Install](https://user-images.githubusercontent.com/22594510/50820932-39c2c580-132e-11e9-8450-77c5bd669536.png)
![Install](https://user-images.githubusercontent.com/22594510/50820950-421b0080-132e-11e9-9b7d-04f681d73f60.png)

## Layout preview support
![Layout](https://user-images.githubusercontent.com/22594510/52371231-433a6d00-2a55-11e9-87d6-8ee9246c4168.png)

## Color preview support
![Color preview](https://user-images.githubusercontent.com/22594510/50722224-20224380-10cc-11e9-8a0a-90e2106b3c9d.png)
![Color preview](https://user-images.githubusercontent.com/22594510/50724036-f9253b00-10e6-11e9-92d0-c092ec9ed1f4.png)
![Color preview](https://user-images.githubusercontent.com/22594510/50724463-30e3b100-10ee-11e9-8d71-97dd83a3a357.png)

To use Android Color preview support you must first uninstall netbeans-color-codes-preview plugin if you have it installed.

And install the modified version of netbeans-color-codes-preview plugin from http://server.arsi.sk/ascp/ or add an update center 
http://server.arsi.sk/ascp/updates.xml. 

I'm waiting for PR to accept. Current source codes are here https://github.com/arsi-apli/netbeans-color-codes-preview. 

And I added basic support for Java, Color constants and RGB Color values.
![Color preview](https://user-images.githubusercontent.com/22594510/50656806-ab98b900-0f94-11e9-9d14-890c3303c7b7.png)

## Checking for external changes - Suspended, high cpu usage<br>
There is a problem with Gradle, it creates broken links in the tmp directory and Netbeans 8.2 contains a bug with their handling.<br>
When you run into this problem, you can install this modules with patch for NB82:<br>
org-netbeans-modules-masterfs-patch-module<br>
org-netbeans-modules-versioning-masterfs-patch-module<br>
From this update center:  http://server.arsi.sk/masterfs/updates.xml

## Current project status:
### What Works:
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

### What Dont works:
* SDK Manager - Update sites configuration
* AVD Manager
* we currently have no Visual Layout Editor 
* and many other things

