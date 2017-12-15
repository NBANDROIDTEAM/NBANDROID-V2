[![Build Status](https://travis-ci.org/NBANDROIDTEAM/NBANDROID-V2.svg?branch=master)](https://travis-ci.org/NBANDROIDTEAM/NBANDROID-V2)

# NBANDROID-V2
Netbeans Android support plugin

This project aims to stabilize and update the discontinued Netbeans IDE plugin NBANDROID.
The current version supports Netbeans 8.1 and 8.2

**Many thanks to Radim Kubacki, the creator of the first NBANDROID!**</br>

## SNAPSHOT
The compiled version of the last commit is here http://server.arsi.sk/nbandroid/

### What Works:
* Code Assistance
* Build
* Run
* Debug
* Android emulator
* SDK Platform manager
* SDK Tools manager

### What Dont works:
* SDK Platform manager - Integration to Gradle build system
* SDK Manager - Update sites configuration
* AVD Manager
* we currently have no Visual Layout Editor 
* and many other things


# How to run/debug this plugin from Netbeans
This plugin depends on Gradle Support plugin. You need to add it to platform folder.

* Install Gradle Support plugin to Netbeans
* Make copy of Netbeans directory
* Copy .netbeans/NB_Version/modules/org-netbeans-gradle-project.jar to NB_COPY/extide/modules/
* Copy .netbeans/NB_Version/modules/ext folder to NB_COPY/extide/modules/
* Copy .netbeans/NB_Version/update_tracking/org-netbeans-modules-options-java.xml to NB_COPY/extide/update_tracking/
* Copy .netbeans/NB_Version/config/Modules/org-netbeans-gradle-project.xml to NB_COPY/extide/config/Modules/
* Add new Profile to pom.xml
```xml
    <profiles>
        <profile>
            <id>your_name-NB_VERSION</id>
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
                            <publicPackages>
                            </publicPackages>
                            <moduleDependencies>
                                <dependency>
                                    <!--Private Package Reference-->
                                    <id>org.netbeans.modules:org-netbeans-modules-gsf-testrunner</id>
                                    <type>impl</type>
                                </dependency>
                                <dependency>
                                    <!--Private Package Reference-->
                                    <id>com.github.kelemen:netbeans-gradle-plugin</id>
                                    <type>impl</type>
                                </dependency>
                            </moduleDependencies>
                            <netbeansInstallation>FULL_PATH_TO_NB_COPY</netbeansInstallation>
                        </configuration>
                    </plugin>
                </plugins>
            </build>
            <properties>
                <version.nb>RELEASE81</version.nb>
            </properties>
        </profile>
        ...
    <profiles>
```
* And finally select your profile
