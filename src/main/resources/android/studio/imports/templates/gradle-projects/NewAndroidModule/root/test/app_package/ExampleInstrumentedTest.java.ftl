package ${packageName};

import android.content.Context;
import ${getMaterialComponentName('android.support.test.InstrumentationRegistry', useAndroidX)};
import ${getMaterialComponentName('android.support.test.runner.AndroidJUnit4', useAndroidX)};

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        <#if isLibraryProject?? && isLibraryProject>
        assertEquals("${packageName}.test", appContext.getPackageName());
        <#else>
        assertEquals("${packageName}", appContext.getPackageName());
        </#if>
    }
}
