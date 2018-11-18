package ${escapeKotlinIdentifiers(packageName)}

import ${superClassFqcn}
import android.os.Bundle
<#if integrateCapacitiveTouchButton>
import android.view.KeyEvent
</#if>
<#if integrateButton || integrateCapacitiveTouchButton ||
    integrateAlphanumericDisplay || integrateNumericDisplay ||
    integrateOledDisplay || integrateLEDStrip || integrateServo ||
    integrateSpeakerBuzzer>
import java.io.IOException
</#if>
<#if integrateButton || integrateCapacitiveTouchButton ||
    integrateAlphanumericDisplay || integrateNumericDisplay ||
    integrateOledDisplay || integrateLEDStrip || integrateAccelerometer ||
    integrateGps || integrateTemperaturePressureSensor || integrateServo ||
    integrateSpeakerBuzzer>
import android.util.Log
</#if>
<#if integrateOledDisplay>
import com.google.android.things.contrib.driver.ssd1306.Ssd1306
</#if>
<#if integrateAlphanumericDisplay>
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay
</#if>
<#if integrateNumericDisplay>
import com.google.android.things.contrib.driver.tm1637.NumericDisplay
</#if>
<#if integrateLEDStrip>
import com.google.android.things.contrib.driver.apa102.Apa102

import android.graphics.Color
</#if>
<#if integrateButton>
import com.google.android.things.contrib.driver.button.Button
</#if>
<#if integrateCapacitiveTouchButton>
import com.google.android.things.contrib.driver.cap12xx.Cap12xx
import com.google.android.things.contrib.driver.cap12xx.Cap12xxInputDriver
</#if>
<#if integrateAccelerometer || integrateTemperaturePressureSensor>
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.DynamicSensorCallback
</#if>
<#if integrateAccelerometer || integrateTemperaturePressureSensor || integrateGps>
import android.content.Intent
</#if>
<#if integrateGps>
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
</#if>
<#if integrateServo>
import com.google.android.things.contrib.driver.pwmservo.Servo
</#if>
<#if integrateSpeakerBuzzer>
import com.google.android.things.contrib.driver.pwmspeaker.Speaker
</#if>

private val TAG = ${activityClass}::class.java.simpleName
<#if integrateButton>
private val gpioButtonPinName = "BUS NAME"
</#if>
<#if integrateCapacitiveTouchButton || integrateAlphanumericDisplay || integrateOledDisplay>
private val I2C_BUS = "BUS NAME"
</#if>
<#if integrateNumericDisplay>
private val GPIO_FOR_DATA = "BUS NAME"
private val GPIO_FOR_CLOCK = "BUS NAME"
</#if>
<#if integrateLEDStrip>
// LED configuration.
private val NUM_LEDS = 7
private val LED_BRIGHTNESS = 5 // 0 ... 31
private val LED_MODE = Apa102.Mode.BGR
</#if>
<#if integrateServo || integrateSpeakerBuzzer>
private val PWM_BUS = "BUS NAME"
</#if>
<#if integrateLEDStrip>
private val SPI_BUS = "BUS NAME"
</#if>

class ${activityClass} : ${superClass}() {
<#if integrateButton>
    private lateinit var mButton: Button
</#if>
<#if integrateCapacitiveTouchButton>
    private lateinit var mInputDriver: Cap12xxInputDriver
</#if>
<#if integrateAlphanumericDisplay>
    private lateinit var mSegmentDisplay: AlphanumericDisplay
</#if>
<#if integrateOledDisplay>
    private lateinit var mScreen: Ssd1306
</#if>
<#if integrateNumericDisplay>
    private lateinit var mNumericSegmentDisplay: NumericDisplay
</#if>
<#if integrateLEDStrip>
    private lateinit var mLedstrip: Apa102
    private lateinit var mLedColors: IntArray
</#if>
<#if integrateAccelerometer || integrateTemperaturePressureSensor>
    private lateinit var mSensorManager: SensorManager
    private val mDynamicSensorCallback = object : DynamicSensorCallback() {
        override fun onDynamicSensorConnected(sensor: Sensor) {
<#if integrateAccelerometer>
            if (sensor.type == Sensor.TYPE_ACCELEROMETER) {
                Log.i(TAG, "Accelerometer sensor connected")
                mAccelerometerListener = AccelerometerListener()
                mSensorManager.registerListener(mAccelerometerListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
</#if>
<#if integrateTemperaturePressureSensor>
            if (sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                Log.i(TAG, "Temperature sensor connected")
                mSensorEventListener = TemperaturePressureEventListener()
                mSensorManager.registerListener(mSensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
</#if>
        }
    }
</#if>
<#if integrateAccelerometer>
    private lateinit var mAccelerometerListener: AccelerometerListener
</#if>
<#if integrateTemperaturePressureSensor>
    private lateinit var mSensorEventListener: TemperaturePressureEventListener
</#if>
<#if integrateGps>
    private lateinit var mLocationManager: LocationManager
    private val mLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.v(TAG, "Location update: " + location)
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}
    }
</#if>
<#if integrateServo>
    private lateinit var mServo: Servo
</#if>
<#if integrateSpeakerBuzzer>
    private lateinit var mSpeaker: Speaker
</#if>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
<#if integrateButton>
        setupButton()
</#if>
<#if integrateCapacitiveTouchButton>
        setupCapacitiveTouchButtons()
</#if>
<#if integrateAlphanumericDisplay>
        setupAlphanumericDisplay()
</#if>
<#if integrateNumericDisplay>
        setupNumericDisplay()
</#if>
<#if integrateOledDisplay>
        setupOledDisplay()
</#if>
<#if integrateLEDStrip>
        setupLedStrip()
        val hsv = floatArrayOf(1f, 1f, 1f)
        for (i in mLedColors.indices) { // Assigns gradient colors.
            hsv[0] = i * 360f / mLedColors.size
            mLedColors[i] = Color.HSVToColor(0, hsv)
        }
        try {
            mLedstrip.write(mLedColors)
        } catch (e: IOException) {
            Log.e(TAG, "Error setting LED colors", e)
        }
</#if>
<#if integrateAccelerometer>
        startAccelerometerRequest()
</#if>
<#if integrateGps>
        startLocationRequest()
</#if>
<#if integrateTemperaturePressureSensor>
        startTemperaturePressureRequest()
</#if>
<#if integrateServo>
        setupServo()
        try {
            mServo.setAngle(30.0)
        } catch (e: IOException) {
            Log.e(TAG, "Error setting the angle", e)
        }
</#if>
<#if integrateSpeakerBuzzer>
        setupSpeaker()
        try {
            mSpeaker.play(/* G4 */391.995)
        } catch (e: IOException) {
            Log.e(TAG, "Error playing note", e)
        }
</#if>
    }

    override fun onDestroy() {
        super.onDestroy()
<#if integrateButton>
        destroyButton()
</#if>
<#if integrateCapacitiveTouchButton>
        destroyCapacitiveTouchButtons()
</#if>
<#if integrateAlphanumericDisplay>
        destroyAlphanumericDisplay()
</#if>
<#if integrateNumericDisplay>
        destroyNumericDisplay()
</#if>
<#if integrateOledDisplay>
        destroyOledDisplay()
</#if>
<#if integrateLEDStrip>
        destroyLedStrip()
</#if>
<#if integrateAccelerometer>
        stopAccelerometerRequest()
</#if>
<#if integrateGps>
        stopLocationRequest()
</#if>
<#if integrateTemperaturePressureSensor>
        stopTemperaturePressureRequest()
</#if>
<#if integrateServo>
        destroyServo()
</#if>
<#if integrateSpeakerBuzzer>
        destroySpeaker()
</#if>
    }

<#if integrateButton>
    private fun setupButton() {
        try {
            mButton = Button(gpioButtonPinName,
                    // high signal indicates the button is pressed
                    // use with a pull-down resistor
                    Button.LogicState.PRESSED_WHEN_HIGH
            )
            mButton.setOnButtonEventListener(object : Button.OnButtonEventListener {
                override fun onButtonEvent(button: Button, pressed: Boolean) {
                    // do something awesome
                }
            })
        } catch (e: IOException) {
            // couldn't configure the button...
        }

    }

    private fun destroyButton() {
        Log.i(TAG, "Closing button")
        try {
            mButton.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing button", e)
        }
    }

</#if>
<#if integrateCapacitiveTouchButton>
    private fun setupCapacitiveTouchButtons() {
        // Set input key codes
        val keyCodes = intArrayOf(KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_5, KeyEvent.KEYCODE_6, KeyEvent.KEYCODE_7, KeyEvent.KEYCODE_8)

        try {
            mInputDriver = Cap12xxInputDriver(this,
                    I2C_BUS, null,
                    Cap12xx.Configuration.CAP1208,
                    keyCodes)

            // Disable repeated events
            mInputDriver.setRepeatRate(Cap12xx.REPEAT_DISABLE)
            // Block touches above 4 unique inputs
            mInputDriver.setMultitouchInputMax(4)

            mInputDriver.register()

        } catch (e: IOException) {
            Log.w(TAG, "Unable to open driver connection", e)
        }

    }

    private fun destroyCapacitiveTouchButtons() {
        mInputDriver.unregister()

        try {
            mInputDriver.close()
        } catch (e: IOException) {
            Log.w(TAG, "Unable to close touch driver", e)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        // Handle key events from captouch inputs
        when (keyCode) {
            KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_5, KeyEvent.KEYCODE_6, KeyEvent.KEYCODE_7, KeyEvent.KEYCODE_8 -> {
                Log.d(TAG, "Captouch key released: " + event.keyCode)
                return true
            }
            else -> {
                Log.d(TAG, "Unknown key released: " + keyCode)
                return super.onKeyUp(keyCode, event)
            }
        }
    }

</#if>
<#if integrateAlphanumericDisplay>
    private fun setupAlphanumericDisplay() {
        try {
            mSegmentDisplay = AlphanumericDisplay(I2C_BUS)
            mSegmentDisplay.setBrightness(1.0f)
            mSegmentDisplay.setEnabled(true)
            mSegmentDisplay.clear()
            mSegmentDisplay.display("ABCD")
        } catch (e: IOException) {
            Log.e(TAG, "Error configuring display", e)
        }

    }

    private fun destroyAlphanumericDisplay() {
        Log.i(TAG, "Closing display")
        try {
            mSegmentDisplay.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing display", e)
        }
    }

</#if>
<#if integrateNumericDisplay>
    private fun setupNumericDisplay() {
        Log.i(TAG, "Starting SegmentDisplayActivity")
        try {
            mNumericSegmentDisplay = NumericDisplay(GPIO_FOR_DATA, GPIO_FOR_CLOCK)
            mNumericSegmentDisplay.setBrightness(1.0f)
            mNumericSegmentDisplay.setColonEnabled(true)
            mNumericSegmentDisplay.display("2342")
        } catch (e: IOException) {
            Log.e(TAG, "Error configuring display", e)
        }

    }

    private fun destroyNumericDisplay() {
        Log.i(TAG, "Closing display")
        try {
            mNumericSegmentDisplay.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing display", e)
        }
    }

</#if>
<#if integrateOledDisplay>
    private fun setupOledDisplay() {
        try {
            mScreen = Ssd1306(I2C_BUS)
        } catch (e: IOException) {
            Log.e(TAG, "Error while opening screen", e)
        }

        Log.d(TAG, "OLED screen activity created")
    }

    private fun destroyOledDisplay() {
        try {
            mScreen.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing SSD1306", e)
        }
    }

</#if>
<#if integrateLEDStrip>
    private fun setupLedStrip() {
        mLedColors = IntArray(NUM_LEDS)
        try {
            Log.d(TAG, "Initializing LED strip")
            mLedstrip = Apa102(SPI_BUS, LED_MODE)
            mLedstrip.setBrightness(LED_BRIGHTNESS)
        } catch (e: IOException) {
            Log.e(TAG, "Error initializing LED strip", e)
        }

    }

    private fun destroyLedStrip() {
        try {
            mLedstrip.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception closing LED strip", e)
        }
    }

</#if>
<#if integrateAccelerometer>
    private fun startAccelerometerRequest() {
        this.startService(Intent(this, ${accelerometerServiceClass}::class.java))
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mSensorManager.registerDynamicSensorCallback(mDynamicSensorCallback)
    }

    private fun stopAccelerometerRequest() {
        this.stopService(Intent(this, ${accelerometerServiceClass}::class.java))
        mSensorManager.unregisterListener(mAccelerometerListener)
    }

</#if>
<#if integrateGps>
    private fun startLocationRequest() {
        this.startService(Intent(this, ${gpsServiceClass}::class.java))

        mLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        // We need permission to get location updates
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // A problem occurred auto-granting the permission
            Log.d(TAG, "No permission")
            return
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                0, 0f, mLocationListener)
    }

    private fun stopLocationRequest() {
        this.stopService(Intent(this, ${gpsServiceClass}::class.java))
        mLocationManager.removeUpdates(mLocationListener)
    }

</#if>
<#if integrateTemperaturePressureSensor>
    private fun startTemperaturePressureRequest() {
        this.startService(Intent(this, ${temperaturePressureServiceClass}::class.java))
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mSensorManager.registerDynamicSensorCallback(mDynamicSensorCallback)
    }

    private fun stopTemperaturePressureRequest() {
        this.stopService(Intent(this, ${temperaturePressureServiceClass}::class.java))
        mSensorManager.unregisterDynamicSensorCallback(mDynamicSensorCallback)
        mSensorManager.unregisterListener(mSensorEventListener)
    }

</#if>
<#if integrateServo>
    private fun setupServo() {
        try {
            mServo = Servo(PWM_BUS)
            mServo.setAngleRange(0.0, 180.0)
            mServo.setEnabled(true)
        } catch (e: IOException) {
            Log.e(TAG, "Error creating Servo", e)
        }

    }

    private fun destroyServo() {
        try {
            mServo.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing Servo")
        }
    }

</#if>
<#if integrateSpeakerBuzzer>
    private fun setupSpeaker() {
        try {
            mSpeaker = Speaker(PWM_BUS)
            mSpeaker.stop() // in case the PWM pin was enabled already
        } catch (e: IOException) {
            Log.e(TAG, "Error initializing speaker")
        }

    }

    private fun destroySpeaker() {
        try {
            mSpeaker.stop()
            mSpeaker.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing speaker", e)
        }
    }

</#if>
<#if integrateAccelerometer>
    private inner class AccelerometerListener : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            Log.i(TAG, "Accelerometer event: " +
                    event.values[0] + ", " + event.values[1] + ", " + event.values[2])
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            Log.i(TAG, "Accelerometer accuracy changed: " + accuracy)
        }
    }

</#if>
<#if integrateTemperaturePressureSensor>
    private inner class TemperaturePressureEventListener : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            Log.i(TAG, "sensor changed: " + event.values[0])
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            Log.i(TAG, "sensor accuracy changed: " + accuracy)
        }
    }

</#if>
}
