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

import java.awt.Rectangle;
import java.util.Collection;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.nbandroid.netbeans.gradle.logcat.LogEvent;
import org.nbandroid.netbeans.gradle.logcat.LogListener;

/**
 *
 * @author NYEREL
 */
public class LogTableManager implements LogListener, ChangeListener {

    private final TableColumnModel columnModel;
    private final JTable table;
    private final LogTableModel model;
    private boolean autoFollowScroll = true;

    public LogTableManager(JTable table) {
        this.table = table;
        this.model = (LogTableModel) table.getModel();
        this.columnModel = table.getColumnModel();
        initTable();
    }

    private void initTable() {
        TableColumn tagColumn = columnModel.getColumn(LogTableModel.COL_TAG);
        TableColumn pidColumn = columnModel.getColumn(LogTableModel.COL_PID);
        TableColumn pNameColumn = columnModel.getColumn(LogTableModel.COL_PROCESS);
        TableColumn timeColumn = columnModel.getColumn(LogTableModel.COL_TIME);
        TableColumn levelColumn = columnModel.getColumn(LogTableModel.COL_LEVEL);

        tagColumn.setMinWidth(50);
        tagColumn.setPreferredWidth(150);
        tagColumn.setMaxWidth(300);

        pidColumn.setMinWidth(35);
        pidColumn.setPreferredWidth(50);
        pidColumn.setMaxWidth(70);

        pNameColumn.setMinWidth(60);
        pNameColumn.setPreferredWidth(80);
        pNameColumn.setMaxWidth(300);

        timeColumn.setMinWidth(60);
        timeColumn.setPreferredWidth(80);
        timeColumn.setMaxWidth(120);

        levelColumn.setMinWidth(20);
        levelColumn.setPreferredWidth(80);
        levelColumn.setMaxWidth(80);

    }

    public LogTableModel getModel() {
        return model;
    }

    public void scrollToBottom() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                Rectangle lastRow = table.getCellRect(table.getRowCount() - 1, 0, true);
                table.scrollRectToVisible(lastRow);
            }
        });
    }

    public void addAllEvents(final Collection<LogEvent> events) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                model.addNewEvents(events);

                if (autoFollowScroll) {
                    scrollToBottom();
                }

                return;
            }
        });
    }

    @Override
    public void newLogEvent(final LogEvent logEvent) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                model.addNewEvent(logEvent);
                if (autoFollowScroll) {
                    scrollToBottom();
                }

                return;
            }
        });
    }

    @Override
    public void error(String m) {
        model.showError(m);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        Object o = e.getSource();
        if (o instanceof JToggleButton) {
            JToggleButton b = (JToggleButton) o;
            if (b.isSelected()) {
                if (!autoFollowScroll) {
                    scrollToBottom();
                }
                autoFollowScroll = true;
            } else {
                autoFollowScroll = false;
            }
        }

    }

    public void clearLog() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                model.clear();
            }
        });
    }

    public boolean isAutoFollowScroll() {
        return autoFollowScroll;
    }

    @Override
    public String toString() {
        return "LogTableManager for filter: " + model.getFilter();
    }
}
