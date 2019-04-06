package ${escapeKotlinIdentifiers(packageName)}

<#if useAndroidX>
import ${getMaterialComponentName('android.support.v4.app.Fragment', useAndroidX)}
<#else>
import android.<#if appCompat>support.v4.</#if>app.Fragment
</#if>
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
<#if applicationPackage??>
import ${applicationPackage}.R
</#if>

/**
 * A placeholder fragment containing a simple view.
 */
class ${fragmentClass} : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.${fragmentLayoutName}, container, false)
    }
}
