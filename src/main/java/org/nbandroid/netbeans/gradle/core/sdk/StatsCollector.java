package org.nbandroid.netbeans.gradle.core.sdk;

import com.timgroup.statsd.NoOpStatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 *
 * @author radim
 */
public class StatsCollector {
  private static final String PREFERENCE_PREFIX = "statsd.";
  private static final Logger LOG = Logger.getLogger(StatsCollector.class.getName());
  private static final StatsCollector INSTANCE = new StatsCollector();
  private static final boolean DO_LOG = System.getProperty("test.all.android.sdks.home") == null;
  private static final long LOGGING_INTERVAL = Long.getLong("nbandroid.log.interval", 24L * 60L * 60L * 1000L);
  
  public static StatsCollector getDefault() {
    return INSTANCE;
  }
  
  private final RequestProcessor rp = new RequestProcessor("NBAndroid-StatsD", 1);
  private final StatsDClient statsd;
  private final RequestProcessor.Task statsTask;
  private final StatsRunnable statsRunnable;
  
  private boolean enabled = true;

  public StatsCollector() {
    statsRunnable = new StatsRunnable();
    statsTask = rp.create(statsRunnable);
    StatsDClient client;
    try {
      client = new NonBlockingStatsDClient("nbandroid", "stats.nbandroid.org", 8125);
    } catch (Exception ex) {
      LOG.log(Level.FINE, "will fallback to no-op statsd", ex);
      client = new NoOpStatsDClient();
    }
    statsd = client;
    Preferences statsPrefs = NbPreferences.forModule(StatsCollector.class);
    enabled = statsPrefs.getBoolean("stats.enabled", true);
    LOG.log(Level.FINE, "enabled = {0}", enabled);
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    Preferences statsPrefs = NbPreferences.forModule(StatsCollector.class);
    statsPrefs.putBoolean("stats.enabled", enabled);
    try {
      statsPrefs.flush();
    } catch (BackingStoreException ex) {
      LOG.log(Level.FINE, null, ex);
    }
  }
  
  public void incrementCounter(final String counterName) {
    if (!DO_LOG || !isEnabled()) {
      return;
    }
    statsRunnable.push(counterName);
    rp.post(statsTask, 60 * 1000);
  }
  
  private class StatsRunnable implements Runnable {
    private final Queue<String> counters = new ConcurrentLinkedQueue<>();
    private final Preferences statToLogTimeMillis = NbPreferences.forModule(StatsCollector.class);
    
    public void push(String counterName) {
      counters.add(counterName);
    }

    @Override
    public void run() {
      boolean changed = false;
      for (String counterName = counters.poll(); counterName != null; counterName = counters.poll()) {
        String prefName = PREFERENCE_PREFIX + counterName;
        long lastLogTime = statToLogTimeMillis.getLong(prefName, -1);
        long currentTime = System.currentTimeMillis();
        LOG.log(Level.FINER, "check logging for statsd {0} {1} {2}", 
            new Object[] {counterName, lastLogTime, currentTime});
        if (lastLogTime == -1) {
          statToLogTimeMillis.putLong(prefName, currentTime);
          changed = true;
        } else if (lastLogTime + LOGGING_INTERVAL < currentTime) {
          log(counterName);
          statToLogTimeMillis.putLong(prefName, currentTime);
          changed = true;
        }
      }
      if (changed) {
        try {
          statToLogTimeMillis.flush();
        } catch (BackingStoreException ex) {
          LOG.log(Level.FINE, null, ex);
        }
      }
    }
    
    private void log(String counterName) {
      LOG.log(Level.FINE, "logging to statsd {0}", counterName);
      statsd.increment(counterName);
    }
  }
}
