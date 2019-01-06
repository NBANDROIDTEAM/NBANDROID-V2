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
package org.nbandroid.netbeans.gradle.v2.layout.values.completion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.nbandroid.netbeans.gradle.v2.layout.completion.RankingProvider;
import org.nbandroid.netbeans.gradle.v2.layout.completion.StyleableIconProvider;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
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
public class BasicValuesCompletionItem implements CompletionItem {

    protected final AndroidValueType type;
    protected final String name;
    protected final String value;
    protected final String comment;
    protected String completionText;
    protected LinkResolver linkResolver;

    public static BasicValuesCompletionItem create(AndroidValueType type, String name, String value, String comment) {
        switch (type) {
            case STRING:
            case INTEGER:
            case BOOL:
                return new BasicValuesCompletionItem(type, name, value, comment);
            case DIMEN:
                return new DimenValuesCompletionItem(type, name, value, comment);
            case COLOR:
                return new ColorValuesCompletionItem(type, name, value, comment);
            case SYMBOL:
                return new SymbolValuesCompletionItem(type, name, value, comment);
            case ITEM:
                return new ItemValuesCompletionItem(type, name, value, comment);
            default:
                throw new AssertionError(type.name());

        }
    }

    public String getCompletionText() {
        return completionText;
    }

    public String getValue() {
        return value;
    }

    public AndroidValueType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    protected BasicValuesCompletionItem(AndroidValueType type, String name, String value, String comment) {
        this.name = name;
        this.value = value;
        this.comment = comment;
        this.type = type;
        switch (type) {
            case STRING:
                completionText = "@string/" + name;
                break;
            case INTEGER:
                completionText = "@integer/" + name;
                break;
            case BOOL:
                completionText = "@bool/" + name;
                break;
            case DIMEN:
                completionText = "@dimen/" + name;
                break;
            case FRACTION:
                completionText = "@fraction/" + name;
                break;
            case LOCALE:
                completionText = "@locale/" + name;
                break;
            case COLOR:
                completionText = "@color/" + name;
                break;
            case FLAG:
                completionText = name;
                break;
            case ENUM:
                completionText = name;
                break;
        }
    }

    @Override
    public void defaultAction(JTextComponent component) {
        if (component != null) {
            try {
                BaseDocument document = (BaseDocument) component.getDocument();
                int caretPosition = component.getCaretPosition();
                int startPosition = caretPosition - 1;
                while ('\"' != (document.getChars(startPosition, 1)[0])) {
                    startPosition--;
                }
                startPosition++;
                document.replace(startPosition, caretPosition - startPosition, completionText, null);
                Completion.get().hideAll();
                RankingProvider.inserted(completionText.hashCode());
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
        return CompletionUtilities.getPreferredWidth(completionText, "",
                g, defaultFont);
    }

    protected String getDoc() {
        return null;
    }

    protected ImageIcon getIcon() {
        ImageIcon icon = null;
        Collection<? extends StyleableIconProvider> iconProviders = Lookup.getDefault().lookupAll(StyleableIconProvider.class
        );
        Iterator<? extends StyleableIconProvider> iterator = iconProviders.iterator();
        while (iterator.hasNext()) {
            StyleableIconProvider iconProvider = iterator.next();
            try {
                icon = iconProvider.getValuesIcon(type);
            } catch (Exception e) {
            }
            if (icon != null) {
                break;
            }
        }
        return icon;
    }

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(getIcon(), completionText, "",
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
        return RankingProvider.getRank(completionText.hashCode());
    }

    @Override
    public CharSequence getSortText() {
        return completionText;
    }

    @Override
    public CharSequence getInsertPrefix() {
        return completionText;

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

        @Override
        public String getText() {
            if (getDoc() == null) {
                String doc = "Type: " + "<b>" + type.name().toLowerCase() + "</b><br>"
                        + "Name: " + "<b>" + name + "</b><br>"
                        + "Value: " + "<b>" + value + "</b><br><br>";
                if (comment != null) {
                    doc += comment;
                }
                return doc;
            } else {
                return getDoc();
            }
        }

        @Override
        public URL getURL() {
            return null;
        }

        @Override
        public CompletionDocumentation resolveLink(String link) {
            if (linkResolver != null) {
                return linkResolver.resolveLink(link);
            }
            return null;
        }

        @Override
        public Action getGotoSourceAction() {
            return null;
        }
    }

    public static interface LinkResolver {

        public CompletionDocumentation resolveLink(String link);
    }

    @Override
    public String toString() {
        return completionText; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.completionText);
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
        final BasicValuesCompletionItem other = (BasicValuesCompletionItem) obj;
        if (!Objects.equals(this.completionText, other.completionText)) {
            return false;
        }
        return true;
    }

}
