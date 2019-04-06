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
package org.netbeans.modules.android.project.api.nodes;

import org.netbeans.api.annotations.common.StaticResource;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author arsi
 */
public class Nodes {

    @StaticResource
    private static final String WAIT_GIF = "org/netbeans/modules/android/project/api/nodes/wait.gif";

    @NbBundle.Messages("LBL_Wait=Please Wait...")
    public static Node createWaitNode() {
        AbstractNode an = new AbstractNode(Children.LEAF);
        an.setIconBaseWithExtension(WAIT_GIF);
        an.setDisplayName(Bundle.LBL_Wait());
        return an;
    }

}
