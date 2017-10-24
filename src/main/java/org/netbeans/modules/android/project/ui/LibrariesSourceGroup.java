/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netbeans.modules.android.project.ui;

import java.beans.PropertyChangeListener;
import java.net.URL;
import javax.swing.Icon;
import org.nbandroid.netbeans.gradle.query.GradleAndroidClassPathProvider;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

/**
 * LibrariesSourceGroup {@link SourceGroup} implementation passed to
 * {@link org.netbeans.spi.java.project.support.ui.PackageView#createPackageView(SourceGroup)}
 */
public final class LibrariesSourceGroup implements SourceGroup {

    private final FileObject root;
    private final String displayName;
    private final Icon icon;
    private final Icon openIcon;

    /**
     * Creates new LibrariesSourceGroup
     *
     * @param root the classpath root
     * @param displayName the display name presented to user
     * @param icon closed icon
     * @param openIcon opened icon
     */
    public LibrariesSourceGroup(FileObject root, String displayName, Icon icon, Icon openIcon, final Project owner) {
        assert root != null;
        this.root = root;
        this.icon = icon;
        this.openIcon = openIcon;

        if (owner != null) {
            GradleAndroidClassPathProvider classPathProvider = owner.getLookup().lookup(GradleAndroidClassPathProvider.class);
            if (classPathProvider != null) {
                URL url = root.toURL();
                this.displayName = classPathProvider.getLibName(url, displayName);
            } else {
                this.displayName = displayName;
            }
        } else {
            this.displayName = displayName;
        }
        System.out.println("org.netbeans.modules.android.project.ui.LibrariesSourceGroup.<init>()");
    }

    @Override
    public FileObject getRootFolder() {
        return this.root;
    }

    @Override
    public String getName() {
        try {
            return root.getURL().toExternalForm();
        } catch (FileStateInvalidException fsi) {
            ErrorManager.getDefault().notify(fsi);
            return root.toString();
        }
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public Icon getIcon(boolean opened) {
        return opened ? openIcon : icon;
    }

    @Override
    public boolean contains(FileObject file) throws IllegalArgumentException {
        return root.equals(file) || FileUtil.isParentOf(root, file);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof LibrariesSourceGroup)) {
            return false;
        }
        LibrariesSourceGroup osg = (LibrariesSourceGroup) other;
        return displayName == null ? osg.displayName == null : displayName.equals(osg.displayName)
                && root == null ? osg.root == null : root.equals(osg.root);
    }

    @Override
    public int hashCode() {
        return ((displayName == null ? 0 : displayName.hashCode()) << 16) | ((root == null ? 0 : root.hashCode()) & 0xffff);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        //Not needed
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        //Not needed
    }
}
