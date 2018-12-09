<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="${packageName}">

    <application>
        <meta-data
            android:name="com.google.android.actions"
            android:resource="@xml/${escapeXmlAttribute(fileName)}" />
    </application>

</manifest>
