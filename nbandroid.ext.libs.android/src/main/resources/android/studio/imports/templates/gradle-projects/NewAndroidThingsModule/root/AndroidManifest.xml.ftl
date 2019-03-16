<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="${packageName}"
    <#if isLibraryProject>/</#if>><#if !isLibraryProject>
    <application android:label="@string/app_name">
        <uses-library android:name="com.google.android.things"/>
    </application>
</manifest></#if>
