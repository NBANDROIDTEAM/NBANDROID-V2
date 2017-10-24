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

package org.netbeans.modules.android.project;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.openide.util.ImageUtilities;

final class Info implements ProjectInformation {

  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  private final AndroidProject project;

  Info(AndroidProject project) {
    this.project = project;
  }

    public @Override String getName() {
        return project.getProjectDirectory().getNameExt();
        // XXX AndroidManifest.xml#/manifest@package
    }

    public @Override String getDisplayName() {
        return getName();
        // XXX AndroidManifest.xml#/manifest/application@android:label -> res/values/strings.xml
    }

    public @Override Icon getIcon() {
        // XXX first check for AndroidManifest.xml#/manifest/application@android:icon -> res/drawable-hdpi/*.png
        return ImageUtilities.loadImageIcon("org/netbeans/modules/android/project/ui/resources/androidProject.png", true);
    }

    public @Override Project getProject() {
        return project;
    }

  public @Override void addPropertyChangeListener(PropertyChangeListener listener) {
      pcs.addPropertyChangeListener(listener);
  }

  public @Override void removePropertyChangeListener(PropertyChangeListener listener) {
      pcs.removePropertyChangeListener(listener);
  }
}
