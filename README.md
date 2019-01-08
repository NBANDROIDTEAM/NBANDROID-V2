[![Build Status](https://travis-ci.org/NBANDROIDTEAM/NBANDROID-V2.svg?branch=master)](https://travis-ci.org/NBANDROIDTEAM/NBANDROID-V2)

# NBANDROID-V2
NetBeans Android support plugin

This project aims to stabilize and update the discontinued NetBeans IDE plugin NBANDROID.
The current version supports NetBeans 8.1 and 8.2

**Many thanks to Radim Kubacki, the creator of the first NBANDROID!**</br>

## How to install NBANDROID-V2

**Please update your Gradle Support plugin to 1.4.4**

The compiled version of the last commit is here:<br>


NetBeans 8.1 http://server.arsi.sk/nbandroid81/

NetBeans 8.2 http://server.arsi.sk/nbandroid82/

Or add an update center

NetBeans 8.1 http://server.arsi.sk/nbandroid81/updates.xml

NetBeans 8.2 http://server.arsi.sk/nbandroid82/updates.xml

![Install](https://user-images.githubusercontent.com/22594510/50820918-2fa0c700-132e-11e9-9cc3-dbdf49bb17b4.png)
![Install](https://user-images.githubusercontent.com/22594510/50820932-39c2c580-132e-11e9-8450-77c5bd669536.png)
![Install](https://user-images.githubusercontent.com/22594510/50820950-421b0080-132e-11e9-9b7d-04f681d73f60.png)


## Color preview support
![Color preview](https://user-images.githubusercontent.com/22594510/50820918-2fa0c700-132e-11e9-9cc3-dbdf49bb17b4.png)
![Color preview](https://user-images.githubusercontent.com/22594510/50724036-f9253b00-10e6-11e9-92d0-c092ec9ed1f4.png)
![Color preview](https://user-images.githubusercontent.com/22594510/50724463-30e3b100-10ee-11e9-8d71-97dd83a3a357.png)

To use Android Color preview support you must first uninstall netbeans-color-codes-preview plugin if you have it installed.

And install the modified version of netbeans-color-codes-preview plugin from http://server.arsi.sk/ascp/ or add an update center 
http://server.arsi.sk/ascp/updates.xml. 

I'm waiting for PR to accept. Current source codes are here https://github.com/arsi-apli/netbeans-color-codes-preview. 

And I added basic support for Java, Color constants and RGB Color values.
![Color preview](https://user-images.githubusercontent.com/22594510/50656806-ab98b900-0f94-11e9-9d14-890c3303c7b7.png)

### What Works:
* Code Assistance
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


# How to run/debug this plugin from NetBeans
This plugin depends on Gradle Support plugin. You need to add it to platform folder.

* Install Gradle Support plugin to NetBeans
* Make copy of NetBeans directory
* Copy .netbeans/NB_Version/modules/org-netbeans-gradle-project.jar to NB_COPY/extide/modules/
* Copy .netbeans/NB_Version/modules/ext folder to NB_COPY/extide/modules/
* Copy .netbeans/NB_Version/update_tracking/org-netbeans-modules-options-java.xml to NB_COPY/extide/update_tracking/
* Copy .netbeans/NB_Version/config/Modules/org-netbeans-gradle-project.xml to NB_COPY/extide/config/Modules/
* Add new Profile to pom.xml
```xml
    <profiles>
        <profile>
            <id>your_name-NB82</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.codehaus.mojo</groupId>
                        <artifactId>nbm-maven-plugin</artifactId>
                        <version>3.13</version>
                        <extensions>true</extensions>
                        <configuration>
                            <netbeansInstallation>FULL_PATH_TO_NB_COPY</netbeansInstallation>
                            <netbeansUserdir>FULL_PATH_TO_CUSTOM_USER_DIR</netbeansUserdir>
                        </configuration>
                    </plugin>
                </plugins>
            </build>
            <properties>
                <version.nb>RELEASE82</version.nb>
                <asm.nb>asm-all-5.0.1</asm.nb>
            </properties>
        </profile>
        ...
    <profiles>
```
* And finally select your profile
