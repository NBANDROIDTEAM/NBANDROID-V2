/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.query;

import com.android.builder.model.AndroidProject;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;

/**
 *
 * @author arsi
 */
public class SourceLevelQueryImpl2 implements SourceLevelQueryImplementation2 {

    private final PropertyEvaluator properties;
    private AndroidProject project = null;

    public SourceLevelQueryImpl2(PropertyEvaluator properties) {
        this.properties = properties;
    }

    public SourceLevelQueryImpl2() {
        this.properties = null;
    }

    public void setProject(AndroidProject project) {
        this.project = project;
    }


    @Override
    public Result getSourceLevel(final FileObject fo) {
        return new SourceLevelQueryImplementation2.Result2() {
            @Override
            public SourceLevelQuery.Profile getProfile() {
                return SourceLevelQuery.Profile.DEFAULT;
            }

            @Override
            public String getSourceLevel() {
                if (properties != null) {
                    String source = properties.getProperty("java.source");
                    return source != null ? source : "1.5";
                } else if (project != null && fo.toString().contains("/src/main")) {
                    String sourceCompatibility = project.getJavaCompileOptions().getSourceCompatibility();
                    return project.getJavaCompileOptions().getSourceCompatibility();
                }
                return "1.7";
            }

            @Override
            public void addChangeListener(ChangeListener cl) {
            }

            @Override
            public void removeChangeListener(ChangeListener cl) {
            }
        };
    }

}
