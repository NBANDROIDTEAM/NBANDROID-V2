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

package org.netbeans.modules.android.testrunner;

import com.android.ddmlib.testrunner.ITestRunListener;
import com.android.ddmlib.testrunner.TestIdentifier;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.CoreManager;
import org.netbeans.modules.gsf.testrunner.api.Status;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.openide.util.Lookup;
/**
 * Test run listener that send output to GSF testrunner.
 */
class TestRunListener implements ITestRunListener {
  private static final CoreManager testManager = Lookup.getDefault().lookup(CoreManager.class);

  private static final Logger LOG = Logger.getLogger(TestRunListener.class.getName());

  private final Project project;
  private TestSession testSession;

  public TestRunListener(Project project) {
    this.project = Preconditions.checkNotNull(project);
  }

  @Override
  public void testRunStarted(String string, int i) {
    testSession = new TestSession("Android tests", project, TestSession.SessionType.TEST);
    String suiteName = string;
    TestSuite testSuite = new TestSuite(suiteName);
    testSession.addSuite(testSuite);
    testManager.displaySuiteRunning(testSession, testSuite);
  }

  @Override
  public void testStarted(TestIdentifier ti) {
    Testcase testcase = new Testcase(ti.getTestName(), null, testSession);
    testcase.setClassName(ti.getClassName());
    testSession.addTestCase(testcase);
    testManager.testStarted(testSession);
  }

  @Override
  public void testFailed(TestIdentifier ti, String string) {
    Trouble trouble = new Trouble(false);
    trouble.setStackTrace(Iterables.toArray(Splitter.on('\n').split(string), String.class));
    testSession.getCurrentTestCase().setTrouble(trouble);
    testSession.getCurrentTestCase().setStatus(Status.FAILED);
  }

  @Override
  public void testEnded(TestIdentifier ti, Map<String, String> map) {
    // no-op - called always
  }

  @Override
  public void testRunFailed(String string) {
    testManager.sessionFinished(testSession);
  }

  @Override
  public void testRunStopped(long l) {
    testManager.sessionFinished(testSession);
    testManager.displayReport(testSession, testSession.getReport(l));
  }

  @Override
  public void testRunEnded(long l, Map<String, String> map) {
    testManager.sessionFinished(testSession);
    testManager.displayReport(testSession, testSession.getReport(l));
  }

  @Override
  public void testAssumptionFailure(TestIdentifier ti, String msg) {
    // TODO how to report this?
    testSession.getCurrentTestCase().setStatus(Status.SKIPPED);
    // testSession.getCurrentTestCase().set(msg);
  }

  @Override
  public void testIgnored(TestIdentifier ti) {
    testSession.getCurrentTestCase().setStatus(Status.IGNORED);
  }

}
