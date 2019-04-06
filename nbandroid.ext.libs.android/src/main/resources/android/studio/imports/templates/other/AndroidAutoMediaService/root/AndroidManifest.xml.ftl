<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="${packageName}">

    <application>

        <meta-data
            android:name="com.google.android.gms.car.application"
            android:resource="@xml/automotive_app_desc" />

        <#if useCustomTheme>
        <!--
             Use this meta data to override the theme from which Android Auto will
             look for colors. If you don't set this, Android Auto will look
             for color attributes in your application theme.
        -->
        <meta-data
            android:name="com.google.android.gms.car.application.theme"
            android:resource="@style/${escapeXmlAttribute(customThemeName)}" />

        </#if>

        <!-- Main music service, provides media browsing and media playback services to
         consumers through MediaBrowserService and MediaSession. Consumers connect to it through
         MediaBrowser (for browsing) and MediaController (for playback control) -->
        <service
            android:name="${packageName}.${mediaBrowserServiceName}"
            android:exported="true">
            <intent-filter>
                <action android:name="android.media.browse.MediaBrowserService" />
            </intent-filter>
        </service>

    </application>

</manifest>
