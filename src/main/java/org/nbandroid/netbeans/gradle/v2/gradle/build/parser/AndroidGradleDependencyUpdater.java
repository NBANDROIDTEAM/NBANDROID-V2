/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.nbandroid.netbeans.gradle.v2.gradle.build.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author arsi
 */
/**
 * @author Lovett Li
 */
public class AndroidGradleDependencyUpdater {

    public static boolean insertDependencies(File file, Map<String, List<String>> dependencies) {
        try {
            AndroidGradleDependenciesVisitor visitor = AndroidGradleDependenciesVisitor.parse(file);
            AndroidGradleDependencies androidDependencies = visitor.getDependencies();
            if (androidDependencies != null) {
                List<AndroidGradleDependency> toRemove = new ArrayList<>();
                List<AndroidGradleDependency> androidDependenciesList = androidDependencies.getDependencies();
                for (AndroidGradleDependency androidDependency : androidDependenciesList) {
                    if (androidDependency.getAndroidDependency() instanceof AndroidGradleDependency.AndroidRemoteBinaryDependency) {
                        String remoteBinary = ((AndroidGradleDependency.AndroidRemoteBinaryDependency) androidDependency.getAndroidDependency()).getRemoteBinary();
                        for (Map.Entry<String, List<String>> entry : dependencies.entrySet()) {
                            String type = entry.getKey();
                            List<String> mavenUrls = entry.getValue();
                            if (androidDependency.getType().equalsIgnoreCase(type)) {
                                for (Iterator<String> iterator1 = mavenUrls.iterator(); iterator1.hasNext();) {
                                    String next = iterator1.next();
                                    String[] split = next.split(":");
                                    String[] split1 = remoteBinary.split(":");
                                    if (remoteBinary.equalsIgnoreCase(next)) {
                                        iterator1.remove();
                                    } else if (split.length == 3 && split1.length == 3 && split[0].equalsIgnoreCase(split1[0]) && split[1].equalsIgnoreCase(split1[1])) {
                                        DependencyProblemPanel panel = new DependencyProblemPanel(remoteBinary, next);
                                        NotifyDescriptor nd = new NotifyDescriptor.Message(panel, NotifyDescriptor.QUESTION_MESSAGE);
                                        nd.setTitle("Resolve Android dependency version problem..");
                                        DialogDisplayer.getDefault().notify(nd);
                                        boolean useFromTemplate = panel.useFromTemplate();
                                        if (useFromTemplate) {
                                            toRemove.add(androidDependency);
                                        } else {
                                            iterator1.remove();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                try {
                    //create the document out of netbeans,there is a problem with gradle subsystem if opened from EditorCookie
                    EditorKit editorKit = CloneableEditorSupport.getEditorKit("text/x-gradle");
                    Document document = editorKit.createDefaultDocument();
                    editorKit.read(new FileInputStream(file), document, 0);
                    List<LineOffsetRecord> removeLines = new ArrayList<>();
                    if (!toRemove.isEmpty()) {
                        for (AndroidGradleDependency toRemoveDep : toRemove) {
                            int firstLine = toRemoveDep.getFirstLine();
                            int startOfLine = getStartOfLine(document, firstLine);
                            int lastLine = toRemoveDep.getLastLine();
                            int startOfLastLine = getStartOfLine(document, lastLine);
                            startOfLastLine += toRemoveDep.getLastColumn();
                            removeLines.add(new LineOffsetRecord(startOfLine, startOfLastLine - startOfLine));
                        }
                    }
                    //sort to descending order
                    Collections.sort(removeLines, new Comparator<LineOffsetRecord>() {
                        @Override
                        public int compare(LineOffsetRecord t, LineOffsetRecord t1) {
                            return Integer.compare(t1.line, t.line);
                        }
                    });
                    int lastLine = androidDependencies.getLastLine();
                    AndroidGradleDependency lastDependency = androidDependenciesList.get(androidDependenciesList.size() - 1);
                    int tabColumn = lastDependency.getFirstColumn() - 1;
                    String spaces = "";
                    for (int i = 0; i < tabColumn; i++) {
                        spaces += " ";
                    }
                    for (Map.Entry<String, List<String>> entry : dependencies.entrySet()) {
                        String key = entry.getKey();
                        List<String> value = entry.getValue();
                        for (String mavenUrl : value) {
                            int position = getStartOfLine(document, lastLine);
                            document.insertString(position, spaces + key + " '" + mavenUrl + "'" + System.lineSeparator(), null);
                        }
                    }
                    for (LineOffsetRecord removeLine : removeLines) {
                        document.remove(removeLine.line, removeLine.offset);
                    }
                    editorKit.write(new FileOutputStream(file), document, 0, document.getLength());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    public static class LineOffsetRecord {

        final int line;
        final int offset;

        public LineOffsetRecord(int line, int offset) {
            this.line = line;
            this.offset = offset;
        }

        public int getLine() {
            return line;
        }

        public int getOffset() {
            return offset;
        }

    }

    public static int getStartOfLine(Document document, int line) {
        Element root = document.getDefaultRootElement();
        line = Math.max(line, 1);
        line = Math.min(line, root.getElementCount());
        return root.getElement(line - 1).getStartOffset();
    }
}
