package org.nbandroid.netbeans.gradle;

import org.gradle.tooling.model.gradle.GradleBuild;

/**
 *
 * @author radim
 */
public interface GradleBuildAware {

    /**
     * Called to update the object with new version of BasicGradleProject.
     */
    void setGradleBuild(GradleBuild build);
}
