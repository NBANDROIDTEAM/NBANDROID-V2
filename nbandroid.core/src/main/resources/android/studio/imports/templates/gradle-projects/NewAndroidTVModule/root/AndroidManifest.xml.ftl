<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    package="${packageName}">

    <application android:allowBackup="true"
        android:label="@string/app_name"<#if copyIcons>
        android:icon="@mipmap/ic_launcher"<#else>
        android:icon="@drawable/${assetName}"</#if>
        <#if buildApi gte 17>android:supportsRtl="true"</#if>
        android:theme="@style/AppTheme">

    </application>

</manifest>
