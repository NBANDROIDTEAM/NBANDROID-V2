/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.nbandroid.netbeans.gradle.v2.color.preview;

import com.junichi11.netbeans.modules.color.codes.preview.api.OffsetRange;
import com.junichi11.netbeans.modules.color.codes.preview.spi.ColorCodeGeneratorItem;
import com.junichi11.netbeans.modules.color.codes.preview.spi.ColorCodesPreviewOptionsPanel;
import com.junichi11.netbeans.modules.color.codes.preview.spi.ColorCodesProvider;
import com.junichi11.netbeans.modules.color.codes.preview.spi.ColorValue;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.Document;
import org.nbandroid.netbeans.gradle.v2.layout.parsers.AndroidResValuesProvider;
import org.nbandroid.netbeans.gradle.v2.layout.values.ColorsDataObject;
import org.nbandroid.netbeans.gradle.v2.layout.values.completion.AndroidValueType;
import static org.nbandroid.netbeans.gradle.v2.layout.values.completion.BasicColorValuesCompletionItem.decodeAlfa;
import org.nbandroid.netbeans.gradle.v2.layout.values.completion.BasicValuesCompletionItem;
import org.nbandroid.netbeans.gradle.v2.layout.values.completion.ColorValuesCompletionItem;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author arsi
 */
public class AndroidColorCodesProvider implements ColorCodesProvider {

    @Override
    public String getId() {
        return "android";
    }

    @Override
    public String getDisplayName() {
        return "Android";
    }

    @Override
    public String getDescription() {
        return "Android hex colors preview,support for Java and xml code.";
    }

    @Override
    public boolean isProviderEnabled(Document document) {
        String mimeType = NbEditorUtilities.getMimeType(document);
        return "text/x-android-layout+xml".equals(mimeType) || "text/x-android-styles+xml".equals(mimeType) || "text/plain+xml".equals(mimeType) || "text/x-java".equals(mimeType) || ColorsDataObject.SETTINGS_MIME_TYPE.equals(mimeType);
    }

    @Override
    public List<ColorValue> getColorValues(Document document, String line, int lineNumber, Map<String, List<ColorValue>> variableColorValues) {
        List<ColorValue> colorValues = new ArrayList<>();
        String mimeType = NbEditorUtilities.getMimeType(document);
        if ("text/x-android-layout+xml".equals(mimeType) || "text/x-android-styles+xml".equals(mimeType) || "text/plain+xml".equals(mimeType) || "text/x-java".equals(mimeType) || ColorsDataObject.SETTINGS_MIME_TYPE.equals(mimeType)) {
            FileObject primaryFile = getPrimaryFile(document);
            if (primaryFile != null) {
                Project project = FileOwnerQuery.getOwner(primaryFile);
                if (project != null && "text/plain+xml".equals(mimeType)) {
                    parseColorVariables(project, line, colorValues, lineNumber);
                } else if (project != null && ColorsDataObject.SETTINGS_MIME_TYPE.equals(mimeType) || "text/x-android-styles+xml".equals(mimeType)) {
                    parseColorVariables(project, line, colorValues, lineNumber);
                    getHexColorCodes(this, line, lineNumber, colorValues);

                } else if (project != null && "text/x-android-layout+xml".equals(mimeType)) {
                    parseLayoutColorVariables(project, line, colorValues, lineNumber);
                    getHexColorCodes(this, line, lineNumber, colorValues);

                } else if (project != null && "text/x-java".equals(mimeType)) {
                    parseJavaCode(project, line, colorValues, lineNumber);
                }
            }
        }
        return colorValues;
    }

    @Override
    public int getStartIndex(Document document, int currentIndex) {
        return 0;
    }

    @Override
    public ColorCodesPreviewOptionsPanel getOptionsPanel() {
        return null;
    }

    @Override
    public boolean canGenerateColorCode() {
        return false;
    }

    @Override
    public List<ColorCodeGeneratorItem> getColorCodeGeneratorItems(String mimeType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static FileObject getPrimaryFile(Document doc) {
        Object o = doc.getProperty(Document.StreamDescriptionProperty);
        if (o instanceof FileObject) {
            return (FileObject) o;
        } else if (o instanceof Lookup.Provider) {
            //Note: DataObject is a Lookup.Provider
            return ((Lookup.Provider) o).getLookup().lookup(FileObject.class);
        } else {
            return null;
        }
    }

    private static final String re11 = "(Color\\.parseColor)";	// Fully Qualified Domain Name 1
    private static final String re12 = "(\\()";	// Any Single Character 1
    private static final String re13 = "(\")";	// Any Single Character 2
    private static final String re14 = "(#{1}(?:[A-F0-9]){6,8})(?![0-9A-F])";	// HTML Color 1
    private static final String re15 = "(\")";	// Any Single Character 3
    private static final String re16 = "(\\))";	// Any Single Character 4

    private void parseJavaCode(Project project, String line, List<ColorValue> colorValues, int lineNumber) {
        AndroidResValuesProvider provider = project.getLookup().lookup(AndroidResValuesProvider.class);
        if (provider != null) {
            Pattern p = Pattern.compile(re11 + re12 + re13 + re14 + re15 + re16, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher = p.matcher(line);
            if (matcher.find()) {
                String colorCode = matcher.group(4);
                Color color = null;
                if (colorCode.length() == 9) {
                    color = decodeAlfa(colorCode);
                } else {
                    color = decodeAlfa(colorCode.replace("#", "#FF"));
                }
                if (color != null) {
                    ColorValue colorValue = new AndroidJavaColorValue(color, colorCode, new OffsetRange(matcher.start(4), matcher.end(4)), lineNumber);
                    colorValues.add(colorValue);
                }
            }
        }
    }

    private static final String re1 = ".*?";	// Non-greedy match on filler
    private static final String re2 = "(#{1}(?:[A-F0-9]){6,8})(?![0-9A-F])";	// HTML Color 1

    public static void getHexColorCodes(@NonNull ColorCodesProvider colorCodesProvider, String line, int lineNumber, List<ColorValue> colorValues) {
        Pattern p = Pattern.compile(re1 + "(#{1}(?:[A-F0-9]){6,8})(?![0-9A-F])", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = p.matcher(line);
        while (matcher.find()) {
            final String colorCode = matcher.group(1);
            Color color = null;
            if (colorCode.length() == 9) {
                color = decodeAlfa(colorCode);
            } else {
                color = decodeAlfa(colorCode.replace("#", "#FF"));
            }
            if (color != null) {
                ColorValue colorValue = new XmlColorValue(color, colorCode, new OffsetRange(matcher.start(1), matcher.end(1)), lineNumber);
                colorValues.add(colorValue);
            }

        }
    }

    private void parseColorVariables(Project project, String line, List<ColorValue> colorValues, int lineNumber) {
        AndroidResValuesProvider provider = project.getLookup().lookup(AndroidResValuesProvider.class);
        if (provider != null) {
            List<BasicValuesCompletionItem> colors = provider.forType(AndroidValueType.COLOR);
            for (int i = 0; i < colors.size(); i++) {
                BasicValuesCompletionItem basicValuesCompletionItem = colors.get(i);
                if (basicValuesCompletionItem instanceof ColorValuesCompletionItem) {
                    Color color = ((ColorValuesCompletionItem) basicValuesCompletionItem).getColor();
                    String completionText = basicValuesCompletionItem.getCompletionText();
                    if (color != null && completionText != null && line.contains("\"" + completionText + "\"")) {
                        colorValues.add(new ReadOnlyColorValue(this, color, completionText, 0, 0, lineNumber));
                    }
                    if (color != null && completionText != null && line.contains(">" + completionText + "<")) {
                        colorValues.add(new ReadOnlyColorValue(this, color, completionText, 0, 0, lineNumber));
                    }
                }
            }
        }
    }

    private void parseLayoutColorVariables(Project project, String line, List<ColorValue> colorValues, int lineNumber) {
        AndroidResValuesProvider provider = project.getLookup().lookup(AndroidResValuesProvider.class);
        if (provider != null) {
            List<BasicValuesCompletionItem> colors = provider.forType(AndroidValueType.COLOR);
            for (int i = 0; i < colors.size(); i++) {
                BasicValuesCompletionItem basicValuesCompletionItem = colors.get(i);
                if (basicValuesCompletionItem instanceof ColorValuesCompletionItem) {
                    Color color = ((ColorValuesCompletionItem) basicValuesCompletionItem).getColor();
                    String completionText = basicValuesCompletionItem.getCompletionText();
                    if (color != null && completionText != null && line.contains("\"" + completionText + "\"")) {
                        colorValues.add(new ReadOnlyColorValue(this, color, completionText, 0, 0, lineNumber));
                    }
                    if (color != null && completionText != null && line.contains(">" + completionText + "<")) {
                        colorValues.add(new ReadOnlyColorValue(this, color, completionText, 0, 0, lineNumber));
                    }
                }
            }
        }
    }
}
