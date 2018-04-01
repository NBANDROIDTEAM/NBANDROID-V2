/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.layout.completion;

import com.android.builder.model.AndroidProject;
import java.util.HashMap;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.xml.namespace.QName;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleableNamespace;
import org.nbandroid.netbeans.gradle.v2.sdk.java.platform.AndroidJavaPlatform;
import org.nbandroid.netbeans.gradle.v2.sdk.java.platform.AndroidJavaPlatformProvider;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.gradle.project.NbGradleProject;
import org.netbeans.modules.xml.schema.completion.spi.CompletionContext;
import org.netbeans.modules.xml.schema.completion.util.CompletionContextImpl;
import org.netbeans.modules.xml.schema.completion.util.CompletionUtil;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.openide.filesystems.FileObject;

/**
 *
 * @author arsi
 */
public class LayoutCompletionQuery extends AsyncCompletionQuery {

    private JTextComponent component;
    private final FileObject primaryFile;

    LayoutCompletionQuery(FileObject primaryFile) {
        this.primaryFile = primaryFile;
    }

    @Override
    protected void prepareQuery(JTextComponent component) {
        this.component = component;
    }

    @Override
    protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
        XMLSyntaxSupport support = (XMLSyntaxSupport) ((BaseDocument) doc).getSyntaxSupport();
        if (!support.noCompletion(component) && CompletionUtil.canProvideCompletion((BaseDocument) doc)) {
            Project owner = FileOwnerQuery.getOwner(primaryFile);
            if (owner instanceof NbGradleProject) {
                AndroidProject androidProject = ((NbGradleProject) owner).getLookup().lookup(AndroidProject.class);
                if (androidProject != null) {
                    String next = androidProject.getBootClasspath().iterator().next();
                    AndroidJavaPlatform findPlatform = AndroidJavaPlatformProvider.findPlatform(next, androidProject.getCompileTarget());
                    if (findPlatform != null) {
                        CompletionContextImpl context = new CompletionContextImpl(primaryFile, support, caretOffset);
                        if (context.initContext()) {
                            CompletionContext.CompletionType completionType = context.getCompletionType();
                            switch (completionType) {
                                case COMPLETION_TYPE_UNKNOWN:
                                    break;
                                case COMPLETION_TYPE_ATTRIBUTE:
                                    makeAttribute(findPlatform, context, primaryFile, androidProject, resultSet, doc, caretOffset);
                                    break;
                                case COMPLETION_TYPE_ATTRIBUTE_VALUE:
                                    makeAttributeValue(findPlatform, context, primaryFile, androidProject, resultSet, doc, caretOffset);
                                    break;
                                case COMPLETION_TYPE_ELEMENT:
                                    makeElement(findPlatform, context, primaryFile, androidProject, resultSet, doc, caretOffset);
                                    break;
                                case COMPLETION_TYPE_ELEMENT_VALUE:
                                    break;
                                case COMPLETION_TYPE_ENTITY:
                                    break;
                                case COMPLETION_TYPE_NOTATION:
                                    break;
                                case COMPLETION_TYPE_DTD:
                                    break;
                                default:
                                    throw new AssertionError(completionType.name());

                            }

                        }
                    }
                }
            }
        }
        resultSet.finish();
    }

    private void makeAttribute(AndroidJavaPlatform findPlatform, CompletionContextImpl context, FileObject primaryFile, AndroidProject androidProject, CompletionResultSet resultSet, Document doc, int caretOffset) {
        HashMap<String, String> declaredNamespaces = context.getDeclaredNamespaces();
        String typedChars = context.getTypedChars();
        List<QName> pathFromRoot = context.getPathFromRoot();
        String attributeRoot = null;
        if (!pathFromRoot.isEmpty()) {
            QName qname = pathFromRoot.get(pathFromRoot.size() - 1);
            attributeRoot = qname.getLocalPart();
            AndroidStyleableNamespace platformWidgetNamespaces = findPlatform.getPlatformWidgetNamespaces();
        }
        System.out.println("org.nbandroid.netbeans.gradle.v2.layout.completion.LayoutCompletionQuery.makeAttribute()");
    }

    private void makeAttributeValue(AndroidJavaPlatform findPlatform, CompletionContextImpl context, FileObject primaryFile, AndroidProject androidProject, CompletionResultSet resultSet, Document doc, int caretOffset) {
        HashMap<String, String> declaredNamespaces = context.getDeclaredNamespaces();
        String attribute = context.getAttribute();
        String typedChars = context.getTypedChars();
        String attributeRoot = null;
        List<QName> pathFromRoot = context.getPathFromRoot();
        if (!pathFromRoot.isEmpty()) {
            QName qname = pathFromRoot.get(pathFromRoot.size() - 1);
            attributeRoot = qname.getLocalPart();
            AndroidStyleableNamespace platformWidgetNamespaces = findPlatform.getPlatformWidgetNamespaces();
        }
        System.out.println("org.nbandroid.netbeans.gradle.v2.layout.completion.LayoutCompletionQuery.makeAttributeValue()");
    }

    private void makeElement(AndroidJavaPlatform findPlatform, CompletionContextImpl context, FileObject primaryFile, AndroidProject androidProject, CompletionResultSet resultSet, Document doc, int caretOffset) {
        HashMap<String, String> declaredNamespaces = context.getDeclaredNamespaces();
        String typedChars = context.getTypedChars();
        String elementRoot = null;
        List<QName> pathFromRoot = context.getPathFromRoot();
        if (!pathFromRoot.isEmpty()) {
            QName qname = pathFromRoot.get(pathFromRoot.size() - 1);
            elementRoot = qname.getLocalPart();
            AndroidStyleableNamespace platformWidgetNamespaces = findPlatform.getPlatformWidgetNamespaces();
        }
        System.out.println("org.nbandroid.netbeans.gradle.v2.layout.completion.LayoutCompletionQuery.makeElement()");
    }

}
