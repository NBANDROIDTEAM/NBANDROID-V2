package ${escapeKotlinIdentifiers(packageName)}

import android.os.Bundle
<#if hasAppBar>
<#if buildApi == 22>
import ${getMaterialComponentName('android.support.design.widget.CollapsingToolbarLayout', useMaterial2)}
</#if>
import ${getMaterialComponentName('android.support.design.widget.Snackbar', useMaterial2)}
import ${getMaterialComponentName('android.support.v7.app.AppCompatActivity', useAndroidX)}
<#else>
import ${superClassFqcn}
</#if>
<#if isNewProject>
import android.view.Menu
import android.view.MenuItem
</#if>
import kotlinx.android.synthetic.main.${layoutName}.*

class ${activityClass} : ${superClass}() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.${layoutName})
<#if hasAppBar>
        setSupportActionBar(toolbar)
<#if buildApi == 22>
        toolbar_layout.title = title
</#if>
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
</#if>
<#if parentActivityClass?has_content>
        ${kotlinActionBar}?.setDisplayHomeAsUpEnabled(true)
</#if>
    }
<#if isNewProject>

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.${menuName}, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
</#if>
}
