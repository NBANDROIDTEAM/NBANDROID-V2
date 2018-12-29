package ${packageName};

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.android.things.contrib.driver.mma7660fc.Mma7660FcAccelerometerDriver;

import java.io.IOException;

/**
 * To use this service, start it from your component (like an activity):
 * <pre>{@code
 * this.startService(new Intent(this, ${accelerometerServiceClass}.class))
 * }</pre>
 */
public class ${accelerometerServiceClass} extends Service {
    private static final String TAG = ${accelerometerServiceClass}.class.getSimpleName();
    private static final String I2C_BUS = "BUS NAME";

    private Mma7660FcAccelerometerDriver mAccelerometerDriver;

    @Override
    public void onCreate() {
        setupAccelerometer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyAccelerometer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    private void setupAccelerometer() {
        try {
            mAccelerometerDriver = new Mma7660FcAccelerometerDriver(I2C_BUS);
            mAccelerometerDriver.register();
            Log.i(TAG, "Accelerometer driver registered");
        } catch (IOException e) {
            Log.e(TAG, "Error initializing accelerometer driver: ", e);
        }
    }

    private void destroyAccelerometer() {
        if (mAccelerometerDriver != null) {
            mAccelerometerDriver.unregister();
            try {
                mAccelerometerDriver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing accelerometer driver: ", e);
            } finally {
                mAccelerometerDriver = null;
            }
        }
    }
}
