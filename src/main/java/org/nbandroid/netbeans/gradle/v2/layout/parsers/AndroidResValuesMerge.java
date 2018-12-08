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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author arsi
 */
public class AndroidResValuesMerge {

    /**
     * Merge two android resource xml files and skip duplicate entries from
     * appended file
     *
     * @param master master xml file
     * @param append appended xml file
     * @param out stream to write to
     * @throws SAXException
     * @throws IOException
     * @throws TransformerException
     * @throws ParserConfigurationException
     */
    public static Document merge(InputStream master, InputStream append) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        document.setXmlStandalone(true);
        List<String> masterResources = new ArrayList<>();
        Document masterDocument = documentBuilder.parse(master);
        Document appendDocument = documentBuilder.parse(append);
        Element masterElement = masterDocument.getDocumentElement();
        Element appendElement = appendDocument.getDocumentElement();
        Node resourcesNode = document.importNode(masterElement, false);
        document.appendChild(resourcesNode);
        NodeList masterChildNodes = masterElement.getChildNodes();
        for (int i = 0; i < masterChildNodes.getLength(); i++) {
            Node node = masterChildNodes.item(i);
            String nodeName = node.getNodeName();
            NamedNodeMap attributes = node.getAttributes();
            if (attributes != null) {
                Node androidNodeTypeName = attributes.getNamedItem("name");
                if (androidNodeTypeName != null) {
                    String nodeValue = androidNodeTypeName.getNodeValue();
                    masterResources.add(nodeName + ":" + nodeValue);
                    Node importNode = document.importNode(node, false);
                    importNode.setTextContent(node.getTextContent());
                    resourcesNode.appendChild(importNode);
                }
            }
        }
        NodeList appendChildNodes = appendElement.getChildNodes();
        for (int i = 0; i < appendChildNodes.getLength(); i++) {
            Node node = appendChildNodes.item(i);
            String nodeName = node.getNodeName();
            NamedNodeMap attributes = node.getAttributes();
            if (attributes != null) {
                Node androidNodeTypeName = attributes.getNamedItem("name");
                if (androidNodeTypeName != null) {
                    String nodeValue = androidNodeTypeName.getNodeValue();
                    if (!masterResources.contains(nodeName + ":" + nodeValue)) {
                        Node importNode = document.importNode(node, false);
                        importNode.setTextContent(node.getTextContent());
                        resourcesNode.appendChild(importNode);
                    }
                }
            }
        }
        return document;

    }

    public static void save(Document document, OutputStream out) throws TransformerConfigurationException, TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        Result output = new StreamResult(out);
        Source input = new DOMSource(document);
        transformer.transform(input, output);
    }

}
