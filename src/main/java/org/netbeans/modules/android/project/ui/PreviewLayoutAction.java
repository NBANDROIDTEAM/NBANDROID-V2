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

import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.netbeans.modules.android.project.ui.layout.PreviewLayoutTopComponent;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author radim
 */
public class PreviewLayoutAction extends AbstractAction {
  private static final Logger LOG = Logger.getLogger(PreviewLayoutAction.class.getName());
  
  private final FileObject fo;

  public PreviewLayoutAction(FileObject fo) {
    super(NbBundle.getMessage(AndroidLogicalViewProvider.class, "LBL_Preview_Layout_Action"));
    this.fo = fo;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    TopComponent c = WindowManager.getDefault().findTopComponent("PreviewLayoutTopComponent");
    if (c instanceof PreviewLayoutTopComponent) {
      PreviewLayoutTopComponent pltc = (PreviewLayoutTopComponent) c;
      c.open();
      c.requestActive();
      pltc.getController().updateFileObject(fo);
    } else {
      LOG.log(Level.INFO, "Cannot find PreviewLayoutTopComponent");
    }
    
  }

}
