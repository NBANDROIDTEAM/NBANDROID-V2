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
package org.nbandroid.netbeans.gradle.logcat;

import com.android.ddmlib.Log.LogLevel;

/**
 *
 * @author NYEREL
 */
public class LogEventInfo {

    private final int pid;
    private final String[] pNameRef;
    private final String time;
    private final String tag;
    private final LogLevel level;

    public LogLevel getLevel() {
        return level;
    }

    public int getPid() {
        return pid;
    }

    public String getProcessName() {
        if (pNameRef[0] != null) {
            return pNameRef[0];
        }
        
        return '#' + Integer.toString(pid);
    }
    
    public String[] getProcessNameRef() {
        return pNameRef;
    }

    public String getTag() {
        return tag;
    }

    public String getTime() {
        return time;
    }

    public LogEventInfo(int pid, String[] pNameRef, String time, String tag, LogLevel level) {
        this.pid = pid;
        this.pNameRef = pNameRef;
        this.time = time;
        this.tag = tag;
        this.level = level;
    }

    @Override
    public int hashCode() {
        return level.hashCode() + pid * 10 + tag.hashCode() + time.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final LogEventInfo other = (LogEventInfo) obj;
      if (this.pid != other.pid) {
        return false;
      }
      if ((this.time == null) ? (other.time != null) : !this.time.equals(other.time)) {
        return false;
      }
      if ((this.tag == null) ? (other.tag != null) : !this.tag.equals(other.tag)) {
        return false;
      }
      if (this.level != other.level) {
        return false;
      }
      return true;
    }
}
