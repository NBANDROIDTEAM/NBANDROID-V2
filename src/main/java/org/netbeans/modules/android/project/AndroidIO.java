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

import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * An envelope around I/O component used to show messages related to communication
 * with Android tools.
 *
 * @author radim
 */
public class AndroidIO {

  public static InputOutput getDefaultIO() {
    return IOProvider.getDefault().getIO("Android development", false);
  }
}
