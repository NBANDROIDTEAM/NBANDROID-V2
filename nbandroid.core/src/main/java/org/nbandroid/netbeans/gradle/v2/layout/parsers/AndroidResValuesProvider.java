/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.layout.parsers;

import com.android.builder.model.AndroidProject;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleableAttrType;
import org.nbandroid.netbeans.gradle.v2.layout.values.completion.AndroidValueType;
import org.nbandroid.netbeans.gradle.v2.layout.values.completion.BasicColorValuesCompletionItem;
import org.nbandroid.netbeans.gradle.v2.layout.values.completion.BasicValuesCompletionItem;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author arsi
 */
public class AndroidResValuesProvider implements FileChangeListener, LookupListener {

    private AndroidProject androidProject = null;
    private final Project nbProject;
    private final Map<FileObject, List<BasicValuesCompletionItem>> completions = new ConcurrentHashMap<>();
    private final Map<FileObject, AtomicBoolean> statuses = new ConcurrentHashMap<>();
    private final RequestProcessor RP = new RequestProcessor("RES VALUES PARSER", 1);
    private final List<BasicColorValuesCompletionItem> colors = new ArrayList<>();
    private final Lookup.Result<AndroidProject> lookupResult;

    public AndroidResValuesProvider(Project nbProject) {
        this.nbProject = nbProject;
        lookupResult = nbProject.getLookup().lookupResult(AndroidProject.class);
        lookupResult.addLookupListener(WeakListeners.create(LookupListener.class, this, lookupResult));
        initBasicColors();
    }

    private void initBasicColors() {
        colors.add(new BasicColorValuesCompletionItem(Color.BLACK, "BLACK"));
        colors.add(new BasicColorValuesCompletionItem(Color.BLUE, "BLUE"));
        colors.add(new BasicColorValuesCompletionItem(Color.CYAN, "CYAN"));
        colors.add(new BasicColorValuesCompletionItem(Color.DARK_GRAY, "DARK_GRAY"));
        colors.add(new BasicColorValuesCompletionItem(Color.GRAY, "GRAY"));
        colors.add(new BasicColorValuesCompletionItem(Color.GREEN, "GREEN"));
        colors.add(new BasicColorValuesCompletionItem(Color.LIGHT_GRAY, "LIGHT_GRAY"));
        colors.add(new BasicColorValuesCompletionItem(Color.MAGENTA, "MAGENTA"));
        colors.add(new BasicColorValuesCompletionItem(Color.ORANGE, "ORANGE"));
        colors.add(new BasicColorValuesCompletionItem(Color.PINK, "PINK"));
        colors.add(new BasicColorValuesCompletionItem(Color.RED, "RED"));
        colors.add(new BasicColorValuesCompletionItem(Color.WHITE, "WHITE"));
        colors.add(new BasicColorValuesCompletionItem(Color.YELLOW, "YELLOW"));

    }

    private List<BasicColorValuesCompletionItem> getColorsForTypedChars(String typedChars) {
        List<BasicColorValuesCompletionItem> tmp = new ArrayList<>();
        if (typedChars.startsWith("#")) {
            int length = typedChars.length();
            System.out.println(length);
            switch (length) {
                case 9:
                    tmp.add(new BasicColorValuesCompletionItem(BasicColorValuesCompletionItem.decodeAlfa(typedChars), typedChars));
                    break;
                case 7:
                    tmp.add(new BasicColorValuesCompletionItem(BasicColorValuesCompletionItem.decodeAlfa(typedChars.replace("#", "#FF")), typedChars));
                    break;
            }
        }
        return tmp;
    }

    public List<BasicValuesCompletionItem> forType(EnumSet<AndroidStyleableAttrType> attrTypes, String typedChars, javax.swing.text.Document doc, int caretOffset) {
        List<BasicValuesCompletionItem> tmp = new ArrayList<>();
        AndroidValueType type = null;
        if (attrTypes.contains(AndroidStyleableAttrType.String)) {
            type = AndroidValueType.STRING;
        } else if (attrTypes.contains(AndroidStyleableAttrType.Boolean)) {
            type = AndroidValueType.BOOL;
        } else if (attrTypes.contains(AndroidStyleableAttrType.Integer)) {
            type = AndroidValueType.INTEGER;
        } else if (attrTypes.contains(AndroidStyleableAttrType.Dimension)) {
            type = AndroidValueType.DIMEN;
        } else if (attrTypes.contains(AndroidStyleableAttrType.Fraction)) {
            type = AndroidValueType.FRACTION;
        } else if (attrTypes.contains(AndroidStyleableAttrType.Color)) {
            type = AndroidValueType.COLOR;
        } else if (attrTypes.contains(AndroidStyleableAttrType.Flag)) {
            type = AndroidValueType.FLAG;
        } else if (attrTypes.contains(AndroidStyleableAttrType.Enum)) {
            type = AndroidValueType.ENUM;
        } else if (attrTypes.contains(AndroidStyleableAttrType.Reference)) {
            Iterator<List<BasicValuesCompletionItem>> iterator = completions.values().iterator();
            while (iterator.hasNext()) {
                List<BasicValuesCompletionItem> next = iterator.next();
                tmp.addAll(next);
            }
        }
        if (type != null) {
            final AndroidValueType t = type;
            completions.forEach((fo, next) -> {
                for (BasicValuesCompletionItem completionItem : next) {
                    if (t.equals(completionItem.getType())) {
                        if (!tmp.contains(completionItem)) {
                            tmp.add(completionItem);
                        }
                    }
                }
            });
        }
        if (attrTypes.contains(AndroidStyleableAttrType.Color)) {
            type = AndroidValueType.COLOR;
            tmp.addAll(colors);
            for (BasicColorValuesCompletionItem color : colors) {
                color.setDocument(doc, caretOffset);
            }
            List<BasicColorValuesCompletionItem> colorsForTypedChars = getColorsForTypedChars(typedChars);
            for (BasicColorValuesCompletionItem color : colorsForTypedChars) {
                color.setDocument(doc, caretOffset);
            }
            tmp.addAll(colorsForTypedChars);
        }
        return tmp;
    }

    private void init() {
        for (File srcDir : androidProject.getDefaultConfig().getSourceProvider().getResDirectories()) {
            if (!srcDir.exists() || srcDir.isFile()) {
                continue;
            }
            FileObject fo = FileUtil.toFileObject(srcDir).getFileObject("values");
            if (fo != null && fo.isValid() && fo.isFolder()) {
                parseFolder(fo);
                fo.addRecursiveListener(WeakListeners.create(FileChangeListener.class, this, fo));
            }
        }
    }

    private void parseFolder(FileObject fo) {

        Enumeration<? extends FileObject> children = fo.getChildren(false);
        while (children.hasMoreElements()) {
            FileObject nextElement = children.nextElement();
            if (nextElement.hasExt("xml")) {
                parseFile(nextElement);
            }
        }
    }

    private void parseFile(FileObject nextElement) {
        List<BasicValuesCompletionItem> tmp = new ArrayList<>();
        try (FileInputStream fileIS = new FileInputStream(nextElement.getPath())) {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setIgnoringComments(false);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document xmlDocument = builder.parse(fileIS);
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList allNodeList = (NodeList) xPath.compile("/resources").evaluate(xmlDocument, XPathConstants.NODESET);
            Node rootNode = allNodeList.item(0);
            NodeList childNodes = rootNode.getChildNodes();
            decodeElements(childNodes, tmp);
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ParserConfigurationException | SAXException | XPathExpressionException ex) {
            Exceptions.printStackTrace(ex);
        }
        completions.compute(nextElement, new BiFunction<FileObject, List<BasicValuesCompletionItem>, List<BasicValuesCompletionItem>>() {
            @Override
            public List<BasicValuesCompletionItem> apply(FileObject t, List<BasicValuesCompletionItem> u) {
                return tmp;
            }
        });
        if (!statuses.containsKey(nextElement)) {
            statuses.put(nextElement, new AtomicBoolean(false));
        }
    }

    private void decodeElements(NodeList childNodes, List<BasicValuesCompletionItem> tmp) {
        String comment = "";
        for (int j = 0; j < childNodes.getLength(); j++) {
            Node node = childNodes.item(j);
            switch (node.getNodeType()) {
                case Node.COMMENT_NODE:
                    if (!"".equals(comment)) {
                        comment += "\n";
                    }
                    comment += node.getTextContent();
                    break;
                case Node.ELEMENT_NODE:
                    String nodeName = node.getNodeName();
                    switch (nodeName) {
                        case "eat-comment":
                            comment = "";
                            break;
                        case "string":
                            makeBasic(AndroidValueType.STRING, node, tmp, comment);
                            comment = "";
                            break;
                        case "integer":
                            makeBasic(AndroidValueType.INTEGER, node, tmp, comment);
                            comment = "";
                            break;
                        case "bool":
                            makeBasic(AndroidValueType.BOOL, node, tmp, comment);
                            comment = "";
                            break;
                        case "color":
                            makeBasic(AndroidValueType.COLOR, node, tmp, comment);
                            comment = "";
                            break;
                        case "dimen":
                            makeBasic(AndroidValueType.DIMEN, node, tmp, comment);
                            comment = "";
                            break;
                        case "java-symbol":
                            makeBasicWithType(AndroidValueType.SYMBOL, node, tmp, comment);
                            comment = "";
                            break;
                        case "item":
                            makeBasicWithType(AndroidValueType.ITEM, node, tmp, comment);
                            comment = "";
                            break;
                    }
            }
        }
    }

    private void makeBasic(AndroidValueType type, Node node, List<BasicValuesCompletionItem> tmp, String comment) throws DOMException {
        Node stringNode = node.getAttributes().getNamedItem("name");
        String name = stringNode.getTextContent();
        String value = node.getTextContent();
        tmp.add(BasicValuesCompletionItem.create(type, name, value, comment));
    }

    private void makeBasicWithType(AndroidValueType type, Node node, List<BasicValuesCompletionItem> tmp, String comment) throws DOMException {
        Node stringNode = node.getAttributes().getNamedItem("name");
        String name = stringNode.getTextContent();
        stringNode = node.getAttributes().getNamedItem("type");
        String nametype = stringNode.getTextContent();
        tmp.add(BasicValuesCompletionItem.create(type, name, nametype, comment));
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        if (fe.getFile().isData() && fe.getFile().hasExt("xml")) {
            parseFile(fe.getFile());
        }
    }

    @Override
    public void fileChanged(FileEvent fe) {
        ProcessFile processFile = new ProcessFile(fe.getFile());
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        completions.remove(fe.getFile());
        statuses.remove(fe.getFile());
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
    }

    public List<BasicValuesCompletionItem> forType(final AndroidValueType type) {
        List<BasicValuesCompletionItem> tmp = new ArrayList<>();
        if (type != null) {
            completions.forEach((fo, next) -> {
                for (BasicValuesCompletionItem completionItem : next) {
                    if (type.equals(completionItem.getType())) {
                        if (!tmp.contains(completionItem)) {
                            tmp.add(completionItem);
                        }
                    }
                }
            });
        }
        return tmp;
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        Collection<? extends AndroidProject> allInstances = lookupResult.allInstances();
        //TODO implement refresh on model reload
        if (!allInstances.isEmpty() && androidProject == null) {
            androidProject = allInstances.iterator().next();
            init();
        }
    }

    private class ProcessFile implements Runnable {

        private final FileObject fo;

        public ProcessFile(FileObject fo) {
            this.fo = fo;
            AtomicBoolean status = statuses.get(fo);
            if (status != null && status.compareAndSet(false, true)) {
                RP.post(this);
            }
        }

        @Override
        public void run() {
            AtomicBoolean status = statuses.get(fo);
            if (status != null) {
                status.set(false);
                parseFile(fo);
            }
        }

    }

}
