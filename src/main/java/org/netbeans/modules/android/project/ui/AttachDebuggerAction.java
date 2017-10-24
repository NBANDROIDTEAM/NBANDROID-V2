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

package org.netbeans.modules.android.project.ui;

import com.android.ddmlib.Client;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.util.Arrays;
import org.netbeans.api.debugger.jpda.DebuggerStartException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.android.project.spi.AndroidDebugInfo;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * A action for android process nodes to attach a debugger to it.
 */
public class AttachDebuggerAction
      extends NodeAction
{

  @Override
  protected void performAction(Node[] activatedNodes) {
    assert activatedNodes.length == 1;
    final Client client = activatedNodes[0].getLookup().lookup(Client.class);
    assert client != null;

    try {
      final String processName = client.getClientData().getClientDescription();

      AndroidDebugInfo di = Iterables.find(
          Iterables.transform(
              Arrays.asList(OpenProjects.getDefault().getOpenProjects()),
              new Function<Project, AndroidDebugInfo>() {
                @Override
                public AndroidDebugInfo apply(Project p) {
                  return p.getLookup().lookup(AndroidDebugInfo.class);
                }
              }),
          new Predicate<AndroidDebugInfo>() {
            @Override
            public boolean apply(AndroidDebugInfo di) {
              return di != null && di.canDebug(processName);
            }
          }, 
          null);

      if (di != null) {
        AndroidDebugInfo.AndroidDebugData debugData = di.data(client);
        JPDADebugger.attach(
            debugData.hostname,
            debugData.port,
            new Object[]{debugData.properties});
      } else {
        StatusDisplayer.getDefault().setStatusText("Cannot find a project to debug " + processName);
      }
    }
    catch (DebuggerStartException e) {
      Exceptions.printStackTrace(e);
    }
  }

  @Override
  protected boolean enable(Node[] activatedNodes) {
    return activatedNodes.length == 1 && activatedNodes[0].getLookup().lookup(Client.class) != null;
  }

  @Override
  public String getName() {
    return NbBundle.getMessage(getClass(), "LBL_AttachDebuggerAction_Name");
  }

  @Override
  public HelpCtx getHelpCtx() {
    return new HelpCtx(AttachDebuggerAction.class.getName());
  }
}
