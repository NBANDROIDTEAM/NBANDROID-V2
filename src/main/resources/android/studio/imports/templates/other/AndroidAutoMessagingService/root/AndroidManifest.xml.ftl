<manifest xmlns:android="http://schemas.android.com/apk/res/android" >

    <application>

        <meta-data
            android:name="com.google.android.gms.car.application"
            android:resource="@xml/automotive_app_desc" />

        <service android:name="${relativePackage}.${serviceName}">
        </service>

        <receiver android:name="${relativePackage}.${readReceiverName}">
            <intent-filter>
                <action android:name="${packageName}.ACTION_MESSAGE_READ"/>
            </intent-filter>
        </receiver>

        <receiver android:name="${relativePackage}.${replyReceiverName}">
            <intent-filter>
                <action android:name="${packageName}.ACTION_MESSAGE_REPLY"/>
            </intent-filter>
        </receiver>

    </application>

</manifest>
