package ${packageName};

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.android.things.contrib.driver.gps.NmeaGpsDriver;

import java.io.IOException;

/**
 * To use this service, start it from your component (like an activity):
 * <pre>{@code
 * this.startService(new Intent(this, ${gpsServiceClass}.class))
 * }</pre>
 */
public class ${gpsServiceClass} extends Service {
    private static final String TAG = ${gpsServiceClass}.class.getSimpleName();
    private static final String UART_BUS = "";
    public static final int UART_BAUD = 9600;
    public static final float ACCURACY = 2.5f; // From GPS datasheet

    private NmeaGpsDriver mGpsDriver;

    @Override
    public void onCreate() {
        setupGps();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyGps();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    private void setupGps() {
        try {
            // Register the GPS driver
            mGpsDriver = new NmeaGpsDriver(this, UART_BUS, UART_BAUD, ACCURACY);
            mGpsDriver.register();
        } catch (IOException e) {
            Log.w(TAG, "Unable to open GPS UART", e);
        }
    }

    private void destroyGps() {
        if (mGpsDriver != null) {
            // Unregister components
            mGpsDriver.unregister();

            try {
                mGpsDriver.close();
            } catch (IOException e) {
                Log.w(TAG, "Unable to close GPS driver", e);
            } finally {
                mGpsDriver = null;
            }
        }
    }
}
