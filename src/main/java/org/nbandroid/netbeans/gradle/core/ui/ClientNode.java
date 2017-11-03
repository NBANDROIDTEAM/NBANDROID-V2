/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.nbandroid.netbeans.gradle.core.ui;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.Client;
import com.android.ddmlib.ClientData;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.NodeAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author tom
 */
public class ClientNode extends AbstractNode implements AndroidDebugBridge.IClientChangeListener {

    private final Client client;

    ClientNode (final Client client) {
        super (Children.LEAF, Lookups.fixed(client));
        assert client != null;
        this.client = client;
        this.setIconBaseWithExtension("org/netbeans/modules/android/core/resources/client.png");
        updateInfo ();
        AndroidDebugBridge.addClientChangeListener(this);
        this.addNodeListener(new NodeListener() {

            @Override
            public void childrenAdded(NodeMemberEvent e) {
            }

            @Override
            public void childrenRemoved(NodeMemberEvent e) {
            }

            @Override
            public void childrenReordered(NodeReorderEvent e) {
            }

            @Override
            public void nodeDestroyed(NodeEvent e) {
                AndroidDebugBridge.removeClientChangeListener(ClientNode.this);
            }

            @Override
            public void propertyChange(PropertyChangeEvent e) {
            }
        });
    }
    //where

    private void updateInfo () {
        final ClientData cd = this.client.getClientData();
        final int pid = cd.getPid();
        String name = cd.getClientDescription();
        if (name == null) {
            //Unknown, show at least pid.
            name = NbBundle.getMessage(ClientNode.class, "TXT_PID", Integer.toString(pid));
        }
        this.setDisplayName(name);
        final String desc = cd.getClientDescription();
        final String description = NbBundle.getMessage(ClientNode.class, "TIP_Client",  desc == null ? "" : desc, Integer.toString(pid));
        if (description != null) {
            this.setShortDescription(description);
        }
    }

    @Override
    public Action[] getActions(boolean context) {
        List<? extends Action> actions = Utilities.actionsForPath("UI/AndroidDevices/AndroidDeviceClients/Actions");
        return actions.toArray(new Action[actions.size()]);
    }


    @Override
    public PropertySet[] getPropertySets() {
        final Sheet.Set ps = Sheet.createPropertiesSet();
        ps.put(new PropertySupport.ReadOnly<String>(
                "PROP_ClientDescription",
                String.class,
                NbBundle.getMessage(ClientNode.class, "PROP_ClientDescription"),
                NbBundle.getMessage(ClientNode.class, "DESC_ClientDescription")) {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return client.getClientData().getClientDescription();
            }
        });
        ps.put(new PropertySupport.ReadOnly<Integer>(
                "PROP_Pid",
                Integer.class,
                NbBundle.getMessage(ClientNode.class, "PROP_Pid"),
                NbBundle.getMessage(ClientNode.class, "DESC_Pid")) {

            @Override
            public Integer getValue() throws IllegalAccessException, InvocationTargetException {
                return client.getClientData().getPid();
            }
        });
        ps.put(new PropertySupport.ReadOnly<String>(
                "PROP_VmId",
                String.class,
                NbBundle.getMessage(ClientNode.class, "PROP_VmId"),
                NbBundle.getMessage(ClientNode.class, "DESC_VmId")) {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return client.getClientData().getVmIdentifier();
            }
        });
        ps.put (new PropertySupport.ReadOnly<Integer>("PROP_Port",
                Integer.class,
                NbBundle.getMessage(ClientNode.class, "PROP_Port"),
                NbBundle.getMessage(ClientNode.class, "DESC_Port")){

            @Override
            public Integer getValue() throws IllegalAccessException, InvocationTargetException {
                return client.getDebuggerListenPort();
            }
        });
        return new PropertySet[] {
            ps
        };
    }

    @Override
    public void clientChanged(Client client, int eventType) {
        if (this.client.equals(client) && (eventType & (Client.CHANGE_INFO | Client.CHANGE_NAME)) != 0) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    updateInfo();
                    firePropertySetsChange(null, null);
                }
            });
        }
    }


    public static GCAction createGCAction() {
        return new GCAction();
    }

    public static class GCAction extends NodeAction {

        public GCAction () {

        }

        @Override
        protected void performAction(Node[] activatedNodes) {
            assert activatedNodes.length == 1;
            Client client = activatedNodes[0].getLookup().lookup(Client.class);
            assert client != null;
            client.executeGarbageCollector();
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            return activatedNodes.length == 1 && activatedNodes[0].getLookup().lookup(Client.class)!=null;
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(ClientNode.class, "ACTION_GC");
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(ClientNode.class.getName());
        }

    }

    public static KillAction createKillAction() {
        return new KillAction();
    }

    public static class KillAction extends NodeAction {

        public KillAction () {

        }

        @Override
        protected void performAction(Node[] activatedNodes) {
            assert activatedNodes.length == 1;
            Client client = activatedNodes[0].getLookup().lookup(Client.class);
            assert client != null;
            Object option = DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(ClientNode.class, "TXT_Kill",
                    Integer.toString(client.getClientData().getPid()))));
            if (option == DialogDescriptor.YES_OPTION) {
                client.kill();
            }
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            return activatedNodes.length == 1 && activatedNodes[0].getLookup().lookup(Client.class)!=null;
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(ClientNode.class, "ACTION_Kill");
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(ClientNode.class.getName());
        }

    }

}
