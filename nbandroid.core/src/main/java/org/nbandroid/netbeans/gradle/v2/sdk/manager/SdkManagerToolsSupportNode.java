/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.nbandroid.netbeans.gradle.v2.sdk.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.TreeNode;
import org.nbandroid.netbeans.gradle.v2.sdk.AbstractSdkToolNode;

/**
 * SDK Tools Root node of Support packages
 *
 * @author arsi
 */
public class SdkManagerToolsSupportNode extends AbstractSdkToolNode {

    private final TreeNode paren;
    private final List<AbstractSdkToolNode> nodes = new ArrayList<>();

    public SdkManagerToolsSupportNode(TreeNode parent) {
        this.paren = parent;
    }

    public void addNode(AbstractSdkToolNode node) {
        nodes.add(node);
        node.setFlatModel(false);
    }

    @Override
    public String toString() {
        return "Support repository"; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
        return nodes.get(childIndex);
    }

    @Override
    public int getChildCount() {
        return nodes.size();
    }

    @Override
    public TreeNode getParent() {
        return paren;
    }

    @Override
    public int getIndex(TreeNode node) {
        return nodes.indexOf(node);
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public boolean isLeaf() {
        return nodes.isEmpty();
    }

    @Override
    public Enumeration children() {
        return Collections.enumeration(nodes);
    }

    public List<AbstractSdkToolNode> getNodes() {
        return nodes;
    }

    @Override
    public boolean isFlatModel() {
        return false;
    }

    @Override
    public void setFlatModel(boolean flatModel) {
    }

}
