package ${escapeKotlinIdentifiers(packageName)}

<#if hasAppBar>
<#if features == 'tabs'>
import ${getMaterialComponentName('android.support.design.widget.TabLayout', useMaterial2)}
</#if>
import ${getMaterialComponentName('android.support.design.widget.Snackbar', useMaterial2)}
import ${getMaterialComponentName('android.support.v7.app.AppCompatActivity', useAndroidX)}
<#else>  <#-- hasAppBar -->
import ${superClassFqcn};
<#if useAndroidX>
import ${getMaterialComponentName('android.support.v7.app.ActionBar', useAndroidX)}
import ${getMaterialComponentName('android.support.v4.app.FragmentTransaction', useAndroidX)}
<#else>
import android.<#if appCompat>support.v7.</#if>app.ActionBar
import android.<#if appCompat>support.v4.</#if>app.FragmentTransaction
</#if>
</#if>   <#-- hasAppBar -->
<#if useAndroidX>
import ${getMaterialComponentName('android.support.v4.app.Fragment', useAndroidX)}
<#else>
import android.<#if appCompat>support.v4.</#if>app.Fragment
</#if>
<#if hasViewPager>
<#if useAndroidX>
import ${getMaterialComponentName('android.support.v4.app.FragmentManager', useAndroidX)}
<#else>
import android.<#if appCompat>support.v4.</#if>app.FragmentManager
</#if>
<#if useAndroidX>
import ${getMaterialComponentName('android.support.v4.app.FragmentPagerAdapter', useAndroidX)}
<#else>
<#if useAndroidX>
import ${getMaterialComponentName('android.support.v4.app.FragmentPagerAdapter', useAndroidX)}
<#else>
import android.support.${(appCompat)?string('v4','v13')}.app.FragmentPagerAdapter
</#if>
</#if>
import ${getMaterialComponentName('android.support.v4.view.ViewPager', useAndroidX)}
</#if>
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
<#if features == 'spinner'>
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.content.Context
<#if buildApi gte 23>
import ${getMaterialComponentName('android.support.v7.widget.ThemedSpinnerAdapter', useAndroidX)}
import android.content.res.Resources.Theme
<#else>
import android.graphics.Color
</#if>
</#if>  <#-- features == 'spinner' -->
<#if applicationPackage??>
import ${applicationPackage}.R
</#if>
import kotlinx.android.synthetic.main.${layoutName}.*
import kotlinx.android.synthetic.main.${fragmentLayoutName}.view.*
<#if features == 'spinner'>
import kotlinx.android.synthetic.main.list_item.view.*
</#if>

class ${activityClass} : ${superClass}()<#if !hasAppBar && features == 'tabs'>, ActionBar.TabListener</#if> {

<#if hasViewPager>
    /**
     * The [${getMaterialComponentName('android.support.v4.view.PagerAdapter', useAndroidX)}] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * <#if useAndroidX>${getMaterialComponentName('android.support.v4.app.FragmentStatePagerAdapter', useAndroidX)}<#else>[android.support.${(appCompat)?string('v4','v13')}.app.FragmentStatePagerAdapter]</#if>.
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

</#if>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.${layoutName})
    <#if hasAppBar>

        setSupportActionBar(toolbar)
    </#if>
<#if parentActivityClass?has_content>
        ${kotlinActionBar}?.setDisplayHomeAsUpEnabled(true)
</#if>
<#if hasViewPager>
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(<#if Support?has_content>supportFragmentManager<#else>fragmentManager</#if>)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter
</#if>
<#if hasAppBar>
    <#if features == 'tabs'>

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
    <#elseif features == 'spinner'>
        ${kotlinActionBar}?.setDisplayShowTitleEnabled(false)

        // Setup spinner
        spinner.adapter = MyAdapter(
                toolbar.context,
                arrayOf("Section 1", "Section 2", "Section 3"))

        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // When the given dropdown item is selected, show its contents in the
                // container view.
                <#if Support?has_content>supportFragmentManager<#else>fragmentManager</#if>.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    </#if>

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
<#else> <#-- hasAppBar -->
    <#if features == 'tabs'>
        ${kotlinActionBar}?.navigationMode = ActionBar.NAVIGATION_MODE_TABS

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        container.setOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                ${kotlinActionBar}?.setSelectedNavigationItem(position)
            }
        })

        mSectionsPagerAdapter?.let {
            // For each of the sections in the app, add a tab to the action bar.
            for (i in 0 until it.count) {
                // Create a tab with text corresponding to the page title defined by
                // the adapter. Also specify this Activity object, which implements
                // the TabListener interface, as the callback (listener) for when
                // this tab is selected.
                actionBar.addTab(
                        actionBar.newTab()
                                .setText(it.getPageTitle(i))
                                .setTabListener(this))
            }
        }
    </#if>
</#if>  <#-- hasAppBar -->
    }

<#include "include_options_menu.kt.ftl">
<#if !hasAppBar && features == 'tabs'>
    override fun onTabSelected(tab: ActionBar.Tab, fragmentTransaction: FragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        container.currentItem = tab.position
    }

    override fun onTabUnselected(tab: ActionBar.Tab, fragmentTransaction: FragmentTransaction) {}

    override fun onTabReselected(tab: ActionBar.Tab, fragmentTransaction: FragmentTransaction) {}

</#if>
<#if features == 'spinner'>
    <#if buildApi gte 23>
    private class MyAdapter(context: Context, objects: Array<String>) : ArrayAdapter<String>(context, R.layout.list_item, objects), ThemedSpinnerAdapter {
        private val mDropDownHelper: ThemedSpinnerAdapter.Helper = ThemedSpinnerAdapter.Helper(context)

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: View

            if (convertView == null) {
                // Inflate the drop down using the helper's LayoutInflater
                val inflater = mDropDownHelper.dropDownViewInflater
                view = inflater.inflate(R.layout.list_item, parent, false)
            } else {
                view = convertView
            }

            view.text1.text = getItem(position)

            return view
        }

        override fun getDropDownViewTheme(): Theme? {
            return mDropDownHelper.dropDownViewTheme
        }

        override fun setDropDownViewTheme(theme: Theme?) {
            mDropDownHelper.dropDownViewTheme = theme
        }
    }

    <#else>  <#-- buildApi gte 23 -->
    private class MyAdapter(context: Context, objects: Array<String>) :
            ArrayAdapter<String>(context, R.layout.list_item, R.id.text1, objects) {

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            // Hack. Use BuildVersion 23 for a better approach.
            view.text1.setTextColor(Color.BLACK)
            view.text1.setBackgroundColor(Color.WHITE)
            return view
        }
    }
    </#if>
</#if>  <#-- features == 'spinner' -->

<#if hasViewPager>
    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1)
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 3
        }
        <#if !appCompat>

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return "SECTION 1"
                1 -> return "SECTION 2"
                2 -> return "SECTION 3"
            }
            return null
        }
        </#if>
    }
</#if>

<#include "include_fragment.kt.ftl">
}
