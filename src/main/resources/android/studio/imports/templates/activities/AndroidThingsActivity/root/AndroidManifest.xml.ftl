<#import "../../common/shared_manifest_macros.ftl" as manifestMacros>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="${packageName}">

    <application>

        <uses-library android:name="com.google.android.things"/>

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
            </#if>
            <#if buildApi gte 16 && parentActivityClass != "">
                android:parentActivityName="${parentActivityClass}"
            </#if>>
            <#if parentActivityClass != "">
                <meta-data android:name="android.support.PARENT_ACTIVITY"
                    android:value="${parentActivityClass}" />
            </#if>
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
