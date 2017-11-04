/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nbandroid.netbeans.gradle.logcat;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.Client;
import com.android.ddmlib.ClientData;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.Log.LogLevel;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.google.common.collect.ImmutableSet;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.nbandroid.netbeans.gradle.core.ddm.AndroidDebugBridgeFactory;
import org.openide.util.RequestProcessor;

/**
 *
 * @author NYEREL
 */
public class LogReader {

    public final static String PROPERTY_DEVICE_LIST = "DEVICE_LIST";
    public final static String PROPERTY_CURRENT_DEVICE = "CURRENT_DEVICE";
    public final static String PROPERTY_CURRENT_DEVICE_STATE = "CURRENT_DEVICE_STATE";

    private static final Logger LOG = Logger.getLogger(LogReader.class.getName());
    // These messages will not be added to log as events
    private static final Set<String> ignoredLines = ImmutableSet.of(
            "--------- beginning of /dev/log/main",
            "--------- beginning of /dev/log/system");

    public static enum CurrentDeviceState {
        ATTACHED_AND_LOGGING,
        ATTACHED,
        DETACHED,
        UNKNOWN,
    }

    private Set<LogListener> listeners;
    private PropertyChangeSupport changeSupport;
    private IDevice currentDevice;
    private String requestedDeviceSerial;
    private LogCatOutputReceiver receiver;
    private LogEventInfo lastLogEventInfo;
    private static final Pattern sLogPattern = Pattern.compile(
            "^\\[\\s\\d\\d-\\d\\d\\s(\\d\\d:\\d\\d:\\d\\d\\.\\d+)"
            + "\\s+(\\d*):((?:0x[0-9a-fA-F]+)|(?:\\s*\\d+))\\s([VDIWE])/(.*)\\]$");
    private final AndroidDebugBridge adb;
    private boolean shouldBeReading = false;
    private volatile boolean reading = true;
    private Timer checkReadingStatusTimer;
    private int checkingPeriod = 10000;

    private Map<String, Map<Integer, String[]>> processNameCache = new HashMap<>();

    private Map<String, Collection<LogEvent>> logEventCache = new HashMap<>();

    public LogReader() {

        changeSupport = new PropertyChangeSupport(this);
        listeners = new HashSet<>();

        adb = AndroidDebugBridgeFactory.getDefault();
        checkReadingStatusTimer = new Timer();
        checkReadingStatusTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    if (!shouldBeReading) {
                        return;
                    }
                    if (!deviceReallyConnected()) {
                        infoMessage("Trying to reconnect to the device in " + checkingPeriod / 1000 + " seconds.");
                        startReading();
                    }
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Unexpected exception on reconnecting the device.", e);
                }
            }
        }, checkingPeriod, checkingPeriod);
    }

    public Set<String> getLoggedDevices() {
        return logEventCache.keySet();
    }

    public Collection<LogEvent> getLogEventsForDevice(String device) {
        return logEventCache.get(device);
    }

    public String getCurrentDevice() {
        if (requestedDeviceSerial != null) {
            return requestedDeviceSerial;
        }

        if (currentDevice != null) {
            return currentDevice.getSerialNumber();
        }

        return null;
    }

    public void setCurrentDevice(String device) {
        if (device != null && !device.equals(requestedDeviceSerial)) {
            // set new device
            this.requestedDeviceSerial = device;

            if (receiver == null) {
                startReading();
            } else {
                // stop reading on current device,
                // reading will be restarted 
                stopReading();
            }
        }
    }

    public CurrentDeviceState getCurrentDeviceState() {
        // device is currently unavailable
        if (currentDevice == null) {
            return CurrentDeviceState.DETACHED;
        }

        // is the "current device" the one we have requested?
        if (currentDevice.getSerialNumber().equals(requestedDeviceSerial) == false) {
            return CurrentDeviceState.DETACHED;
        }

        // device is offline
        if (!deviceReallyConnected()) {
            return CurrentDeviceState.DETACHED;
        }

        // is currently receiving events?
        if (receiver == null || !reading) {
            return CurrentDeviceState.ATTACHED;
        }

        // device is ready!
        return CurrentDeviceState.ATTACHED_AND_LOGGING;
    }

    private boolean deviceReallyConnected() {
        if (adb == null || !adb.isConnected() || !reading || currentDevice == null
                || currentDevice.isOffline() || !currentDevice.isOnline() || !isReading()) {
            return false;
        }
        boolean gotIt = false;
        LOG.log(Level.INFO, "searching for device with sn: {0}", currentDevice.getSerialNumber());
        for (IDevice d : adb.getDevices()) {
            LOG.log(Level.INFO, "device: {0}", d.getSerialNumber());
            if (d.getSerialNumber().equals(currentDevice.getSerialNumber())) {
                gotIt = true;
            }
        }
        if (!gotIt) {
            LOG.info("wasnt found in adb.getDevices");
            return false;
        }
        return true;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.changeSupport.removePropertyChangeListener(listener);
    }

    private void firePropertyChange(String property, Object oldVal, Object newVal) {
        this.changeSupport.firePropertyChange(property, oldVal, newVal);
    }

    public void addLogListener(LogListener listener) {
        listeners.add(listener);
        if (adb == null) {
            errorMessage("Error - failed to initialize connection with adb");
        }
    }

    public void removeAllLogListeners() {
        listeners.clear();
    }

    public void removeLogListener(LogListener listener) {
        listeners.remove(listener);
    }

    private void sendNewLogEvent(LogEvent logEvent) {
        for (LogListener listener : listeners) {
            listener.newLogEvent(logEvent);
        }
    }

    private void errorMessage(String message) {
        LogEventInfo i = new LogEventInfo(0, new String[]{""}, "", "", LogLevel.ERROR);
        LogEvent e = new LogEvent(i, message);
        sendNewLogEvent(e);
    }

    private void infoMessage(String message) {
        LogEventInfo i = new LogEventInfo(0, new String[]{""}, "", "", LogLevel.INFO);
        LogEvent e = new LogEvent(i, message);
        sendNewLogEvent(e);
    }

    private final class LogCatOutputReceiver extends MultiLineReceiver {

        private final IDevice loggedDevice;
        public boolean isCancelled = false;

        public LogCatOutputReceiver(IDevice device) {
            super();
            setTrimLine(false);
            loggedDevice = device;
        }

        @Override
        public void processNewLines(String[] lines) {
            if (isCancelled == false) {
                processLogLines(loggedDevice, lines);
            }
        }

        @Override
        public boolean isCancelled() {
            return isCancelled;
        }
    }

    public void startReading() {
        shouldBeReading = true;
        if (adb == null) {
            return;
        }
        if (!adb.isConnected()) {
            RequestProcessor.getDefault().post(new Runnable() {

                @Override
                public void run() {
                    adb.restart();
                    reallyStartReading();
                }

            });
        } else {
            reallyStartReading();
        }
    }

    private void reallyStartReading() {
        String lastDeviceSerial = currentDevice != null ? currentDevice.getSerialNumber() : "";
        IDevice[] devs = adb.getDevices();

        // clear the current device, because the device may have gone
        currentDevice = null;

        if (requestedDeviceSerial == null) {
            currentDevice = null;

            // if no device was requested, select the first available
            if (devs != null && devs.length > 0) {
                requestedDeviceSerial = devs[0].getSerialNumber();
            } else {
                // previous device has gone?
                if (lastDeviceSerial.isEmpty() == false) {
                    changeSupport.firePropertyChange(PROPERTY_CURRENT_DEVICE, lastDeviceSerial, "");
                }

                // no devices available - announce the current state and stop here
                changeSupport.firePropertyChange(PROPERTY_CURRENT_DEVICE_STATE, null, getCurrentDeviceState());

                return;
            }
        }

        // always select current device by requested serial,
        // because the device object may have changed when was disconnected
        if (devs != null) {
            for (IDevice dev : devs) {
                if (dev.getSerialNumber().equals(requestedDeviceSerial)) {
                    currentDevice = dev;
                    break;
                }
            }
        }

        // get the serial of the current device (or empty string, if none connected)
        String currentDeviceSerial = currentDevice != null ? currentDevice.getSerialNumber() : "";

        // notify all clients, if the selected device has changed
        if (!lastDeviceSerial.equals(currentDeviceSerial)) {
            changeSupport.firePropertyChange(PROPERTY_CURRENT_DEVICE, lastDeviceSerial, currentDeviceSerial);
        }

        if (currentDevice != null && !currentDevice.isOffline()) {
            stopReading();
            shouldBeReading = true;
            receiver = new LogCatOutputReceiver(currentDevice);
            RequestProcessor.getDefault().post(new Runnable() {

                @Override
                public void run() {
                    reading = true;

                    // announce the new device state
                    changeSupport.firePropertyChange(PROPERTY_CURRENT_DEVICE_STATE, null, getCurrentDeviceState());

                    IDevice currDevice = currentDevice;
                    if (currDevice == null) {
                        return;
                    }
                    try {
                        currDevice.executeShellCommand("logcat -v long", receiver);
                    } catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
                        LOG.log(Level.FINE, null, e);
                        reading = false;
                    } finally {
                        receiver = null;

                        // announce the new device state
                        changeSupport.firePropertyChange(PROPERTY_CURRENT_DEVICE_STATE, null, getCurrentDeviceState());

                        // when the device has changed, we restart the logging process
                        String serial = currDevice.getSerialNumber();
                        if (!serial.equals(requestedDeviceSerial)) {
                            startReading();
                        }
                    }
                }
            });
        }
    }

    public void stopReading() {
        if (receiver != null) {
            receiver.isCancelled = true;
        }

        shouldBeReading = false;
    }

    public boolean isReading() {
        if (receiver != null) {
            return !receiver.isCancelled();
        }
        return false;
    }

    LogEventInfo parseLine(IDevice device, String line) {
        LOG.log(Level.FINER, line);
        Matcher matcher = sLogPattern.matcher(line);

        if (matcher.matches()) {
            String time = matcher.group(1);
            int pid = Integer.valueOf(matcher.group(2));
            String[] process = getProcessName(device, pid);
            LogLevel level = LogLevel.getByLetterString(matcher.group(4));
            String tag = matcher.group(5).trim();
            return new LogEventInfo(pid, process, time, tag, level);
        }
        return null;
    }

    private void processLogLines(IDevice device, String[] lines) {
        for (String line : lines) {
            // ignore empty lines.
            if (line.length() > 0) {
                String time;
                int pid;
                String[] process;
                LogLevel level;
                String tag;
                LogEventInfo lei = parseLine(device, line);
                if (lei != null) {
                    // this is a header line, parse the header and keep it around.
                    lastLogEventInfo = lei;
                } else {
                    if (lastLogEventInfo == null) {
                        // The first line of output wasn't preceded
                        // by a header line; make something up so
                        // that users of mc.data don't NPE.
                        time = "??-?? ??:??:??.???"; //$NON-NLS1$
                        pid = 0;
                        process = new String[]{""};
                        level = LogLevel.INFO;
                        tag = "<unknown>"; //$NON-NLS1$
                        lastLogEventInfo = new LogEventInfo(pid, process, time, tag, level);
                    }
                    // tabs seem to display as only 1 tab so we replace the leading tabs
                    // by 4 spaces.
                    String message = line.replaceAll("\t", "    "); //$NON-NLS-1$ //$NON-NLS-2$
                    if (ignoredLines.contains(message)) {
                        continue;
                    }

                    LogEvent event = new LogEvent(lastLogEventInfo, message);

                    Collection<LogEvent> loggedEvents = logEventCache.get(device.getSerialNumber());
                    if (loggedEvents == null) {
                        loggedEvents = new LinkedHashSet<>();
                        logEventCache.put(device.getSerialNumber(), loggedEvents);

                        // notify listeners for the new device
                        firePropertyChange(PROPERTY_DEVICE_LIST, null, getLoggedDevices());
                    }

                    if (!loggedEvents.contains(event)) {
                        loggedEvents.add(event);
                        sendNewLogEvent(event);
                    }
                }
            }
        }
    }

    /**
     * Get a reference to the name of the process with the given ID. The
     * reference may contain a null-object, couldn't be retrieved, but may be
     * available later.
     *
     * @param device Device, where the process runs.
     * @param pid ID of the process.
     * @return A reference to a string containing the process name or
     * {@code null}, if the process couldn't be retrieved yet.
     */
    private String[] getProcessName(IDevice device, int pid) {
        Map<Integer, String[]> cache = processNameCache.get(device.getSerialNumber());
        if (cache == null) {
            cache = new HashMap<>();
            processNameCache.put(device.getSerialNumber(), cache);
        }

        String[] nameref = cache.get(pid);
        if (nameref == null) {
            nameref = new String[1];
            cache.put(pid, nameref);
        }

        if (nameref[0] == null) {
            for (Client client : device.getClients()) {
                ClientData data = client.getClientData();

                if (data.getPid() == pid) {
                    nameref[0] = data.getClientDescription();
                }
            }
        }

        return nameref;
    }
}
