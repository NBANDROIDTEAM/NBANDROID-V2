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

/**
 * Utility class for HTML escaping.
 * @author Christian Fischer
 */
public class HtmlUtil {
    /**
     * Escape html entities.
     * @param str The input string.
     * @return    The HTML escaped output string.
     */
    public static String htmlEscape(String str) {
        StringBuilder sb = new StringBuilder();
        int size = str.length();
        
        for(int i=0; i<size; i++) {
            char c = str.charAt(i);
            
            switch(c) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                default: {
                    int cc = Character.getNumericValue(c);
                    
                    if (cc < 0xA0) {
                        sb.append(c);
                    } else {
                        // encode in unicode reference
                        sb.append("&#").append(Integer.toString(cc)).append(';');
                    }
                    
                    break;
                }
            }
        }
        
        return sb.toString();
    }
}
