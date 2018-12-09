package ${packageName};

import android.os.Bundle;
import ${getMaterialComponentName('android.support.annotation.NonNull', useAndroidX)};
import ${getMaterialComponentName('android.support.design.widget.BottomNavigationView', useMaterial2)};
import ${getMaterialComponentName('android.support.v7.app.AppCompatActivity', useAndroidX)};
import android.view.MenuItem;
import android.widget.TextView;

public class ${activityClass} extends AppCompatActivity {

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
<#if isLibraryProject!false>
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                mTextMessage.setText(R.string.title_home);
                return true;
            }
            else if (id == R.id.navigation_dashboard) {
                mTextMessage.setText(R.string.title_dashboard);
                return true;
            }
            else if (id == R.id.navigation_notifications) {
                mTextMessage.setText(R.string.title_notifications);
                return true;
            }
<#else>
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
</#if>
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.${layoutName});

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}
