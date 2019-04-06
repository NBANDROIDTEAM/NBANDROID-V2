/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.android.project.properties;

import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.util.Mutex;
import org.openide.xml.XMLUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author arsi
 */
public class AndroidProjectPropsImpl implements AuxiliaryProperties {

    private static final String NAMESPACE = "http://www.netbeans.org/ns/android-properties-data/1"; //NOI18N
    private static final String ROOT = "properties"; //NOI18N
    private Mutex mutex;
    private static final Logger LOG = Logger.getLogger(AndroidProjectPropsImpl.class.getName());

    private final AuxiliaryConfiguration aux;

    public AndroidProjectPropsImpl(AuxiliaryConfiguration aux) {
        this.aux = aux;
    }

    private AuxiliaryConfiguration getAuxConf() {
        return aux;
    }

    @Override
    public String get(String key, boolean shared) {
        return getMutex().readAccess((Mutex.Action<String>) () -> {
            TreeMap<String, String> props = readProperties(getAuxConf(), shared);
            //TODO optimize
            String ret = props.get(key);
            if (ret != null) {
                return ret;
            }
            return null;
        });
    }

    @Override
    public void put(String key, String value, boolean shared) {
        getMutex().writeAccess((Mutex.Action<Void>) () -> {
            writeAuxiliaryData(getAuxConf(), key, value, shared);
            return null;
        });
    }

    @Override
    public Iterable<String> listKeys(boolean shared) {
        return getMutex().readAccess((Mutex.Action<Iterable<String>>) () -> {
            TreeMap<String, String> props = readProperties(getAuxConf(), shared);
            return props.keySet();
        });
    }

    private void writeAuxiliaryData(AuxiliaryConfiguration conf, String property, String value, boolean shared) {
        Element el = getOrCreateRootElement(conf, shared);
        Element enEl;
        NodeList list = el.getElementsByTagNameNS(NAMESPACE, property);
        if (list.getLength() > 0) {
            enEl = (Element) list.item(0);
        } else {
            try {
                enEl = el.getOwnerDocument().createElementNS(NAMESPACE, property);
            } catch (DOMException x) {
                LOG.log(Level.WARNING, "#200901: {0} from {1}", new Object[]{x.getMessage(), property});
                return;
            }
            el.appendChild(enEl);
        }
        if (value != null) {
            enEl.setTextContent(value);
        } else {
            el.removeChild(enEl);
        }
        if (el.getElementsByTagNameNS(NAMESPACE, "*").getLength() > 0) {
            conf.putConfigurationFragment(el, shared);
        } else {
            conf.removeConfigurationFragment(ROOT, NAMESPACE, shared);
        }
    }

    public static void writeAuxiliaryData(AuxiliaryConfiguration conf, TreeMap<String, String> props, boolean shared) {
        Element el = getOrCreateRootElement(conf, shared);
        Element enEl;
        for (String key : props.keySet()) {
            NodeList list = el.getElementsByTagNameNS(NAMESPACE, key);
            if (list.getLength() > 0) {
                enEl = (Element) list.item(0);
            } else {
                try {
                    enEl = el.getOwnerDocument().createElementNS(NAMESPACE, key);
                } catch (DOMException x) {
                    LOG.log(Level.WARNING, "#200901: {0} from {1}", new Object[]{x.getMessage(), key});
                    continue;
                }
                el.appendChild(enEl);
            }
            String value = props.get(key);
            if (value != null) {
                enEl.setTextContent(value);
            } else {
                el.removeChild(enEl);
            }
        }
        if (el.getElementsByTagNameNS(NAMESPACE, "*").getLength() > 0) {
            conf.putConfigurationFragment(el, shared);
        } else {
            conf.removeConfigurationFragment(ROOT, NAMESPACE, shared);
        }
    }

    private static Element getOrCreateRootElement(AuxiliaryConfiguration conf, boolean shared) {
        Element el = conf.getConfigurationFragment(ROOT, NAMESPACE, shared);
        if (el == null) {
            el = XMLUtil.createDocument(ROOT, NAMESPACE, null, null).getDocumentElement();
        }
        return el;
    }

    private TreeMap<String, String> readProperties(AuxiliaryConfiguration aux, boolean shared) {
        return getMutex().readAccess((Mutex.Action<TreeMap<String, String>>) () -> {
            TreeMap<String, String> props = new TreeMap<>();
            Element el = aux.getConfigurationFragment(ROOT, NAMESPACE, shared);
            if (el != null) {
                NodeList list = el.getChildNodes();
                if (list.getLength() > 0) {
                    for (int i = 0; i < list.getLength(); i++) {
                        Object nd = list.item(i);
                        if (nd instanceof Element) {
                            Element enEl = (Element) nd;
                            props.put(enEl.getNodeName(), enEl.getTextContent());
                        }
                    }
                }
            }
            return props;
        });
    }

    private synchronized Mutex getMutex() {
        if (mutex == null) {
            mutex = new Mutex();
        }
        return mutex;
    }

}
