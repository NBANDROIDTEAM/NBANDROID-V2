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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.nbandroid.netbeans.gradle.logcat.LogEvent;
import org.nbandroid.netbeans.gradle.logcat.LogEventInfo;
import org.openide.util.NbBundle;

/**
 *
 * @author NYEREL
 */
public class LogTableModel extends AbstractTableModel {

    public static final int COL_TIME = 0;
    public static final int COL_PID = 1;
    public static final int COL_PROCESS = 2;
    public static final int COL_LEVEL = 3;
    public static final int COL_TAG = 4;
    public static final int COL_MESSAGE = 5;
    public static final String COL_TIME_NAME = "time";
    public static final String COL_PID_NAME = "pid";
    public static final String COL_PROCESS_NAME = "pname";
    public static final String COL_LEVEL_NAME = "level";
    public static final String COL_TAG_NAME = "tag";
    public static final String COL_MESSAGE_NAME = "message";

    private List<LogEvent> data;
    private LogFilter filter;

    public LogTableModel() {
        this(null);
    }

    public LogTableModel(LogFilter filter) {
        this.data = Collections.synchronizedList(new ArrayList<LogEvent>());
        this.filter = filter;
    }


    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case COL_MESSAGE:
                return String.class;
            case COL_PID:
                return Integer.class;
            case COL_PROCESS:
                return String.class;
            case COL_TIME:
                return String.class; // TODO(radim): how to sort this properly
            case COL_TAG:
                return String.class;
            case COL_LEVEL:
                return LogLevel.class;
        }
        throw new IllegalArgumentException("This column does not exist: " + columnIndex);
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case COL_TIME:
                return NbBundle.getMessage(getClass(), "LogCatColumn." + COL_TIME_NAME);
            case COL_PID:
                return NbBundle.getMessage(getClass(), "LogCatColumn." + COL_PID_NAME);
            case COL_PROCESS:
                return NbBundle.getMessage(getClass(), "LogCatColumn." + COL_PROCESS_NAME);
            case COL_MESSAGE:
                return NbBundle.getMessage(getClass(), "LogCatColumn." + COL_MESSAGE_NAME);
            case COL_TAG:
                return NbBundle.getMessage(getClass(), "LogCatColumn." + COL_TAG_NAME);
            case COL_LEVEL:
                return NbBundle.getMessage(getClass(), "LogCatColumn." + COL_LEVEL_NAME);
        }
        throw new IllegalArgumentException("This column does not exist: " + column);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    public LogEvent getValueAt(int rowIndex) {
        return data.get(rowIndex);
    }

    public void clear() {
        data.clear();
        fireTableDataChanged();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        LogEvent e = data.get(rowIndex);
        switch (columnIndex) {
            case COL_MESSAGE:
                return e.getMessage();
            case COL_PID:
                return e.getPid();
            case COL_PROCESS:
                return e.getProcessName();
            case COL_TIME:
                return e.getTime();
            case COL_TAG:
                return e.getTag();
            case COL_LEVEL:
                return e.getLevel();
        }
        throw new IllegalArgumentException("This column does not exist: " + columnIndex);
    }

    public void addNewEvent(LogEvent event) {
       // Do filtering
        if(filter != null && !filter.satisfy(event)) {
            return;
        }

        if (event.getTime().equals("")) {
            event = changeTime(event);
        }
        data.add(event);
        fireTableRowsInserted(data.size() - 1, data.size() - 1);
    }

    public void addNewEvents(Collection<? extends LogEvent> events) {
        int oldsize = data.size();

        for(LogEvent event : events) {
            if (filter != null && !filter.satisfy(event)) {
                continue;
            }

            if (event.getTime().equals("")) {
                event = changeTime(event);
            }

            data.add(event);
        }

        if (data.size() > oldsize) {
            fireTableRowsInserted(oldsize, data.size() - 1);
        }
    }

    public void showError(String errorMessage) {
        LogEventInfo info = new LogEventInfo(0, new String[]{""}, "LogCat Error", "-----", LogLevel.ERROR);
        LogEvent event = new LogEvent(info, errorMessage);
        addNewEvent(event);
    }

    public void showError(Exception e) {
        showError(e.toString());
    }

    private LogEvent changeTime(LogEvent event) {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int sec = cal.get(Calendar.SECOND);
        int mili = cal.get(Calendar.MILLISECOND);

        String h = String.format("%02d", hour);
        String m = String.format("%02d", minute);
        String s = String.format("%02d", sec);
        String ms = String.format("%02d", mili);

        String time = h+":" + m + ":" + s + "." + ms;
        return new LogEvent(
            new LogEventInfo(event.getPid(), event.getProcessNameRef(), time, event.getTag(), event.getLevel()),
            event.getMessage());
    }

    public LogFilter getFilter() {
        return filter;
    }
}
