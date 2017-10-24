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

package org.netbeans.modules.android.project.ui.customizer;

import com.android.sdklib.IAndroidTarget;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Table model to show project build targets.
 *
 * Cf. sdk/sdkmanager/libs/sdkuilib/src/com/android/sdkuilib/internal/widgets/SdkTargetSelector.java
 *
 * @author radim
 */
public class AndroidTargetTableModel extends AbstractTableModel {
  static final int COLUMN_TARGET = 0;
  static final int COLUMN_VENDOR = 1;
  static final int COLUMN_PLATFORM = 2;
  static final int COLUMN_API_LEVEL = 3;

  private  final List<IAndroidTarget> targets;

  public AndroidTargetTableModel(final List<? extends IAndroidTarget> targets) {
    this.targets = new ArrayList<IAndroidTarget>();
    setTargets(targets);
  }

  @Override
  public String getColumnName(int columnIndex) {
    if (columnIndex == COLUMN_TARGET) {
      return "Target Name";
    } else if (columnIndex == COLUMN_VENDOR) {
      return "Vendor";
    } else if (columnIndex == COLUMN_PLATFORM) {
      return "Platform";
    } else if (columnIndex == COLUMN_API_LEVEL) {
      return "API Level";
    }
    throw new IllegalArgumentException("wrong column: " + columnIndex);
  }

  public int getTargetRow(IAndroidTarget target) {
    int i = 0;
    for (IAndroidTarget t : targets) {
      if (t.equals(target)) {
        return i;
      }
      i++;
    }
    return -1;
  }

  public IAndroidTarget getTargetAt(int row) {
    if (row >= 0 && row < getRowCount()) {
      return targets.get(row);
    }
    return null;
  }

  public final List<? extends IAndroidTarget> getTargets() {
    return new ArrayList<IAndroidTarget>(this.targets);
  }

  public final void setTargets(final List<? extends IAndroidTarget> targets) {
    this.targets.clear();
    this.targets.addAll(targets);
    fireTableDataChanged();
  }

  @Override
  public int getRowCount() {
    return targets.size();
  }

  @Override
  public int getColumnCount() {
    return 4;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    if (columnIndex == COLUMN_TARGET) {
      return targets.get(rowIndex).getName();
    } else if (columnIndex == COLUMN_VENDOR) {
      return targets.get(rowIndex).getVendor();
    } else if (columnIndex == COLUMN_PLATFORM) {
      return targets.get(rowIndex).getVersionName();
    } else if (columnIndex == COLUMN_API_LEVEL) {
      return targets.get(rowIndex).getVersion().getApiString();
    }
    throw new IllegalArgumentException("wrong column: " + columnIndex);
  }
}
