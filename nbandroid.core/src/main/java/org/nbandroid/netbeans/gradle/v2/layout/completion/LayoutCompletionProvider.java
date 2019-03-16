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

import com.android.builder.model.AndroidProject;
import javax.swing.text.JTextComponent;
import org.nbandroid.netbeans.gradle.v2.sdk.java.platform.AndroidJavaPlatform;
import org.nbandroid.netbeans.gradle.v2.sdk.java.platform.AndroidJavaPlatformProvider;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.android.project.api.NbAndroidProject;
import org.netbeans.modules.xml.schema.completion.util.CompletionUtil;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.spi.editor.completion.CompletionProvider;
import static org.netbeans.spi.editor.completion.CompletionProvider.COMPLETION_QUERY_TYPE;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.filesystems.FileObject;

/**
 *
 * @author arsi
 */
@MimeRegistration(mimeType = "text/xml", service = CompletionProvider.class)
public class LayoutCompletionProvider implements CompletionProvider {

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        BaseDocument doc = Utilities.getDocument(component);
        if (typedText == null || typedText.trim().length() == 0) {
            return 0;
        }
        // do not pop up if the end of text contains some whitespaces.
        if (Character.isWhitespace(typedText.charAt(typedText.length() - 1))) {
            return 0;
        }
        if (doc == null) {
            return 0;
        }
        XMLSyntaxSupport support = ((XMLSyntaxSupport) doc.getSyntaxSupport());
        if (support.noCompletion(component) || !CompletionUtil.canProvideCompletion(doc)) {
            return 0;
        }
        FileObject primaryFile = CompletionUtil.getPrimaryFile(component.getDocument());
        Project owner = FileOwnerQuery.getOwner(primaryFile);
        if (owner instanceof NbAndroidProject) {
            AndroidProject androidProject = owner.getLookup().lookup(AndroidProject.class);
            if (androidProject != null) {
                String next = androidProject.getBootClasspath().iterator().next();
                AndroidJavaPlatform findPlatform = AndroidJavaPlatformProvider.findPlatform(next, androidProject.getCompileTarget());
                if (findPlatform == null) {
                    return 0;
                }
            } else {
                return 0;
            }
        } else {
            return 0;
        }

        return COMPLETION_QUERY_TYPE;
    }

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (queryType == COMPLETION_QUERY_TYPE || queryType == COMPLETION_ALL_QUERY_TYPE) {
            return new AsyncCompletionTask(new LayoutCompletionQuery(CompletionUtil.getPrimaryFile(component.getDocument()), queryType), component);
        }

        return null;
    }

}
