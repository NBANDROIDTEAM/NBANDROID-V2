<?xml version="1.0" encoding="utf-8"?>

<${getMaterialComponentName('android.support.wear.widget.SwipeDismissFrameLayout', useAndroidX)}
        xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools"
        xmlns:map="http://schemas.android.com/apk/res-auto"
        android:id="@+id/swipe_dismiss_root_container"
        android:layout_height="match_parent"
        android:layout_width="match_parent"
        tools:context="${packageName}.${activityClass}">

    <FrameLayout
            android:id="@+id/map_container"
            android:layout_width="match_parent"
            android:layout_height="match_parent">

        <fragment
                android:id="@+id/map"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:name="com.google.android.gms.maps.MapFragment"/>

    </FrameLayout>
</${getMaterialComponentName('android.support.wear.widget.SwipeDismissFrameLayout', useAndroidX)}>
