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
package org.nbandroid.netbeans.gradle.logcat.logtable;

import com.android.ddmlib.Log.LogLevel;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import static org.nbandroid.netbeans.gradle.logcat.HtmlUtil.htmlEscape;
import org.nbandroid.netbeans.gradle.logcat.LogEvent;

/**
 *
 * @author NYEREL
 */
public class LogTableCellRenderer extends DefaultTableCellRenderer {

    public final static Color COLOR_ASSERT = new Color(0x880000);
    public final static Color COLOR_ERROR = new Color(0xee0000);
    public final static Color COLOR_WARN = new Color(0xee8000);
    public final static Color COLOR_INFO = new Color(0x00bb00);
    public final static Color COLOR_DEBUG = new Color(0x0000cc);
    public final static Color COLOR_VERBOSE = new Color(0x4d4d4d);

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        TableRowSorter rowsorter = (TableRowSorter) table.getRowSorter();

        LogTableModel model = (LogTableModel) table.getModel();
        LogLineRowFilter rowfilter = null;
        String line = htmlEscape(String.valueOf(value));

        // if possible, try to translate the visual row number to the model's row number
        if (rowsorter != null) {
            row = rowsorter.convertRowIndexToModel(row);

            if (rowsorter.getRowFilter() instanceof LogLineRowFilter) {
                rowfilter = (LogLineRowFilter) rowsorter.getRowFilter();
            }
        }

        // get the according event object
        LogEvent event = model.getValueAt(row);
        LogLevel level = event.getLevel();

        final String htmlLeft = "<html><nobr>";
        final String htmlRight = "</nobr></html>";

        switch (column) {
            case LogTableModel.COL_TIME: {
                return super.getTableCellRendererComponent(table, htmlLeft + line + htmlRight, isSelected, hasFocus, row, column);
            }

            case LogTableModel.COL_TAG:
            case LogTableModel.COL_PROCESS: {
                line = rowfilter.highlight(line);
                break;
            }

            case LogTableModel.COL_MESSAGE: {
                line = rowfilter.highlight(line);
                int indentSize = 0;

                while (indentSize < line.length() && line.charAt(indentSize) == ' ') {
                    ++indentSize;
                }

                String indent = indentSize == 0 ? "" : line.substring(0, indentSize - 1);
                line = indentSize == 0 ? line : line.substring(indentSize);

                if (event.getStackTraceElement() != null) {
                    line = "<pre>" + indent + "<u>" + line + "</u></pre>";
                }

                break;
            }
        }

        Component c = super.getTableCellRendererComponent(table, htmlLeft + line + htmlRight, isSelected, hasFocus, row, column);
        Color color = Color.BLACK;

        switch (level) {
            case ASSERT:
                color = COLOR_ASSERT;
                break;
            case DEBUG:
                color = COLOR_DEBUG;
                break;
            case ERROR:
                color = COLOR_ERROR;
                break;
            case INFO:
                color = COLOR_INFO;
                break;
            case WARN:
                color = COLOR_WARN;
                break;
            default:
                color = COLOR_VERBOSE;
                break;
        }

        c.setForeground(color);

        if (column == LogTableModel.COL_MESSAGE && c instanceof JLabel) {
            ((JLabel) c).setToolTipText(value != null ? value.toString() : "");
        }

        return c;
    }
}
