<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
<#if appCompat>
    xmlns:app="http://schemas.android.com/apk/res-auto"
</#if>
    android:layout_width="match_parent"
    android:layout_height="@dimen/nav_header_height"
    android:background="@drawable/side_nav_bar"
    android:paddingBottom="@dimen/activity_vertical_margin"
    android:paddingLeft="@dimen/activity_horizontal_margin"
    android:paddingRight="@dimen/activity_horizontal_margin"
    android:paddingTop="@dimen/activity_vertical_margin"
    android:theme="@style/ThemeOverlay.AppCompat.Dark"
    android:orientation="vertical"
    android:gravity="bottom">
<#if !(isLibraryProject!false)>

    <ImageView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:paddingTop="@dimen/nav_header_vertical_spacing"
<#if appCompat && buildApi gte 25 && targetApi gte 25>
        app:srcCompat="@mipmap/ic_launcher_round"
<#elseif appCompat>
        app:srcCompat="@mipmap/ic_launcher"
<#else>
        android:src="@mipmap/ic_launcher"
</#if>
        android:contentDescription="@string/nav_header_desc"
        android:id="@+id/imageView" />
</#if>

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:paddingTop="@dimen/nav_header_vertical_spacing"
        android:text="@string/nav_header_title"
        android:textAppearance="@style/TextAppearance.AppCompat.Body1" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/nav_header_subtitle"
        android:id="@+id/textView" />

</LinearLayout>
