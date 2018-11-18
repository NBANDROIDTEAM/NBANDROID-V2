<#-- Some common elements used in multiple files -->
<#macro commonActivityBody>
    <#-- If isInstantApp is true and includeInstantAppUrl is true, add a BROWSABLE category so that instant app is reachable via URL -->
    <#if (isInstantApp!false) && (includeInstantAppUrl!false)>
        <intent-filter android:order="${instantAppActivityOrder}">
            <action android:name="android.intent.action.VIEW" />
            <category android:name="android.intent.category.BROWSABLE" />
            <category android:name="android.intent.category.DEFAULT" />
            <data
                android:host="${instantAppActivityHost}"
                android:${instantAppActivityRouteType}="${instantAppActivityRoute}"
                android:scheme="https" />
        </intent-filter>
    </#if>
    <#if isLauncher && (!(isLibraryProject!false) || isInstantApp)>
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </#if>
</#macro>
