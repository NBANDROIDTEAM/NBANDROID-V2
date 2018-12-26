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
package org.nbandroid.netbeans.gradle.v2.project.template;

import android.studio.imports.templates.Parameter;
import android.studio.imports.templates.Template;
import android.studio.imports.templates.TemplateManager;
import android.studio.imports.templates.TemplateMetadata;
import static android.studio.imports.templates.TemplateMetadata.ATTR_BUILD_API;
import static android.studio.imports.templates.TemplateMetadata.ATTR_BUILD_API_STRING;
import static android.studio.imports.templates.TemplateMetadata.ATTR_TARGET_API;
import static android.studio.imports.templates.TemplateMetadata.ATTR_TARGET_API_STRING;
import android.studio.imports.templates.recipe.Recipe;
import android.studio.imports.templates.recipe.TemplateProcessingException;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import javax.xml.bind.JAXBException;
import static org.nbandroid.netbeans.gradle.v2.project.template.AndroidProjectTemplatePanelMobileActivityAndroidSettings.PROP_MOBILE_CONFIG;
import static org.nbandroid.netbeans.gradle.v2.project.template.AndroidProjectTemplatePanelVisualAndroidSettings.PROP_PHONE_TABLET_FOLDER;
import static org.nbandroid.netbeans.gradle.v2.project.template.AndroidProjectTemplatePanelVisualAndroidSettings.PROP_PLATFORM_CONFIG_DONE;
import static org.nbandroid.netbeans.gradle.v2.project.template.AndroidProjectTemplatePanelVisualAndroidSettings.PROP_TV_FOLDER;
import static org.nbandroid.netbeans.gradle.v2.project.template.AndroidProjectTemplatePanelVisualAndroidSettings.PROP_WEAR_FOLDER;
import org.nbandroid.netbeans.gradle.v2.project.template.freemarker.ProjectTemplateLoader;
import org.nbandroid.netbeans.gradle.v2.project.template.parameters.Globals;
import org.nbandroid.netbeans.gradle.v2.project.template.parameters.TemplateValueInjector;
import org.netbeans.api.templates.TemplateRegistration;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

// TODO define position attribute
@TemplateRegistration(folder = "Project/Android", displayName = "#AndroidProjectTemplate_displayName", description = "AndroidProjectTemplateDescription.html", iconBase = "org/netbeans/modules/android/project/ui/resources/androidProject.png", content = "AndroidProjectTemplateProject.zip")
@Messages("AndroidProjectTemplate_displayName=Android Project")
public class AndroidProjectTemplateWizardIterator implements WizardDescriptor./*Progress*/InstantiatingIterator, PropertyChangeListener {

    private int index;
    private List<WizardDescriptor.Panel> panels;
    private WizardDescriptor wiz;

    private final WizardDescriptor.Panel panel_project = new AndroidProjectTemplateWizardPanellBasicSettings();
    private final WizardDescriptor.Panel panel_sdk = new AndroidProjectTemplateWizardPanellVisualAndroidSettings();
    private final WizardDescriptor.Panel panel_mobile = new AndroidProjectTemplateWizardPanelMobileActivityAndroidSettings();
    private final WizardDescriptor.Panel panel_mobile_config = new AndroidProjectTemplateWizardPanelConfigureActivityAndroidSettings(AndroidProjectTemplatePanelMobileActivityAndroidSettings.PROP_MOBILE_TEMPLATE, AndroidProjectTemplatePanelConfigureActivityAndroidSettings.PROP_MOBILE_ACTIVITY_PARAMETERS);
    private final WizardDescriptor.Panel panel_wear = new AndroidProjectTemplateWizardPanelWearActivityAndroidSettings();
    private final WizardDescriptor.Panel panel_wear_config = new AndroidProjectTemplateWizardPanelConfigureActivityAndroidSettings(AndroidProjectTemplatePanelWearActivityAndroidSettings.PROP_WEAR_TEMPLATE, AndroidProjectTemplatePanelConfigureActivityAndroidSettings.PROP_WEAR_ACTIVITY_PARAMETERS);
    private final WizardDescriptor.Panel panel_tv = new AndroidProjectTemplateWizardPanelTvActivityAndroidSettings();
    private final WizardDescriptor.Panel panel_tv_config = new AndroidProjectTemplateWizardPanelConfigureActivityAndroidSettings(AndroidProjectTemplatePanelTvActivityAndroidSettings.PROP_TV_TEMPLATE, AndroidProjectTemplatePanelConfigureActivityAndroidSettings.PROP_TV_ACTIVITY_PARAMETERS);

    private final String text_project = NbBundle.getMessage(AndroidProjectTemplateWizardIterator.class, "LBL_CreateProjectStep");
    private final String text_sdk = "Android platform";
    public static final String TEXT_MOBILE = "Mobile Activity";
    public static final String TEXT_MOBILE_CONFIG = "Configure Mobile Activity";
    private final String text_wear = "Wear Activity";
    private final String text_wear_config = "Configure Wear Activity";
    private final String text_tv = "TV Activity";
    private final String text_tv_config = "Configure TV Activity";
    private List<String> steps;

    public AndroidProjectTemplateWizardIterator() {
    }

    public static AndroidProjectTemplateWizardIterator createIterator() {
        return new AndroidProjectTemplateWizardIterator();
    }

    private List< WizardDescriptor.Panel> createPanels() {
        List< WizardDescriptor.Panel> tmp = new ArrayList<>();
        tmp.add(panel_project);
        tmp.add(panel_sdk);
        tmp.add(panel_mobile);
        tmp.add(panel_mobile_config);
        tmp.add(panel_wear);
        tmp.add(panel_wear_config);
        tmp.add(panel_tv);
        tmp.add(panel_tv_config);
        return tmp;
    }

    private List<String> createSteps() {
        List<String> tmp = new ArrayList<>();
        tmp.add(text_project);
        tmp.add(text_sdk);
        tmp.add(TEXT_MOBILE);
        tmp.add(TEXT_MOBILE_CONFIG);
        tmp.add(text_wear);
        tmp.add(text_wear_config);
        tmp.add(text_tv);
        tmp.add(text_tv_config);
        return tmp;
    }

    @Override
    public Set/*<FileObject>*/ instantiate(/*ProgressHandle handle*/) throws IOException {
        Set<FileObject> resultSet = new LinkedHashSet<>();
        File projectRoot = (File) wiz.getProperty(AndroidProjectTemplatePanelVisualBasicSettings.PROP_PROJECT_DIR);
        FileObject dir = FileUtil.toFileObject(projectRoot);
        resultSet.add(dir);
        Template projectTemplate = TemplateManager.findProjectTemplate("Android Project");
        Map<String, Object> parameters = new HashMap<>();
        TemplateValueInjector.setupNewModule(parameters, wiz, AndroidProjectTemplatePanelVisualAndroidSettings.PROP_PHONE_TABLET_PLATFORM, true);
        TemplateValueInjector.setupModuleRoots(parameters, wiz, "");
        //basic project structure
        if (projectTemplate != null) {
            List<FileObject> toOpen = processTemplate(projectTemplate, parameters);
            if (toOpen != null) {
                resultSet.addAll(toOpen);
            }
        }
        String mobileFolder = (String) wiz.getProperty(PROP_PHONE_TABLET_FOLDER);
        String wearFolder = (String) wiz.getProperty(PROP_WEAR_FOLDER);
        String tvFolder = (String) wiz.getProperty(PROP_TV_FOLDER);

        //android mobile
        boolean androidMobile = (boolean) wiz.getProperty(AndroidProjectTemplatePanelVisualAndroidSettings.PROP_PHONE_TABLET_ENABLED);
        if (androidMobile) {
            Template mobileTemplate = TemplateManager.findProjectTemplate("Android Module");
            if (mobileTemplate != null) {
                mobileTemplate.getMetadata().configureParameters(parameters);
                TemplateValueInjector.setupNewModule(parameters, wiz, AndroidProjectTemplatePanelVisualAndroidSettings.PROP_PHONE_TABLET_PLATFORM, true);
                TemplateValueInjector.setupModuleRoots(parameters, wiz, mobileFolder);
                parameters.put("unitTestsSupported", false);
                List<FileObject> toOpen = processTemplate(mobileTemplate, parameters);
                if (toOpen != null) {
                    resultSet.addAll(toOpen);
                }
                String moduleRoot = (String) parameters.get(TemplateMetadata.ATTR_PROJECT_OUT);
                FileObject fo = FileUtil.toFileObject(new File(moduleRoot));
                if (fo != null) {
                    resultSet.add(fo);
                }
            }
        }
        // Mobile activity
        Template mobileTemplate = (Template) wiz.getProperty(AndroidProjectTemplatePanelMobileActivityAndroidSettings.PROP_MOBILE_TEMPLATE);
        Map<Parameter, Object> userValues = (Map<Parameter, Object>) wiz.getProperty(AndroidProjectTemplatePanelConfigureActivityAndroidSettings.PROP_MOBILE_ACTIVITY_PARAMETERS);
        if (mobileTemplate != null && userValues != null) {
            mobileTemplate.getMetadata().configureParameters(parameters);
            TemplateValueInjector.setupNewModule(parameters, wiz, AndroidProjectTemplatePanelVisualAndroidSettings.PROP_PHONE_TABLET_PLATFORM, true);
            TemplateValueInjector.setupModuleRoots(parameters, wiz, mobileFolder);
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
        //android wear
        boolean androidWear = (boolean) wiz.getProperty(AndroidProjectTemplatePanelVisualAndroidSettings.PROP_WEAR_ENABLED);
        if (androidWear) {
            Template wearTemplate = TemplateManager.findProjectTemplate("Wear OS Module");
            if (wearTemplate != null) {
                wearTemplate.getMetadata().configureParameters(parameters);
                TemplateValueInjector.setupNewModule(parameters, wiz, AndroidProjectTemplatePanelVisualAndroidSettings.PROP_WEAR_PLATFORM, true);
                parameters.put(ATTR_BUILD_API, 28);
                parameters.put(ATTR_BUILD_API_STRING, String.valueOf(28));
                parameters.put(ATTR_TARGET_API, 28);
                parameters.put(ATTR_TARGET_API_STRING, 28);
                TemplateValueInjector.setupModuleRoots(parameters, wiz, wearFolder);
                parameters.put("unitTestsSupported", false);
                parameters.put(TemplateMetadata.ATTR_CREATE_ACTIVITY, wiz.getProperty(AndroidProjectTemplatePanelWearActivityAndroidSettings.PROP_WEAR_TEMPLATE) != null);
                List<FileObject> toOpen = processTemplate(wearTemplate, parameters);
                if (toOpen != null) {
                    resultSet.addAll(toOpen);
                }
                String moduleRoot = (String) parameters.get(TemplateMetadata.ATTR_PROJECT_OUT);
                FileObject fo = FileUtil.toFileObject(new File(moduleRoot));
                if (fo != null) {
                    resultSet.add(fo);
                }
            }
        }

        // Wear activity
        Template wearTemplate = (Template) wiz.getProperty(AndroidProjectTemplatePanelWearActivityAndroidSettings.PROP_WEAR_TEMPLATE);
        userValues = (Map<Parameter, Object>) wiz.getProperty(AndroidProjectTemplatePanelConfigureActivityAndroidSettings.PROP_WEAR_ACTIVITY_PARAMETERS);
        if (wearTemplate != null && userValues != null) {
            wearTemplate.getMetadata().configureParameters(parameters);
            TemplateValueInjector.setupNewModule(parameters, wiz, AndroidProjectTemplatePanelVisualAndroidSettings.PROP_WEAR_PLATFORM, true);
            TemplateValueInjector.setupModuleRoots(parameters, wiz, wearFolder);
            for (Map.Entry<Parameter, Object> entry : userValues.entrySet()) {
                Parameter parameter = entry.getKey();
                Object value = entry.getValue();
                parameters.put(parameter.id, value);
            }

            List<FileObject> toOpen = processTemplate(wearTemplate, parameters);
            if (toOpen != null) {
                resultSet.addAll(toOpen);
            }
        }

        //android tvFolder
        boolean androidTV = (boolean) wiz.getProperty(AndroidProjectTemplatePanelVisualAndroidSettings.PROP_TV_ENABLED);
        if (androidTV) {
            Template tvTemplate = TemplateManager.findProjectTemplate("Android TV Module");
            if (tvTemplate != null) {
                tvTemplate.getMetadata().configureParameters(parameters);
                TemplateValueInjector.setupNewModule(parameters, wiz, AndroidProjectTemplatePanelVisualAndroidSettings.PROP_TV_PLATFORM, true);
                TemplateValueInjector.setupModuleRoots(parameters, wiz, tvFolder);
                parameters.put("unitTestsSupported", false);
                parameters.put(TemplateMetadata.ATTR_CREATE_ACTIVITY, wiz.getProperty(AndroidProjectTemplatePanelTvActivityAndroidSettings.PROP_TV_TEMPLATE) != null);
                List<FileObject> toOpen = processTemplate(tvTemplate, parameters);
                if (toOpen != null) {
                    resultSet.addAll(toOpen);
                }
                String moduleRoot = (String) parameters.get(TemplateMetadata.ATTR_PROJECT_OUT);
                FileObject fo = FileUtil.toFileObject(new File(moduleRoot));
                if (fo != null) {
                    resultSet.add(fo);
                }
            }
        }

        // tvFolder activity
        Template tvActTemplate = (Template) wiz.getProperty(AndroidProjectTemplatePanelTvActivityAndroidSettings.PROP_TV_TEMPLATE);
        userValues = (Map<Parameter, Object>) wiz.getProperty(AndroidProjectTemplatePanelConfigureActivityAndroidSettings.PROP_TV_ACTIVITY_PARAMETERS);
        if (tvActTemplate != null && userValues != null) {
            tvActTemplate.getMetadata().configureParameters(parameters);
            TemplateValueInjector.setupNewModule(parameters, wiz, AndroidProjectTemplatePanelVisualAndroidSettings.PROP_TV_PLATFORM, true);
            TemplateValueInjector.setupModuleRoots(parameters, wiz, tvFolder);
            for (Map.Entry<Parameter, Object> entry : userValues.entrySet()) {
                Parameter parameter = entry.getKey();
                Object value = entry.getValue();
                parameters.put(parameter.id, value);
            }

            List<FileObject> toOpen = processTemplate(tvActTemplate, parameters);
            if (toOpen != null) {
                resultSet.addAll(toOpen);
            }
        }

        //  throw new IndexOutOfBoundsException("test");
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

    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        steps = createSteps();

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
        wiz.addPropertyChangeListener(this);
    }

    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty("projdir", null);
        this.wiz.putProperty("name", null);
        this.wiz = null;
        panels = null;
    }

    public String name() {
        return MessageFormat.format("{0} of {1}",
                new Object[]{new Integer(index + 1), new Integer(panels.size())});
    }

    public boolean hasNext() {
        return index < panels.size() - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    public WizardDescriptor.Panel current() {
        return panels.get(index);
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {
    }

    public final void removeChangeListener(ChangeListener l) {
    }

    private static void unZipFile(InputStream source, FileObject projectRoot) throws IOException {
        try {
            ZipInputStream str = new ZipInputStream(source);
            ZipEntry entry;
            while ((entry = str.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder(projectRoot, entry.getName());
                } else {
                    FileObject fo = FileUtil.createData(projectRoot, entry.getName());
                    if ("nbproject/project.xml".equals(entry.getName())) {
                        // Special handling for setting name of Ant-based projects; customize as needed:
                        filterProjectXML(fo, str, projectRoot.getName());
                    } else {
                        writeFile(str, fo);
                    }
                }
            }
        } finally {
            source.close();
        }
    }

    private static void writeFile(ZipInputStream str, FileObject fo) throws IOException {
        OutputStream out = fo.getOutputStream();
        try {
            FileUtil.copy(str, out);
        } finally {
            out.close();
        }
    }

    private static void filterProjectXML(FileObject fo, ZipInputStream str, String name) throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileUtil.copy(str, baos);
            Document doc = XMLUtil.parse(new InputSource(new ByteArrayInputStream(baos.toByteArray())), false, false, null, null);
            NodeList nl = doc.getDocumentElement().getElementsByTagName("name");
            if (nl != null) {
                for (int i = 0; i < nl.getLength(); i++) {
                    Element el = (Element) nl.item(i);
                    if (el.getParentNode() != null && "data".equals(el.getParentNode().getNodeName())) {
                        NodeList nl2 = el.getChildNodes();
                        if (nl2.getLength() > 0) {
                            nl2.item(0).setNodeValue(name);
                        }
                        break;
                    }
                }
            }
            OutputStream out = fo.getOutputStream();
            try {
                XMLUtil.write(doc, out, "UTF-8");
            } finally {
                out.close();
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            writeFile(str, fo);
        }

    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        if (PROP_PLATFORM_CONFIG_DONE.equals(pce.getPropertyName())) {
            Object value = pce.getNewValue();
            if ((value instanceof Boolean[]) && ((Boolean[]) value).length == 3) {
                Boolean[] userSelection = (Boolean[]) value;
                boolean mobile = userSelection[0];
                boolean wear = userSelection[1];
                boolean tv = userSelection[2];
                if (mobile && !wear && !tv) {
                    panels = createPanels();
                    panels.remove(panel_wear);
                    panels.remove(panel_wear_config);
                    panels.remove(panel_tv);
                    panels.remove(panel_tv_config);
                    steps = createSteps();
                    steps.remove(text_wear);
                    steps.remove(text_wear_config);
                    steps.remove(text_tv);
                    steps.remove(text_tv_config);
                    wiz.putProperty(WizardDescriptor.PROP_CONTENT_DATA, steps.toArray(new String[steps.size()]));
                } else if (!mobile && wear && !tv) {
                    panels = createPanels();
                    panels.remove(panel_mobile);
                    panels.remove(panel_mobile_config);
                    panels.remove(panel_tv);
                    panels.remove(panel_tv_config);
                    steps = createSteps();
                    steps.remove(TEXT_MOBILE);
                    steps.remove(TEXT_MOBILE_CONFIG);
                    steps.remove(text_tv);
                    steps.remove(text_tv_config);
                    wiz.putProperty(WizardDescriptor.PROP_CONTENT_DATA, steps.toArray(new String[steps.size()]));
                } else if (!mobile && !wear && tv) {
                    panels = createPanels();
                    panels.remove(panel_mobile);
                    panels.remove(panel_mobile_config);
                    panels.remove(panel_wear);
                    panels.remove(panel_wear_config);
                    steps = createSteps();
                    steps.remove(TEXT_MOBILE);
                    steps.remove(TEXT_MOBILE_CONFIG);
                    steps.remove(text_wear);
                    steps.remove(text_wear_config);
                    wiz.putProperty(WizardDescriptor.PROP_CONTENT_DATA, steps.toArray(new String[steps.size()]));
                } else if (mobile && wear && !tv) {
                    panels = createPanels();
                    panels.remove(panel_tv);
                    panels.remove(panel_tv_config);
                    steps = createSteps();
                    steps.remove(text_tv);
                    steps.remove(text_tv_config);
                    wiz.putProperty(WizardDescriptor.PROP_CONTENT_DATA, steps.toArray(new String[steps.size()]));
                } else if (mobile && !wear && tv) {
                    panels = createPanels();
                    panels.remove(panel_wear);
                    panels.remove(panel_wear_config);
                    steps = createSteps();
                    steps.remove(text_wear);
                    steps.remove(text_wear_config);
                    wiz.putProperty(WizardDescriptor.PROP_CONTENT_DATA, steps.toArray(new String[steps.size()]));
                } else if (!mobile && wear && tv) {
                    panels = createPanels();
                    panels.remove(panel_mobile);
                    panels.remove(panel_mobile_config);
                    steps = createSteps();
                    steps.remove(TEXT_MOBILE);
                    steps.remove(TEXT_MOBILE_CONFIG);
                    wiz.putProperty(WizardDescriptor.PROP_CONTENT_DATA, steps.toArray(new String[steps.size()]));
                } else {
                    panels = createPanels();
                    steps = createSteps();
                    wiz.putProperty(WizardDescriptor.PROP_CONTENT_DATA, steps.toArray(new String[steps.size()]));
                }
            }
        } else if (PROP_MOBILE_CONFIG.equals(pce.getPropertyName())) {
            Object value = pce.getNewValue();
            if (value instanceof Boolean) {
                if (((boolean) value) == false) {
                    panels.remove(panel_mobile_config);
                    steps.remove(TEXT_MOBILE_CONFIG);
                    wiz.putProperty(WizardDescriptor.PROP_CONTENT_DATA, steps.toArray(new String[steps.size()]));
                } else {
                    if (!panels.contains(panel_mobile_config)) {
                        int indexOf = panels.indexOf(panel_mobile);
                        panels.add(indexOf + 1, panel_mobile_config);
                        steps.add(indexOf + 1, TEXT_MOBILE_CONFIG);
                    }
                    wiz.putProperty(WizardDescriptor.PROP_CONTENT_DATA, steps.toArray(new String[steps.size()]));
                }
            }
        } else if (AndroidProjectTemplatePanelWearActivityAndroidSettings.PROP_WEAR_CONFIG.equals(pce.getPropertyName())) {
            Object value = pce.getNewValue();
            if (value instanceof Boolean) {
                if (((boolean) value) == false) {
                    panels.remove(panel_wear_config);
                    steps.remove(text_wear_config);
                    wiz.putProperty(WizardDescriptor.PROP_CONTENT_DATA, steps.toArray(new String[steps.size()]));
                } else {
                    if (!panels.contains(panel_wear_config)) {
                        int indexOf = panels.indexOf(panel_wear);
                        panels.add(indexOf + 1, panel_wear_config);
                        steps.add(indexOf + 1, text_wear_config);
                    }
                    wiz.putProperty(WizardDescriptor.PROP_CONTENT_DATA, steps.toArray(new String[steps.size()]));
                }
            }
        } else if (AndroidProjectTemplatePanelTvActivityAndroidSettings.PROP_TV_CONFIG.equals(pce.getPropertyName())) {
            Object value = pce.getNewValue();
            if (value instanceof Boolean) {
                if (((boolean) value) == false) {
                    panels.remove(panel_tv_config);
                    steps.remove(text_tv_config);
                    wiz.putProperty(WizardDescriptor.PROP_CONTENT_DATA, steps.toArray(new String[steps.size()]));
                } else {
                    if (!panels.contains(panel_tv_config)) {
                        int indexOf = panels.indexOf(panel_tv);
                        panels.add(indexOf + 1, panel_tv_config);
                        steps.add(indexOf + 1, text_tv_config);
                    }
                    wiz.putProperty(WizardDescriptor.PROP_CONTENT_DATA, steps.toArray(new String[steps.size()]));
                }
            }
        }
    }

}
