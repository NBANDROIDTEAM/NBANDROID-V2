<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="${packageName}">

    <application>

        <meta-data
            android:name="com.google.android.gms.car.application"
            android:resource="@xml/automotive_app_desc" />

        <service android:name="${packageName}.${serviceName}">
        </service>

        <receiver android:name="${packageName}.${readReceiverName}">
            <intent-filter>
                <action android:name="${packageName}.ACTION_MESSAGE_READ"/>
            </intent-filter>
        </receiver>

        <receiver android:name="${packageName}.${replyReceiverName}">
            <intent-filter>
                <action android:name="${packageName}.ACTION_MESSAGE_REPLY"/>
            </intent-filter>
        </receiver>

    </application>

</manifest>
