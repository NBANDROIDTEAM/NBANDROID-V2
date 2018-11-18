<#assign kPackageName=escapeKotlinIdentifiers(packageName)>
package ${kPackageName}

import android.app.IntentService
import android.content.Intent
<#if includeHelper>import android.content.Context</#if>

// TODO: Rename actions, choose action names that describe tasks that this
// IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
<#if includeHelper>private</#if> const val ACTION_FOO = "${kPackageName}.action.FOO"
<#if includeHelper>private</#if> const val ACTION_BAZ = "${kPackageName}.action.BAZ"

// TODO: Rename parameters
<#if includeHelper>private</#if> const val EXTRA_PARAM1 = "${kPackageName}.extra.PARAM1"
<#if includeHelper>private</#if> const val EXTRA_PARAM2 = "${kPackageName}.extra.PARAM2"
/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
<#if includeHelper>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
<#else>
 * TODO: Customize class - update intent actions and extra parameters.
</#if>
 */
class ${className} : IntentService("${className}") {

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_FOO -> {
                val param1 = intent.getStringExtra(EXTRA_PARAM1)
                val param2 = intent.getStringExtra(EXTRA_PARAM2)
                handleActionFoo(param1, param2)
            }
            ACTION_BAZ -> {
                val param1 = intent.getStringExtra(EXTRA_PARAM1)
                val param2 = intent.getStringExtra(EXTRA_PARAM2)
                handleActionBaz(param1, param2)
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionFoo(param1: String, param2: String) {
        TODO("Handle action Foo")
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionBaz(param1: String, param2: String) {
        TODO("Handle action Baz")
    }
<#if includeHelper>
    companion object {
        /**
         * Starts this service to perform action Foo with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        // TODO: Customize helper method
        @JvmStatic fun startActionFoo(context: Context, param1: String, param2: String) {
            val intent = Intent(context, ${className}::class.java).apply {
                action = ACTION_FOO
                putExtra(EXTRA_PARAM1, param1)
                putExtra(EXTRA_PARAM2, param2)
            }
            context.startService(intent)
        }

        /**
         * Starts this service to perform action Baz with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        // TODO: Customize helper method
        @JvmStatic fun startActionBaz(context: Context, param1: String, param2: String) {
            val intent = Intent(context, ${className}::class.java).apply {
                action = ACTION_BAZ
                putExtra(EXTRA_PARAM1, param1)
                putExtra(EXTRA_PARAM2, param2)
            }
            context.startService(intent)
        }
    }
</#if>
}
