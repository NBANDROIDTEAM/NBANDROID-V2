/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.layout.parsers;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
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
import org.nbandroid.netbeans.gradle.v2.sdk.java.platform.AndroidJavaPlatform;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
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

    private static String getLastToken(String strValue, String splitter) {
        String[] strArray = strValue.split(Pattern.quote(splitter));
        return strArray[strArray.length - 1];
    }

    public static final AndroidWidgetNamespace parseXml(AndroidJavaPlatform androidPlatform) {
        AndroidWidgetNamespace namespace = new AndroidWidgetNamespace("http://schemas.android.com/apk/res/android", androidPlatform);
        try {
            Map<String, String> superWidgetMap = new HashMap<>();
            Map<String, AndroidWidget> widgetMap = new HashMap<>();
            AndroidWidget objectWidget = new AndroidWidget(namespace, "Object", null);
            widgetMap.put("Object", objectWidget);
            FileObject platformDir = FileUtil.toFileObject(androidPlatform.getPlatformFolder());
            FileObject attrFo = platformDir.getFileObject("data/res/values/attrs.xml");
            FileObject widgetFo = platformDir.getFileObject("data/widgets.txt");
            List<String> asLines = widgetFo.asLines();
            for (String line : asLines) {
                line = line.substring(1);
                StringTokenizer tok = new StringTokenizer(line, " ", false);
                if (tok.countTokens() >= 2) {
                    String widgetName = getLastToken(tok.nextToken(), ".");
                    String superWidgetName = getLastToken(tok.nextToken(), ".");
                    superWidgetMap.put(widgetName, superWidgetName);
                }
            }
            try (FileInputStream fileIS = new FileInputStream(attrFo.getPath())) {
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                builderFactory.setIgnoringComments(false);
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                Document xmlDocument = builder.parse(fileIS);
                XPath xPath = XPathFactory.newInstance().newXPath();
                String expression = "/resources/declare-styleable";
                NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node item = nodeList.item(i);
                    NamedNodeMap attributes = item.getAttributes();
                    Node witgetNode = attributes.getNamedItem("name");
                    String witgetName = witgetNode.getTextContent();
                    NodeList attrNodes = item.getChildNodes();
                    String comment = "";
                    List<AndroidWidgetAttr> widgetAttrs = new ArrayList<>();
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
                                        widgetAttrs.add(AndroidWidgetStore.getOrAddAttr(new AndroidWidgetAttr(attrName, comment.trim(), flags, enums, attrType.toArray(new AndroidWidgetAttrType[attrType.size()]))));
                                        comment = "";
                                        break;
                                }
                                break;
                        }

                    }
                    AndroidWidget widget = new AndroidWidget(namespace, witgetName, superWidgetMap.get(witgetName));
                    widget.getAttrs().addAll(widgetAttrs);
                    widgetMap.put(witgetName, widget);
                }
            } catch (IOException | ParserConfigurationException | SAXException | XPathExpressionException ex) {
                Exceptions.printStackTrace(ex);
            }
            for (Map.Entry<String, AndroidWidget> entry : widgetMap.entrySet()) {
                AndroidWidget widget = entry.getValue();
                String superWidgetName = widget.getSuperWidgetName();
                if (superWidgetName != null) {
                    widget.setSuperWidget(widgetMap.get(superWidgetName));
                }
            }
            namespace.getWidgets().addAll(widgetMap.values());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return namespace;
    }
}
