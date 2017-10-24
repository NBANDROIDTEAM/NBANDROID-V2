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
package org.netbeans.modules.android.core.sdk;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 *
 * @author radim
 */
public class UIClassDescriptors {
  private static Predicate<UIClassDescriptor> simpleName(final String simpleName) {
    return new Predicate<UIClassDescriptor>() {

      @Override
      public boolean apply(UIClassDescriptor input) {
        return input.getSimpleName().equals(simpleName);
      }
    };
  }
  private static Predicate<UIClassDescriptor> fqName(final String fqName) {
    return new Predicate<UIClassDescriptor>() {

      @Override
      public boolean apply(UIClassDescriptor input) {
        return input.getFQClassName().equals(fqName);
      }
    };
  }
  private static Predicate<UIClassDescriptor> outerClassName(final String outerName) {
    return new Predicate<UIClassDescriptor>() {

      @Override
      public boolean apply(UIClassDescriptor input) {
        if (outerName == null) {
          return false;
        }
        return input.getFQClassName().equals(outerName + ".LayoutParams");
      }
    };
  }

  public static Iterable<UIClassDescriptor> findBySimpleName(
      WidgetData classData, String simpleClassName) {
    return findByName(Iterables.concat(classData.data.values()), simpleName(simpleClassName));
  }

  public static Iterable<UIClassDescriptor> findBySimpleName(
      WidgetData classData, LayoutElementType type, String simpleClassName) {
    return findByName(classData.data.get(type), simpleName(simpleClassName));
  }

  public static UIClassDescriptor findByFQName(
      WidgetData classData, String fqClassName) {
    return Iterables.getOnlyElement(findByName(classData.classes, fqName(fqClassName)), null);
  }

  public static UIClassDescriptor findByFQName(
      WidgetData classData, LayoutElementType type, String fqClassName) {
    return Iterables.getOnlyElement(findByName(classData.data.get(type), fqName(fqClassName)), null);
  }

  public static UIClassDescriptor findParamsForName(
      WidgetData classData, String viewGroupName) {
    return Iterables.getOnlyElement(
        findByName(classData.data.get(LayoutElementType.LAYOUT_PARAM), outerClassName(viewGroupName)), null);
  }

  private static Iterable<UIClassDescriptor> findByName(
      Iterable<UIClassDescriptor> classes, Predicate<UIClassDescriptor> predicate) {
    return Iterables.filter(classes, predicate);
  }
}
