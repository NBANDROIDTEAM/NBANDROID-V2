package ${escapeKotlinIdentifiers(packageName)}

import ${getMaterialComponentName('android.support.test.InstrumentationRegistry', useAndroidX)}
import ${getMaterialComponentName('android.support.test.runner.AndroidJUnit4', useAndroidX)}

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
<#if isLibraryProject?? && isLibraryProject>
        assertEquals("${packageName}.test", appContext.packageName)
<#else>
        assertEquals("${packageName}", appContext.packageName)
</#if>
    }
}
