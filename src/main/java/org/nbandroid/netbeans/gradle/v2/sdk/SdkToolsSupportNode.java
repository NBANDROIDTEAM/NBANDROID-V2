/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.sdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.TreeNode;

/**
 *
 * @author arsi
 */
public class SdkToolsSupportNode extends AbstractSdkToolNode {

    private final TreeNode paren;
    private final List<AbstractSdkToolNode> nodes = new ArrayList<>();

    public SdkToolsSupportNode(TreeNode parent) {
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
