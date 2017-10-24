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
package org.netbeans.modules.android.project.ui.layout;

import java.awt.BorderLayout;
import java.util.Collection;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
    dtd = "-//org.netbeans.modules.android.project.ui.layout//PreviewLayout//EN",
autostore = false)
@Messages({
  "CTL_PreviewLayoutAction=Android Layout Preview",
  "CTL_PreviewLayoutTopComponent=Android Layout",
  "HINT_PreviewLayoutTopComponent=Preview of Android Layout"
})
public final class PreviewLayoutTopComponent extends TopComponent {
  
  private final LookupLsnr lookupLsnr = new LookupLsnr();
  private Lookup.Result<FileObject> result;
  private PreviewModel model;
  private PreviewController controller;
  
  private LayoutPreviewForm contentComponent;

  public PreviewLayoutTopComponent() {
    model = new PreviewModel();
    controller = new PreviewController(model);
    setName(Bundle.CTL_PreviewLayoutTopComponent());
    setToolTipText(Bundle.HINT_PreviewLayoutTopComponent());
    putClientProperty(TopComponent.PROP_UNDOCKING_DISABLED, Boolean.TRUE);
    putClientProperty(TopComponent.PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, Boolean.TRUE);
  }

  @Override
  protected void componentShowing() {
    super.componentShowing();
    if (contentComponent == null) {
      setLayout(new BorderLayout());
      contentComponent = new LayoutPreviewForm();
      contentComponent.attachToController(model, controller);

      // contentComponent.setName(NbBundle.getMessage());
      add(contentComponent, BorderLayout.CENTER);  //NOI18N
    }
  }
  
  @Override
  public void componentOpened() {
    Lookup.Template<FileObject> tpl = new Lookup.Template<FileObject>(FileObject.class);
    result = Utilities.actionsGlobalContext().lookup(tpl);
    result.addLookupListener(lookupLsnr);
  }

  @Override
  public void componentClosed() {
    result.removeLookupListener(lookupLsnr);
  }

  @Override
  public int getPersistenceType() {
    return PERSISTENCE_ALWAYS;
  }

  @Override
  protected String preferredID() {
    return "PreviewLayoutTopComponent";
  }

  public PreviewController getController() {
    return controller;
  }

  private class LookupLsnr implements LookupListener {

    @Override
    public void resultChanged(LookupEvent le) {
      Collection<? extends FileObject> c = result.allInstances();
      if (!c.isEmpty()) {
        FileObject fo = (FileObject) c.iterator().next();
        controller.updateFileObject(fo);
      } else {
        controller.updateFileObject(null);
      }
    }
  }

  void writeProperties(java.util.Properties p) {
    // better to version settings since initial version as advocated at
    // http://wiki.apidesign.org/wiki/PropertyFiles
    p.setProperty("version", "1.0");
    // TODO store your settings
  }

  void readProperties(java.util.Properties p) {
    String version = p.getProperty("version");
    // TODO read your settings according to their version
  }
  
}
