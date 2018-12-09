package ${escapeKotlinIdentifiers(packageName)}

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

import com.google.android.things.contrib.driver.bmx280.Bmx280SensorDriver

import java.io.IOException

private val TAG = ${temperaturePressureServiceClass}::class.java.simpleName
private val I2C_BUS = "BUS NAME"

/**
 * To use this service, start it from your component (like an activity):
 * <pre>{@code
 * this.startService(Intent(this, ${temperaturePressureServiceClass}::class.java))
 * }</pre>
 */
class ${temperaturePressureServiceClass} : Service() {
    private lateinit var mTemperatureSensorDriver: Bmx280SensorDriver

    override fun onCreate() {
        setupTemperaturePressureSensor()
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyTemperaturePressureSensor()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return Service.START_NOT_STICKY
    }

    private fun setupTemperaturePressureSensor() {
        try {
            mTemperatureSensorDriver = Bmx280SensorDriver(I2C_BUS)
            mTemperatureSensorDriver.registerTemperatureSensor()
        } catch (e: IOException) {
            Log.e(TAG, "Error configuring sensor", e)
        }

    }

    private fun destroyTemperaturePressureSensor() {
        mTemperatureSensorDriver.unregisterTemperatureSensor()
        try {
            mTemperatureSensorDriver.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing sensor", e)
        }
    }
}
