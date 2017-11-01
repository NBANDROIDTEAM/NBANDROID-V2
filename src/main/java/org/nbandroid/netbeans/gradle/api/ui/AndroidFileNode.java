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
package org.nbandroid.netbeans.gradle.api.ui;

import java.util.Set;
import org.nbandroid.netbeans.gradle.ui.ImportantFilesNodeFactory;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 * Node to represent some special file in a project. Mostly just a wrapper around the normal data node.
 *
 * @author radim
 */
public class AndroidFileNode extends FilterNode {

  private final String displayName;

  public AndroidFileNode(Node orig, String displayName) {
    super(orig);
    this.displayName = displayName;
  }

  public @Override
  String getDisplayName() {
    if (displayName != null) {
      return displayName;
    } else {
      return super.getDisplayName();
    }
  }

  public @Override
  boolean canRename() {
    return false;
  }

  public @Override
  boolean canDestroy() {
    return false;
  }

  public @Override
  boolean canCut() {
    return false;
  }

  public @Override
  String getHtmlDisplayName() {
    String result = null;
    DataObject dob = getLookup().lookup(DataObject.class);
    if (dob != null) {
      Set<FileObject> files = dob.files();
      result = ImportantFilesNodeFactory.computeAnnotatedHtmlDisplayName(getDisplayName(), files);
    }
    return result;
  }
}
