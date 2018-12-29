package ${escapeKotlinIdentifiers(packageName)}

import ${superClassFqcn}
import android.os.Bundle
<#if (includeCppSupport!false) && generateLayout>
import kotlinx.android.synthetic.main.${layoutName}.*
</#if>

class ${activityClass} : ${superClass}() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
<#if generateLayout>
        setContentView(R.layout.${layoutName})
        <#include "../../../../common/jni_code_usage.kt.ftl">
<#elseif includeCppSupport!false>

        // Example of a call to a native method
        android.util.Log.d("${activityClass}", stringFromJNI())
</#if>
    }
<#include "../../../../common/jni_code_snippet.kt.ftl">
}
