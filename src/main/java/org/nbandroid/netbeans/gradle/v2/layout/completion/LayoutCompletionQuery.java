/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.layout.completion;

import com.android.builder.model.AndroidProject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.xml.namespace.QName;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleable;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleableNamespace;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleableStore;
import org.nbandroid.netbeans.gradle.v2.sdk.java.platform.AndroidJavaPlatform;
import org.nbandroid.netbeans.gradle.v2.sdk.java.platform.AndroidJavaPlatformProvider;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.gradle.project.NbGradleProject;
import org.netbeans.modules.xml.schema.completion.spi.CompletionContext;
import org.netbeans.modules.xml.schema.completion.util.CompletionContextImpl;
import org.netbeans.modules.xml.schema.completion.util.CompletionUtil;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author arsi
 */
public class LayoutCompletionQuery extends AsyncCompletionQuery {

    private JTextComponent component;
    private final FileObject primaryFile;
    private final int queryType;
    private CompletionContext.CompletionType currentMode = CompletionContext.CompletionType.COMPLETION_TYPE_UNKNOWN;
    private List<AndroidStyleable> items = new ArrayList<>();
    private String startChars = "";

    LayoutCompletionQuery(FileObject primaryFile, int queryType) {
        this.primaryFile = primaryFile;
        this.queryType = queryType;
    }

    @Override
    protected void prepareQuery(JTextComponent component) {
        this.component = component;
    }

    @Override
    protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
        resultSet.setWaitText("Loading android Styleables..");
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
    private String typedCharsFilter = "";

    @Override
    protected boolean canFilter(JTextComponent component) {
        try {
            typedCharsFilter = Utilities.getIdentifierBefore((BaseDocument) component.getDocument(), component.getCaretPosition());
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (typedCharsFilter == null) {
            typedCharsFilter = "";
        }
        return true;
    }

    @Override
    protected void filter(CompletionResultSet resultSet) {
        resultSet.addAllItems(items.stream().filter(c -> c.getFullClassName().startsWith(typedCharsFilter) || c.getName().startsWith(typedCharsFilter)).collect(Collectors.toList()));
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
        currentMode = CompletionContext.CompletionType.COMPLETION_TYPE_ELEMENT;
        String typedChars = context.getTypedChars();
        if (typedChars == null) {
            //when is cursor at end of text context returns null
            try {
                typedChars = Utilities.getIdentifierBefore((BaseDocument) doc, caretOffset);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (typedChars == null) {
            //cursor is after <
            typedChars = "";
        }
        final String typed = typedChars;
        String elementRoot = null;
        List<QName> pathFromRoot = context.getPathFromRoot();
        if (!pathFromRoot.isEmpty()) {
            QName qname = pathFromRoot.get(pathFromRoot.size() - 1);
            elementRoot = qname.getLocalPart();
        }
        Map<String, AndroidStyleableNamespace> namespacesIn = AndroidStyleableStore.findNamespaces(primaryFile);
        Map<String, AndroidStyleableNamespace> namespaces = new HashMap<>();
        for (Map.Entry<String, String> entry : declaredNamespaces.entrySet()) {
            String name = entry.getKey();
            String nameSpace = entry.getValue();
            AndroidStyleableNamespace tmp = namespacesIn.get(nameSpace);
            if (tmp != null) {
                namespaces.put(name, tmp);
                items.addAll(tmp.getLayouts().values());
                items.addAll(tmp.getWitgets().values());
            }
        }
        if (namespaces.isEmpty()) {
            return;
        }
        startChars = typed;
        if ("".equals(typed)) {
            resultSet.addAllItems(items);
        } else {
            resultSet.addAllItems(items.stream().filter(c -> c.getFullClassName().startsWith(typed) || c.getName().startsWith(typed)).collect(Collectors.toList()));
        }
        System.out.println("org.nbandroid.netbeans.gradle.v2.layout.completion.LayoutCompletionQuery.makeElement()");
    }

}
