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
package org.nbandroid.netbeans.gradle.api.ui;

import java.awt.Image;
import java.beans.BeanInfo;
import javax.swing.Icon;
import javax.swing.UIManager;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * @author radim
 */
public class UiUtils {

    private static final String ICON_KEY_UIMANAGER = "Tree.closedIcon"; // NOI18N
    private static final String OPENED_ICON_KEY_UIMANAGER = "Tree.openIcon"; // NOI18N
    private static final String ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.icon"; // NOI18N
    private static final String OPENED_ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.openedIcon"; // NOI18N
    public static final Image TEST_BADGE = ImageUtilities.loadImage(
            "org/netbeans/modules/android/project/ui/resources/test_badge.png", true);
    public static final Image LIBRARIES_BADGE = ImageUtilities.loadImage(
            "org/netbeans/modules/android/project/ui/resources/libraries-badge.png");    //NOI18N

    /**
     * Returns default folder icon as {@link java.awt.Image}. Never returns
     * <code>null</code>.
     *
     * @param opened whether closed or opened icon should be returned.
     */
    public static Image getTreeFolderIcon(boolean opened) {
        Image base = (Image) UIManager.get(opened ? OPENED_ICON_KEY_UIMANAGER_NB : ICON_KEY_UIMANAGER_NB); // #70263;
        if (base == null) {
            Icon baseIcon = UIManager.getIcon(opened ? OPENED_ICON_KEY_UIMANAGER : ICON_KEY_UIMANAGER); // #70263
            if (baseIcon != null) {
                base = ImageUtilities.icon2Image(baseIcon);
            } else {
                try {
                    // fallback to our owns
                    return opened
                            ? DataObject.find(FileUtil.getConfigRoot()).getNodeDelegate().getOpenedIcon(BeanInfo.ICON_COLOR_16x16)
                            : DataObject.find(FileUtil.getConfigRoot()).getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        assert base != null;
        return base;
    }
}
