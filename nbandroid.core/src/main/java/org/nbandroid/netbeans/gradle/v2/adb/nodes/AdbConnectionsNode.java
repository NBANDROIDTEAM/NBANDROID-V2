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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.Action;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;
import org.openide.util.WeakListeners;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author arsi
 */
public class AdbConnectionsNode extends AbstractNode {

    public static final String ICON_PATH = "org/netbeans/modules/android/project/resources/connections.png";
    public static final String NAME = "Network connections";

    public AdbConnectionsNode() {
        super(new ConnectionsChildren());
        setIconBaseWithExtension(ICON_PATH);
        setName(NAME);
        setDisplayName(NAME);
    }

    @Override
    public Action[] getActions(boolean context) {
        // TODO add action to restart debug bridge
        return new Action[]{
            SystemAction.get(AddConnectionAction.class)
        };
    }

    public static List<String> getConnections() {
        String connectionsIn = NbPreferences.forModule(AdbConnectionsNode.class).get("CONNECTIONS", "");
        StringTokenizer tokenizer = new StringTokenizer(connectionsIn, ";", false);
        List<String> connections = new ArrayList<>();
        while (tokenizer.hasMoreElements()) {
            connections.add(tokenizer.nextToken());

        }
        return connections;
    }

    public static void saveConnections(List<String> connections) {
        String join = String.join(";", connections);
        NbPreferences.forModule(AdbConnectionsNode.class).put("CONNECTIONS", join);
    }

    private static class ConnectionsChildren extends Children.Keys<String> implements PreferenceChangeListener {

        public ConnectionsChildren() {
            NbPreferences.forModule(AdbConnectionsNode.class).addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, NbPreferences.forModule(AdbConnectionsNode.class)));
            preferenceChange(null);
        }

        @Override
        protected Node[] createNodes(String key) {
            return new Node[]{new AdbConnectionNode(key)};
        }

        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            this.setKeys(getConnections());
        }

    }

    private static class AddConnectionAction extends NodeAction {

        @Override
        protected void performAction(Node[] activatedNodes) {
            List<String> connections = getConnections();
            do {
                NotifyDescriptor.InputLine inputLine = new NotifyDescriptor.InputLine("Please enter IP:Port", "Add ADB connection", NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
                Object notify = DialogDisplayer.getDefault().notify(inputLine);
                if (NotifyDescriptor.OK_OPTION.equals(notify)) {
                    String inputText = inputLine.getInputText();
                    if (DevicesNode.validate(inputText)) {
                        connections.add(inputText);
                        saveConnections(connections);
                        return;
                    }
                } else {
                    return;
                }
            } while (true);

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
            return "Add Network connection";
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }

    }

}
