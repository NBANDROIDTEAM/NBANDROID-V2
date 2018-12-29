<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="${packageName}">

    <application>
        <service android:name="${packageName}.${className}"
            android:exported="false" >
        </service>
    </application>

</manifest>
