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
package org.nbandroid.netbeans.gradle.v2.layout.values;

import java.beans.FeatureDescriptor;
import java.util.Enumeration;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleable;
import org.netbeans.modules.xml.api.model.DTDUtil;
import org.netbeans.modules.xml.api.model.GrammarEnvironment;
import org.netbeans.modules.xml.api.model.GrammarQuery;
import org.netbeans.modules.xml.api.model.GrammarQueryManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Enumerations;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 *
 * @author arsi
 */
@ServiceProvider(service = GrammarQueryManager.class, path = "Plugins/XML/GrammarQueryManagers")
public class StylesGrammarQueryManager extends GrammarQueryManager {

    private static final GrammarQuery QUERY;

    static {
        QUERY = DTDUtil.parseDTD(true, new InputSource(AndroidStyleable.class.getResourceAsStream("styles.dtd")));
    }

    public StylesGrammarQueryManager() {
    }

    @Override
    public Enumeration enabled(GrammarEnvironment ctx) {
        FileObject f = ctx.getFileObject();
        if (f != null && !f.getMIMEType().equals(StylesDataObject.SETTINGS_MIME_TYPE)) {
            return null;
        }
        Enumeration en = ctx.getDocumentChildren();
        while (en.hasMoreElements()) {
            Node next = (Node) en.nextElement();
            if (next.getNodeType() == Node.ELEMENT_NODE) {
                Element root = (Element) next;
                if ("resources".equals(root.getNodeName())) { // NOI18N
                    return Enumerations.singleton(next);
                }
            }
        }
        return null;
    }

    @Override
    public FeatureDescriptor getDescriptor() {
        return new FeatureDescriptor();
    }

    @Override
    public GrammarQuery getGrammar(GrammarEnvironment env) {
        // XXX pick up env.fileObject too
        return QUERY;
    }

}
