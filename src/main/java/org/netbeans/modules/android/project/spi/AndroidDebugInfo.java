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
package org.netbeans.modules.android.project.spi;

import com.android.ddmlib.Client;
import java.util.Map;
import javax.annotation.Nonnull;
import org.netbeans.api.project.Project;

/**
 * Information provider used when attaching JPDA debugger to Android device (Client).
 * 
 * @author radim
 */
public interface AndroidDebugInfo {

  public static final class AndroidDebugData {
    public final String hostname;
    public final int port;
    public final Map<String, Object> properties;

    public AndroidDebugData(String hostname, int port, Map<String, Object> properties) {
      this.hostname = hostname;
      this.port = port;
      this.properties = properties;
    }
  }
  /** General query to answer if there is a chance to debug Android application using this project. */
  boolean supportsDebugging();
  /** More strict check to find if this project is suitable to debug application with given process name. */
  boolean canDebug(String processName);
  AndroidDebugData data(Client client);
  
  @Nonnull
  Project project();
}
