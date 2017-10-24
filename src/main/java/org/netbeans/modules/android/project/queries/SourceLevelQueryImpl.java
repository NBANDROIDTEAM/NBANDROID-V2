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

package org.netbeans.modules.android.project.queries;

import com.google.common.base.Preconditions;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;

/**
 * Returns source level of project sources.
 */
public class SourceLevelQueryImpl implements SourceLevelQueryImplementation {
  private static final Logger LOG = Logger.getLogger(AndroidProjectEncodingQueryImpl.class.getName());

  // TODO listen to property changes and update encoding
  
  private final PropertyEvaluator properties;

  public SourceLevelQueryImpl(PropertyEvaluator properties) {
    this.properties = Preconditions.checkNotNull(properties);
  }

  @Override
  public String getSourceLevel(FileObject javaFile) {        
    String source = properties.getProperty("java.source");
    LOG.log(Level.FINE, "Source level for {0}: {1}", new Object[] {javaFile, source});
    return source != null ? source : "1.5";
  }

}
