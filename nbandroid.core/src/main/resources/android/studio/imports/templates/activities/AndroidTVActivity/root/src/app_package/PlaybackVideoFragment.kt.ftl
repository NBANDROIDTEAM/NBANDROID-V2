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

<#if buildApi gte 27>
import android.net.Uri
</#if>
import android.os.Bundle
import ${getMaterialComponentName('android.support.v17.leanback.app.VideoSupportFragment', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.app.VideoSupportFragmentGlueHost', useAndroidX)}
<#if buildApi gte 27>
import ${getMaterialComponentName('android.support.v17.leanback.media.MediaPlayerAdapter', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.media.PlaybackTransportControlGlue', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.widget.PlaybackControlsRow', useAndroidX)}
<#else>
import ${getMaterialComponentName('android.support.v17.leanback.media.MediaPlayerGlue', useAndroidX)}
import ${getMaterialComponentName('android.support.v17.leanback.media.PlaybackGlue', useAndroidX)}
</#if>

/** Handles video playback with media controls. */
class PlaybackVideoFragment : VideoSupportFragment() {

<#if buildApi gte 27>
    private lateinit var mTransportControlGlue: PlaybackTransportControlGlue<MediaPlayerAdapter>
<#else>
    private lateinit var mTransportControlGlue: MediaPlayerGlue
</#if>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val (_, title, description, _, _, videoUrl) =
                activity?.intent?.getSerializableExtra(DetailsActivity.MOVIE) as Movie

        val glueHost = VideoSupportFragmentGlueHost(this@PlaybackVideoFragment)
<#if buildApi gte 27>
        val playerAdapter = MediaPlayerAdapter(<#if minApiLevel lt 23>activity<#else>context</#if>)
        playerAdapter.setRepeatAction(PlaybackControlsRow.RepeatAction.INDEX_NONE)

        mTransportControlGlue = PlaybackTransportControlGlue(getActivity(), playerAdapter)
<#else>
        mTransportControlGlue = MediaPlayerGlue(<#if minApiLevel lt 23>activity<#else>context</#if>)
        mTransportControlGlue.setMode(MediaPlayerGlue.NO_REPEAT)
</#if>
        mTransportControlGlue.host = glueHost
<#if buildApi gte 27>
        mTransportControlGlue.title = title
        mTransportControlGlue.subtitle = description
        mTransportControlGlue.playWhenPrepared()

        playerAdapter.setDataSource(Uri.parse(videoUrl))
<#else>
        mTransportControlGlue.addPlayerCallback(object : PlaybackGlue.PlayerCallback() {
            override fun onPreparedStateChanged(glue: PlaybackGlue?) {
                glue?.let {
                    if (it.isPrepared) {
                        it.play()
                    }
                }
            }
        })
        mTransportControlGlue.setTitle(title)
        mTransportControlGlue.setArtist(description)
        mTransportControlGlue.setVideoUrl(videoUrl)
</#if>
    }

    override fun onPause() {
        super.onPause()
        mTransportControlGlue.pause()
    }
}