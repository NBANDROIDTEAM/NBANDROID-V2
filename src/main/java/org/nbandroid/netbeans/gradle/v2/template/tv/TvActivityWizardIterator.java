/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.nbandroid.netbeans.gradle.v2.template.tv;

import org.nbandroid.netbeans.gradle.v2.template.ProjectWizardSummaryPanel;
import android.studio.imports.templates.Parameter;
import android.studio.imports.templates.Template;
import android.studio.imports.templates.TemplateManager;
import android.studio.imports.templates.recipe.Recipe;
import android.studio.imports.templates.recipe.TemplateProcessingException;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.Variant;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import javax.xml.bind.JAXBException;
import org.nbandroid.netbeans.gradle.api.AndroidConstants;
import org.nbandroid.netbeans.gradle.api.AndroidProjects;
import org.nbandroid.netbeans.gradle.config.BuildVariant;
import org.nbandroid.netbeans.gradle.query.GradleAndroidSources;
import org.nbandroid.netbeans.gradle.v2.project.template.AndroidProjectTemplatePanelConfigureActivityAndroidSettings;
import org.nbandroid.netbeans.gradle.v2.project.template.AndroidProjectTemplatePanelTvActivityAndroidSettings;
import org.nbandroid.netbeans.gradle.v2.project.template.AndroidProjectTemplatePanelVisualAndroidSettings;
import org.nbandroid.netbeans.gradle.v2.project.template.AndroidProjectTemplatePanelVisualBasicSettings;
import static org.nbandroid.netbeans.gradle.v2.project.template.AndroidProjectTemplatePanelVisualBasicSettings.PROP_PROJECT_PACKAGE;
import static org.nbandroid.netbeans.gradle.v2.project.template.AndroidProjectTemplatePanelVisualBasicSettings.PROP_PROJECT_SDK;
import org.nbandroid.netbeans.gradle.v2.project.template.AndroidProjectTemplateWizardIterator;
import org.nbandroid.netbeans.gradle.v2.project.template.AndroidProjectTemplateWizardPanelConfigureActivityAndroidSettings;
import org.nbandroid.netbeans.gradle.v2.project.template.AndroidProjectTemplateWizardPanelTvActivityAndroidSettings;
import org.nbandroid.netbeans.gradle.v2.project.template.freemarker.ProjectTemplateLoader;
import org.nbandroid.netbeans.gradle.v2.project.template.parameters.Globals;
import org.nbandroid.netbeans.gradle.v2.project.template.parameters.TemplateValueInjector;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidPlatformInfo;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdk;
import org.nbandroid.netbeans.gradle.v2.template.mobile.*;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.gradle.project.NbGradleProject;
import org.netbeans.modules.project.ui.NewFileWizard;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

// TODO define position attribute
@TemplateRegistration(category = {"android-activity"}, id = "TvTemplate.java", position = 100, folder = "Android", displayName = "#TvActivityWizardIterator_displayName", description = "tvActivity.html", iconBase = "org/netbeans/modules/android/project/ui/resources/tv.png")
@Messages("TvActivityWizardIterator_displayName=TV Activity")
public final class TvActivityWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {

    private int index;

    private WizardDescriptor wizard;
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;
    public static final String MANIFEST_ROOT_FOLDER = "MANIFEST_ROOT_FOLDER";
    public static final String BUILD_VARIANT = "BUILD_VARIANT";
    public static final String BUILD_TOOL_VERSION = "BUILD_TOOL_VERSION";
    public static final String ERROR_NO_ANDROID = "ERROR_NO_ANDROID";
    private final WizardDescriptor.Panel panel_tv = new AndroidProjectTemplateWizardPanelTvActivityAndroidSettings();
    private final WizardDescriptor.Panel panel_tv_config = new AndroidProjectTemplateWizardPanelConfigureActivityAndroidSettings(AndroidProjectTemplatePanelTvActivityAndroidSettings.PROP_TV_TEMPLATE, AndroidProjectTemplatePanelConfigureActivityAndroidSettings.PROP_TV_ACTIVITY_PARAMETERS);
    private List<String> steps;

    @Override
    public Set<?> instantiate() throws IOException {
        Set<FileObject> resultSet = new LinkedHashSet<>();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(BUILD_VARIANT, wizard.getProperty(BUILD_VARIANT));
        String mobileFolder = (String) wizard.getProperty(AndroidProjectTemplatePanelVisualAndroidSettings.PROP_TV_FOLDER);
        Template mobileTemplate = (Template) wizard.getProperty(AndroidProjectTemplatePanelTvActivityAndroidSettings.PROP_TV_TEMPLATE);
        Map<Parameter, Object> userValues = (Map<Parameter, Object>) wizard.getProperty(AndroidProjectTemplatePanelConfigureActivityAndroidSettings.PROP_TV_ACTIVITY_PARAMETERS);
        if (mobileTemplate != null && userValues != null) {
            mobileTemplate.getMetadata().configureParameters(parameters);
            TemplateValueInjector.setupNewModule(parameters, wizard, AndroidProjectTemplatePanelVisualAndroidSettings.PROP_TV_PLATFORM, false);
            TemplateValueInjector.setupModuleRoots(parameters, wizard, mobileFolder);
            for (Map.Entry<Parameter, Object> entry : userValues.entrySet()) {
                Parameter parameter = entry.getKey();
                Object value = entry.getValue();
                parameters.put(parameter.id, value);
            }

            List<FileObject> toOpen = processTemplate(mobileTemplate, parameters);
            if (toOpen != null) {
                resultSet.addAll(toOpen);
            }
        }

        return resultSet;
    }

    public List<FileObject> processTemplate(Template projectTemplate, Map<String, Object> parameters) {
        String executeFileName = projectTemplate.getMetadata().getExecute();
        String globalsFileName = projectTemplate.getMetadata().getGlobals();
        FileObject rootFolder = TemplateManager.getRootFolder();
        String path = projectTemplate.getFileObject().getParent().getPath();
        path = TemplateManager.getJarPath(path);
        String builGradle = (String) parameters.get("projectOut") + File.separator + "build.gradle";
        ProjectTemplateLoader loader = ProjectTemplateLoader.setupRecipeExecutor(builGradle, rootFolder, path, parameters);
        String globalsParsed = loader.process(path + "/" + globalsFileName, parameters);
        if (globalsParsed != null) {
            Scanner scanner = new Scanner(globalsParsed);
            StringBuilder builder = new StringBuilder();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!line.contains("<?xml") && !line.contains("<globals") && !line.contains("</globals")) {
                    builder.append(line).append("\n");
                }
            }
            builder.insert(0, "<globals>\n");
            builder.insert(0, "<?xml version=\"1.0\"?>\n");
            builder.append("</globals>");
            scanner.close();
            globalsParsed = builder.toString();
        }
        try {
            Globals globals = Globals.parse(new StringReader(globalsParsed));
            globals.addParameters(parameters);

        } catch (JAXBException ex) {
            Exceptions.printStackTrace(ex);
        }

        String process = loader.process(path + "/" + executeFileName, parameters);
        try {
            Recipe recipe = Recipe.parse(new StringReader(process));
            recipe.execute(loader);
            return loader.getToOpen();
        } catch (JAXBException ex) {
            Exceptions.printStackTrace(ex);
        } catch (TemplateProcessingException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        steps = createSteps();
        wizard.putProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, 0);
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_*:
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps.toArray(new String[steps.size()]));
            }
        }
        NewFileWizard fileWizard = (NewFileWizard) wizard;
        try {
            Project project = (Project) wizard.getProperty("project");
            if (AndroidProjects.isAndroidMavenProject(project) && (project instanceof NbGradleProject)) {
                NbGradleProject gradleProject = (NbGradleProject) project;
                File projectDirectory = gradleProject.getProjectDirectoryAsFile();
                DataFolder targetFolder = fileWizard.getTargetFolder();
                String targetPath = targetFolder.getPrimaryFile().getPath();
                BuildVariant buildVariant = project.getLookup().lookup(BuildVariant.class);
                GradleAndroidSources androidSources = project.getLookup().lookup(GradleAndroidSources.class);
                SourceGroup[] sourceGroups = androidSources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                FileObject srcRootFolder = null;
                for (SourceGroup sourceGroup : sourceGroups) {
                    if (sourceGroup.contains(targetFolder.getPrimaryFile())) {
                        srcRootFolder = sourceGroup.getRootFolder();
                    }
                }
                sourceGroups = androidSources.getSourceGroups(AndroidConstants.ANDROID_MANIFEST_XML);
                FileObject manifestRootFolder = null;
                if (sourceGroups.length == 1) {
                    manifestRootFolder = sourceGroups[0].getRootFolder();
                }
                wizard.putProperty(MANIFEST_ROOT_FOLDER, FileUtil.toFile(manifestRootFolder));
                Variant variant = buildVariant.getCurrentVariant();
                wizard.putProperty(BUILD_VARIANT, variant);
                if (srcRootFolder != null) {
                    String packageName = targetPath.replace(srcRootFolder.getPath(), "");
                    packageName = packageName.replace("/", ".").replace("\\", ".");
                    packageName = packageName.substring(1);
                    wizard.putProperty(PROP_PROJECT_PACKAGE, packageName);
                }
                AndroidPlatformInfo projectPlatform = AndroidProjects.projectPlatform(project);
                if (projectPlatform != null) {
                    AndroidSdk sdk = projectPlatform.getSdk();
                    wizard.putProperty(PROP_PROJECT_SDK, sdk);
                    wizard.putProperty(AndroidProjectTemplatePanelVisualAndroidSettings.PROP_TV_PLATFORM, projectPlatform);
                    wizard.putProperty(AndroidProjectTemplatePanelVisualAndroidSettings.PROP_TV_ENABLED, true);
                }
                Project rootProject = AndroidProjects.findRootProject(project.getProjectDirectory(), project);
                if (rootProject != null) {
                    wizard.putProperty(AndroidProjectTemplatePanelVisualBasicSettings.PROP_PROJECT_DIR, FileUtil.toFile(rootProject.getProjectDirectory()));
                    String folderName = project.getProjectDirectory().getPath().replace(rootProject.getProjectDirectory().getPath(), "").substring(1);
                    if (!folderName.contains("/") && !folderName.contains("\\")) {
                        wizard.putProperty(AndroidProjectTemplatePanelVisualAndroidSettings.PROP_TV_FOLDER, folderName);
                    }
                }
                AndroidProject aPrj = project.getLookup().lookup(AndroidProject.class);
                if (aPrj != null) {
                    String buildToolsVersion = aPrj.getBuildToolsVersion();
                    wizard.putProperty(BUILD_TOOL_VERSION, buildToolsVersion);
                }
                wizard.putProperty(ERROR_NO_ANDROID, false);
            } else {
                wizard.putProperty(ERROR_NO_ANDROID, true);
            }

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels.get(index);
    }

    @Override
    public String name() {
        return MessageFormat.format("{0} of {1}",
                new Object[]{new Integer(index + 1), new Integer(panels.size())});
    }

    @Override
    public boolean hasNext() {
        return index < panels.size() - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }
    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then use
    // ChangeSupport to implement add/removeChangeListener and call fireChange
    // when needed

    // You could safely ignore this method. Is is here to keep steps which were
    // there before this wizard was instantiated. It should be better handled
    // by NetBeans Wizard API itself rather than needed to be implemented by a
    // client code.
    private List<String> createSteps() {
        List<String> tmp = new ArrayList<>();
        tmp.add("Choose File Type");
        tmp.add("Android project summary");
        tmp.add(AndroidProjectTemplateWizardIterator.TEXT_TV);
        tmp.add(AndroidProjectTemplateWizardIterator.TEXT_TV_CONFIG);
        return tmp;
    }

    private List<WizardDescriptor.Panel<WizardDescriptor>> createPanels() {
        List<WizardDescriptor.Panel<WizardDescriptor>> tmp = new ArrayList<>();
        tmp.add(new ProjectWizardSummaryPanel(ProjectWizardSummaryPanel.Type.TV));
        tmp.add(panel_tv);
        tmp.add(panel_tv_config);
        return tmp;
    }

}
