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

package ${packageName};

<#if buildApi gte 27>
import android.net.Uri;
</#if>
import android.os.Bundle;
import ${getMaterialComponentName('android.support.v17.leanback.app.VideoSupportFragment', useAndroidX)};
import ${getMaterialComponentName('android.support.v17.leanback.app.VideoSupportFragmentGlueHost', useAndroidX)};
<#if buildApi gte 27>
import ${getMaterialComponentName('android.support.v17.leanback.media.MediaPlayerAdapter', useAndroidX)};
import ${getMaterialComponentName('android.support.v17.leanback.media.PlaybackTransportControlGlue', useAndroidX)};
import ${getMaterialComponentName('android.support.v17.leanback.widget.PlaybackControlsRow', useAndroidX)};
<#else>
import ${getMaterialComponentName('android.support.v17.leanback.media.MediaPlayerGlue', useAndroidX)};
import ${getMaterialComponentName('android.support.v17.leanback.media.PlaybackGlue', useAndroidX)};
</#if>

/** Handles video playback with media controls. */
public class PlaybackVideoFragment extends VideoSupportFragment {

<#if buildApi gte 27>
    private PlaybackTransportControlGlue<MediaPlayerAdapter> mTransportControlGlue;
<#else>
    private MediaPlayerGlue mTransportControlGlue;
</#if>

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Movie movie =
                (Movie) getActivity().getIntent().getSerializableExtra(DetailsActivity.MOVIE);

        VideoSupportFragmentGlueHost glueHost =
                new VideoSupportFragmentGlueHost(PlaybackVideoFragment.this);

<#if buildApi gte 27>
        MediaPlayerAdapter playerAdapter = new MediaPlayerAdapter(<#if minApiLevel lt 23>getActivity()<#else>getContext()</#if>);
        playerAdapter.setRepeatAction(PlaybackControlsRow.RepeatAction.INDEX_NONE);

        mTransportControlGlue = new PlaybackTransportControlGlue<>(<#if minApiLevel lt 23>getActivity()<#else>getContext()</#if>, playerAdapter);
<#else>
        mTransportControlGlue = new MediaPlayerGlue(<#if minApiLevel lt 23>getActivity()<#else>getContext()</#if>);
        mTransportControlGlue.setMode(MediaPlayerGlue.NO_REPEAT);
</#if>
        mTransportControlGlue.setHost(glueHost);
        mTransportControlGlue.setTitle(movie.getTitle());
<#if buildApi gte 27>
        mTransportControlGlue.setSubtitle(movie.getDescription());
        mTransportControlGlue.playWhenPrepared();
        playerAdapter.setDataSource(Uri.parse(movie.getVideoUrl()));
<#else>
        mTransportControlGlue.setArtist(movie.getDescription());
        mTransportControlGlue.addPlayerCallback(
                new PlaybackGlue.PlayerCallback() {
                    @Override
                    public void onPreparedStateChanged(PlaybackGlue glue) {
                        if (glue.isPrepared()) {
                            glue.play();
                        }
                    }
                });
        mTransportControlGlue.setVideoUrl(movie.getVideoUrl());
</#if>
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTransportControlGlue != null) {
            mTransportControlGlue.pause();
        }
    }
}