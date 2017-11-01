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
package org.nbandroid.netbeans.gradle.core.sdk;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 *
 * @author radim
 */
public class WidgetData {

  public final Map<LayoutElementType, Collection<UIClassDescriptor>> data;
  public final Collection<UIClassDescriptor> classes;

  public WidgetData(Map<LayoutElementType, Collection<UIClassDescriptor>> data, Collection<UIClassDescriptor> classes) {
    this.data = Collections.unmodifiableMap(data);
    this.classes = Collections.unmodifiableCollection(classes);
  }
}
