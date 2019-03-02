package org.nbandroid.netbeans.gradle.config;

import com.android.builder.model.AndroidProject;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.android.spi.AndroidModelAware;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.util.ChangeSupport;
import org.openide.util.RequestProcessor;

// TODO extract common class to share with BuildVariant
/**
 *
 * @author radim
 */
@Deprecated
public class AndroidTestRunConfiguration implements AndroidModelAware {

    private static final Logger LOG = Logger.getLogger(AndroidTestRunConfiguration.class.getName());

    private static final String PREFERENCE_TEST_RUNNER = "androidTestRunner";

    @VisibleForTesting
    public static final RequestProcessor RP = new RequestProcessor("gradle-test-runner", 1);

    private final ChangeSupport cs = new ChangeSupport(this);
    private final Object lock = new Object();
    private final AuxiliaryProperties auxProps;
    private String testRunner;
    private String defaultTestRunner;

    public AndroidTestRunConfiguration(AuxiliaryProperties auxProps) {
        this.auxProps = Preconditions.checkNotNull(auxProps);
        testRunner = auxProps.get(PREFERENCE_TEST_RUNNER, false);
        LOG.log(Level.FINE, "loaded test runner {0}", testRunner);
    }

    @Nullable
    public String getTestRunner() {
        synchronized (lock) {
            if (testRunner == null) {
                testRunner = auxProps.get(PREFERENCE_TEST_RUNNER, false);
                LOG.log(Level.FINE, "re-loaded test runner {0}", testRunner);
            }
            return testRunner != null ? testRunner : defaultTestRunner;
        }
    }

    public void setTestRunner(final String testRunner) {
        synchronized (lock) {
            this.testRunner = testRunner;
            auxProps.put(PREFERENCE_TEST_RUNNER, testRunner, false);
            LOG.log(Level.FINE, "saved test runner {0}", testRunner);
        }
        fireChange();
    }

    private void setDefaultTestRunner(final String testRunner) {
        synchronized (lock) {
            this.defaultTestRunner = testRunner;
        }
        fireChange();
    }

    private void fireChange() {
        RP.post(new Runnable() {

            @Override
            public void run() {
                cs.fireChange();
            }
        });
    }

    public void addChangeListener(ChangeListener listener) {
        cs.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
    }

    @Override
    public void setAndroidProject(AndroidProject aPrj) {
        if (aPrj == null) {
            return;
        }
        String runner = aPrj.getDefaultConfig().getProductFlavor().getTestInstrumentationRunner();
        if (runner == null) {
            return;
        }
        setDefaultTestRunner(runner);
    }
}
