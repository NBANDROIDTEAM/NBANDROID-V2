package org.nbandroid.netbeans.gradle;

import com.android.builder.model.AndroidProject;

/**
 *
 * @author radim
 */
public interface AndroidModelAware {

    /**
     * Called to update the object with new version of AndroidProject.
     */
    void setAndroidProject(AndroidProject aPrj);
}
