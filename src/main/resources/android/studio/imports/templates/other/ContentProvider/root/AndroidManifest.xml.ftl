<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="${packageName}">

    <application>
        <provider android:name="${packageName}.${className}"
            android:authorities="${authorities}"
            android:exported="${isExported?string}"
            android:enabled="${isEnabled?string}" >
        </provider>
    </application>

</manifest>
