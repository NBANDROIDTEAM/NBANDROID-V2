<?xml version="1.0" encoding="utf-8"?>
<${getMaterialComponentName('android.support.design.widget.CoordinatorLayout', useAndroidX)} xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/main_content"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true"
    tools:context="${packageName}.${activityClass}">

    <${getMaterialComponentName('android.support.design.widget.AppBarLayout', useMaterial2)}
        android:id="@+id/appbar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:paddingTop="@dimen/appbar_padding_top"
        android:theme="@style/${themeNameAppBarOverlay}">

        <${getMaterialComponentName('android.support.v7.widget.Toolbar', useAndroidX)}
            android:id="@+id/toolbar"
            app:title="@string/app_name"
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            android:layout_weight="1"
            android:background="?attr/colorPrimary"
            app:popupTheme="@style/${themeNamePopupOverlay}"
            app:layout_scrollFlags="scroll|enterAlways">

            <#if features == 'spinner'>
                <Spinner
                    android:id="@+id/spinner"
                    app:popupTheme="@style/${themeNamePopupOverlay}"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content" />
            </#if>
        </${getMaterialComponentName('android.support.v7.widget.Toolbar', useAndroidX)}>

        <#if features == 'tabs'>
        <${getMaterialComponentName('android.support.design.widget.TabLayout', useMaterial2)}
            android:id="@+id/tabs"
            android:layout_width="match_parent"
            android:layout_height="wrap_content">

            <${getMaterialComponentName('android.support.design.widget.TabItem', useMaterial2)}
                android:id="@+id/tabItem"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/tab_text_1" />

            <${getMaterialComponentName('android.support.design.widget.TabItem', useMaterial2)}
                android:id="@+id/tabItem2"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/tab_text_2" />

            <${getMaterialComponentName('android.support.design.widget.TabItem', useMaterial2)}
                android:id="@+id/tabItem3"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/tab_text_3" />

        </${getMaterialComponentName('android.support.design.widget.TabLayout', useMaterial2)}>
        </#if>
    </${getMaterialComponentName('android.support.design.widget.AppBarLayout', useMaterial2)}>

    <${viewContainer}
        android:id="@+id/container"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_behavior="@string/appbar_scrolling_view_behavior"/>

    <${getMaterialComponentName('android.support.design.widget.FloatingActionButton', useMaterial2)}
        android:id="@+id/fab"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="end|bottom"
        android:layout_margin="@dimen/fab_margin"
        app:srcCompat="@android:drawable/ic_dialog_email" />

</${getMaterialComponentName('android.support.design.widget.CoordinatorLayout', useAndroidX)}>
