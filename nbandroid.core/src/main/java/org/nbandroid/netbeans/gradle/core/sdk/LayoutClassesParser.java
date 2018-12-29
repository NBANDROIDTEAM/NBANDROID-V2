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
package org.nbandroid.netbeans.gradle.core.sdk;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.Sets;
import com.google.common.io.LineProcessor;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to parse widgets.txt file from SDK that contains info about class type
 * and its inheritance tree.
 *
 * Each line is in the following format<br/>
 * {@code [code][class name] [super class name] [super class name]...} where
 * code is a single letter (W for widget, L for layout, P for layout
 * parameters), and class names
 *
 * Cf. WidgetClassLoader in ADT plugin.
 *
 * @author radim
 */
class LayoutClassesParser implements Supplier<WidgetData> {

    private static final Logger LOG = Logger.getLogger(LayoutClassesParser.class.getName());

    private final URL url;

    public LayoutClassesParser(URL url) {
        this.url = Preconditions.checkNotNull(url);
    }

    @Override
    public WidgetData get() {
        try {
            WidgetData clzDescriptors
                    = Resources.readLines(url, Charsets.ISO_8859_1,
                            new LineProcessorImpl());
            return clzDescriptors;
        } catch (IOException ex) {
            LOG.log(Level.INFO, null, ex);
            return new WidgetData(Collections.<LayoutElementType, Collection<UIClassDescriptor>>emptyMap(),
                    Collections.<UIClassDescriptor>emptySet());
        }
    }

    private class LineProcessorImpl implements LineProcessor<WidgetData> {

        private Map<LayoutElementType, Collection<UIClassDescriptor>> data
                = new EnumMap<LayoutElementType, Collection<UIClassDescriptor>>(LayoutElementType.class);
        private Set<UIClassDescriptor> classes = Sets.newHashSet();

        public LineProcessorImpl() {
            for (LayoutElementType t : LayoutElementType.values()) {
                data.put(t, Sets.<UIClassDescriptor>newHashSet());
            }
        }

        @Override
        public boolean processLine(String line) throws IOException {
            if (line.length() < 1) {
                // weird but OK
                LOG.log(Level.INFO, "empty line when reading from {0}", url);
                return true;
            }
            char type = line.charAt(0);
            LayoutElementType layoutType = null;
            for (LayoutElementType t : LayoutElementType.values()) {
                if (type == t.getPrefix()) {
                    layoutType = t;
                    break;
                }
            }
            if (layoutType == null) {
                LOG.log(Level.INFO, "unknown widget type {1} when reading from {0}", new Object[]{url, line});
                return true;
            }
            String hierarchy = line.substring(1);
            UIClassDescriptor clz = parseOneClass(hierarchy);
            boolean first = true;
            // we need to add superclasses too for params
            while (clz != null
                    && !clz.getFQClassName().equals(Object.class.getName())) {
                if (first) {
                    data.get(layoutType).add(clz);
                }
                first = false;
                classes.add(clz);
                hierarchy = hierarchy.substring(hierarchy.indexOf(' ') + 1);
                clz = parseOneClass(hierarchy);
            }
//      if (clz != null) {
//        data.get(layoutType).add(clz);
//      }
            return true;
        }

        private UIClassDescriptor parseOneClass(String hierarchy) {
            String[] clzNames = hierarchy.split(" ");
            if (clzNames.length < 2) {
                return null;
            }
            final String clzName = clzNames[0];
            final String superClzName = clzNames[1];
            String simpleClzName = clzName.substring(clzName.lastIndexOf('.') + 1);
            // check for example LinearLayout.LayoutParams
            String nestedClzName = clzName.substring(clzName.substring(0, clzName.lastIndexOf('.')).lastIndexOf('.') + 1);
            final String simpleName = Character.isUpperCase(nestedClzName.charAt(0)) ? nestedClzName : simpleClzName;
            UIClassDescriptor clz = new UIClassDescriptorImpl(clzName, superClzName, simpleName);
            return clz;
        }

        @Override
        public WidgetData getResult() {
            if (LOG.isLoggable(Level.FINE)) {
                for (Map.Entry<LayoutElementType, Collection<UIClassDescriptor>> entry : data.entrySet()) {
                    LOG.log(Level.FINE, "parsed {1} classes of type {2} when reading from {0}",
                            new Object[]{url, entry.getValue().size(), entry.getKey()});
                }
            }
            return new WidgetData(data, classes);
        }

    }

    private static class UIClassDescriptorImpl implements UIClassDescriptor {

        private final String clzName;
        private final String superClzName;
        private final String simpleName;

        public UIClassDescriptorImpl(String clzName, String superClzName, String simpleName) {
            this.clzName = clzName;
            this.superClzName = superClzName;
            this.simpleName = simpleName;
        }

        @Override
        public String getFQClassName() {
            return clzName;
        }

        @Override
        public String getSuperclass() {
            return superClzName;
        }

        @Override
        public String getSimpleName() {
            return simpleName;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final UIClassDescriptorImpl other = (UIClassDescriptorImpl) obj;
            if ((this.clzName == null) ? (other.clzName != null) : !this.clzName.equals(other.clzName)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 71 * hash + (this.clzName != null ? this.clzName.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "UIClassDescriptor{" + clzName + ", " + superClzName + ", " + simpleName + '}';
        }
    }
}
