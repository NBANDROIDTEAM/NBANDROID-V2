package ${packageName};

import android.os.Bundle;
import ${getMaterialComponentName('android.support.wearable.activity.WearableActivity', useAndroidX)};
import android.widget.TextView;

public class ${activityClass} extends WearableActivity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.${layoutName});

        mTextView = (TextView) findViewById(R.id.text);

        // Enables Always-on
        setAmbientEnabled();
    }
}
