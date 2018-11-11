package ${packageName};

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
<#if applicationPackage??>
import ${applicationPackage}.R;
</#if>

/**
 * Example shell activity which simply broadcasts to our receiver and exits.
 */
public class ${stubActivityClass} extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = new Intent();
        i.setAction("${packageName}.SHOW_NOTIFICATION");
        i.putExtra(${receiverClass}.CONTENT_KEY, getString(R.string.title));
        sendBroadcast(i);
        finish();
    }
}
