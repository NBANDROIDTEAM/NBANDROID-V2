<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android" >

    <uses-feature android:name="android.hardware.type.watch" />

    <application>
        <service
            android:name="${relativePackage}.${serviceClass}"
<#if style == "analog">
            android:label="@string/my_analog_name"
<#elseif style == "digital">
            android:label="@string/my_digital_name"
</#if>
            android:permission="android.permission.BIND_WALLPAPER" >
            <meta-data
                    android:name="android.service.wallpaper"
                    android:resource="@xml/watch_face" />
            <meta-data
                android:name="com.google.android.wearable.watchface.preview"
<#if style == "analog">
                android:resource="@drawable/preview_analog" />
<#elseif style == "digital">
                android:resource="@drawable/preview_digital" />
</#if>
            <meta-data
                android:name="com.google.android.wearable.watchface.preview_circular"
<#if style == "analog">
                android:resource="@drawable/preview_analog" />
<#elseif style == "digital">
                android:resource="@drawable/preview_digital_circular" />
</#if>
            <intent-filter>
                <action android:name="android.service.wallpaper.WallpaperService" />
                <category android:name="com.google.android.wearable.watchface.category.WATCH_FACE" />
            </intent-filter>
        </service>

        <meta-data
            android:name="com.google.android.gms.version"
            android:value="@integer/google_play_services_version" />
    </application>
</manifest>
