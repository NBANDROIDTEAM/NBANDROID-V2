<?xml version="1.0" encoding="utf-8"?>
<${getMaterialComponentName('android.support.constraint.ConstraintLayout', useAndroidX)}
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/container"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="${packageName}.${activityClass}">

    <TextView
        android:id="@+id/message"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="@dimen/activity_horizontal_margin"
        android:layout_marginLeft="@dimen/activity_horizontal_margin"
        android:layout_marginTop="@dimen/activity_vertical_margin"
        android:text="@string/title_home"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <${getMaterialComponentName('android.support.design.widget.BottomNavigationView', useMaterial2)}
        android:id="@+id/navigation"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginEnd="0dp"
        android:layout_marginStart="0dp"
        android:background="?android:attr/windowBackground"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:menu="@menu/navigation" />

</${getMaterialComponentName('android.support.constraint.ConstraintLayout', useAndroidX)}>
