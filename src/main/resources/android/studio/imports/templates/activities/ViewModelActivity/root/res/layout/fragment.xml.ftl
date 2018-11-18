<?xml version="1.0" encoding="utf-8"?>
<${getMaterialComponentName('android.support.constraint.ConstraintLayout', useAndroidX)}
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/${classToResource(fragmentClass)?replace('_', '')}"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".${fragmentPackage}.${fragmentClass}">

    <TextView
        android:id="@+id/message"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="${fragmentClass}"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"/>

</${getMaterialComponentName('android.support.constraint.ConstraintLayout', useAndroidX)}>
