<?xml version="1.0" encoding="utf-8"?>

<FrameLayout
        xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools"
        xmlns:map="http://schemas.android.com/apk/res-auto"
        android:id="@+id/root_container"
        android:layout_height="match_parent"
        android:layout_width="match_parent"
        tools:context="${relativePackage}.${activityClass}">

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

    <android.support.wearable.view.DismissOverlayView
            android:id="@+id/dismiss_overlay"
            android:layout_height="match_parent"
            android:layout_width="match_parent"/>
</FrameLayout>
