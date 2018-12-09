<fragment xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
<#if hasAppBar>
    xmlns:app="http://schemas.android.com/apk/res-auto"
</#if>
    android:id="@+id/fragment"
    android:name="${packageName}.${fragmentClass}"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
<#if hasAppBar>
    app:layout_behavior="@string/appbar_scrolling_view_behavior"
</#if>
    tools:layout="@layout/${fragmentLayoutName}" />
