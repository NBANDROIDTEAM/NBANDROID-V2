<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="${packageName}">

    <application>
        <receiver android:name="${packageName}.${className}"
            android:exported="${isExported?string}"
            android:enabled="${isEnabled?string}" >
        </receiver>
    </application>

</manifest>
