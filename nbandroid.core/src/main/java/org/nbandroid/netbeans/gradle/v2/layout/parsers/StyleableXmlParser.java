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
import org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleable;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleableAttr;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleableAttrEnum;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleableAttrFlag;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleableAttrType;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleableNamespace;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleableStore;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleableType;
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
public class StyleableXmlParser {

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
    public static String getSecondLastToken(String strValue, String splitter) {
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
    public static final AndroidStyleableNamespace parseAndroidPlatform(AndroidJavaPlatform androidPlatform) {
        AndroidStyleableNamespace namespace = new AndroidStyleableNamespace(AndroidStyleableStore.ANDROID_NAMESPACE, androidPlatform.getHashString());
        try {
            // Full styleable name -> Full super class name, from data/widgets.txt
            Map<String, String> superStyleableMap = new HashMap<>();
            //Full styleable name -> styleable type, from data/widgetstxt
            Map<String, AndroidStyleableType> typeStyleableMap = new HashMap<>();
            // Simple styleable name -> Full styleable name, from data/widgets.txt
            Map<String, String> fullNameStyleableMap = new HashMap<>();
            // Simple styleable name -> Styleable, from xml parser
            Map<String, AndroidStyleable> styleableMap = new HashMap<>();
            // Full styleable name -> Styleable, after styleable update from from data/widgets.txt and classes
            Map<String, AndroidStyleable> fullStyleableMap = new HashMap<>();
            //Create java.lang.Object Styleable, as basic super class
            AndroidStyleable objectStyleable = new AndroidStyleable(namespace, "Object");
            objectStyleable.setFullClassName("java.lang.Object");
            objectStyleable.setAndroidStyleableType(AndroidStyleableType.Other);
            styleableMap.put("Object", objectStyleable);
            fullStyleableMap.put("java.lang.Object", objectStyleable);
            namespace.getOther().put("java.lang.Object", objectStyleable);
            namespace.getOtherSimpleNames().put("Object", objectStyleable);
            //find FileObjects
            FileObject platformDir = FileUtil.toFileObject(androidPlatform.getPlatformFolder());
            FileObject attrFo = platformDir.getFileObject("data/res/values/attrs.xml");
            FileObject styleableFo = platformDir.getFileObject("data/widgets.txt");
            FileObject androidJar = platformDir.getFileObject("android.jar");
            FileObject layoutLibJar = platformDir.getFileObject("data/layoutlib.jar");
            List<FileObject> jars = new ArrayList<>();
            jars.add(androidJar);
            jars.add(layoutLibJar);
            // Make class map from jars
            ClassScanResult scanClasses = scanClasses(jars);
            //decode data/widgets.txt
            List<String> asLines = styleableFo.asLines();
            for (String line : asLines) {
                AndroidStyleableType androidStyleableType = AndroidStyleableType.decode(line);
                line = line.substring(1);
                StringTokenizer tok = new StringTokenizer(line, " ", false);
                if (tok.countTokens() >= 2) {
                    String fullName = tok.nextToken();
                    while (tok.hasMoreElements()) {
                        String styleableName = getLastToken(fullName, ".");
                        styleableName = updateMethodName(styleableName, fullName);
                        fullNameStyleableMap.put(styleableName, fullName);
                        String superStyleableName = tok.nextToken();
                        superStyleableMap.put(fullName, superStyleableName);
                        typeStyleableMap.put(fullName, androidStyleableType);
                        fullName = superStyleableName;
                    }

                }
            }
            //parse XML file
            parseXml(attrFo, namespace, styleableMap);
            //assign full class names from data/widgets.txt
            for (Map.Entry<String, AndroidStyleable> entry : styleableMap.entrySet()) {
                AndroidStyleable styleable = entry.getValue();
                String fullName = fullNameStyleableMap.get(styleable.getName());
                if (fullName != null) {
                    styleable.setFullClassName(fullName);
                    fullStyleableMap.put(fullName, styleable);
                }
            }
            //assign full class names from scanned classes
            for (Map.Entry<String, AndroidStyleable> entry : styleableMap.entrySet()) {
                AndroidStyleable styleable = entry.getValue();
                StyleableResultCollector result = scanClasses.simpleNameMap.get(styleable.getName());
                if (result != null) {
                    String className = result.getClassName().replace("$", ".");
                    styleable.setFullClassName(className);
                    fullStyleableMap.put(className, styleable);
                    if (result.getSuperClassName() != null) { //java.lang.Object has no super class
                        superStyleableMap.put(className, result.getSuperClassName().replace("$", "."));
                    }
                }
            }
            //assign super styleable and type
            for (Map.Entry<String, AndroidStyleable> entry : styleableMap.entrySet()) {
                AndroidStyleable styleable = entry.getValue();
                if (styleable.getFullClassName() != null && !"java.lang.Object".equals(styleable.getFullClassName())) {
                    styleable.setAndroidStyleableType(typeStyleableMap.get(styleable.getFullClassName()));
                    styleable.setSuperStyleableName(superStyleableMap.get(styleable.getFullClassName()));
                    styleable.setSuperStyleable(fullStyleableMap.get(styleable.getSuperStyleableName()));
                }
            }
            //assign styleable type from super class, for scanned classes
            for (Map.Entry<String, AndroidStyleable> entry : styleableMap.entrySet()) {
                AndroidStyleable styleable = entry.getValue();
                if (styleable.getSuperStyleable() != null && styleable.getAndroidStyleableType() == AndroidStyleableType.ToBeDetermined) {
                    styleable.setAndroidStyleableType(styleable.getSuperStyleable().getAndroidStyleableType());
                }
            }
            //sort styleables
            List<AndroidStyleable> uknown = new ArrayList<>();
            List<AndroidStyleable> todo = new ArrayList<>();
            Map<String, AndroidStyleable> layouts = new HashMap<>();
            Map<String, AndroidStyleable> layoutsSimple = new HashMap<>();
            Map<String, AndroidStyleable> layoutsParams = new HashMap<>();
            Map<String, AndroidStyleable> layoutsParamsSimple = new HashMap<>();
            Map<String, AndroidStyleable> witgets = new HashMap<>();
            Map<String, AndroidStyleable> witgetsSimple = new HashMap<>();
            for (Map.Entry<String, AndroidStyleable> entry : styleableMap.entrySet()) {
                AndroidStyleable styleable = entry.getValue();
                String superStyleableName = styleable.getSuperStyleableName();
                if (superStyleableName != null) {
                    styleable.setSuperStyleable(styleableMap.get(getSimpleClassName(superStyleableName)));
                }
                switch (styleable.getAndroidStyleableType()) {

                    case Widget:
                        witgets.put(styleable.getFullClassName(), styleable);
                        witgetsSimple.put(styleable.getName(), styleable);
                        break;
                    case Layout:
                        layouts.put(styleable.getFullClassName(), styleable);
                        layoutsSimple.put(styleable.getName(), styleable);
                        break;
                    case LayoutParams:
                        layoutsParams.put(styleable.getFullClassName(), styleable);
                        layoutsParamsSimple.put(styleable.getName(), styleable);
                        break;
                    case Other:
                        break;
                    case ToBeDetermined:
                        if (styleable.getFullClassName() != null) {
                            todo.add(styleable);
                        } else {
                            if (!"java.lang.Object".equals(styleable.getFullClassName())) {
                                uknown.add(styleable);
                            }
                        }
                        break;

                }
                if (scanClasses.fullNameMap.get(styleable.getFullClassName()) != null) {
                    styleable.setClassFileURL(scanClasses.fullNameMap.get(styleable.getFullClassName()).getFileUrl());
                }
            }
            //store Styleables
            namespace.getAll().addAll(styleableMap.values());
            namespace.getLayouts().putAll(layouts);
            namespace.getLayoutsSimpleNames().putAll(layoutsSimple);
            namespace.getLayoutsParams().putAll(layoutsParams);
            namespace.getLayoutsParamsSimpleNames().putAll(layoutsParamsSimple);
            namespace.getWitgets().putAll(witgets);
            namespace.getWitgetsSimpleNames().putAll(witgetsSimple);
            namespace.getUknown().addAll(uknown);
            namespace.getTodo().addAll(todo);
            namespace.getFullClassNameMap().putAll(scanClasses.fullNameMap);
            for (Map.Entry<String, AndroidStyleable> entry : layoutsParams.entrySet()) {
                AndroidStyleable styleable = entry.getValue();
                styleable.createLayuotIfNotExist(fullStyleableMap, namespace.getLayouts(), namespace.getLayoutsSimpleNames());

            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return namespace;
    }

    /**
     * Parse styleables from xml, return basic styleable with name and
     * attributes
     *
     * @param attrFo FileObject xml file data/res/values/attrs.xml or
     * data/res/values/values.xml for .aar
     * @param namespace NamesPaspace
     * @param styleableMap Map to add SimpleStyleableName->Styleable
     */
    public static void parseXml(FileObject attrFo, AndroidStyleableNamespace namespace, Map<String, AndroidStyleable> styleableMap) {
        try (FileInputStream fileIS = new FileInputStream(attrFo.getPath())) {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setIgnoringComments(false);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document xmlDocument = builder.parse(fileIS);
            XPath xPath = XPathFactory.newInstance().newXPath();
            // Parse global ATTS, there are referenced fro styleable ATTRS
            NodeList allNodeList = (NodeList) xPath.compile("/resources").evaluate(xmlDocument, XPathConstants.NODESET);
            Node rootNode = allNodeList.item(0);
            NodeList childNodes = rootNode.getChildNodes();
            List<AndroidStyleableAttr> globalStyleableAttrs = new ArrayList<>();
            decodeAttrs(childNodes, globalStyleableAttrs);
            Map<String, AndroidStyleableAttr> globalAttrsMap = new HashMap<>();
            for (AndroidStyleableAttr globalStyleableAttr : globalStyleableAttrs) {
                globalAttrsMap.put(globalStyleableAttr.getName(), AndroidStyleableStore.getOrAddAttr(globalStyleableAttr));
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
                List<AndroidStyleableAttr> styleableAttrs = new ArrayList<>();
                decodeAttrs(attrNodes, styleableAttrs);
                AndroidStyleable styleable = new AndroidStyleable(namespace, witgetName);
                List<AndroidStyleableAttr> styleableAttrsOut = new ArrayList<>();
                for (AndroidStyleableAttr styleableAttr : styleableAttrs) {
                    if (styleableAttr.getAttrTypes().contains(AndroidStyleableAttrType.Unknown)) {
                        AndroidStyleableAttr attr = globalAttrsMap.get(styleableAttr.getName());
                        if (attr != null) {
                            styleableAttrsOut.add(AndroidStyleableStore.getOrAddAttr(attr));
                        } else {
                            styleableAttrsOut.add(AndroidStyleableStore.findOrAddAttr(styleableAttr));
                        }
                    } else {
                        styleableAttrsOut.add(AndroidStyleableStore.getOrAddAttr(styleableAttr));
                    }
                }
                styleable.getAttrs().addAll(styleableAttrsOut);
                styleableMap.put(witgetName, styleable);
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
                            StyleableResultCollector resultCollector = StyleableClassFileVisitor.visitClass(nextElement.getName(), nextElement.getInputStream(), nextElement.toURL());
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
     * Parse Styleables from exploded .aar
     *
     * @param namespace
     * @param platformNamespace
     * @param jarFo
     * @param xmlFo
     * @return true if OK
     */
    public static boolean parseAar(AndroidStyleableNamespace namespace, AndroidStyleableNamespace platformNamespace, FileObject jarFo, FileObject xmlFo) {
        Map<String, AndroidStyleable> styleableMap = new HashMap<>();
        parseXml(xmlFo, namespace, styleableMap);
        if (styleableMap.isEmpty()) {
            return false;
        }
        List<FileObject> jars = new ArrayList<>();
        jars.add(FileUtil.getArchiveFile(jarFo));
        ClassScanResult scanClasses = scanClasses(jars);
        //assign full class name and super full class name
        Map<String, AndroidStyleable> fullNameMap = new HashMap<>();
        for (Map.Entry<String, AndroidStyleable> entry : styleableMap.entrySet()) {
            AndroidStyleable styleable = entry.getValue();
            StyleableResultCollector result = scanClasses.simpleNameMap.get(styleable.getName());
            if (result != null) {
                styleable.setFullClassName(result.getClassName().replace("$", "."));
                styleable.setSuperStyleableName(result.getSuperClassName().replace("$", "."));
                fullNameMap.put(result.getClassName().replace("$", "."), styleable);
            }
        }
        //add Styleables from platform to fullNameMap
        fullNameMap.putAll(platformNamespace.getLayouts());
        fullNameMap.putAll(platformNamespace.getLayoutsParams());
        fullNameMap.putAll(platformNamespace.getWitgets());
        //assign super Styleables and type
        for (Map.Entry<String, AndroidStyleable> entry : styleableMap.entrySet()) {
            AndroidStyleable styleable = entry.getValue();
            AndroidStyleable superStyleable = fullNameMap.get(styleable.getSuperStyleableName());
            if (superStyleable != null) {
                styleable.setSuperStyleable(superStyleable);
                if (superStyleable.getAndroidStyleableType() != AndroidStyleableType.Other && superStyleable.getAndroidStyleableType() != AndroidStyleableType.ToBeDetermined) {
                    styleable.setAndroidStyleableType(superStyleable.getAndroidStyleableType());
                }
            }
        }
        //assign type from super
        for (Map.Entry<String, AndroidStyleable> entry : styleableMap.entrySet()) {
            AndroidStyleable styleable = entry.getValue();
            if (styleable.getSuperStyleable() != null && (styleable.getAndroidStyleableType() == AndroidStyleableType.ToBeDetermined || styleable.getAndroidStyleableType() == AndroidStyleableType.Other)) {
                if (styleable.getSuperStyleable().getAndroidStyleableType() != AndroidStyleableType.Other && styleable.getSuperStyleable().getAndroidStyleableType() != AndroidStyleableType.ToBeDetermined) {
                    styleable.setAndroidStyleableType(styleable.getSuperStyleable().getAndroidStyleableType());
                }
            }
        }
        //sort styleables
        List<AndroidStyleable> uknown = new ArrayList<>();
        List<AndroidStyleable> todo = new ArrayList<>();
        Map<String, AndroidStyleable> layouts = new HashMap<>();
        Map<String, AndroidStyleable> layoutsSimple = new HashMap<>();
        Map<String, AndroidStyleable> layoutsParams = new HashMap<>();
        Map<String, AndroidStyleable> layoutsParamsSimple = new HashMap<>();
        Map<String, AndroidStyleable> witgets = new HashMap<>();
        Map<String, AndroidStyleable> witgetsSimple = new HashMap<>();
        for (Map.Entry<String, AndroidStyleable> entry : styleableMap.entrySet()) {
            AndroidStyleable styleable = entry.getValue();
            String superStyleableName = styleable.getSuperStyleableName();
            if (superStyleableName != null && styleable.getSuperStyleable() == null) {
                styleable.setSuperStyleable(styleableMap.get(superStyleableName));
            }
            switch (styleable.getAndroidStyleableType()) {

                case Widget:
                    witgets.put(styleable.getFullClassName(), styleable);
                    witgetsSimple.put(styleable.getName(), styleable);
                    break;
                case Layout:
                    layouts.put(styleable.getFullClassName(), styleable);
                    layoutsSimple.put(styleable.getName(), styleable);
                    break;
                case LayoutParams:
                    layoutsParams.put(styleable.getFullClassName(), styleable);
                    layoutsParamsSimple.put(styleable.getName(), styleable);
                    break;
                case Other:
                    break;
                case ToBeDetermined:
                    if (styleable.getFullClassName() != null) {
                        todo.add(styleable);
                    } else {
                        uknown.add(styleable);
                    }
                    break;

            }
            if (scanClasses.fullNameMap.get(styleable.getFullClassName()) != null) {
                styleable.setClassFileURL(scanClasses.fullNameMap.get(styleable.getFullClassName()).getFileUrl());
            }
        }
        //store Styleables
        namespace.getAll().addAll(styleableMap.values());
        namespace.getLayouts().putAll(layouts);
        namespace.getLayoutsSimpleNames().putAll(layoutsSimple);
        namespace.getLayoutsParams().putAll(layoutsParams);
        namespace.getLayoutsParamsSimpleNames().putAll(layoutsParamsSimple);
        namespace.getWitgets().putAll(witgets);
        namespace.getWitgetsSimpleNames().putAll(witgetsSimple);
        namespace.getUknown().addAll(uknown);
        namespace.getTodo().addAll(todo);
        namespace.getFullClassNameMap().putAll(scanClasses.fullNameMap);
        for (Map.Entry<String, AndroidStyleable> entry : layoutsParams.entrySet()) {
            AndroidStyleable styleable = entry.getValue();
            styleable.createLayuotIfNotExist(fullNameMap, namespace.getLayouts(), namespace.getLayoutsSimpleNames());

        }
        return !layouts.isEmpty() || !layoutsParams.isEmpty() || !witgets.isEmpty();
    }

    private static String getSimpleClassName(String superStyleableName) {
        if (superStyleableName.contains(".")) {
            return superStyleableName.substring(superStyleableName.lastIndexOf('.') + 1);
        } else {
            return superStyleableName;
        }
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
     * @param styleableName
     * @param fullName
     * @return
     */
    public static String updateMethodName(String styleableName, String fullName) {
        if (styleableName.endsWith("LayoutParams")) {
            styleableName = getSecondLastToken(fullName, ".") + "." + styleableName;
        }
        return styleableName;
    }

    /**
     * Parse attibutes from xml
     *
     * @param attrNodes
     * @param styleableAttrs
     * @throws DOMException
     */
    public static void decodeAttrs(NodeList attrNodes, List<AndroidStyleableAttr> styleableAttrs) throws DOMException {
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
                            List<AndroidStyleableAttrType> attrType = AndroidStyleableAttrType.decode(format);
                            List<AndroidStyleableAttrEnum> enums = new ArrayList<>();
                            List<AndroidStyleableAttrFlag> flags = new ArrayList<>();
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
                                                    enums.add(AndroidStyleableStore.getOrAddEnum(new AndroidStyleableAttrEnum(enumName, enumValue, enumOrFlagComment)));
                                                    enumOrFlagComment = "";
                                                    break;
                                                case "flag":
                                                    Node namedItem2 = enumOrFlagOrCommentNode.getAttributes().getNamedItem("name");
                                                    String flagName = namedItem2.getTextContent();
                                                    Node namedItem3 = enumOrFlagOrCommentNode.getAttributes().getNamedItem("value");
                                                    String flagValue = namedItem3.getTextContent();
                                                    flags.add(AndroidStyleableStore.getOrAddFlag(new AndroidStyleableAttrFlag(flagName, flagValue, enumOrFlagComment)));
                                                    enumOrFlagComment = "";
                                                    break;
                                            }
                                    }
                                }

                            }
                            if (!flags.isEmpty()) {
                                //add flag to format list and remove unknown
                                if (!attrType.contains(AndroidStyleableAttrType.Flag)) {
                                    attrType.add(AndroidStyleableAttrType.Flag);
                                    attrType.remove(AndroidStyleableAttrType.Unknown);
                                }
                            }
                            if (!enums.isEmpty()) {
                                //add enum to format list and remove unknown
                                if (!attrType.contains(AndroidStyleableAttrType.Enum)) {
                                    attrType.add(AndroidStyleableAttrType.Enum);
                                    attrType.remove(AndroidStyleableAttrType.Unknown);
                                }
                            }
                            AndroidStyleableAttr styleableAttr = new AndroidStyleableAttr(attrName, comment.trim(), flags, enums, attrType.toArray(new AndroidStyleableAttrType[attrType.size()]));
                            styleableAttrs.add(styleableAttr);
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
