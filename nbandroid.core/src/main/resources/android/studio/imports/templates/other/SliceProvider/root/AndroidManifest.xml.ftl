<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="${packageName}">

    <application>
        <provider android:name="${packageName}.${className}"
            android:authorities="${authorities}"
            android:exported="true" >
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.app.slice.category.SLICE" />
                <data android:scheme="http"
                    android:host="${hostUrl}"
                    android:pathPrefix="${pathPrefix}" />
            </intent-filter>
        </provider>
    </application>

</manifest>
