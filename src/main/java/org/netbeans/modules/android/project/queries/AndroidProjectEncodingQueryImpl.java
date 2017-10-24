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

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;

public class AndroidProjectEncodingQueryImpl extends FileEncodingQueryImplementation {
  private static final Logger LOG = Logger.getLogger(AndroidProjectEncodingQueryImpl.class.getName());

  // TODO listen to property changes and update encoding
  // TODO java encoding vs. the rest of sources
  
  private final PropertyEvaluator properties;

  public AndroidProjectEncodingQueryImpl(PropertyEvaluator properties) {
    this.properties = Preconditions.checkNotNull(properties);
  }

  public @Override Charset getEncoding(FileObject file) {
    String encoding = properties.getProperty("java.encoding");
    if (encoding != null) {
      try {
        return Charset.forName(encoding);
      } catch (IllegalArgumentException ex) {
        LOG.log(Level.INFO, "android project has invalid java.encoding", ex);
      }
    }
    return Charsets.UTF_8;
  }
}
