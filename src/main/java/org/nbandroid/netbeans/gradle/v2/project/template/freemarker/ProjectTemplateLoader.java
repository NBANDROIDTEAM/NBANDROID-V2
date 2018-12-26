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
package org.nbandroid.netbeans.gradle.v2.project.template.freemarker;

import android.studio.imports.templates.recipe.RecipeExecutor;
import android.studio.imports.templates.recipe.RecipeMergeUtils;
import android.studio.imports.templates.recipe.TemplateProcessingException;
import com.android.SdkConstants;
import com.android.utils.FileUtils;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.gradle.internal.impldep.org.apache.commons.io.IOUtils;
import org.nbandroid.netbeans.gradle.v2.gradle.build.parser.AndroidGradleDependencies;
import org.nbandroid.netbeans.gradle.v2.gradle.build.parser.AndroidGradleDependenciesVisitor;
import org.nbandroid.netbeans.gradle.v2.gradle.build.parser.AndroidGradleDependencyUpdater;
import org.nbandroid.netbeans.gradle.v2.layout.parsers.AndroidResValuesMerge;
import org.nbandroid.netbeans.gradle.v2.project.template.freemarker.converters.FmGetConfigurationNameMethod;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author arsi
 */
public class ProjectTemplateLoader implements TemplateLoader, RecipeExecutor {

    private Configuration myFreemarker;
    private final FileObject root;
    private final List<String> pushedFolders = new ArrayList<>();
    private final Map<String, List<String>> dependencyList = new HashMap<>();
    public static final String PROJECT_TEMPLATE_LOADER = "PROJECT_TEMPLATE_LOADER";
    private final List<FileObject> toOpen = new ArrayList<>();

    private ProjectTemplateLoader(FileObject root) {
        myFreemarker = new FreemarkerConfiguration();
        myFreemarker.setTemplateLoader(this);
        this.root = root;
    }

    public List<FileObject> getToOpen() {
        return toOpen;
    }

    public String process(String name, Map<String, Object> parameters) {
        try {
            Template template = myFreemarker.getTemplate(name);
            StringWriter writer = new StringWriter();
            template.process(TemplateUtils.createParameterMap(parameters), writer);
            return writer.toString();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    @Override
    public Object findTemplateSource(String path) throws IOException {
        path = path.replace("root://", "");
        FileObject fileObject = root.getFileObject(path);
        if (fileObject == null) {
            if (pushedFolders.isEmpty()) {
                throw new IOException("Template source " + path + " not found!");
            } else {
                String name = path.replace(templatePath, "");
                for (String pushedFolder : pushedFolders) {
                    if (!pushedFolder.endsWith("/") || !pushedFolder.endsWith("\\")) {
                        name = pushedFolder + File.separator + name;
                    } else {
                        name = pushedFolder + name;
                    }
                    name = name.replace("root://", "");
                    fileObject = root.getFileObject(name);
                    if (fileObject != null) {
                        break;
                    }
                }
                if (fileObject == null) {
                    throw new IOException("Template source " + path + " not found!");
                }
            }
        }
        return fileObject;
    }

    @Override
    public long getLastModified(Object templateSource) {
        return 0;
    }

    @Override
    public Reader getReader(Object templateSource, String encoding) throws IOException {
        if (templateSource instanceof FileObject) {
            return new InputStreamReader(((FileObject) templateSource).getInputStream(), encoding);
        }
        throw new IOException();
    }

    @Override
    public void closeTemplateSource(Object templateSource) throws IOException {
    }

    private String templatePath;
    private Map<String, Object> parameters;

    public static ProjectTemplateLoader setupRecipeExecutor(String buildGradleLocation, FileObject root, String path, Map<String, Object> parameters) {
        ProjectTemplateLoader loader = new ProjectTemplateLoader(root);
        loader.parameters = parameters;
        parameters.put(PROJECT_TEMPLATE_LOADER, loader);
        loader.templatePath = path;
        if (!loader.templatePath.endsWith("/")) {
            loader.templatePath += "/";
        }
        if (buildGradleLocation != null) {
            loader.buildGradleLocation = new File(buildGradleLocation);
        }
        return loader;
    }

    @Override
    public void copy(File from, File to) {
        FileObject fileObject = root.getFileObject(templatePath + from.getPath());
        if (fileObject != null) {
            if (fileObject.isData()) {
                try {
                    to.getParentFile().mkdirs();
                    FileObject fo = FileUtil.toFileObject(to.getParentFile());
                    if (fo != null) {
                        fileObject.copy(fo, to.getName(), "");
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                to.mkdirs();
                Enumeration<? extends FileObject> children = fileObject.getChildren(false);
                FileObject fo = FileUtil.toFileObject(to);
                if (fo != null) {
                    while (children.hasMoreElements()) {
                        FileObject nextFo = children.nextElement();
                        try {
                            nextFo.copy(fo, nextFo.getName(), nextFo.getExt());
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }

            }
        }
    }

    private File buildGradleLocation = null;

    @Override
    public void instantiate(File from, File to) throws TemplateProcessingException {
        if ("build.gradle".equals(to.getName()) && buildGradleLocation == null) {
            buildGradleLocation = to;
        }
        String process = process(templatePath + from.getPath(), parameters);
        if (process == null) {
            NotifyDescriptor nd = new NotifyDescriptor.Message("Unable to instantiate template " + from.getPath(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(nd);
        } else {
            try {
                FileUtils.writeToFile(to, process);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public void mkDir(File at) {
        at.mkdirs();
    }

    /**
     * Merges the given source file into the given destination file (or it just
     * copies it over if the destination file does not exist).
     * <p/>
     * Only XML and Gradle files are currently supported.
     */
    @Override
    public void merge(File from, File to) throws TemplateProcessingException {
        //     if (from.getName().endsWith(".ftl")) {
        String process = process(templatePath + from.getPath(), parameters);
        if (process == null) {
            NotifyDescriptor nd = new NotifyDescriptor.Message("Unable to instantiate template " + from.getPath(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(nd);
        } else if (to.exists() && to.length() > 0) {
            if (to.getName().endsWith(".gradle")) {
                mergeGradle(process, to);
            } else if (to.getName().endsWith(".xml")) {
                mergeXml(process, to);
            }
        } else {
            createFile(process, to);
        }
//        } else {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }

    }

    private void mergeGradle(String process, File to) {
        try {
            Files.write(to.toPath(), process.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void mergeXml(String process, File to) {
        if (SdkConstants.FN_ANDROID_MANIFEST_XML.equals(to.getName())) {
            String projectLocation = (String) parameters.get("projectLocation");
            try {
                String mergeXml = RecipeMergeUtils.mergeXml(new File(projectLocation), IOUtils.toString(new FileInputStream(to)), process, to);
                Files.write(to.toPath(), mergeXml.getBytes(), StandardOpenOption.CREATE);
            } catch (IOException ex) {
                NotifyDescriptor nd = new NotifyDescriptor.Message("Unable to merge into " + SdkConstants.FN_ANDROID_MANIFEST_XML, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(nd);
                Exceptions.printStackTrace(ex);
            }
        } else {
            try {
                byte[] bytes = process.getBytes();
                Document document = AndroidResValuesMerge.merge(new FileInputStream(to), new ByteArrayInputStream(bytes));
                AndroidResValuesMerge.save(document, new FileOutputStream(to));
            } catch (ParserConfigurationException | SAXException | IOException | TransformerException ex) {
                NotifyDescriptor nd = new NotifyDescriptor.Message("Unable to merge into " + to.getName(), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(nd);
                Exceptions.printStackTrace(ex);
            }
        }

    }

    private void createFile(String process, File to) {
        try {
            to.getParentFile().mkdirs();
            Files.write(to.toPath(), process.getBytes(), StandardOpenOption.CREATE);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void addFilesToOpen(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo != null) {
            toOpen.add(fo);
        }
    }

    @Override
    public void applyPlugin(String plugin) {
    }

    @Override
    public void addClasspath(String mavenUrl) {
    }

    @Override
    public void addDependency(String configuration, String mavenUrl) {
        try {
            // Translate from "configuration" to "implementation" based on the parameter map context
            configuration = FmGetConfigurationNameMethod.convertConfiguration(parameters, configuration);
            List<String> tmp = dependencyList.get(configuration);
            if (tmp == null) {
                tmp = new ArrayList<>();
                dependencyList.put(configuration, tmp);
            }
            if (!tmp.contains(mavenUrl)) {
                tmp.add(mavenUrl);
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void updateAndSync() {
        if (!dependencyList.isEmpty()) {
            boolean insertDependencies = AndroidGradleDependencyUpdater.insertDependencies(buildGradleLocation, dependencyList);
            if (!insertDependencies) {
                NotifyDescriptor nd = new NotifyDescriptor.Message("Unable to insert dependencies to build.gradle", NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(nd);
            }
        }
    }

    public AndroidGradleDependencies getDependencies() {
        try {
            AndroidGradleDependenciesVisitor visitor = AndroidGradleDependenciesVisitor.parse(buildGradleLocation);
            return visitor.getDependencies();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    @Override
    public void pushFolder(String folder) {
        pushedFolders.add(0, folder);
    }

    @Override
    public void popFolder() {
        if (!pushedFolders.isEmpty()) {
            pushedFolders.remove(0);
        }
    }

    @Override
    public void append(File from, File to) {
    }

}
