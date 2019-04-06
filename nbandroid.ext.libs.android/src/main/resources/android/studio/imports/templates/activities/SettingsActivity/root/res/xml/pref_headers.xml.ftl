<preference-headers xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- These settings headers are only used on tablets. -->

    <header
        android:fragment="${packageName}.${activityClass}$GeneralPreferenceFragment"
        android:title="@string/pref_header_general"
        android:icon="@drawable/ic_info_black_24dp" />

    <header
        android:fragment="${packageName}.${activityClass}$NotificationPreferenceFragment"
        android:title="@string/pref_header_notifications"
        android:icon="@drawable/ic_notifications_black_24dp" />

    <header
        android:fragment="${packageName}.${activityClass}$DataSyncPreferenceFragment"
        android:title="@string/pref_header_data_sync"
        android:icon="@drawable/ic_sync_black_24dp" />

</preference-headers>
