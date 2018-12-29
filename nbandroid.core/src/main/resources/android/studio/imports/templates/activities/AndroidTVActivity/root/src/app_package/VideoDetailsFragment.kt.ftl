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

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import ${getMaterialComponentName('android.support.v17.leanback.app.DetailsFragment', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.app.DetailsFragmentBackgroundController', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.widget.Action', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.widget.ArrayObjectAdapter', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.widget.ClassPresenterSelector', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.widget.DetailsOverviewRow', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.widget.FullWidthDetailsOverviewRowPresenter', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.widget.FullWidthDetailsOverviewSharedElementHelper', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.widget.HeaderItem', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.widget.ImageCardView', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.widget.ListRow', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.widget.ListRowPresenter', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.widget.OnActionClickedListener', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.widget.OnItemViewClickedListener', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.widget.Presenter', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.widget.Row', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.widget.RowPresenter', useAndroidX)}
import ${getMaterialComponentName('android.support.v4.app.ActivityOptionsCompat', useAndroidX)}
import ${getMaterialComponentName('android.support.v4.content.ContextCompat', useAndroidX)}
import android.util.Log
import android.widget.Toast

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget

import java.util.Collections

/**
 * A wrapper fragment for leanback details screens.
 * It shows a detailed view of video and its metadata plus related videos.
 */
class ${detailsFragment} : DetailsFragment() {

    private var mSelectedMovie: Movie? = null

    private lateinit var mDetailsBackground: DetailsFragmentBackgroundController
    private lateinit var mPresenterSelector: ClassPresenterSelector
    private lateinit var mAdapter: ArrayObjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate DetailsFragment")
        super.onCreate(savedInstanceState)

        mDetailsBackground = DetailsFragmentBackgroundController(this)

        mSelectedMovie = activity.intent.getSerializableExtra(${detailsActivity}.MOVIE) as Movie
        if (mSelectedMovie != null) {
            mPresenterSelector = ClassPresenterSelector()
            mAdapter = ArrayObjectAdapter(mPresenterSelector)
            setupDetailsOverviewRow()
            setupDetailsOverviewRowPresenter()
            setupRelatedMovieListRow()
            adapter = mAdapter
            initializeBackground(mSelectedMovie)
            onItemViewClickedListener = ItemViewClickedListener()
        } else {
            val intent = Intent(<#if minApiLevel lt 23>activity<#else>context</#if>, ${activityClass}::class.java)
            startActivity(intent)
        }
    }

    private fun initializeBackground(movie: Movie?) {
        mDetailsBackground.enableParallax()
        Glide.with(<#if minApiLevel lt 23>activity<#else>context</#if>)
                .load(movie?.backgroundImageUrl)
                .asBitmap()
                .centerCrop()
                .error(R.drawable.default_background)
                .into<SimpleTarget<Bitmap>>(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(bitmap: Bitmap,
                                                 glideAnimation: GlideAnimation<in Bitmap>) {
                        mDetailsBackground.coverBitmap = bitmap
                        mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size())
                    }
                })
    }

    private fun setupDetailsOverviewRow() {
        Log.d(TAG, "doInBackground: " + mSelectedMovie?.toString())
        val row = DetailsOverviewRow(mSelectedMovie)
        row.imageDrawable = ContextCompat.getDrawable(<#if minApiLevel lt 23>activity<#else>context</#if>, R.drawable.default_background)
        val width = convertDpToPixel(<#if minApiLevel lt 23>activity<#else>context</#if>, DETAIL_THUMB_WIDTH)
        val height = convertDpToPixel(<#if minApiLevel lt 23>activity<#else>context</#if>, DETAIL_THUMB_HEIGHT)
        Glide.with(<#if minApiLevel lt 23>activity<#else>context</#if>)
            .load(mSelectedMovie?.cardImageUrl)
            .centerCrop()
            .error(R.drawable.default_background)
            .into<SimpleTarget<GlideDrawable>>(object : SimpleTarget<GlideDrawable>(width, height) {
                        override fun onResourceReady(resource: GlideDrawable,
                                                     glideAnimation: GlideAnimation<in GlideDrawable>) {
                            Log.d(TAG, "details overview card image url ready: " + resource)
                            row.imageDrawable = resource
                            mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size())
                        }
                    })

        val actionAdapter = ArrayObjectAdapter()

        actionAdapter.add(
                Action(
                        ACTION_WATCH_TRAILER,
                        resources.getString(R.string.watch_trailer_1),
                        resources.getString(R.string.watch_trailer_2)))
        actionAdapter.add(
                Action(
                        ACTION_RENT,
                        resources.getString(R.string.rent_1),
                        resources.getString(R.string.rent_2)))
        actionAdapter.add(
                Action(
                        ACTION_BUY,
                        resources.getString(R.string.buy_1),
                        resources.getString(R.string.buy_2)))
        row.actionsAdapter = actionAdapter

        mAdapter.add(row)
    }

    private fun setupDetailsOverviewRowPresenter() {
        // Set detail background.
        val detailsPresenter = FullWidthDetailsOverviewRowPresenter(DetailsDescriptionPresenter())
        detailsPresenter.backgroundColor =
                ContextCompat.getColor(<#if minApiLevel lt 23>activity<#else>context</#if>, R.color.selected_background)

        // Hook up transition element.
        val sharedElementHelper = FullWidthDetailsOverviewSharedElementHelper()
        sharedElementHelper.setSharedElementEnterTransition(
                activity, ${detailsActivity}.SHARED_ELEMENT_NAME)
        detailsPresenter.setListener(sharedElementHelper)
        detailsPresenter.isParticipatingEntranceTransition = true

        detailsPresenter.onActionClickedListener = OnActionClickedListener { action ->
            if (action.id == ACTION_WATCH_TRAILER) {
                val intent = Intent(<#if minApiLevel lt 23>activity<#else>context</#if>, PlaybackActivity::class.java)
                intent.putExtra(${detailsActivity}.MOVIE, mSelectedMovie)
                startActivity(intent)
            } else {
                Toast.makeText(<#if minApiLevel lt 23>activity<#else>context</#if>, action.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        mPresenterSelector.addClassPresenter(DetailsOverviewRow::class.java, detailsPresenter)
    }

    private fun setupRelatedMovieListRow() {
        val subcategories = arrayOf(getString(R.string.related_movies))
        val list = MovieList.list

        Collections.shuffle(list)
        val listRowAdapter = ArrayObjectAdapter(CardPresenter())
        for (j in 0 until NUM_COLS ) {
            listRowAdapter.add(list[j % 5])
        }

<#if buildApi gte 22>
        val header = HeaderItem(0, subcategories[0])
<#else>
        val header = HeaderItem(0, subcategories[0], null)
</#if>
        mAdapter.add(ListRow(header, listRowAdapter))
        mPresenterSelector.addClassPresenter(ListRow::class.java, ListRowPresenter())
    }

    private fun convertDpToPixel(context: Context, dp: Int): Int {
        val density = context.applicationContext.resources.displayMetrics.density
        return Math.round(dp.toFloat() * density)
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(
                itemViewHolder: Presenter.ViewHolder?,
                item: Any?,
                rowViewHolder: RowPresenter.ViewHolder,
                row: Row) {
            if (item is Movie) {
                Log.d(TAG, "Item: " + item.toString())
                val intent = Intent(<#if minApiLevel lt 23>activity<#else>context</#if>, ${detailsActivity}::class.java)
                intent.putExtra(resources.getString(R.string.movie), mSelectedMovie)

                val bundle =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                            activity,
                            (itemViewHolder?.view as ImageCardView).mainImageView,
                            ${detailsActivity}.SHARED_ELEMENT_NAME)
                        .toBundle()
                activity.startActivity(intent, bundle)
            }
        }
    }

    companion object {
        private val TAG = "${truncate(detailsFragment,23)}"

        private val ACTION_WATCH_TRAILER = 1L
        private val ACTION_RENT = 2L
        private val ACTION_BUY = 3L

        private val DETAIL_THUMB_WIDTH = 274
        private val DETAIL_THUMB_HEIGHT = 274

        private val NUM_COLS = 10
    }
}