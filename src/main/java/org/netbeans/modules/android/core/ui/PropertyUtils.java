/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.netbeans.modules.android.core.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 *
 * @author radim
 */
public class PropertyUtils {

  /**
   * Creates delegating property editor that displays passed string as a representation of {@code null} value.
   * In addition to this it is not paintable for {@code null} value.
   */
  public static PropertyEditor stringPropertyEditorWithTags(final String[] tags) {
    final PropertyEditor delegate = PropertyEditorManager.findEditor(String.class);
    ExPropertyEditor editor = new ExPropertyEditor() {

      @Override
      public void setValue(Object value) {
        delegate.setValue(value);
      }

      @Override
      public Object getValue() {
        return delegate.getValue();
      }

      @Override
      public boolean isPaintable() {
        return getValue() == null ? false : delegate.isPaintable();
      }

      @Override
      public void paintValue(Graphics gfx, Rectangle box) {
        delegate.paintValue(gfx, box);
      }

      @Override
      public String getJavaInitializationString() {
        return delegate.getJavaInitializationString();
      }

      @Override
      public String getAsText() {
        return delegate.getAsText();
      }

      @Override
      public void setAsText(String text) throws IllegalArgumentException {
        delegate.setAsText(text);
      }

      @Override
      public String[] getTags() {
        return tags;
      }

      @Override
      public Component getCustomEditor() {
        return delegate.getCustomEditor();
      }

      @Override
      public boolean supportsCustomEditor() {
        return delegate.supportsCustomEditor();
      }

      @Override
      public void addPropertyChangeListener(PropertyChangeListener listener) {
        delegate.addPropertyChangeListener(listener);
      }

      @Override
      public void removePropertyChangeListener(PropertyChangeListener listener) {
        delegate.removePropertyChangeListener(listener);
      }

      @Override
      public void attachEnv(PropertyEnv env) {
        if (delegate instanceof ExPropertyEditor) {
          ((ExPropertyEditor) delegate).attachEnv(env);
        }
      }
    };
    return editor;
  }
}
