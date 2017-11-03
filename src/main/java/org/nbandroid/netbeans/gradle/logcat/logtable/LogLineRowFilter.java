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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.RowFilter;
import static org.nbandroid.netbeans.gradle.logcat.HtmlUtil.htmlEscape;
import org.nbandroid.netbeans.gradle.logcat.LogEvent;

/**
 *
 * @author Christian Fischer
 */
public class LogLineRowFilter extends RowFilter<LogTableModel, Integer> {

    interface RowMatcher {
        boolean include(Entry<? extends LogTableModel, ? extends Integer> entry);
        String highlight(String str);
    }

    private LogLevel level = LogLevel.VERBOSE;
    private boolean useRegexp = false;
    private String filterText = "";
    private RowMatcher currentFilter;
    
    public LogLineRowFilter() {
        setFilterString("");
    }
    
    /**
     * Set the filter string for this RowFilter.
     * The string will be split into single keywords.
     */
    public final void setFilterString(String filterText) {
        this.filterText = filterText;
        updateFilter();
    }

    private void updateFilter() {
        currentFilter = useRegexp ? new RegexpRowMatcher(filterText) : new TextSearchFilter(filterText);
    }

    public void setUseRegExp(boolean useRegexp) {
        this.useRegexp = useRegexp;
        updateFilter();
    }

    public boolean isUseRegexp() {
        return useRegexp;
    }
    
    /**
     * Set the minimum LogLevel of messages, which should be included in this filter.
     */
    public void setLogLevel(LogLevel level) {
        this.level = level;
    }
    
    /**
     * Get the current minimum LogLevel of this filter.
     * @return 
     */
    public LogLevel getLogLevel() {
        return level;
    }
    
    /**
     * Highlights all keywords in this string via HTML formatting.
     * @param str input string.
     * @return output string, containing html
     */
    public String highlight(String str) {
        return currentFilter.highlight(str);
    }
    
    @Override
    public boolean include(Entry<? extends LogTableModel, ? extends Integer> entry) {
        return currentFilter.include(entry);
    }

    private class TextSearchFilter implements RowMatcher {
        private String[] searchKeyWords = new String[]{};
        private Pattern keywordFinder  = null;

        public TextSearchFilter(String filterText) {
            searchKeyWords = filterText.toLowerCase().trim().split("\\s");

            // if the list contains just one empty string, replace it with an empty list.
            if (searchKeyWords.length == 1 && searchKeyWords[0].equals("")) {
                searchKeyWords = new String[0];
            }

            // we create a reg-ex pattern, which finds all of this keywords
            StringBuilder pattern = new StringBuilder();
            for (String keyword : searchKeyWords) {
                if (pattern.length() != 0) {
                    pattern.append('|');
                }

                pattern.append(Pattern.quote(htmlEscape(keyword)));
            }

            keywordFinder = Pattern.compile('(' + pattern.toString() + ')', Pattern.CASE_INSENSITIVE);
        }

        @Override
        public boolean include(Entry<? extends LogTableModel, ? extends Integer> entry) {
            LogTableModel model = entry.getModel();
            LogEvent event = model.getValueAt(entry.getIdentifier());

            if (event.getLevel().getPriority() < level.getPriority()) {
                return false;
            }

            if (searchKeyWords.length > 0) {
                String message = event.getMessage().toLowerCase();
                String pname = event.getProcessName().toLowerCase();
                String tag = event.getTag().toLowerCase();

                for (String exp : searchKeyWords) {
                    if (!tag.contains(exp)
                            && !pname.contains(exp)
                            && !message.contains(exp)) {
                        return false;
                    }
                }
            }

            return true;
        }

        @Override
        public String highlight(String str) {
            if (searchKeyWords.length > 0) {
                return keywordFinder.matcher(str).replaceAll("<b>$1</b>");
            }

            return str;
        }
    }

    private class RegexpRowMatcher implements RowMatcher {
        private Pattern keywordFinder  = null;

        public RegexpRowMatcher(String filterText) {
            try {
                keywordFinder = Pattern.compile(filterText);
            } catch (PatternSyntaxException pse) {
                // ignore
            }
        }

        @Override
        public boolean include(Entry<? extends LogTableModel, ? extends Integer> entry) {
            if (keywordFinder == null) {
                return true;
            }
            LogTableModel model = entry.getModel();
            LogEvent event = model.getValueAt(entry.getIdentifier());

            if (event.getLevel().getPriority() < level.getPriority()) {
                return false;
            }

            if (keywordFinder.matcher(event.getMessage()).find() ||
                    keywordFinder.matcher(event.getTag()).find()) {
                return true;
            }
            return false;
        }

        @Override
        public String highlight(String str) {
            return str;
        }

    }
}
