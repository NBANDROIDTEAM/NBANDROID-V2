<?xml version="1.0" encoding="utf-8"?>
<${getMaterialComponentName('android.support.constraint.ConstraintLayout', useAndroidX)}
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
<#if hasAppBar && appBarLayoutName??>
    tools:showIn="@layout/${appBarLayoutName}"
</#if>
    tools:context="${packageName}.${fragmentClass}">

<#if isNewProject!false>
    <TextView
<#if includeCppSupport!false>
        android:id="@+id/sample_text"
</#if>
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Hello World!"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

</#if>
</${getMaterialComponentName('android.support.constraint.ConstraintLayout', useAndroidX)}>
