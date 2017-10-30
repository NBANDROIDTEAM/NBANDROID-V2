/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.sdk;

import com.android.repository.Revision;
import com.android.repository.api.RepoPackage;
import com.android.repository.api.UpdatablePackage;
import java.util.Enumeration;
import javax.swing.tree.TreeNode;

/**
 *
 * @author arsi
 */
public class SdkToolsPackageNode extends AbstractSdkToolNode {

    private final TreeNode parent;
    private final UpdatablePackage pkg;
    private boolean flatModel = true;

    public SdkToolsPackageNode(TreeNode parent, UpdatablePackage pkg) {
        this.parent = parent;
        this.pkg = pkg;
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getChildCount() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TreeNode getParent() {
        return parent;
    }

    @Override
    public int getIndex(TreeNode node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean getAllowsChildren() {
        return false;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public Enumeration children() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public UpdatablePackage getPackage() {
        return pkg;
    }

    @Override
    public boolean isFlatModel() {
        return flatModel;
    }

    @Override
    public void setFlatModel(boolean flatModel) {
        this.flatModel = flatModel;
    }

    @Override
    public String toString() {
        if (parent instanceof SdkToolsMultiPackageNode) {
            RepoPackage representative = pkg.getRepresentative();
            String name = representative.getDisplayName();
            String suffix = representative.getPath().substring(representative.getPath().lastIndexOf(RepoPackage.PATH_SEPARATOR) + 1);
            String shortRevision;
            try {
                shortRevision = Revision.parseRevision(suffix).toShortString();
            } catch (NumberFormatException ignore) {
                shortRevision = null;
            }
            if (representative.getDisplayName().endsWith(suffix)
                    || (shortRevision != null && representative.getDisplayName().endsWith(shortRevision))) {
                name = suffix;
            }

            if (pkg.getRepresentative().obsolete()) {
                name += " (Obsolete)";
            }
            return name;
        } else {
            return pkg.getRepresentative().getDisplayName();
        }
    }

}
