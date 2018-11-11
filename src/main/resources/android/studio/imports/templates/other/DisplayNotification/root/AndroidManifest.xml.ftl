<manifest xmlns:android="http://schemas.android.com/apk/res/android" >

    <application>
        <activity
            android:name="${relativePackage}.${stubActivityClass}"
            android:label="${stubActivityClass}" >
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        <activity
            android:name="${relativePackage}.${displayActivityClass}"
            android:allowEmbedded="true"
            android:exported="true"
            android:theme="@android:style/Theme.DeviceDefault.Light"
            android:taskAffinity="" >
        </activity>
        <receiver
            android:name="${relativePackage}.${receiverClass}"
            android:exported="${isExported?string}">
            <intent-filter>
                <action android:name="${packageName}.SHOW_NOTIFICATION" />
            </intent-filter>
        </receiver>
    </application>

</manifest>
