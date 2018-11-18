package ${escapeKotlinIdentifiers(packageName)}

import android.content.Intent
import android.os.Bundle
<#if hasAppBar>
import ${getMaterialComponentName('android.support.design.widget.Snackbar', useMaterial2)}
</#if>
import ${superClassFqcn}
<#if minApiLevel lt 16>
import ${getMaterialComponentName('android.support.v4.app.NavUtils', useAndroidX)}
</#if>
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_${detail_name}.*

/**
 * An activity representing a single ${objectKind} detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [${CollectionName}Activity].
 */
class ${DetailName}Activity : ${superClass}() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_${detail_name})
<#if hasAppBar>
        setSupportActionBar(detail_toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own detail action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
</#if>

        // Show the Up button in the action bar.
        ${kotlinActionBar}?.setDisplayHomeAsUpEnabled(true)

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            val fragment = ${DetailName}Fragment().apply {
                arguments = Bundle().apply {
                    putString(${DetailName}Fragment.ARG_ITEM_ID,
                            intent.getStringExtra(${DetailName}Fragment.ARG_ITEM_ID))
                }
            }

            ${kotlinFragmentManager}.beginTransaction()
                    .add(R.id.${detail_name}_container, fragment)
                    .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) =
            when (item.itemId) {
                android.R.id.home -> {
<#if minApiLevel lt 16>
                    // This ID represents the Home or Up button. In the case of this
                    // activity, the Up button is shown. Use NavUtils to allow users
                    // to navigate up one level in the application structure. For
                    // more details, see the Navigation pattern on Android Design:
                    //
                    // http://developer.android.com/design/patterns/navigation.html#up-vs-back

                    NavUtils.navigateUpTo(this, Intent(this, ${CollectionName}Activity::class.java))
<#else>
                    // This ID represents the Home or Up button. In the case of this
                    // activity, the Up button is shown. For
                    // more details, see the Navigation pattern on Android Design:
                    //
                    // http://developer.android.com/design/patterns/navigation.html#up-vs-back

                    navigateUpTo(Intent(this, ${CollectionName}Activity::class.java))
</#if>
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
}
