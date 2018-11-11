package ${packageName};

import android.<#if appCompat>support.v4.</#if>app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
<#if applicationPackage??>
import ${applicationPackage}.R;
</#if>

/**
 * A placeholder fragment containing a simple view.
 */
public class ${fragmentClass} extends Fragment {

    public ${fragmentClass}() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.${fragmentLayoutName}, container, false);
    }
}
