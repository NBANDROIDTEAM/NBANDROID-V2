package com.example.nageshs.tabbedkot

import android.app.Activity
import android.app.ActionBar
import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
<#if applicationPackage??>
import ${applicationPackage}.R
</#if>

import kotlinx.android.synthetic.main.${fragmentLayoutName}.view.*

class ${activityClass} : ${superClass}(), ActionBar.OnNavigationListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.${layoutName})

        // Set up the action bar to show a dropdown list.
        actionBar?.let {
            it.setDisplayShowTitleEnabled(false)
            it.navigationMode = ActionBar.NAVIGATION_MODE_LIST
            <#if parentActivityClass != "">
            // Show the Up button in the action bar.
            it.setDisplayHomeAsUpEnabled(true)
            </#if>

            // Set up the dropdown list navigation in the action bar.
            it.setListNavigationCallbacks(
                    // Specify a SpinnerAdapter to populate the dropdown list.
                    ArrayAdapter(
                            actionBar.themedContext,
                            android.R.layout.simple_list_item_1,
                            android.R.id.text1,
                            arrayOf("Section 1", "Section 2", "Section 3")),
                    this)
        }
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        // Restore the previously serialized current dropdown position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            actionBar?.setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM))
        }
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        // Serialize the current dropdown position.
        actionBar?.let {
            outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, it.selectedNavigationIndex)
        }
    }

    <#include "include_options_menu.kt.ftl">

    override fun onNavigationItemSelected(position: Int, id: Long): Boolean {
        // When the given dropdown item is selected, show its contents in the
        // container view.
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit()
        return true
    }

    <#include "include_fragment.kt.ftl">

    companion object {

        /**
         * The serialization (saved instance state) Bundle key representing the
         * current dropdown position.
         */
        private val STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item"
    }
}
