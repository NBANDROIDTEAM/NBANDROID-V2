package ${escapeKotlinIdentifiers(packageName)}

import android.content.Intent
import android.os.Bundle
import ${getMaterialComponentName('android.support.v7.app.AppCompatActivity', useAndroidX)}
import ${getMaterialComponentName('android.support.v7.widget.RecyclerView', useAndroidX)}
<#if hasAppBar>
import ${getMaterialComponentName('android.support.design.widget.Snackbar', useMaterial2)}
</#if>
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
<#if parentActivityClass?has_content>
import ${getMaterialComponentName('android.support.v4.app.NavUtils', useAndroidX)}
import ${actionBarClassFqcn}
import android.view.MenuItem
</#if>
<#if applicationPackage??>
import ${applicationPackage}.R
</#if>

import ${kotlinEscapedPackageName}.dummy.DummyContent
import kotlinx.android.synthetic.main.activity_${item_list_layout}.*
import kotlinx.android.synthetic.main.${item_list_content_layout}.view.*
import kotlinx.android.synthetic.main.${collection_name}.*

/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [${DetailName}Activity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class ${CollectionName}Activity : ${superClass}() {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var twoPane: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
<#if hasAppBar>
        setContentView(R.layout.activity_${item_list_layout})

        setSupportActionBar(toolbar)
        toolbar.title = title

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
<#else>
        setContentView(R.layout.${item_list_layout})
</#if>
<#if parentActivityClass != "">
        // Show the Up button in the action bar.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
</#if>

        if (${detail_name}_container != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
        }

        setupRecyclerView(${collection_name})
    }
<#if parentActivityClass != "">

    override fun onOptionsItemSelected(item: MenuItem) =
            when (item.itemId) {
                android.R.id.home -> {
                    // This ID represents the Home or Up button. In the case of this
                    // activity, the Up button is shown. Use NavUtils to allow users
                    // to navigate up one level in the application structure. For
                    // more details, see the Navigation pattern on Android Design:
                    //
                    // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                    NavUtils.navigateUpFromSameTask(this)
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
</#if>

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = SimpleItemRecyclerViewAdapter(this, DummyContent.ITEMS, twoPane)
    }

    class SimpleItemRecyclerViewAdapter(private val parentActivity: ${CollectionName}Activity,
                                        private val values: List<DummyContent.DummyItem>,
                                        private val twoPane: Boolean) :
            RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener

        init {
            onClickListener = View.OnClickListener { v ->
                val item = v.tag as DummyContent.DummyItem
                if (twoPane) {
                    val fragment = ${DetailName}Fragment().apply {
                        arguments = Bundle().apply {
                            putString(${DetailName}Fragment.ARG_ITEM_ID, item.id)
                        }
                    }
                    parentActivity.supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.${detail_name}_container, fragment)
                            .commit()
                } else {
                    val intent = Intent(v.context, ${DetailName}Activity::class.java).apply {
                        putExtra(${DetailName}Fragment.ARG_ITEM_ID, item.id)
                    }
                    v.context.startActivity(intent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.${item_list_content_layout}, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            holder.idView.text = item.id
            holder.contentView.text = item.content

            with(holder.itemView) {
                tag = item
                setOnClickListener(onClickListener)
            }
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val idView: TextView = view.id_text
            val contentView: TextView = view.content
        }
    }
}
