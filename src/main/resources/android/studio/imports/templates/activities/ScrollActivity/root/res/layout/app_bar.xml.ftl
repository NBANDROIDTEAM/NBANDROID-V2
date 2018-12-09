<?xml version="1.0" encoding="utf-8"?>
<${getMaterialComponentName('android.support.design.widget.CoordinatorLayout', useAndroidX)}
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true"
    tools:context="${packageName}.${activityClass}">

    <${getMaterialComponentName('android.support.design.widget.AppBarLayout', useMaterial2)}
        android:id="@+id/app_bar"
        android:fitsSystemWindows="true"
        android:layout_height="@dimen/app_bar_height"
        android:layout_width="match_parent"
        android:theme="@style/${themeNameAppBarOverlay}">

        <${getMaterialComponentName('android.support.design.widget.CollapsingToolbarLayout', useMaterial2)}
            android:id="@+id/toolbar_layout"
            android:fitsSystemWindows="true"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:toolbarId="@+id/toolbar"
            app:layout_scrollFlags="scroll|exitUntilCollapsed"
            app:contentScrim="?attr/colorPrimary">

            <${getMaterialComponentName('android.support.v7.widget.Toolbar', useAndroidX)}
                android:id="@+id/toolbar"
                android:layout_height="?attr/actionBarSize"
                android:layout_width="match_parent"
                app:layout_collapseMode="pin"
                app:popupTheme="@style/${themeNamePopupOverlay}" />

        </${getMaterialComponentName('android.support.design.widget.CollapsingToolbarLayout', useMaterial2)}>
    </${getMaterialComponentName('android.support.design.widget.AppBarLayout', useMaterial2)}>

    <include layout="@layout/${simpleLayoutName}" />

    <${getMaterialComponentName('android.support.design.widget.FloatingActionButton', useMaterial2)}
        android:id="@+id/fab"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_margin="@dimen/fab_margin"
        app:layout_anchor="@id/app_bar"
        app:layout_anchorGravity="bottom|end"
        app:srcCompat="@android:drawable/ic_dialog_email" />

</${getMaterialComponentName('android.support.design.widget.CoordinatorLayout', useAndroidX)}>
