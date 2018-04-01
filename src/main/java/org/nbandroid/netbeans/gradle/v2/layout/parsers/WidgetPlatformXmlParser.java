/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.layout.parsers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidWidget;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidWidgetAttr;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidWidgetAttrEnum;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidWidgetAttrFlag;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidWidgetAttrType;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidWidgetNamespace;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidWidgetStore;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidWidgetType;
import org.nbandroid.netbeans.gradle.v2.layout.completion.analyzer.StyleableClassFileVisitor;
import org.nbandroid.netbeans.gradle.v2.layout.completion.analyzer.StyleableResultCollector;
import org.nbandroid.netbeans.gradle.v2.sdk.java.platform.AndroidJavaPlatform;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author arsi
 */
public class WidgetPlatformXmlParser {

    /**
     * get last token from full class name
     *
     * @param strValue
     * @param splitter
     * @return
     */
    private static String getLastToken(String strValue, String splitter) {
        if (strValue != null) {
            String[] strArray = strValue.split(Pattern.quote(splitter));
            return strArray[strArray.length - 1];
        }
        return null;
    }

    /**
     * get second-last token from full class name
     *
     * @param strValue
     * @param splitter
     * @return
     */
    private static String getSecondLastToken(String strValue, String splitter) {
        if (strValue != null) {
            String[] strArray = strValue.split(Pattern.quote(splitter));
            return strArray[strArray.length - 2];
        }
        return null;
    }

    /**
     * parse Styleables from Android platform
     *
     * @param androidPlatform
     * @return
     */
    public static final AndroidWidgetNamespace parseAndroidPlatform(AndroidJavaPlatform androidPlatform) {
        AndroidWidgetNamespace namespace = new AndroidWidgetNamespace("http://schemas.android.com/apk/res/android", androidPlatform.getHashString());
        try {
            // Full widget name -> Full super class name, from data/widgets.txt
            Map<String, String> superWidgetMap = new HashMap<>();
            //Full widget name -> widget type, from data/widgets.txt
            Map<String, AndroidWidgetType> typeWidgetMap = new HashMap<>();
            // Simple widget name -> Full widget name, from data/widgets.txt
            Map<String, String> fullNameWidgetMap = new HashMap<>();
            // Simple widget name -> Widget, from xml parser
            Map<String, AndroidWidget> widgetMap = new HashMap<>();
            // Full widget name -> Widget, after widget update from from data/widgets.txt and classes
            Map<String, AndroidWidget> fullWidgetMap = new HashMap<>();
            //Create java.lang.Object Widget, as basic super class
            AndroidWidget objectWidget = new AndroidWidget(namespace, "Object");
            objectWidget.setFullClassName("java.lang.Object");
            objectWidget.setAndroidWidgetType(AndroidWidgetType.Other);
            widgetMap.put("Object", objectWidget);
            //find FileObjects
            FileObject platformDir = FileUtil.toFileObject(androidPlatform.getPlatformFolder());
            FileObject attrFo = platformDir.getFileObject("data/res/values/attrs.xml");
            FileObject widgetFo = platformDir.getFileObject("data/widgets.txt");
            FileObject androidJar = platformDir.getFileObject("android.jar");
            FileObject layoutLibJar = platformDir.getFileObject("data/layoutlib.jar");
            List<FileObject> jars = new ArrayList<>();
            jars.add(androidJar);
            jars.add(layoutLibJar);
            // Make class map from jars
            ClassScanResult scanClasses = scanClasses(jars);
            //decode data/widgets.txt
            List<String> asLines = widgetFo.asLines();
            for (String line : asLines) {
                AndroidWidgetType androidWidgetType = AndroidWidgetType.decode(line);
                line = line.substring(1);
                StringTokenizer tok = new StringTokenizer(line, " ", false);
                if (tok.countTokens() >= 2) {
                    String fullName = tok.nextToken();
                    while (tok.hasMoreElements()) {
                        String widgetName = getLastToken(fullName, ".");
                        widgetName = updateMethodName(widgetName, fullName);
                        fullNameWidgetMap.put(widgetName, fullName);
                        String superWidgetName = tok.nextToken();
                        superWidgetMap.put(fullName, superWidgetName);
                        typeWidgetMap.put(fullName, androidWidgetType);
                        fullName = superWidgetName;
                    }

                }
            }
            //parse XML file
            parseXml(attrFo, namespace, widgetMap);
            //assign full class names from data/widgets.txt
            for (Map.Entry<String, AndroidWidget> entry : widgetMap.entrySet()) {
                AndroidWidget widget = entry.getValue();
                String fullName = fullNameWidgetMap.get(widget.getName());
                if (fullName != null) {
                    widget.setFullClassName(fullName);
                    fullWidgetMap.put(fullName, widget);
                }
            }
            //assign full class names from scanned classes
            for (Map.Entry<String, AndroidWidget> entry : widgetMap.entrySet()) {
                AndroidWidget widget = entry.getValue();
                StyleableResultCollector result = scanClasses.simpleNameMap.get(widget.getName());
                if (result != null) {
                    String className = result.getClassName().replace("$", ".");
                    widget.setFullClassName(className);
                    fullWidgetMap.put(className, widget);
                    if (result.getSuperClassName() != null) { //java.lang.Object has no super class
                        superWidgetMap.put(className, result.getSuperClassName().replace("$", "."));
                    }
                }
            }
            //assign super widget and type
            for (Map.Entry<String, AndroidWidget> entry : widgetMap.entrySet()) {
                AndroidWidget widget = entry.getValue();
                if (widget.getFullClassName() != null) {
                    widget.setAndroidWidgetType(typeWidgetMap.get(widget.getFullClassName()));
                    widget.setSuperWidgetName(superWidgetMap.get(widget.getFullClassName()));
                    widget.setSuperWidget(fullWidgetMap.get(widget.getSuperWidgetName()));
                }
            }
            //assign widget type from super class, for scanned classes
            for (Map.Entry<String, AndroidWidget> entry : widgetMap.entrySet()) {
                AndroidWidget widget = entry.getValue();
                if (widget.getSuperWidget() != null && widget.getAndroidWidgetType() == AndroidWidgetType.ToBeDetermined) {
                    widget.setAndroidWidgetType(widget.getSuperWidget().getAndroidWidgetType());
                }
            }
            //sort widgets
            List<AndroidWidget> uknown = new ArrayList<>();
            Map<String, AndroidWidget> layouts = new HashMap<>();
            Map<String, AndroidWidget> layoutsSimple = new HashMap<>();
            Map<String, AndroidWidget> layoutsParams = new HashMap<>();
            Map<String, AndroidWidget> layoutsParamsSimple = new HashMap<>();
            Map<String, AndroidWidget> witgets = new HashMap<>();
            Map<String, AndroidWidget> witgetsSimple = new HashMap<>();
            for (Map.Entry<String, AndroidWidget> entry : widgetMap.entrySet()) {
                AndroidWidget widget = entry.getValue();
                String superWidgetName = widget.getSuperWidgetName();
                if (superWidgetName != null) {
                    widget.setSuperWidget(widgetMap.get(superWidgetName));
                }
                switch (widget.getAndroidWidgetType()) {

                    case Widget:
                        witgets.put(widget.getFullClassName(), widget);
                        witgetsSimple.put(widget.getName(), widget);
                        break;
                    case Layout:
                        layouts.put(widget.getFullClassName(), widget);
                        layoutsSimple.put(widget.getName(), widget);
                        break;
                    case LayoutParams:
                        layoutsParams.put(widget.getFullClassName(), widget);
                        layoutsParamsSimple.put(widget.getName(), widget);
                        break;
                    case Other:
                    case ToBeDetermined:
                        uknown.add(widget);
                        break;

                }
            }
            //store Styleables
            namespace.getAll().addAll(widgetMap.values());
            namespace.getLayouts().putAll(layouts);
            namespace.getLayoutsSimpleNames().putAll(layoutsSimple);
            namespace.getLayoutsParams().putAll(layoutsParams);
            namespace.getLayoutsParamsSimpleNames().putAll(layoutsParamsSimple);
            namespace.getWitgets().putAll(witgets);
            namespace.getWitgetsSimpleNames().putAll(witgetsSimple);
            namespace.getUknown().addAll(uknown);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return namespace;
    }

    /**
     * Parse widgets from xml, return basic widget with name and attributes
     *
     * @param attrFo FileObject xml file data/res/values/attrs.xml or
     * data/res/values/values.xml for .aar
     * @param namespace NamesPaspace
     * @param widgetMap Map to add SimpleWidgetName->Widget
     */
    public static void parseXml(FileObject attrFo, AndroidWidgetNamespace namespace, Map<String, AndroidWidget> widgetMap) {
        try (FileInputStream fileIS = new FileInputStream(attrFo.getPath())) {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setIgnoringComments(false);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document xmlDocument = builder.parse(fileIS);
            XPath xPath = XPathFactory.newInstance().newXPath();
            // Parse global ATTS, there are referenced fro widget ATTRS
            NodeList allNodeList = (NodeList) xPath.compile("/resources").evaluate(xmlDocument, XPathConstants.NODESET);
            Node rootNode = allNodeList.item(0);
            NodeList childNodes = rootNode.getChildNodes();
            List<AndroidWidgetAttr> globalWidgetAttrs = new ArrayList<>();
            decodeAttrs(childNodes, globalWidgetAttrs);
            Map<String, AndroidWidgetAttr> globalAttrsMap = new HashMap<>();
            for (AndroidWidgetAttr globalWidgetAttr : globalWidgetAttrs) {
                globalAttrsMap.put(globalWidgetAttr.getName(), globalWidgetAttr);
            }
            //
            String expression = "/resources/declare-styleable";
            NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node item = nodeList.item(i);
                NamedNodeMap attributes = item.getAttributes();
                Node witgetNode = attributes.getNamedItem("name");
                String witgetName = witgetNode.getTextContent();
                if (witgetName.contains("_") && witgetName.endsWith("Layout")) {
                    witgetName = witgetName.replace("_", ".") + "Params";
                }
                NodeList attrNodes = item.getChildNodes();
                List<AndroidWidgetAttr> widgetAttrs = new ArrayList<>();
                decodeAttrs(attrNodes, widgetAttrs);
                AndroidWidget widget = new AndroidWidget(namespace, witgetName);
                List<AndroidWidgetAttr> widgetAttrsOut = new ArrayList<>();
                for (AndroidWidgetAttr widgetAttr : widgetAttrs) {
                    if (widgetAttr.getAttrTypes().contains(AndroidWidgetAttrType.Unknown)) {
                        AndroidWidgetAttr attr = globalAttrsMap.get(widgetAttr.getName());
                        if (attr != null) {
                            if (widgetAttr.getEnums().length > 0 || widgetAttr.getFlags().length > 0) {
                                System.out.println("org.nbandroid.netbeans.gradle.v2.layout.parsers.WidgetPlatformXmlParser.parseXml()");
                            }
                            widgetAttrsOut.add(AndroidWidgetStore.getOrAddAttr(attr));
                        } else {
                            widgetAttrsOut.add(AndroidWidgetStore.getOrAddAttr(widgetAttr));
                        }
                    } else {
                        widgetAttrsOut.add(AndroidWidgetStore.getOrAddAttr(widgetAttr));
                    }
                }
                widget.getAttrs().addAll(widgetAttrsOut);
                widgetMap.put(witgetName, widget);
            }
        } catch (IOException | ParserConfigurationException | SAXException | DOMException | XPathExpressionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Return list of all class names, and super class name in jars
     *
     * @param fos
     * @return
     */
    public static final ClassScanResult scanClasses(List<FileObject> fos) {
        ClassScanResult result = new ClassScanResult();
        for (FileObject fo : fos) {
            if (fo != null && fo.isValid()) {
                Collections.list(FileUtil.getArchiveRoot(fo).getChildren(true)).stream().forEach(nextElement -> {
                    if (nextElement.hasExt("class")) {
                        try {
                            StyleableResultCollector resultCollector = StyleableClassFileVisitor.visitClass(nextElement.getName(), nextElement.getInputStream());
                            result.fullNameMap.put(resultCollector.getClassName().replace("$", "."), resultCollector);
                            String fullName = resultCollector.getClassName();
                            if (fullName.contains("$")) {
                                fullName = getLastToken(fullName, ".");
                                fullName = fullName.replace("$", ".");
                                result.simpleNameMap.put(fullName, resultCollector);
                            } else {
                                result.simpleNameMap.put(getLastToken(fullName, "."), resultCollector);
                            }

                        } catch (FileNotFoundException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            }
        }
        return result;
    }

    /**
     * Class scan result
     */
    public static final class ClassScanResult {

        public Map<String, StyleableResultCollector> simpleNameMap = new HashMap<>();
        public Map<String, StyleableResultCollector> fullNameMap = new HashMap<>();
    }

    /**
     * Convert xml name to inner class simple name
     *
     * @param widgetName
     * @param fullName
     * @return
     */
    public static String updateMethodName(String widgetName, String fullName) {
        if (widgetName.endsWith("LayoutParams")) {
            widgetName = getSecondLastToken(fullName, ".") + "." + widgetName;
        }
        return widgetName;
    }

    /**
     * Parse attibutes from xml
     *
     * @param attrNodes
     * @param widgetAttrs
     * @throws DOMException
     */
    public static void decodeAttrs(NodeList attrNodes, List<AndroidWidgetAttr> widgetAttrs) throws DOMException {
        String comment = "";
        for (int j = 0; j < attrNodes.getLength(); j++) {
            Node node = attrNodes.item(j);
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
                        case "attr":
                            Node namedAttrNode = node.getAttributes().getNamedItem("name");
                            String attrName = namedAttrNode.getTextContent();
                            Node formatAttrNode = node.getAttributes().getNamedItem("format");
                            String format = "unknown";
                            if (formatAttrNode != null) {
                                format = formatAttrNode.getTextContent();
                            }
                            List<AndroidWidgetAttrType> attrType = AndroidWidgetAttrType.decode(format);
                            List<AndroidWidgetAttrEnum> enums = new ArrayList<>();
                            List<AndroidWidgetAttrFlag> flags = new ArrayList<>();
                            if (node.hasChildNodes()) {
                                NodeList childNodes = node.getChildNodes();
                                String enumOrFlagComment = "";
                                for (int k = 0; k < childNodes.getLength(); k++) {
                                    Node enumOrFlagOrCommentNode = childNodes.item(k);
                                    switch (enumOrFlagOrCommentNode.getNodeType()) {
                                        case Node.COMMENT_NODE:
                                            if (!"".equals(enumOrFlagComment)) {
                                                enumOrFlagComment += "\n";
                                            }
                                            enumOrFlagComment += enumOrFlagOrCommentNode.getTextContent();
                                            break;
                                        case Node.ELEMENT_NODE:
                                            switch (enumOrFlagOrCommentNode.getNodeName()) {
                                                case "eat-comment":
                                                    enumOrFlagComment = "";
                                                    break;
                                                case "enum":
                                                    Node namedItem = enumOrFlagOrCommentNode.getAttributes().getNamedItem("name");
                                                    String enumName = namedItem.getTextContent();
                                                    Node namedItem1 = enumOrFlagOrCommentNode.getAttributes().getNamedItem("value");
                                                    String enumValue = namedItem1.getTextContent();
                                                    enums.add(AndroidWidgetStore.getOrAddEnum(new AndroidWidgetAttrEnum(enumName, enumValue, enumOrFlagComment)));
                                                    enumOrFlagComment = "";
                                                    break;
                                                case "flag":
                                                    Node namedItem2 = enumOrFlagOrCommentNode.getAttributes().getNamedItem("name");
                                                    String flagName = namedItem2.getTextContent();
                                                    Node namedItem3 = enumOrFlagOrCommentNode.getAttributes().getNamedItem("value");
                                                    String flagValue = namedItem3.getTextContent();
                                                    flags.add(AndroidWidgetStore.getOrAddFlag(new AndroidWidgetAttrFlag(flagName, flagValue, enumOrFlagComment)));
                                                    enumOrFlagComment = "";
                                                    break;
                                            }
                                    }
                                }

                            }
                            if (!flags.isEmpty()) {
                                //add flag to format list and remove unknown
                                if (!attrType.contains(AndroidWidgetAttrType.Flag)) {
                                    attrType.add(AndroidWidgetAttrType.Flag);
                                    attrType.remove(AndroidWidgetAttrType.Unknown);
                                }
                            }
                            if (!enums.isEmpty()) {
                                //add enum to format list and remove unknown
                                if (!attrType.contains(AndroidWidgetAttrType.Enum)) {
                                    attrType.add(AndroidWidgetAttrType.Enum);
                                    attrType.remove(AndroidWidgetAttrType.Unknown);
                                }
                            }
                            AndroidWidgetAttr widgetAttr = new AndroidWidgetAttr(attrName, comment.trim(), flags, enums, attrType.toArray(new AndroidWidgetAttrType[attrType.size()]));
                            widgetAttrs.add(widgetAttr);
                            comment = "";
                            break;
                        default:
                            comment = "";
                            break;
                    }
                    break;

            }

        }
    }
}
