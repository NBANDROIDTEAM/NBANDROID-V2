package ${packageName};

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
<#if applicationPackage??>
import ${applicationPackage}.R;
</#if>

public class ${displayActivityClass} extends Activity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.${displayActivityLayout});
        mTextView = (TextView) findViewById(R.id.text);
    }
}
