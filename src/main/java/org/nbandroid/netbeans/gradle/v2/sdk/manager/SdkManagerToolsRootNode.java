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

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import javax.swing.tree.TreeNode;
import org.nbandroid.netbeans.gradle.v2.sdk.AbstractSdkToolNode;

/**
 * Root Node of Sdk Tools packages
 *
 * @author arsi
 */
public class SdkManagerToolsRootNode implements TreeNode {

    private final Vector<AbstractSdkToolNode> nodes = new Vector<>();
    private boolean flatModel = true;

    public SdkManagerToolsRootNode(List<AbstractSdkToolNode> node) {
        this.nodes.addAll(node);
    }

    public SdkManagerToolsRootNode() {
    }

    public void addNode(AbstractSdkToolNode node) {
        nodes.add(node);
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
        return nodes.elementAt(childIndex);
    }

    @Override
    public int getChildCount() {
        return nodes.size();
    }

    @Override
    public TreeNode getParent() {
        return null;
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
        return nodes.elements();
    }

    /**
     * Get view type
     *
     * @return true-flat, false-full
     */
    public boolean isFlatModel() {
        return flatModel;
    }

    /**
     * Set view type
     *
     * @param flatModel true-flat, false-full
     */
    public void setFlatModel(boolean flatModel) {
        this.flatModel = flatModel;
        for (AbstractSdkToolNode node : nodes) {
            node.setFlatModel(flatModel);
        }
    }

}
