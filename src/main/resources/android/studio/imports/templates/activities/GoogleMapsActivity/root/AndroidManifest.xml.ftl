<manifest xmlns:android="http://schemas.android.com/apk/res/android" >
      <!-- 
         The ACCESS_COARSE/FINE_LOCATION permissions are not required to use
         Google Maps Android API v2, but you must specify either coarse or fine
         location permissions for the 'MyLocation' functionality. 
     -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>

    <application>

        <!-- 
             The API key for Google Maps-based APIs is defined as a string resource.
             (See the file "res/values/google_maps_api.xml").
             Note that the API key is linked to the encryption key used to sign the APK.
             You need a different API key for each encryption key, including the release key that is used to
             sign the APK for publishing.
             You can define the keys for the debug and release targets in src/debug/ and src/release/. 
         -->
        <meta-data android:name="com.google.android.geo.API_KEY" android:value="@string/google_maps_key"/>

        <activity android:name="${relativePackage}.${activityClass}"
            android:label="@string/title_${simpleName}">
            <#if parentActivityClass != "">
            <meta-data android:name="android.support.PARENT_ACTIVITY"
                android:value="${parentActivityClass}" />
            </#if>
            <#if isLauncher && !(isLibraryProject!false)>
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
            </#if>

        </activity>
    </application>

</manifest>
