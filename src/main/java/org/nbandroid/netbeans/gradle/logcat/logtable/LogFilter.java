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
import java.util.Properties;
import org.nbandroid.netbeans.gradle.logcat.LogEvent;
import org.openide.util.NbBundle;

/**
 *
 * @author Michal Kytýr <michal.kytyr at syntea.cz>
 */
public class LogFilter {

    private final static String SERIALIZE_NAME = "name";
    private final static String SERIALIZE_TAG = "tag";
    private final static String SERIALIZE_PROCESS = "process";
    private final static String SERIALIZE_LEVEL = "level";

    private final String name;
    private final String filterTag;
    private final String filterProcess;
    private final int filterPid;
    private final LogLevel filterLevel;

    public static LogFilter createDefaultTab() {
        return new LogFilter(NbBundle.getMessage(LogFilter.class, "LogCat.DefaultTab.title"));
    }

    public LogFilter(String title) {
        this(title, null, null, null);
    }

    public LogFilter(String name, String filterTag, String filterProcess, LogLevel filterLevel) {
        this.name = name;
        this.filterTag = filterTag;
        this.filterLevel = filterLevel;
        this.filterProcess = filterProcess;

        if (filterProcess == null) {
            this.filterPid = -1;
        } else {
            int pid = -1;

            try {
                pid = Integer.parseInt(filterProcess);
            } catch (NumberFormatException e) {
            }

            this.filterPid = pid;
        }
    }

    /**
     * Store all attributes of this filter into the given properties-list.
     */
    public void serialize(Properties p, String prefix) {
        p.put(prefix + "." + SERIALIZE_NAME, name);

        if (filterTag != null) {
            p.put(prefix + "." + SERIALIZE_TAG, filterTag);
        }

        if (filterProcess != null) {
            p.put(prefix + "." + SERIALIZE_PROCESS, filterProcess);
        }

        if (filterLevel != null) {
            p.put(prefix + "." + SERIALIZE_LEVEL, filterLevel.toString());
        }
    }

    /**
     * Create a new filter, which was previously stored in the given properties
     * list.
     */
    public static LogFilter deserialize(Properties p, String prefix) {
        String name = p.getProperty(prefix + "." + SERIALIZE_NAME);
        String process = p.getProperty(prefix + "." + SERIALIZE_PROCESS);
        String tag = p.getProperty(prefix + "." + SERIALIZE_TAG);
        String lvlname = p.getProperty(prefix + "." + SERIALIZE_LEVEL);
        LogLevel level = null;

        if (lvlname != null) {
            try {
                level = LogLevel.valueOf(lvlname);
            } catch (IllegalArgumentException e) {
            }
        }

        if (name != null) {
            return new LogFilter(name, tag, process, level);
        }

        return null;
    }

    public boolean satisfy(LogEvent event) {

        boolean ok = true;

        if (filterTag != null) {
            ok &= filterTag.equalsIgnoreCase(event.getTag());
        }

        if (filterProcess != null) {
            ok &= filterProcess.equalsIgnoreCase(event.getProcessName());
        }

        if (filterPid != -1) {
            ok &= (filterPid == event.getPid());
        }

        if (filterLevel != null) {
            ok &= (event.getLevel().getPriority() >= filterLevel.getPriority());
        }

        return ok;
    }

    public String getName() {
        return name;
    }

    /**
     * creates a string describing the criterias of this filter.
     *
     * @return
     */
    public String getDescription() {
        String description = "<html><b>" + getName() + "</b>";

        if (filterTag != null) {
            description += "<br/>" + NbBundle.getMessage(this.getClass(), "LogCatColumn.tag") + ": " + filterTag;
        }

        if (filterProcess != null) {
            description += "<br/>" + NbBundle.getMessage(this.getClass(), "LogCatColumn.pname") + ": " + filterProcess;
        }

        if (filterPid != -1) {
            description += "<br/>" + NbBundle.getMessage(this.getClass(), "LogCatColumn.pid") + ": " + filterPid;
        }

        if (filterLevel != null) {
            description += "<br/>" + NbBundle.getMessage(this.getClass(), "LogCatColumn.level") + ": " + filterLevel;
        }

        return description;
    }

    @Override
    public String toString() {
        return "[LogFilter name=" + name + " tag=" + filterTag + " process=" + filterProcess + " pid=" + filterPid + " level=" + filterLevel + "]";
    }
}
