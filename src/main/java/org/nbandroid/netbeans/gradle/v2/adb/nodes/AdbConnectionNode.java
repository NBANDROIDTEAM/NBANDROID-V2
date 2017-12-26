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

import com.android.ddmlib.NbAndroidAdbHelper;
import java.util.List;
import javax.swing.Action;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author arsi
 */
public class AdbConnectionNode extends AbstractNode {
    public static final String ICON_PATH = "org/netbeans/modules/android/project/resources/connection.png";
    private final String ipPort;

    public AdbConnectionNode(String ipPort) {
        super(Children.LEAF, Lookups.fixed(ipPort));
        this.ipPort = ipPort;
        setName(ipPort);
        setDisplayName(ipPort);
        setIconBaseWithExtension(ICON_PATH);
    }

    public String getIpPort() {
        return ipPort;
    }

    @Override
    public Action[] getActions(boolean context) {
        // TODO add action to restart debug bridge
        return new Action[]{SystemAction.get(ConnectAction.class),
            SystemAction.get(RemoveConnectionAction.class),        };
    }

    private static class RemoveConnectionAction extends NodeAction {

        @Override
        protected void performAction(Node[] activatedNodes) {
            if (activatedNodes.length == 1) {
                String ipport = activatedNodes[0].getLookup().lookup(String.class);
                if (ipport != null) {
                    List<String> connections = AdbConnectionsNode.getConnections();
                    connections.remove(ipport);
                    AdbConnectionsNode.saveConnections(connections);
                }
            }
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            if (activatedNodes.length != 1) {
                return false;
            }
            return true;
        }

        @Override
        public String getName() {
            return "Remove Network connection";
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }

    }

    private static class ConnectAction extends NodeAction {

        @Override
        protected void performAction(Node[] activatedNodes) {
            if (activatedNodes.length == 1) {
                String ipport = activatedNodes[0].getLookup().lookup(String.class);
                if (ipport != null) {
                    if (!NbAndroidAdbHelper.connectEthernet(ipport)) {
                        NotifyDescriptor nd = new NotifyDescriptor.Message("Unable to connect to " + ipport, NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(nd);
                    }

                }
            }
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            if (activatedNodes.length != 1) {
                return false;
            }
            return true;
        }

        @Override
        public String getName() {
            return "Connect..";
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }

    }

}
