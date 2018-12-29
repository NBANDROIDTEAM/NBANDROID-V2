package ${escapeKotlinIdentifiers(packageName)}

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.android.things.contrib.driver.gps.NmeaGpsDriver

import java.io.IOException

private val TAG = ${gpsServiceClass}::class.java.simpleName
private val UART_BAUD = 9600
private val ACCURACY = 2.5f // From GPS datasheet
private val UART_BUS = ""

/**
 * To use this service, start it from your component (like an activity):
 * <pre>{@code
 * this.startService(Intent(this, ${gpsServiceClass}::class.java))
 * }</pre>
 */
class ${gpsServiceClass} : Service() {
    private lateinit var mGpsDriver: NmeaGpsDriver

    override fun onCreate() {
        setupGps()
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyGps()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return Service.START_NOT_STICKY
    }

    private fun setupGps() {
        try {
            // Register the GPS driver
            mGpsDriver = NmeaGpsDriver(this, UART_BUS, UART_BAUD, ACCURACY)
            mGpsDriver.register()
        } catch (e: IOException) {
            Log.w(TAG, "Unable to open GPS UART", e)
        }

    }

    private fun destroyGps() {
        // Unregister components
        mGpsDriver.unregister()

        try {
            mGpsDriver.close()
        } catch (e: IOException) {
            Log.w(TAG, "Unable to close GPS driver", e)
        }
    }
}
