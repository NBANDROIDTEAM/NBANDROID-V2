/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ${escapeKotlinIdentifiers(packageName)}

import java.util.Collections
import java.util.Timer
import java.util.TimerTask

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import ${getMaterialComponentName('android.support.v17.leanback.app.BackgroundManager', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.app.BrowseFragment', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.widget.ArrayObjectAdapter', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.widget.HeaderItem', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.widget.ImageCardView', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.widget.ListRow', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.widget.ListRowPresenter', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.widget.OnItemViewClickedListener', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.widget.OnItemViewSelectedListener', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.widget.Presenter', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.widget.Row', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.widget.RowPresenter', useAndroidX)}
import ${getMaterialComponentName('android.support.v4.app.ActivityOptionsCompat', useAndroidX)}
import ${getMaterialComponentName('android.support.v4.content.ContextCompat', useAndroidX)}
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget

/**
 * Loads a grid of cards with movies to browse.
 */
class ${mainFragment} : BrowseFragment() {

    private val mHandler = Handler()
    private lateinit var mBackgroundManager: BackgroundManager
    private var mDefaultBackground: Drawable? = null
    private lateinit var mMetrics: DisplayMetrics
    private var mBackgroundTimer: Timer? = null
    private var mBackgroundUri: String? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate")
        super.onActivityCreated(savedInstanceState)

        prepareBackgroundManager()

        setupUIElements()

        loadRows()

        setupEventListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: " + mBackgroundTimer?.toString())
        mBackgroundTimer?.cancel()
    }

    private fun prepareBackgroundManager() {

        mBackgroundManager = BackgroundManager.getInstance(activity)
        mBackgroundManager.attach(activity.window)
        mDefaultBackground = ContextCompat.getDrawable(<#if minApiLevel lt 23>activity<#else>context</#if>, R.drawable.default_background)
        mMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(mMetrics)
    }

    private fun setupUIElements() {
        title = getString(R.string.browse_title)
        // over title
        headersState = BrowseFragment.HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        // set fastLane (or headers) background color
        brandColor = ContextCompat.getColor(<#if minApiLevel lt 23>activity<#else>context</#if>, R.color.fastlane_background)
        // set search icon color
        searchAffordanceColor = ContextCompat.getColor(<#if minApiLevel lt 23>activity<#else>context</#if>, R.color.search_opaque)
    }

    private fun loadRows() {
        val list = MovieList.list

        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        val cardPresenter = CardPresenter()

        for (i in 0 until NUM_ROWS) {
            if (i != 0) {
                Collections.shuffle(list)
            }
            val listRowAdapter = ArrayObjectAdapter(cardPresenter)
            for (j in 0 until NUM_COLS ) {
                listRowAdapter.add(list[j % 5])
            }
            <#if buildApi gte 22>
                val header = HeaderItem(i.toLong(), MovieList.MOVIE_CATEGORY[i])
            <#else>
                val header = HeaderItem(i.toLong(), MovieList.MOVIE_CATEGORY[i], null)
            </#if>
            rowsAdapter.add(ListRow(header, listRowAdapter))
        }

        <#if buildApi gte 22>
            val gridHeader = HeaderItem(NUM_ROWS.toLong(), "PREFERENCES")
        <#else>
            val gridHeader = HeaderItem(NUM_ROWS.toLong(), "PREFERENCES", null)
        </#if>

        val mGridPresenter = GridItemPresenter()
        val gridRowAdapter = ArrayObjectAdapter(mGridPresenter)
        gridRowAdapter.add(resources.getString(R.string.grid_view))
        gridRowAdapter.add(getString(R.string.error_fragment))
        gridRowAdapter.add(resources.getString(R.string.personal_settings))
        rowsAdapter.add(ListRow(gridHeader, gridRowAdapter))

        adapter = rowsAdapter
    }

    private fun setupEventListeners() {
        setOnSearchClickedListener {
            Toast.makeText(<#if minApiLevel lt 23>activity<#else>context</#if>, "Implement your own in-app search", Toast.LENGTH_LONG)
                .show()
        }

        onItemViewClickedListener = ItemViewClickedListener()
        onItemViewSelectedListener = ItemViewSelectedListener()
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(
                itemViewHolder: Presenter.ViewHolder,
                item: Any,
                rowViewHolder: RowPresenter.ViewHolder,
                row: Row) {

            if (item is Movie) {
                Log.d(TAG, "Item: " + item.toString())
                val intent = Intent(<#if minApiLevel lt 23>activity<#else>context</#if>, ${detailsActivity}::class.java)
                intent.putExtra(${detailsActivity}.MOVIE, item)

                val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                                activity,
                                                (itemViewHolder.view as ImageCardView).mainImageView,
                                                ${detailsActivity}.SHARED_ELEMENT_NAME)
                                        .toBundle()
                activity.startActivity(intent, bundle)
            } else if (item is String) {
                if (item.contains(getString(R.string.error_fragment))) {
                    val intent = Intent(<#if minApiLevel lt 23>activity<#else>context</#if>, BrowseErrorActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(<#if minApiLevel lt 23>activity<#else>context</#if>, item, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private inner class ItemViewSelectedListener : OnItemViewSelectedListener {
        override fun onItemSelected(itemViewHolder: Presenter.ViewHolder?, item: Any?,
                                    rowViewHolder: RowPresenter.ViewHolder, row: Row) {
            if (item is Movie) {
                mBackgroundUri = item.backgroundImageUrl
                startBackgroundTimer()
            }
        }
    }

    private fun updateBackground(uri: String?) {
        val width = mMetrics.widthPixels
        val height = mMetrics.heightPixels
        Glide.with(<#if minApiLevel lt 23>activity<#else>context</#if>)
                .load(uri)
                .centerCrop()
                .error(mDefaultBackground)
                .into<SimpleTarget<GlideDrawable>>(
                        object : SimpleTarget<GlideDrawable>(width, height) {
                            override fun onResourceReady(resource: GlideDrawable,
                                                         glideAnimation: GlideAnimation<in GlideDrawable>) {
                                mBackgroundManager.drawable = resource
                            }
                        })
        mBackgroundTimer?.cancel()
    }

    private fun startBackgroundTimer() {
        mBackgroundTimer?.cancel()
        mBackgroundTimer = Timer()
        mBackgroundTimer?.schedule(UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY.toLong())
    }

    private inner class UpdateBackgroundTask : TimerTask() {

        override fun run() {
            mHandler.post { updateBackground(mBackgroundUri) }
        }
    }

    private inner class GridItemPresenter : Presenter() {
        override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
            val view = TextView(parent.context)
            view.layoutParams = ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT)
            view.isFocusable = true
            view.isFocusableInTouchMode = true
            view.setBackgroundColor(ContextCompat.getColor(<#if minApiLevel lt 23>activity<#else>context</#if>, R.color.default_background))
            view.setTextColor(Color.WHITE)
            view.gravity = Gravity.CENTER
            return Presenter.ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any) {
            (viewHolder.view as TextView).text = item as String
        }

        override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {}
    }

    companion object {
        private val TAG = "${truncate(mainFragment,23)}"

        private val BACKGROUND_UPDATE_DELAY = 300
        private val GRID_ITEM_WIDTH = 200
        private val GRID_ITEM_HEIGHT = 200
        private val NUM_ROWS = 6
        private val NUM_COLS = 15
    }
}
