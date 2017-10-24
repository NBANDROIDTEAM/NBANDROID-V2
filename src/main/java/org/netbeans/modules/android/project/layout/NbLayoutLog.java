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
package org.netbeans.modules.android.project.layout;

import com.android.ide.common.rendering.api.LayoutLog;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author radim
 */
public class NbLayoutLog extends LayoutLog {
  private static final Logger LOG = Logger.getLogger(NbLayoutLog.class.getName());

  @Override
  public void error(String tag, String msg, Object o) {
    LOG.log(Level.WARNING, "Layout error {0}: {1}", new Object[] {tag, msg});
    if (o != null) {
      LOG.log(Level.WARNING, o.toString());
    }
  }

  @Override
  public void error(String tag, String msg, Throwable thrwbl, Object o) {
    LOG.log(Level.WARNING, "Layout error " + msg + ": " + msg, thrwbl);
    if (o != null) {
      LOG.log(Level.WARNING, o.toString());
    }
  }

  @Override
  public void fidelityWarning(String tag, String msg, Throwable thrwbl, Object o) {
    LOG.log(Level.WARNING, "Layout fidelity warning " + msg + ": " + msg, thrwbl);
    if (o != null) {
      LOG.log(Level.WARNING, o.toString());
    }
  }

  @Override
  public void warning(String tag, String msg, Object o) {
    LOG.log(Level.INFO, "Layout warning {0}: {1}", new Object[] {tag, msg});
    if (o != null) {
      LOG.log(Level.INFO, o.toString());
    }
  }

}
