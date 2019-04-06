<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    package="${packageName}">

    <uses-feature
        android:name="android.hardware.touchscreen"
        android:required="false" />

    <uses-feature android:name="android.software.leanback"
        android:required="true" />

    <application>

        <activity android:name="${packageName}.${activityClass}"
            android:icon="@drawable/app_icon_your_company"
            android:logo="@drawable/app_icon_your_company"
            android:banner="@drawable/app_icon_your_company"
            android:screenOrientation="landscape"
            <#if isNewProject>
            android:label="@string/app_name"
            <#else>
            android:label="@string/title_${activityToLayout(activityClass)}"
            </#if>
            <#if buildApi gte 16 && parentActivityClass != "">android:parentActivityName="${parentActivityClass}"</#if>>
            <#if parentActivityClass != "">
            <meta-data android:name="android.support.PARENT_ACTIVITY"
                android:value="${parentActivityClass}" />
            </#if>
            <#if !(isLibraryProject!false)>
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
            </intent-filter>
            </#if>
        </activity>

        <activity android:name="${packageName}.${detailsActivity}" />
        <activity android:name="PlaybackActivity" />
        <activity android:name="BrowseErrorActivity" />

    </application>

</manifest>
