/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.sdk;

import javax.swing.tree.TreeNode;

/**
 *
 * @author arsi
 */
public abstract class AbstractSdkToolNode implements TreeNode {

    /**
     * Get view type
     *
     * @return true-flat, false-full
     */
    public abstract boolean isFlatModel();

    /**
     * Set view type
     *
     * @param flatModel true-flat, false-full
     */
    public abstract void setFlatModel(boolean flatModel);
}
