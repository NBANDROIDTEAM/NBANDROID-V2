<#if !hasAppBar>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/${detail_name}_container"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="${packageName}.${DetailName}Activity"
    tools:ignore="MergeRootFrame" />
<#else>
<${getMaterialComponentName('android.support.design.widget.CoordinatorLayout', useAndroidX)} xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true"
    tools:context="${packageName}.${DetailName}Activity"
    tools:ignore="MergeRootFrame">

    <${getMaterialComponentName('android.support.design.widget.AppBarLayout', useMaterial2)}
        android:id="@+id/app_bar"
        android:layout_width="match_parent"
        android:layout_height="@dimen/app_bar_height"
        android:fitsSystemWindows="true"
        android:theme="@style/ThemeOverlay.AppCompat.Dark.ActionBar">

        <${getMaterialComponentName('android.support.design.widget.CollapsingToolbarLayout', useMaterial2)}
            android:id="@+id/toolbar_layout"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:fitsSystemWindows="true"
            app:contentScrim="?attr/colorPrimary"
            app:layout_scrollFlags="scroll|exitUntilCollapsed"
            app:toolbarId="@+id/toolbar">

            <${getMaterialComponentName('android.support.v7.widget.Toolbar', useAndroidX)}
                android:id="@+id/detail_toolbar"
                android:layout_width="match_parent"
                android:layout_height="?attr/actionBarSize"
                app:layout_collapseMode="pin"
                app:popupTheme="@style/ThemeOverlay.AppCompat.Light" />

        </${getMaterialComponentName('android.support.design.widget.CollapsingToolbarLayout', useMaterial2)}>

    </${getMaterialComponentName('android.support.design.widget.AppBarLayout', useMaterial2)}>

    <${getMaterialComponentName('android.support.v4.widget.NestedScrollView', useAndroidX)}
        android:id="@+id/${detail_name}_container"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_behavior="@string/appbar_scrolling_view_behavior" />

    <${getMaterialComponentName('android.support.design.widget.FloatingActionButton', useMaterial2)}
        android:id="@+id/fab"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center_vertical|start"
        android:layout_margin="@dimen/fab_margin"
        app:srcCompat="@android:drawable/stat_notify_chat"
        app:layout_anchor="@+id/${detail_name}_container"
        app:layout_anchorGravity="top|end" />

</${getMaterialComponentName('android.support.design.widget.CoordinatorLayout', useAndroidX)}>
</#if>
