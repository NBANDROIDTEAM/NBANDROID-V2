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
package org.nbandroid.netbeans.gradle.apk;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

@NbBundle.Messages("APKMIME_DisplayName=Android Packages (.apk)")
@DataObject.Registration(displayName = "#APKDataObject_DisplayName", 
    iconBase = "org/netbeans/modules/android/project/resources/android.png", 
    mimeType = "application/vnd.android.package-archive",
    position = 1023)
// register before JAR/ZIP
@MIMEResolver.ExtensionRegistration(
    displayName = "#APKMIME_DisplayName", extension = "apk", mimeType = "application/vnd.android.package-archive",
    position = 479)
public class ApkDataObject extends MultiDataObject implements Deployable {

  public ApkDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
    super(pf, loader);

  }

  @Override
  protected Node createNodeDelegate() {
    return new DataNode(this, Children.LEAF, getLookup());
  }

  @Override
  public Lookup getLookup() {
    return getCookieSet().getLookup();
  }

  @Override
  public FileObject getDeployableFile() {
    return getPrimaryFile();
  }
}
