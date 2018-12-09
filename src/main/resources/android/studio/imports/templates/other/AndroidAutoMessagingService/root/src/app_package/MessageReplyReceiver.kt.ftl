/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ${kotlinEscapedPackageName}

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ${getMaterialComponentName('android.support.v4.app.RemoteInput', useAndroidX)}
import android.util.Log

private const val TAG = "${truncate(replyReceiverName,23)}"

/**
 * A receiver that gets called when a reply is sent to a given conversationId
 */
class ${replyReceiverName} : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (REPLY_ACTION == intent.action) {
            val conversationId = intent.getIntExtra(CONVERSATION_ID, -1)
            val reply = getMessageText(intent)
            Log.d(TAG, "Got reply ($reply) for ConversationId $conversationId")
        }
    }

    /**
     * Get the message text from the intent.
     * Note that you should call `RemoteInput#getResultsFromIntent(intent)` to process
     * the RemoteInput.
     */
    private fun getMessageText(intent: Intent): CharSequence? {
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        return remoteInput?.getCharSequence(EXTRA_VOICE_REPLY)
    }
}
