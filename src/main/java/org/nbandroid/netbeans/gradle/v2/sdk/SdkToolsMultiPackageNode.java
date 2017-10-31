/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.nbandroid.netbeans.gradle.v2.sdk;

import com.android.repository.api.RepoPackage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.TreeNode;
import org.nbandroid.netbeans.gradle.v2.ext.ComparableVersion;

/**
 * SDK Tool node with more versions of single package
 *
 * @author arsi
 */
public class SdkToolsMultiPackageNode extends AbstractSdkToolNode {

    private final TreeNode parent;
    private final String prefix;
    private final List<SdkToolsPackageNode> nodes = new ArrayList<>();
    private boolean flatModel = true;
    private String displayName = null;

    SdkToolsMultiPackageNode(TreeNode parent, String prefix) {
        this.parent = parent;
        this.prefix = prefix;
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
        return nodes.get(childIndex);
    }

    @Override
    public int getChildCount() {
        return nodes.size();
    }

    @Override
    public TreeNode getParent() {
        return parent;
    }

    @Override
    public int getIndex(TreeNode node) {
        return nodes.indexOf(node);
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public boolean isLeaf() {
        return nodes.isEmpty() || flatModel;
    }

    @Override
    public Enumeration children() {
        return Collections.enumeration(nodes);
    }

    void addNode(SdkToolsPackageNode node) {
        nodes.add(node);
    }

    @Override
    public boolean isFlatModel() {
        return flatModel;
    }

    @Override
    public void setFlatModel(boolean flatModel) {
        this.flatModel = flatModel;
        for (SdkToolsPackageNode node : nodes) {
            node.setFlatModel(flatModel);
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public List<SdkToolsPackageNode> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return getDisplayName(); //To change body of generated methods, choose Tools | Templates.
    }

    public String getDisplayName() {
        if (displayName == null) {
            Collections.sort(nodes, new Comparator<SdkToolsPackageNode>() {
                @Override
                public int compare(SdkToolsPackageNode o1, SdkToolsPackageNode o2) {
                    return new ComparableVersion(o2.toString()).compareTo(new ComparableVersion(o1.toString()));
                }
            });
            RepoPackage maxPackage = nodes.get(0).getPackage().getRepresentative();
            String maxName = maxPackage.getDisplayName();
            String maxPath = maxPackage.getPath();
            String suffix = maxPath.substring(maxPath.lastIndexOf(RepoPackage.PATH_SEPARATOR) + 1);
            maxName = trimEnd(maxName, suffix).trim();
            maxName = trimEnd(maxName, ":");
            displayName = maxName;
            return maxName;
        } else {
            return displayName;
        }
    }

    public static String trimEnd(String s, String suffix) {
        return trimEnd(s, suffix, false);
    }

    public static String trimEnd(String s, String suffix, boolean ignoreCase) {
        boolean endsWith = ignoreCase ? endsWithIgnoreCase(s, suffix) : s.endsWith(suffix);
        if (endsWith) {
            return s.substring(0, s.length() - suffix.length());
        }
        return s;
    }

    public static String trimEnd(String s, char suffix) {
        if (endsWithChar(s, suffix)) {
            return s.substring(0, s.length() - 1);
        }
        return s;
    }

    public static boolean endsWithIgnoreCase(String text, String suffix) {
        int l1 = text.length();
        int l2 = suffix.length();
        if (l1 < l2) {
            return false;
        }

        for (int i = l1 - 1; i >= l1 - l2; i--) {
            if (!charsEqualIgnoreCase(text.charAt(i), suffix.charAt(i + l2 - l1))) {
                return false;
            }
        }

        return true;
    }

    public static boolean charsEqualIgnoreCase(char a, char b) {
        return a == b || toUpperCase(a) == toUpperCase(b) || toLowerCase(a) == toLowerCase(b);
    }

    public static char toUpperCase(char a) {
        if (a < 'a') {
            return a;
        }
        if (a <= 'z') {
            return (char) (a + ('A' - 'a'));
        }
        return Character.toUpperCase(a);
    }

    public static char toLowerCase(char a) {
        if (a < 'A' || a >= 'a' && a <= 'z') {
            return a;
        }

        if (a <= 'Z') {
            return (char) (a + ('a' - 'A'));
        }

        return Character.toLowerCase(a);
    }

    public static boolean startsWithIgnoreCase(String str, String prefix) {
        final int stringLength = str.length();
        final int prefixLength = prefix.length();
        return stringLength >= prefixLength && str.regionMatches(true, 0, prefix, 0, prefixLength);
    }

    public static boolean endsWithChar(CharSequence s, char suffix) {
        return s != null && s.length() != 0 && s.charAt(s.length() - 1) == suffix;
    }

}
