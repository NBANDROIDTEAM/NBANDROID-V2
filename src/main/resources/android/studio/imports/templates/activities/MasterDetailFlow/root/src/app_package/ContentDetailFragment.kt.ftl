package ${escapeKotlinIdentifiers(packageName)}

import android.os.Bundle
<#if useAndroidX>
import ${getMaterialComponentName('android.support.v4.app.Fragment', useAndroidX)}
<#else>
import android.<#if appCompat>support.v4.</#if>app.Fragment
</#if>
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
<#if applicationPackage??>
import ${applicationPackage}.R
</#if>
import ${packageName}.dummy.DummyContent
import kotlinx.android.synthetic.main.activity_${detail_name}.*
import kotlinx.android.synthetic.main.${detail_name}.view.*

/**
 * A fragment representing a single ${objectKind} detail screen.
 * This fragment is either contained in a [${CollectionName}Activity]
 * in two-pane mode (on tablets) or a [${DetailName}Activity]
 * on handsets.
 */
class ${DetailName}Fragment : Fragment() {

    /**
     * The dummy content this fragment is presenting.
     */
    private var item: DummyContent.DummyItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(ARG_ITEM_ID)) {
                // Load the dummy content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                item = DummyContent.ITEM_MAP[it.getString(ARG_ITEM_ID)]
<#if hasAppBar>
                activity?.toolbar_layout?.title = item?.content
</#if>
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.${detail_name}, container, false)

        // Show the dummy content as text in a TextView.
        item?.let {
            rootView.${detail_name}.text = it.details
        }

        return rootView
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        const val ARG_ITEM_ID = "item_id"
    }
}
