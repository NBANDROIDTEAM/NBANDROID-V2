<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="${packageName}">

    <uses-feature android:name="android.hardware.type.watch" />

    <application>

        <uses-library android:name="com.google.android.wearable" android:required="true" />

        <!--
               Set to true if your app is Standalone, that is, it does not require the handheld
               app to run.
        -->
        <meta-data android:name="com.google.android.wearable.standalone" android:value="true"/>

        <activity android:name="${packageName}.${activityClass}"
            <#if isNewProject>
            android:label="@string/app_name"
            <#else>
            android:label="@string/title_${activityToLayout(activityClass)}"
            </#if>
            >
            <#if isLauncher && !(isLibraryProject!false)>
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
            </#if>
        </activity>
    </application>

</manifest>
