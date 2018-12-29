<#import "../../common/shared_manifest_macros.ftl" as manifestMacros>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="${packageName}">

<#if integrateGps>
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="com.google.android.things.permission.MANAGE_GPS_DRIVERS" />
</#if>
<#if integrateAccelerometer || integrateTemperaturePressureSensor>
    <uses-permission android:name="com.google.android.things.permission.MANAGE_SENSOR_DRIVERS" />
</#if>
<#if integrateCapacitiveTouchButton>
    <uses-permission android:name="com.google.android.things.permission.MANAGE_INPUT_DRIVERS" />
</#if>

    <application>

        <uses-library android:name="com.google.android.things"/>
<#if integrateAccelerometer>
        <service android:name="${packageName}.${accelerometerServiceClass}">
        </service>
</#if>
<#if integrateGps>
        <service android:name="${packageName}.${gpsServiceClass}">
        </service>
</#if>
<#if integrateTemperaturePressureSensor>
        <service android:name="${packageName}.${temperaturePressureServiceClass}">
        </service>
</#if>

        <activity android:name="${packageName}.${activityClass}"
            <#if generateActivityTitle!true>
                <#if isNewProject>
                    android:label="@string/app_name"
                <#else>
                    android:label="@string/title_${activityToLayout(activityClass)}"
                </#if>
            </#if>
            <#if hasNoActionBar>
                android:theme="@style/${themeNameNoActionBar}"
            <#elseif (requireTheme!false) && !hasApplicationTheme && appCompat>
                android:theme="@style/${themeName}"
            </#if>>
            <@manifestMacros.commonActivityBody />
            <#if isThingsLauncher>
                <!-- Make this the first activity that is displayed when the device boots. -->
                <intent-filter>
                    <action android:name="android.intent.action.MAIN" />
                    <category android:name="android.intent.category.HOME" />
                    <category android:name="android.intent.category.DEFAULT" />
                </intent-filter>
            </#if>
        </activity>
    </application>
</manifest>
