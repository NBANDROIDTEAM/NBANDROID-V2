<manifest xmlns:android="http://schemas.android.com/apk/res/android"
          <#if isInstantApp!false>xmlns:instant="http://schemas.android.com/instantapps"</#if>
    package="${packageName}"
    <#if isInstantApp!false>split="${splitName}"</#if>>

    <#if !(isInstantApp!false) || isLibraryProject>
    <application <#if minApiLevel gte 4 && buildApi gte 4>android:allowBackup="true"</#if>
        android:label="@string/app_name"<#if copyIcons && !isLibraryProject>
        android:icon="@mipmap/ic_launcher"
        <#if buildApi gte 25 && targetApi gte 25>android:roundIcon="@mipmap/ic_launcher_round"</#if><#elseif assetName??>
        android:icon="@drawable/${assetName}"</#if>
        <#if buildApi gte 17>android:supportsRtl="true"</#if>
        <#if baseTheme != "none" && (!isLibraryProject || (isInstantApp!false))>
        android:theme="@style/AppTheme"</#if>>

    </application>
    </#if>

</manifest>
