/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.android.project.actions;

import com.android.build.OutputFile;
import com.android.build.VariantOutput;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.ProjectBuildOutput;
import com.android.builder.model.VariantBuildOutput;
import com.google.common.collect.Lists;
import java.io.File;
import java.util.Collection;
import nbandroid.gradle.spi.GradleCommandExecutor;
import nbandroid.gradle.spi.GradleCommandTemplate;
import org.gradle.internal.impldep.org.apache.commons.lang.StringUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.project.api.AndroidConstants;
import org.netbeans.modules.android.project.api.NbAndroidProjectImpl;
import org.netbeans.modules.android.project.build.BuildVariant;
import org.netbeans.modules.android.project.launch.GradleLaunchExecutor;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.spi.project.ActionProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 *
 * @author arsi
 */
public class AndroidProjectActionProvider implements ActionProvider, LookupListener {

    private final NbAndroidProjectImpl project;
    private final Lookup.Result<AndroidProject> modelLookupResult;

    public AndroidProjectActionProvider(NbAndroidProjectImpl project) {
        this.project = project;
        modelLookupResult = project.getLookup().lookupResult(AndroidProject.class);
        modelLookupResult.addLookupListener(WeakListeners.create(LookupListener.class, this, project.getLookup()));
    }

    @Override
    public String[] getSupportedActions() {
        return new String[]{ActionProvider.COMMAND_REBUILD, ActionProvider.COMMAND_BUILD,
            AndroidConstants.COMMAND_BUILD_TEST,
            ActionProvider.COMMAND_CLEAN,
            ActionProvider.COMMAND_RUN,
            ActionProvider.COMMAND_TEST,
            ActionProvider.COMMAND_DEBUG,};
    }

    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        if (project.getLookup().lookup(AndroidProject.class) != null) {
            switch (command) {
                case ActionProvider.COMMAND_REBUILD:
                    callGradleRebuild();
                    break;
                case ActionProvider.COMMAND_BUILD:
                    callGradleBuild();
                    break;
                case ActionProvider.COMMAND_CLEAN:
                    callGradleClean();
                    break;
                case AndroidConstants.COMMAND_BUILD_TEST:
                    callAndroidTest();
                    break;
                case ActionProvider.COMMAND_RUN:
                    callLaunch(COMMAND_RUN);
                    break;
                case ActionProvider.COMMAND_DEBUG:
                    callLaunch(COMMAND_DEBUG);
                    break;
            }
        }
    }

    private void callGradleRebuild() {
        GradleCommandExecutor executor = project.getLookup().lookup(GradleCommandExecutor.class);
        if (executor != null) {
            BuildVariant buildVariant = project.getLookup().lookup(BuildVariant.class);
            GradleCommandTemplate.Builder builder = null;
            if (buildVariant != null && buildVariant.getCurrentVariant() != null) {
                String name = StringUtils.capitalize(buildVariant.getCurrentVariant().getName());
                builder = new GradleCommandTemplate.Builder("Clean and Build project (" + name + ")", Lists.newArrayList("clean", "assemble" + name));
            } else {
                builder = new GradleCommandTemplate.Builder("Clean and Build project", Lists.newArrayList("clean", "assemble"));
            }
            executor.executeCommand(builder.create());
        }
    }

    private void callGradleBuild() {
        GradleCommandExecutor executor = project.getLookup().lookup(GradleCommandExecutor.class);
        if (executor != null) {
            BuildVariant buildVariant = project.getLookup().lookup(BuildVariant.class);
            GradleCommandTemplate.Builder builder = null;
            if (buildVariant != null && buildVariant.getCurrentVariant() != null) {
                String name = StringUtils.capitalize(buildVariant.getCurrentVariant().getName());
                builder = new GradleCommandTemplate.Builder("Build project (" + name + ")", Lists.newArrayList("assemble" + name));
            } else {
                builder = new GradleCommandTemplate.Builder("Build project", Lists.newArrayList("assemble"));
            }
            executor.executeCommand(builder.create());
        }
    }

    private void callLaunch(String command) {
        GradleCommandExecutor executor = project.getLookup().lookup(GradleCommandExecutor.class);
        if (executor != null) {
            final BuildVariant buildVariant = project.getLookup().lookup(BuildVariant.class);
            GradleCommandTemplate.Builder builder = null;
            if (buildVariant != null && buildVariant.getCurrentVariant() != null) {
                String name = StringUtils.capitalize(buildVariant.getCurrentVariant().getName());
                final Lookup.Result<ProjectBuildOutput> lookupResult = project.getLookup().lookupResult(ProjectBuildOutput.class);
                //wait for model reload after build command
                lookupResult.addLookupListener(new LookupListener() {
                    @Override
                    public void resultChanged(LookupEvent ev) {
                        lookupResult.removeLookupListener(this);
                        Collection<? extends ProjectBuildOutput> allInstances = lookupResult.allInstances();
                        if (!allInstances.isEmpty()) {
                            ProjectBuildOutput buildOutput = allInstances.iterator().next();
                            Collection<VariantBuildOutput> variantsBuildOutput = buildOutput.getVariantsBuildOutput();
                            if (!variantsBuildOutput.isEmpty()) {
                                for (VariantBuildOutput vbo : variantsBuildOutput) {
                                    String variantName = vbo.getName();
                                    if ("debug".equalsIgnoreCase(variantName)) {
                                        Collection<OutputFile> outputs = vbo.getOutputs();
                                        for (OutputFile outputFile : outputs) {
                                            String outputType = outputFile.getOutputType();
                                            if (VariantOutput.MAIN.equals(outputType)) {
                                                File output = outputFile.getOutputFile();
                                                AndroidProject androidProject = project.getLookup().lookup(AndroidProject.class);
                                                if (androidProject != null) {
                                                    File manifestFile = androidProject.getDefaultConfig().getSourceProvider().getManifestFile();
                                                    GradleLaunchExecutor launchExecutor = project.getLookup().lookup(GradleLaunchExecutor.class);
                                                    if (manifestFile != null && launchExecutor != null) {
                                                        try {
                                                            launchExecutor.doLaunchAfterBuild(command, output, manifestFile);
                                                        } catch (Exception e) {
                                                            Exceptions.printStackTrace(e);
                                                        }
                                                    }
                                                }
                                                return;
                                            }

                                        }

                                    }
                                }
                            }
                        }
                        NotifyDescriptor nd = new NotifyDescriptor.Message("APK not found in returned Android model!", NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(nd);

                    }
                });
                builder = new GradleCommandTemplate.Builder("Build project (" + name + ")", Lists.newArrayList("assemble" + name));
            } else {
                NotifyDescriptor nd = new NotifyDescriptor.Message("Unable to find Build variant!", NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(nd);
            }
            executor.executeCommand(builder.create());
        }
    }

    private void callGradleClean() {
        GradleCommandExecutor executor = project.getLookup().lookup(GradleCommandExecutor.class);
        if (executor != null) {
            GradleCommandTemplate.Builder builder = new GradleCommandTemplate.Builder("Clean project", Lists.newArrayList("clean"));
            executor.executeCommand(builder.create());
        }
    }

    private void callAndroidTest() {
        GradleCommandExecutor executor = project.getLookup().lookup(GradleCommandExecutor.class);
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        if (project.getLookup().lookup(AndroidProject.class) == null) {
            return false;
        }
        return true;
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        //Refresh Build(Run) Toolbar, before model loading Actions are disabled
        //Stupid but I didn't find another solution
        Project mainProject = OpenProjectList.getDefault().getMainProject();
        if (project.equals(mainProject)) {
            OpenProjectList.getDefault().setMainProject(project);
        }
    }

}
