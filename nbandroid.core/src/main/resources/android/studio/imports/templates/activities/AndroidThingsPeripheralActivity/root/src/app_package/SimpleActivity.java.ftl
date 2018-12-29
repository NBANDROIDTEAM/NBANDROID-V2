package ${packageName};

import ${superClassFqcn};
import android.os.Bundle;
<#if integrateCapacitiveTouchButton>
import android.view.KeyEvent;
</#if>
<#if integrateButton || integrateCapacitiveTouchButton ||
    integrateAlphanumericDisplay || integrateNumericDisplay ||
    integrateOledDisplay || integrateLEDStrip || integrateServo ||
    integrateSpeakerBuzzer>
import java.io.IOException;
</#if>
<#if integrateButton || integrateCapacitiveTouchButton ||
    integrateAlphanumericDisplay || integrateNumericDisplay ||
    integrateOledDisplay || integrateLEDStrip || integrateAccelerometer ||
    integrateGps || integrateTemperaturePressureSensor || integrateServo ||
    integrateSpeakerBuzzer>
import android.util.Log;
</#if>
<#if integrateOledDisplay>
import com.google.android.things.contrib.driver.ssd1306.Ssd1306;
</#if>
<#if integrateAlphanumericDisplay>
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;
</#if>
<#if integrateNumericDisplay>
import com.google.android.things.contrib.driver.tm1637.NumericDisplay;
</#if>
<#if integrateLEDStrip>
import com.google.android.things.contrib.driver.apa102.Apa102;
import android.graphics.Color;
</#if>
<#if integrateButton>
import com.google.android.things.contrib.driver.button.Button;
</#if>
<#if integrateCapacitiveTouchButton>
import com.google.android.things.contrib.driver.cap12xx.Cap12xx;
import com.google.android.things.contrib.driver.cap12xx.Cap12xxInputDriver;
</#if>
<#if integrateAccelerometer || integrateTemperaturePressureSensor>
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.SensorManager.DynamicSensorCallback;
</#if>
<#if integrateAccelerometer || integrateTemperaturePressureSensor || integrateGps>
import android.content.Intent;
</#if>
<#if integrateGps>
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
</#if>
<#if integrateServo>
import com.google.android.things.contrib.driver.pwmservo.Servo;
</#if>
<#if integrateSpeakerBuzzer>
import com.google.android.things.contrib.driver.pwmspeaker.Speaker;
</#if>

public class ${activityClass} extends ${superClass} {
    private static final String TAG = ${activityClass}.class.getSimpleName();

<#if integrateButton>
    private static final String gpioButtonPinName = "BUS NAME";
    private Button mButton;

</#if>
<#if integrateCapacitiveTouchButton>
    private Cap12xxInputDriver mInputDriver;
</#if>
<#if integrateCapacitiveTouchButton || integrateAlphanumericDisplay || integrateOledDisplay>
    private static final String I2C_BUS = "BUS NAME";
</#if>
<#if integrateAlphanumericDisplay>
    private AlphanumericDisplay mSegmentDisplay;
</#if>
<#if integrateOledDisplay>
    private Ssd1306 mScreen;
</#if>
<#if integrateNumericDisplay>
    private static final String GPIO_FOR_DATA = "BUS NAME";
    private static final String GPIO_FOR_CLOCK = "BUS NAME";

    private NumericDisplay mNumericSegmentDisplay;

</#if>
<#if integrateLEDStrip>
    // LED configuration.
    private static final int NUM_LEDS = 7;
    private static final int LED_BRIGHTNESS = 5; // 0 ... 31
    private static final Apa102.Mode LED_MODE = Apa102.Mode.BGR;

    private Apa102 mLedstrip;
    private int[] mLedColors;
</#if>
<#if integrateAccelerometer || integrateTemperaturePressureSensor>
    private SensorManager mSensorManager;
    private DynamicSensorCallback mDynamicSensorCallback = new DynamicSensorCallback() {
        @Override
        public void onDynamicSensorConnected(Sensor sensor) {
<#if integrateAccelerometer>
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                Log.i(TAG, "Accelerometer sensor connected");
                mAccelerometerListener = new AccelerometerListener();
                mSensorManager.registerListener(mAccelerometerListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
</#if>
<#if integrateTemperaturePressureSensor>
            if (sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                Log.i(TAG, "Temperature sensor connected");
                mSensorEventListener = new TemperaturePressureEventListener();
                mSensorManager.registerListener(mSensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
</#if>
        }
    };
</#if>
<#if integrateAccelerometer>
private AccelerometerListener mAccelerometerListener;
</#if>
<#if integrateTemperaturePressureSensor>
    private TemperaturePressureEventListener mSensorEventListener;
</#if>
<#if integrateGps>
    private LocationManager mLocationManager;
    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.v(TAG, "Location update: " + location);
            Log.d(TAG, "Location " + location.getLatitude() + ", " + location.getLongitude() + " was read");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };
</#if>
<#if integrateServo || integrateSpeakerBuzzer>
    private static final String PWM_BUS = "BUS NAME";
</#if>
<#if integrateServo>
    private Servo mServo;
</#if>
<#if integrateSpeakerBuzzer>
    private Speaker mSpeaker;
</#if>

<#if integrateLEDStrip>
    private static final String SPI_BUS = "BUS NAME";
</#if>

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
<#if integrateButton>
        setupButton();
</#if>
<#if integrateCapacitiveTouchButton>
        setupCapacitiveTouchButtons();
</#if>
<#if integrateAlphanumericDisplay>
        setupAlphanumericDisplay();
</#if>
<#if integrateNumericDisplay>
        setupNumericDisplay();
</#if>
<#if integrateOledDisplay>
        setupOledDisplay();
</#if>
<#if integrateLEDStrip>
        setupLedStrip();
        float[] hsv = {1f, 1f, 1f};
        for (int i = 0; i < mLedColors.length; i++) { // Assigns gradient colors.
            hsv[0] = i * 360.f / mLedColors.length;
            mLedColors[i] = Color.HSVToColor(0, hsv);
        }
        try {
            mLedstrip.write(mLedColors);
        } catch (IOException e) {
            Log.e(TAG, "Error setting LED colors", e);
        }
</#if>
<#if integrateAccelerometer>
        startAccelerometerRequest();
</#if>
<#if integrateGps>
        startLocationRequest();
</#if>
<#if integrateTemperaturePressureSensor>
        startTemperaturePressureRequest();
</#if>
<#if integrateServo>
        setupServo();
        try {
            mServo.setAngle(30);
        } catch (IOException e) {
            Log.e(TAG, "Error setting the angle", e);
        }
</#if>
<#if integrateSpeakerBuzzer>
        setupSpeaker();
        try {
            mSpeaker.play(/* G4 */ 391.995);
        } catch (IOException e) {
            Log.e(TAG, "Error playing note", e);
        }
</#if>
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
<#if integrateButton>
        destroyButton();
</#if>
<#if integrateCapacitiveTouchButton>
        destroyCapacitiveTouchButtons();
</#if>
<#if integrateAlphanumericDisplay>
        destroyAlphanumericDisplay();
</#if>
<#if integrateNumericDisplay>
        destroyNumericDisplay();
</#if>
<#if integrateOledDisplay>
        destroyOledDisplay();
</#if>
<#if integrateLEDStrip>
        destroyLedStrip();
</#if>
<#if integrateAccelerometer>
        stopAccelerometerRequest();
</#if>
<#if integrateGps>
        stopLocationRequest();
</#if>
<#if integrateTemperaturePressureSensor>
        stopTemperaturePressureRequest();
</#if>
<#if integrateServo>
        destroyServo();
</#if>
<#if integrateSpeakerBuzzer>
        destroySpeaker();
</#if>
    }

<#if integrateButton>
    private void setupButton() {
        try {
            mButton = new Button(gpioButtonPinName,
                    // high signal indicates the button is pressed
                    // use with a pull-down resistor
                    Button.LogicState.PRESSED_WHEN_HIGH
            );
            mButton.setOnButtonEventListener(new Button.OnButtonEventListener() {
                @Override
                public void onButtonEvent(Button button, boolean pressed) {
                    // do something awesome
                }
            });
        } catch (IOException e) {
            // couldn't configure the button...
        }
    }

    private void destroyButton() {
        if (mButton != null) {
            Log.i(TAG, "Closing button");
            try {
                mButton.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing button", e);
            } finally {
                mButton = null;
            }
        }
    }

</#if>
<#if integrateCapacitiveTouchButton>
    private void setupCapacitiveTouchButtons() {
        // Set input key codes
        int[] keyCodes = {
                KeyEvent.KEYCODE_1,
                KeyEvent.KEYCODE_2,
                KeyEvent.KEYCODE_3,
                KeyEvent.KEYCODE_4,
                KeyEvent.KEYCODE_5,
                KeyEvent.KEYCODE_6,
                KeyEvent.KEYCODE_7,
                KeyEvent.KEYCODE_8
        };

        try {
            mInputDriver = new Cap12xxInputDriver(this,
                    I2C_BUS,
                    null,
                    Cap12xx.Configuration.CAP1208,
                    keyCodes);

            // Disable repeated events
            mInputDriver.setRepeatRate(Cap12xx.REPEAT_DISABLE);
            // Block touches above 4 unique inputs
            mInputDriver.setMultitouchInputMax(4);

            mInputDriver.register();

        } catch (IOException e) {
            Log.w(TAG, "Unable to open driver connection", e);
        }
    }

    private void destroyCapacitiveTouchButtons() {
        if (mInputDriver != null) {
            mInputDriver.unregister();

            try {
                mInputDriver.close();
            } catch (IOException e) {
                Log.w(TAG, "Unable to close touch driver", e);
            } finally {
                mInputDriver = null;
            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Handle key events from captouch inputs
        switch (keyCode) {
            case KeyEvent.KEYCODE_1:
            case KeyEvent.KEYCODE_2:
            case KeyEvent.KEYCODE_3:
            case KeyEvent.KEYCODE_4:
            case KeyEvent.KEYCODE_5:
            case KeyEvent.KEYCODE_6:
            case KeyEvent.KEYCODE_7:
            case KeyEvent.KEYCODE_8:
                Log.d(TAG, "Captouch key released: " + event.getKeyCode());
                return true;
            default:
                Log.d(TAG, "Unknown key released: " + keyCode);
                return super.onKeyUp(keyCode, event);
        }
    }

</#if>
<#if integrateAlphanumericDisplay>
    private void setupAlphanumericDisplay() {
        try {
            mSegmentDisplay = new AlphanumericDisplay(I2C_BUS);
            mSegmentDisplay.setBrightness(1.0f);
            mSegmentDisplay.setEnabled(true);
            mSegmentDisplay.clear();
            mSegmentDisplay.display("ABCD");
        } catch (IOException e) {
            Log.e(TAG, "Error configuring display", e);
        }
    }

    private void destroyAlphanumericDisplay() {
        if (mSegmentDisplay != null) {
            Log.i(TAG, "Closing display");
            try {
                mSegmentDisplay.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing display", e);
            } finally {
                mSegmentDisplay = null;
            }
        }
    }

</#if>
<#if integrateNumericDisplay>
    private void setupNumericDisplay() {
        Log.i(TAG, "Starting SegmentDisplayActivity");
        try {
            mNumericSegmentDisplay = new NumericDisplay(GPIO_FOR_DATA, GPIO_FOR_CLOCK);
            mNumericSegmentDisplay.setBrightness(1.0f);
            mNumericSegmentDisplay.setColonEnabled(true);
            mNumericSegmentDisplay.display("2342");
        } catch (IOException e) {
            Log.e(TAG, "Error configuring display", e);
        }
    }

    private void destroyNumericDisplay() {
        if (mNumericSegmentDisplay != null) {
            Log.i(TAG, "Closing display");
            try {
                mNumericSegmentDisplay.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing display", e);
            } finally {
                mNumericSegmentDisplay = null;
            }
        }
    }

</#if>
<#if integrateOledDisplay>
    private void setupOledDisplay() {
        try {
            mScreen = new Ssd1306(I2C_BUS);
        } catch (IOException e) {
            Log.e(TAG, "Error while opening screen", e);
        }
        Log.d(TAG, "OLED screen activity created");
    }

    private void destroyOledDisplay() {
        if (mScreen != null) {
            try {
                mScreen.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing SSD1306", e);
            } finally {
                mScreen = null;
            }
        }
    }

</#if>
<#if integrateLEDStrip>
    private void setupLedStrip() {
        mLedColors = new int[NUM_LEDS];
        try {
            Log.d(TAG, "Initializing LED strip");
            mLedstrip = new Apa102(SPI_BUS, LED_MODE);
            mLedstrip.setBrightness(LED_BRIGHTNESS);
        } catch (IOException e) {
            Log.e(TAG, "Error initializing LED strip", e);
        }
    }

    private void destroyLedStrip() {
        if (mLedstrip != null) {
            try {
                mLedstrip.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception closing LED strip", e);
            } finally {
                mLedstrip = null;
            }
         }
    }

</#if>
<#if integrateAccelerometer>
    private void startAccelerometerRequest() {
        this.startService(new Intent(this, ${accelerometerServiceClass}.class));
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.registerDynamicSensorCallback(mDynamicSensorCallback);
    }

    private void stopAccelerometerRequest() {
        this.stopService(new Intent(this, ${accelerometerServiceClass}.class));
        mSensorManager.unregisterListener(mAccelerometerListener);
    }

</#if>
<#if integrateGps>
    private void startLocationRequest() {
        this.startService(new Intent(this, ${gpsServiceClass}.class));

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // We need permission to get location updates
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // A problem occurred auto-granting the permission
            Log.d(TAG, "No permission");
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                0, 0, mLocationListener);
    }

    private void stopLocationRequest() {
        this.stopService(new Intent(this, ${gpsServiceClass}.class));
        mLocationManager.removeUpdates(mLocationListener);
    }
</#if>
<#if integrateTemperaturePressureSensor>
    private void startTemperaturePressureRequest() {
        this.startService(new Intent(this, ${temperaturePressureServiceClass}.class));
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.registerDynamicSensorCallback(mDynamicSensorCallback);
    }

    private void stopTemperaturePressureRequest() {
        this.stopService(new Intent(this, ${temperaturePressureServiceClass}.class));
        mSensorManager.unregisterDynamicSensorCallback(mDynamicSensorCallback);
        mSensorManager.unregisterListener(mSensorEventListener);
    }
</#if>
<#if integrateServo>
    private void setupServo() {
        try {
            mServo = new Servo(PWM_BUS);
            mServo.setAngleRange(0f, 180f);
            mServo.setEnabled(true);
        } catch (IOException e) {
            Log.e(TAG, "Error creating Servo", e);
        }
    }

    private void destroyServo() {
        if (mServo != null) {
            try {
                mServo.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing Servo");
            } finally {
                mServo = null;
            }
        }
    }
</#if>
<#if integrateSpeakerBuzzer>
    private void setupSpeaker() {
        try {
            mSpeaker = new Speaker(PWM_BUS);
            mSpeaker.stop(); // in case the PWM pin was enabled already
        } catch (IOException e) {
            Log.e(TAG, "Error initializing speaker");
        }
    }

    private void destroySpeaker() {
        if (mSpeaker != null) {
            try {
                mSpeaker.stop();
                mSpeaker.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing speaker", e);
            } finally {
                mSpeaker = null;
            }
        }
    }
</#if>
<#if integrateAccelerometer>
    private class AccelerometerListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            Log.i(TAG, "Accelerometer event: " +
                    event.values[0] + ", " + event.values[1] + ", " + event.values[2]);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.i(TAG, "Accelerometer accuracy changed: " + accuracy);
        }
    }
</#if>
<#if integrateTemperaturePressureSensor>
    private class TemperaturePressureEventListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            Log.i(TAG, "sensor changed: " + event.values[0]);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.i(TAG, "sensor accuracy changed: " + accuracy);
        }
    }
</#if>
}
