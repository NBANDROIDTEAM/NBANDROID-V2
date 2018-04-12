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
package org.nbandroid.netbeans.gradle.v2.layout.completion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleable;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleableAttr;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleableAttrEnum;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleableAttrFlag;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleableAttrType;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author arsi
 */
public class AttrCompletionItem implements CompletionItem {

    private final AndroidStyleable styleable;
    private final AndroidStyleableAttr attr;
    private final String prefix;
    private final String completionText;
    private final String lowerCasecompletionText;
    private final String lowerCaseSimpleCompletionText;
    private String classNameText;
    private final String typeNames;

    public AttrCompletionItem(AndroidStyleable styleable, AndroidStyleableAttr attr, String prefix) {
        if (prefix == null) {
            prefix = "";
        }
        prefix = prefix.replace("xmlns:", "");
        this.styleable = styleable;
        this.attr = attr;
        this.prefix = prefix;
        completionText = prefix + ":" + attr.getName();
        classNameText = "";
        EnumSet<AndroidStyleableAttrType> attrTypes = attr.getAttrTypes();
        List<String> types = new ArrayList<>();
        attrTypes.forEach(t -> {
            types.add(t.toString());
        });
        typeNames = String.join(", ", types);
        lowerCasecompletionText = completionText.toLowerCase();
        if (attr.getName() != null) {
            lowerCaseSimpleCompletionText = attr.getName().toLowerCase();
        } else {
            lowerCaseSimpleCompletionText = "";
        }
    }

    public String getLowerCasecompletionText() {
        return lowerCasecompletionText;
    }

    public String getLowerCaseSimpleCompletionText() {
        return lowerCaseSimpleCompletionText;
    }

    public String getClassNameText() {
        return classNameText;
    }

    public String getCompletionText() {
        return completionText;
    }

    private ImageIcon getIcon() {
        ImageIcon icon = null;
        Collection<? extends StyleableIconProvider> iconProviders = Lookup.getDefault().lookupAll(StyleableIconProvider.class);
        Iterator<? extends StyleableIconProvider> iterator = iconProviders.iterator();
        while (iterator.hasNext()) {
            StyleableIconProvider iconProvider = iterator.next();
            try {
                switch (styleable.getAndroidStyleableType()) {
                    case Widget:
                        try {
                            icon = iconProvider.getWidgetAttrIcon();
                        } catch (Exception e) {
                            Exceptions.printStackTrace(e);
                        }
                        break;
                    case Layout:
                        try {
                            icon = iconProvider.getLayoutAttrIcon();
                        } catch (Exception e) {
                            Exceptions.printStackTrace(e);
                        }
                        break;
                    case LayoutParams:
                        try {
                            icon = iconProvider.getWidgetLayoutAttrIcon();
                        } catch (Exception e) {
                            Exceptions.printStackTrace(e);
                        }
                        break;
                }
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
            if (icon != null) {
                break;
            }
        }
        return icon;
    }

    @Override
    public void defaultAction(JTextComponent component) {
        if (component != null) {
            try {
                BaseDocument document = (BaseDocument) component.getDocument();
                int caretPosition = component.getCaretPosition();
                String text = Utilities.getIdentifierBefore(document, component.getCaretPosition());
                if (text == null) {
                    text = "";
                }
                int startPosition = caretPosition - text.length();
                document.replace(startPosition, text.length(), completionText + "=\"\"", null);
                Completion.get().hideAll();
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public void processKeyEvent(KeyEvent evt) {
    }

    @Override
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(completionText + " ", classNameText + " ",
                g, defaultFont);
    }

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(getIcon(), completionText, classNameText,
                g, defaultFont, defaultColor, width, height, selected);
    }

    @Override
    public CompletionTask createDocumentationTask() {
        return new AsyncCompletionTask(new DocQuery());
    }

    @Override
    public CompletionTask createToolTipTask() {
        return null;
    }

    @Override
    public boolean instantSubstitution(JTextComponent component) {
        return false;
    }

    @Override
    public int getSortPriority() {
        return RankingProvider.getRank(attr.getName().hashCode());
    }

    @Override
    public CharSequence getSortText() {
        return attr.getName();
    }

    @Override
    public CharSequence getInsertPrefix() {
        return attr.getName();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.completionText);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AttrCompletionItem other = (AttrCompletionItem) obj;
        if (!Objects.equals(this.completionText, other.completionText)) {
            return false;
        }
        return true;
    }

    private class DocQuery extends AsyncCompletionQuery {

        public DocQuery() {
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            resultSet.setDocumentation(new DocItem());
            resultSet.finish();
        }
    }

    private class DocItem implements CompletionDocumentation {

        public DocItem() {
        }

        private String getEnums() {
            if (attr.getEnums().length == 0) {
                return "";
            } else {
                String out = "Enums: <br>";
                for (int i = 0; i < attr.getEnums().length; i++) {
                    AndroidStyleableAttrEnum aEnum = attr.getEnums()[i];
                    out += "<b>" + aEnum.getName() + "</b><br>" + aEnum.getComment() + "<br>";
                }
                return out;
            }
        }

        private String getFlags() {
            if (attr.getFlags().length == 0) {
                return "";
            } else {
                String out = "Flags: <br>";
                for (int i = 0; i < attr.getFlags().length; i++) {
                    AndroidStyleableAttrFlag aFlag = attr.getFlags()[i];
                    out += "<b>" + aFlag.getName() + "</b><br>" + aFlag.getComment() + "<br>";
                }
                return out;
            }
        }

        @Override
        public String getText() {
            if (attr.getDescription() != null && !"".equals(attr.getDescription())) {
                return "&nbsp;Attr: <b>" + attr.getName() + "</b><br>Class: <b>" + styleable.getFullClassName() + "<br></b>Value: <b>" + typeNames + "</b><br><br>" + attr.getDescription() + "<br><br>" + getEnums() + getFlags();
            } else {
                return "&nbsp;Attr: <b>" + attr.getName() + "</b><br>Class: <b>" + styleable.getFullClassName() + "<br></b>Value: <b>" + typeNames + "</b><br><br><font color=\"#7c0000\">Attribute doc not found.</font><br><br>" + getEnums() + getFlags();
            }
        }

        @Override
        public URL getURL() {
            return null;
        }

        @Override
        public CompletionDocumentation resolveLink(String link) {
            return null;
        }

        @Override
        public Action getGotoSourceAction() {
            return null;
        }
    }

    @Override
    public String toString() {
        return completionText; //To change body of generated methods, choose Tools | Templates.
    }

}
