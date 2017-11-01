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

package org.nbandroid.netbeans.gradle.launch;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.android.ddmlib.testrunner.ITestRunListener;
import com.android.ddmlib.testrunner.RemoteAndroidTestRunner;
import com.android.ddmlib.testrunner.TestIdentifier;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.nbandroid.netbeans.gradle.AndroidIO;
import org.nbandroid.netbeans.gradle.api.TestOutputConsumer;
import org.nbandroid.netbeans.gradle.config.AndroidProjectProperties;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;

/**
 * Launch strategy to run test(s).
 *
 * @author radim
 */
class TestLaunchAction implements LaunchAction {
  private static final Logger LOG = Logger.getLogger(TestLaunchAction.class.getName());

  @Override
  public boolean doLaunch(LaunchInfo launchInfo, final IDevice device, final Project project) {
    Preconditions.checkNotNull(project);
    String runnerClass = launchInfo.launchConfig.getInstrumentationRunner();
    final RemoteAndroidTestRunner runner = new RemoteAndroidTestRunner(
        launchInfo.manifestData.getPackage(),
        runnerClass != null ? runnerClass : AndroidProjectProperties.INSTR_RUNNER_DEFAULT, 
        device);

    if (!Strings.isNullOrEmpty(launchInfo.testClass)) {
//        if (mLaunchInfo.getTestMethod() != null) {
//            runner.setMethodName(mLaunchInfo.getTestClass(), mLaunchInfo.getTestMethod());
//        } else {
       runner.setClassName(launchInfo.testClass);
//        }
    }
//
//    if (mLaunchInfo.getTestPackage() != null) {
//        runner.setTestPackageName(mLaunchInfo.getTestPackage());
//    }
    runner.setDebug(launchInfo.debug);
    
    if (launchInfo.debug) {
      // TODO this is wrong
      // we need to start test runner asynchronously to be able to attach
      RequestProcessor.getDefault().post(new Runnable() {

        @Override
        public void run() {
          callRunner(runner, device, project);
        }
      });
      return true;
    }
    return callRunner(runner, device, project);
  }
  
  private boolean callRunner(RemoteAndroidTestRunner runner, IDevice device, Project project) {
    InputOutput io = AndroidIO.getDefaultIO();

    try {
      // now we actually launch the app.
      io.getOut().println("Starting tests on device " + device);
      runner.run(new TestRunListener(project));
      return true;

    } catch (TimeoutException ex) {
      io.getErr().println("Launch error: timeout");
      LOG.log(Level.INFO, null, ex);
    } catch (AdbCommandRejectedException ex) {
      io.getErr().println("Launch error: adb rejected command: " + ex.getMessage());
      LOG.log(Level.INFO, null, ex);
    } catch (ShellCommandUnresponsiveException ex) {
      io.getErr().println(MessageFormat.format("Unresponsive shell when executing tests: {0}",
          ex.getMessage()));
      LOG.log(Level.INFO, null, ex);
    } catch (IOException ex) {
      io.getErr().println("Test launch error: " + ex.getMessage());
      LOG.log(Level.INFO, null, ex);
    }
    return false;
  }

  private static class TestRunListener implements ITestRunListener {
    private Iterable<ITestRunListener> delegates;
    private final ITestRunListener simpleLsnr = new SimpleTestRunListener();

    public TestRunListener(final Project project) {
      Preconditions.checkNotNull(project);
      delegates = Lists.newArrayList(Iterables.concat(
               Collections.singleton(simpleLsnr), 
               Iterables.transform(
                   project.getLookup().lookupAll(TestOutputConsumer.class),
                   new Function<TestOutputConsumer, ITestRunListener>() {

                     @Override
                     public ITestRunListener apply(TestOutputConsumer input) {
                       return input.createTestListener(project);
                     }
                   })));
      LOG.log(Level.FINE, "Sending test output to {0}", Iterables.toString(delegates));
    }
    @Override
    public void testRunStarted(String string, int testCount) {
      for (ITestRunListener lsnr : delegates) {
        lsnr.testRunStarted(string, testCount);
      }
    }

    @Override
    public void testStarted(TestIdentifier ti) {
      for (ITestRunListener lsnr : delegates) {
        lsnr.testStarted(ti);
      }
    }

    @Override
    public void testFailed(TestIdentifier ti, String trace) {
      for (ITestRunListener lsnr : delegates) {
        lsnr.testFailed(ti, trace);
      }
    }

    @Override
    public void testIgnored(TestIdentifier ti) {
      for (ITestRunListener lsnr : delegates) {
        lsnr.testIgnored(ti);
      }
    }
    
    @Override
    public void testEnded(TestIdentifier ti, Map<String, String> map) {
      for (ITestRunListener lsnr : delegates) {
        lsnr.testEnded(ti, map);
      }
    }

    @Override
    public void testRunFailed(String string) {
      for (ITestRunListener lsnr : delegates) {
        lsnr.testRunFailed(string);
      }
    }

    @Override
    public void testRunStopped(long timeElapsed) {
      for (ITestRunListener lsnr : delegates) {
        lsnr.testRunStopped(timeElapsed);
      }
    }

    @Override
    public void testRunEnded(long timeElapsed, Map<String, String> map) {
      for (ITestRunListener lsnr : delegates) {
        lsnr.testRunEnded(timeElapsed, map);
      }
    }

    @Override
    public void testAssumptionFailure(TestIdentifier ti, String msg) {
      for (ITestRunListener lsnr : delegates) {
        lsnr.testAssumptionFailure(ti, msg);
      }
    }
  }

  private static class SimpleTestRunListener implements ITestRunListener {
    InputOutput io = AndroidIO.getDefaultIO();

    @Override
    public void testRunStarted(String string, int testCount) {
      io.getOut().println("testRunStarted " + string + ", " + testCount);
    }

    @Override
    public void testStarted(TestIdentifier ti) {
      io.getOut().println("testStarted " + ti);
    }

    @Override
    public void testFailed(TestIdentifier ti, String trace) {
      io.getErr().println("testFailed " + ti + ", " + trace);
    }
    
    @Override
    public void testIgnored(TestIdentifier ti) {
      io.getOut().println("testIgnored " + ti);
    }

    @Override
    public void testEnded(TestIdentifier ti, Map<String, String> map) {
      io.getOut().println("testEnded " + ti + ", " + map);
    }

    @Override
    public void testRunFailed(String string) {
      io.getErr().println("testRunFailed " + string);
    }

    @Override
    public void testRunStopped(long timeElapsed) {
      io.getErr().println("testRunStopped " + timeElapsed);
    }

    @Override
    public void testRunEnded(long timeElapsed, Map<String, String> map) {
      io.getOut().println("testRunEnded " + timeElapsed + ", " + map);
    }

    @Override
    public void testAssumptionFailure(TestIdentifier ti, String msg) {
      io.getOut().println("testAssumptionFailure " + ti + ", " + msg);
    }

  }
}
