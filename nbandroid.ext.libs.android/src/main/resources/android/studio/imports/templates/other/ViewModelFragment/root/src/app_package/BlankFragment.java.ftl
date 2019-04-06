package ${packageName};

import ${getMaterialComponentName('android.arch.lifecycle.ViewModelProviders', useAndroidX)};
import android.os.Bundle;
import ${getMaterialComponentName('android.support.annotation.NonNull', useAndroidX)};
import ${getMaterialComponentName('android.support.annotation.Nullable', useAndroidX)};
import ${getMaterialComponentName('android${SupportPackage}.app.Fragment', useAndroidX)};
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
<#if applicationPackage??>
import ${applicationPackage}.R;
</#if>

public class ${className} extends Fragment {

    public static ${className} newInstance() {
        return new ${className}();
    }

    private ${viewModelName} mViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.${layoutName}, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(${viewModelName}.class);
        // TODO: Use the ViewModel
    }

}
