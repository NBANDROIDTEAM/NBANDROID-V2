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

package org.nbandroid.netbeans.gradle.v2.adb.nodes;

import com.android.ddmlib.AndroidDebugBridge;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.nbandroid.netbeans.gradle.core.ddm.AndroidDebugBridgeFactory;
import org.nbandroid.netbeans.gradle.core.sdk.DalvikPlatformManager;
import org.nbandroid.netbeans.gradle.v2.adb.nodes.actions.RestartADBAction;
import org.netbeans.api.core.ide.ServicesTabNodeRegistration;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.xml.XMLUtil;

/**
 *
 * @author arsi
 */
@ServicesTabNodeRegistration(name = DevicesNode.NAME,
        displayName = "#TXT_ADB",
        shortDescription = "#HINT_ADB",
        iconResource = AdbNode.ICON_PATH, position = 430)
@NbBundle.Messages({
    "TXT_ADB=Android debug bridge",
    "HINT_ADB=Android Devices connected via ADB",
    "HINT_DevicesBroken=Android Devices are not accessible. Configure Android SDK?",})
public class AdbNode extends AbstractNode implements PropertyChangeListener {

    public static final String NAME = "Android debug bridge";
    public static final String ICON_PATH = "org/netbeans/modules/android/project/resources/android.png";
    private volatile boolean broken;
    public AdbNode() {
        super(Children.create(new ADBChildren(), true));
        final DalvikPlatformManager dpm = DalvikPlatformManager.getDefault();
        dpm.addPropertyChangeListener(WeakListeners.propertyChange(this, dpm));
        updateDescription();
        setIconBaseWithExtension(ICON_PATH);
    }

    @Override
    public Action[] getActions(boolean context) {
        // TODO add action to restart debug bridge
        return new Action[]{
            new RestartADBAction(),
            SystemAction.get(PropertiesAction.class)
        };
    }

    @Override
    public PropertySet[] getPropertySets() {
        final Sheet.Set set = Sheet.createPropertiesSet();
        set.put(new PropertySupport.ReadOnly<Boolean>(
                "PROP_DebugBridgeConnected",
                Boolean.class,
                NbBundle.getMessage(DevicesNode.class, "PROP_DebugBridgeConnected"),
                NbBundle.getMessage(DevicesNode.class, "DESC_DebugBridgeConnected")) {
            @Override
            public Boolean getValue() throws IllegalAccessException, InvocationTargetException {
                final AndroidDebugBridge jp = AndroidDebugBridgeFactory.getDefault();
                return jp == null ? Boolean.FALSE : jp.isConnected();
            }
        });
        return new PropertySet[]{
            set
        };
    }

    private void updateDescription() {
        setName(NAME);
        setDisplayName(NAME);
        final AndroidDebugBridge debugBridge = AndroidDebugBridgeFactory.getDefault();
        broken = debugBridge == null;
        String description = broken ? "Debugbridge broken! Please restart ADB!" : NAME;
        setShortDescription(description);
    }

    @Override
    public String getHtmlDisplayName() {
        String dispName = super.getDisplayName();
        try {
            dispName = XMLUtil.toElementContent(dispName);
        } catch (CharConversionException ex) {
            return dispName;
        }
        return broken ? "<font color=\"#A40000\">" + dispName + "</font>" : dispName;           //NOI18N
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (DalvikPlatformManager.PROP_INSTALLED_PLATFORMS.equals(event.getPropertyName())) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateDescription();
                    fireDisplayNameChange(null, null);
                    firePropertySetsChange(null, null);
                }
            });
        }
    }

    private static class ADBChildren extends ChildFactory<Integer> {

        @Override
        protected boolean createKeys(List<Integer> toPopulate) {
            toPopulate.add(0);
            toPopulate.add(1);
            return true;
        }

        @Override
        protected Node createNodeForKey(Integer key) {
            switch (key) {
                case 0:
                    return new DevicesNode();
                case 1:
                    return new AdbConnectionsNode();
                default:
                    return null;
            }
        }

    }

}
