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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author NYEREL
 */
public class LogEvent implements Comparable<LogEvent> {
    
    /**
     * Reg-Ex copied from Java Project Support JavaAntLogger
     */
    
    /** Java identifier */
    private static final String JIDENT = "[\\p{javaJavaIdentifierStart}][\\p{javaJavaIdentifierPart}]*"; // NOI18N
    
    private static final Pattern STACK_TRACE = Pattern.compile(
            "(.*?((?:" + JIDENT + "[.])*)(" + JIDENT + ")[.](?:" + JIDENT + "|<init>|<clinit>)" + // NOI18N
            "[(])((" + JIDENT + "[.]java):([0-9]+)|Unknown Source)([)].*)"); // NOI18N




    private final LogEventInfo info;
    private final String message;
    private final StackTraceElement ste;


    public LogEvent(LogEventInfo info, String message) {
        this.info = info;
        this.message = message;
        this.ste = parseStackTraceElement();
    }

    /**
     * Parse the log message and create a {@link java.lang.StackTraceElement},
     * if the message is part of a stack trace.
     * Source is also copied from Java Project Support JavaAntLogger
     */
    private StackTraceElement parseStackTraceElement() {
        Matcher m = STACK_TRACE.matcher(message);
        
        if (m.matches()) {
            // We have a stack trace.
            String pkg = m.group(2);
            String cls = m.group(3);
            String filename = m.group(5);
            int lineNumber;
            
            if (filename == null) {
                filename = m.group(3).replaceFirst("[$].+", "") + ".java"; // NOI18N
                lineNumber = 1;
            }
            else {
                lineNumber = Integer.parseInt(m.group(6));
            }
            
            String[] str = new String[m.groupCount()];
            for(int i=0; i<str.length; i++) {
                str[i] = m.group(i);
            }
            
            return new StackTraceElement(pkg + cls, "", pkg.replace('.', '/') + filename, lineNumber);
        }
        
        return null;
    }


    public String getTime() {
        return info.getTime();
    }

    public String getTag() {
        return info.getTag();
    }

    public int getPid() {
        return info.getPid();
    }

    public String getProcessName() {
        return info.getProcessName();
    }

    public String[] getProcessNameRef() {
        return info.getProcessNameRef();
    }

    public LogLevel getLevel() {
        return info.getLevel();
    }
    
    public String getMessage() {
        return message;
    }
    
    public StackTraceElement getStackTraceElement() {
        return ste;
    }

    @Override
    public String toString() {
            return info.getTime() + ": "
                + info.getLevel() + "/"
                + info.getTag() + "("
                + info.getPid() + "): "
                + message;
        }

    @Override
    public int compareTo(LogEvent o) {
        return getTime().compareTo(o.getTime());
    }

    @Override
    public int hashCode() {
        return info.hashCode() * 37 + message.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof LogEvent) {
            LogEvent o = (LogEvent) obj;

            if (message == null && o.message != null) {
                return false;
            }
            if (!message.equals(o.message)) {
                return false;
            }
            if (info == null && o.info != null) {
                return false;
            }
            if (info.equals(o.info) == false) {
                return false;
            }
            return true;
        }

        return super.equals(obj);
    }
}
