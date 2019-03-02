package org.netbeans.modules.android.spi;

import com.android.builder.model.AndroidProject;
import org.nbandroid.netbeans.gradle.api.AndroidClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.ProjectOpenedHook;

public final class GradleProjectOpenedHook extends ProjectOpenedHook implements AndroidModelAware {

    private final Project project;

    public GradleProjectOpenedHook(Project project) {
        this.project = project;
    }

    private AndroidClassPath classpath() {
        return project.getLookup().lookup(AndroidClassPath.class);
    }

    @Override
    protected void projectOpened() {
        AndroidClassPath cp = classpath();
        if (cp != null) {
            cp.register();
        }
    }

    @Override
    public void projectClosed() {
        AndroidClassPath cp = classpath();
        if (cp != null) {
            cp.unregister();
        }
    }

    @Override
    public void setAndroidProject(AndroidProject aPrj) {
        AndroidClassPath cp = classpath();
        if (cp != null) {
            cp.register();
        }

    }

}
