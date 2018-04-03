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
package org.nbandroid.netbeans.gradle.v2.layout;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.lang.model.element.TypeElement;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.nbandroid.netbeans.gradle.v2.layout.completion.RankingProvider;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ui.ElementJavadoc;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * @author arsi
 */
public class AndroidStyleable implements Serializable, CompletionItem {

    private final AndroidStyleableNamespace nameSpace;
    private final String nameSpacePath;
    private final String name;
    private final List<AndroidStyleableAttr> attrs = new ArrayList<>();
    private String superStyleableName;
    private AndroidStyleable superStyleable;
    private String fullClassName;
    private AndroidStyleableType androidStyleableType = AndroidStyleableType.ToBeDetermined;
    private URL classFileURL = null;

    public AndroidStyleable(AndroidStyleableNamespace nameSpace, String name) {
        this.nameSpace = nameSpace;
        this.name = name;

        if (nameSpace != null) {
            this.nameSpacePath = nameSpace.getNamespace();
        } else {
            this.nameSpacePath = null;
        }
    }

    public URL getClassFileURL() {
        return classFileURL;
    }

    public void setClassFileURL(URL classFileURL) {
        this.classFileURL = classFileURL;
    }

    public String getFullClassName() {
        return fullClassName;
    }

    public AndroidStyleableNamespace getNameSpace() {
        return nameSpace;
    }

    public String getName() {
        return name;
    }

    public void createLayuotIfNotExist(Map<String, AndroidStyleable> fullClassNames, Map<String, AndroidStyleable> layouts, Map<String, AndroidStyleable> layoutsSimple) {
        if (androidStyleableType == AndroidStyleableType.LayoutParams) {
            StringTokenizer tok = new StringTokenizer(name, ".", false);
            if (tok.countTokens() == 2) {
                AndroidStyleable laout = new AndroidStyleable(nameSpace, tok.nextToken());
                String layoutText = tok.nextToken();
                laout.setFullClassName(fullClassName.replace("." + layoutText, ""));
                laout.setAndroidStyleableType(AndroidStyleableType.Layout);
                String superName = null;
                if (!"java.lang.Object".equals(superStyleableName)) {
                    superName = superStyleableName.substring(0, superStyleableName.lastIndexOf('.'));
                } else {
                    superName = superStyleableName;
                }
                laout.setSuperStyleableName(superName);
                laout.setSuperStyleable(fullClassNames.get(superName));
                if (!layouts.containsKey(laout.fullClassName)) {
                    layouts.put(laout.fullClassName, laout);
                    layoutsSimple.put(laout.name, laout);
                }

            }
        }
    }

    public List<AndroidStyleableAttr> getAttrs() {
        return attrs;
    }

    public AndroidStyleable getSuperStyleable() {
        return superStyleable;
    }

    public String getSuperStyleableName() {
        return superStyleableName;
    }

    public void setSuperStyleable(AndroidStyleable superStyleable) {
        this.superStyleable = superStyleable;
    }

    public AndroidStyleableType getAndroidStyleableType() {
        return androidStyleableType;
    }

    public void setAndroidStyleableType(AndroidStyleableType androidStyleableType) {
        if (androidStyleableType != null) {
            this.androidStyleableType = androidStyleableType;
        } else {
            this.androidStyleableType = AndroidStyleableType.ToBeDetermined;
        }
    }

    public void setSuperStyleableName(String superStyleableName) {
        this.superStyleableName = superStyleableName;
    }

    public void setFullClassName(String fullClassName) {
        this.fullClassName = fullClassName;
    }

    public String getNameSpacePath() {
        return nameSpacePath;
    }

    @Override
    public String toString() {
        return "AndroidStyleable{" + ", name=" + name + ", type=" + androidStyleableType + ", attrs=" + attrs + "nameSpace=" + nameSpacePath + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.nameSpacePath);
        hash = 61 * hash + Objects.hashCode(this.name);
        hash = 61 * hash + Objects.hashCode(this.attrs);
        hash = 61 * hash + Objects.hashCode(this.superStyleableName);
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
        final AndroidStyleable other = (AndroidStyleable) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.superStyleableName, other.superStyleableName)) {
            return false;
        }
        if (!Objects.equals(this.nameSpacePath, other.nameSpacePath)) {
            return false;
        }
        if (!Objects.equals(this.attrs, other.attrs)) {
            return false;
        }
        return true;
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
                if (AndroidStyleableStore.ANDROID_NAMESPACE.equals(nameSpacePath)) {
                    document.replace(startPosition, text.length(), name, null);
                } else {
                    document.replace(startPosition, text.length(), fullClassName, null);
                }
                Completion.get().hideDocumentation();
                Completion.get().hideCompletion();
                RankingProvider.inserted(fullClassName.hashCode());
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
        return CompletionUtilities.getPreferredWidth(getFullClassName(), getAndroidStyleableType().name(),
                g, defaultFont);
    }

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(null, getFullClassName(), getAndroidStyleableType().name(),
                g, defaultFont, defaultColor, width, height, selected);
        Completion c = Completion.get();
        c.hideDocumentation();
        c.showDocumentation();
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
        return RankingProvider.getRank(fullClassName.hashCode());
    }

    @Override
    public CharSequence getSortText() {
        return name;
    }

    @Override
    public CharSequence getInsertPrefix() {
        return "";
    }

    private class DocItem implements CompletionDocumentation {

        private final String text;

        public DocItem(String fullClassName) {
            this.text = fullClassName;
        }

        @Override
        public String getText() {
            return text;
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

    private class JavaDocItem implements CompletionDocumentation {

        private final ElementJavadoc javadoc;

        public JavaDocItem(ElementJavadoc javadoc) {
            this.javadoc = javadoc;
        }

        @Override
        public String getText() {
            return javadoc.getText();
        }

        @Override
        public URL getURL() {
            return javadoc.getURL();
        }

        @Override
        public CompletionDocumentation resolveLink(String link) {
            return new JavaDocItem(javadoc.resolveLink(link));
        }

        @Override
        public Action getGotoSourceAction() {
            return javadoc.getGotoSourceAction();
        }
    }

    private class DocQuery extends AsyncCompletionQuery {

        public DocQuery() {
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            System.out.println("org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleable.DocQuery.query()");
            if (classFileURL != null) {
                JavaSource javaSource = JavaSource.forFileObject(URLMapper.findFileObject(classFileURL));
                final CountDownLatch countDownLatch = new CountDownLatch(1);
                try {
                    javaSource.runUserActionTask(new Task<CompilationController>() {
                        @Override
                        public void run(CompilationController p) throws Exception {
                            p.toPhase(Phase.ELEMENTS_RESOLVED);
                            List<? extends TypeElement> topLevelElements = p.getTopLevelElements();
                            ElementJavadoc javadoc = ElementJavadoc.create(p, topLevelElements.get(0));
                            resultSet.setDocumentation(new JavaDocItem(javadoc));
                            countDownLatch.countDown();
                        }
                    }, true);
                    countDownLatch.await(10, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                resultSet.setDocumentation(new DocItem(getFullClassName()));
            }
            resultSet.finish();
        }
    }

}
