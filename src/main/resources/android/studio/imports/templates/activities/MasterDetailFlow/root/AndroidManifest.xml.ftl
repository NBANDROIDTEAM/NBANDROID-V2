<#import "../../common/shared_manifest_macros.ftl" as manifestMacros>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="${packageName}">

    <application>
        <activity
            android:name="${packageName}.${CollectionName}Activity"
            <#if isNewProject>
            android:label="@string/app_name"
            <#else>
            android:label="@string/title_${collection_name}"
            </#if>
            <#if hasAppBar>
            android:theme="@style/${themeNameNoActionBar}"
            <#elseif !hasApplicationTheme>
            android:theme="@style/${themeName}"
            </#if>
            <#if buildApi gte 16 && parentActivityClass != "">android:parentActivityName="${parentActivityClass}"</#if>>
            <#if parentActivityClass != "">
            <meta-data android:name="android.support.PARENT_ACTIVITY"
                android:value="${parentActivityClass}" />
            </#if>
            <@manifestMacros.commonActivityBody />
        </activity>

        <activity android:name="${packageName}.${DetailName}Activity"
            android:label="@string/title_${detail_name}"
            <#if hasAppBar>
            android:theme="@style/${themeNameNoActionBar}"
            <#elseif !hasApplicationTheme>
            android:theme="@style/${themeName}"
            </#if>
            <#if buildApi gte 16>android:parentActivityName="${packageName}.${CollectionName}Activity"</#if>>
            <meta-data android:name="android.support.PARENT_ACTIVITY"
                android:value="${packageName}.${CollectionName}Activity" />
        </activity>
    </application>

</manifest>
