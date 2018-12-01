/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.project.template.freemarker;

import android.studio.imports.templates.recipe.RecipeExecutor;
import android.studio.imports.templates.recipe.TemplateProcessingException;
import com.android.utils.FileUtils;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.nbandroid.netbeans.gradle.v2.project.template.freemarker.converters.FmGetConfigurationNameMethod;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author arsi
 */
public class ProjectTemplateLoader implements TemplateLoader, RecipeExecutor {

    private Configuration myFreemarker;
    private final FileObject root;
    private final List<String> pushedFolders = new ArrayList<>();
    private final Map<String, List<String>> dependencyList = new HashMap<>();

    public ProjectTemplateLoader(FileObject root) {
        myFreemarker = new FreemarkerConfiguration();
        myFreemarker.setTemplateLoader(this);
        this.root = root;
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
                File f = new File(path);
                String name = f.getName();
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

    public RecipeExecutor setupRecipeExecutor(String path, Map<String, Object> parameters) {
        this.parameters = parameters;
        templatePath = path;
        if (!templatePath.endsWith("/")) {
            templatePath += "/";
        }
        return this;
    }

    @Override
    public void copy(File from, File to) {
        System.out.println("Copy: " + from.getPath() + " " + to.getPath());
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

    private File lastGradle = null;

    @Override
    public void instantiate(File from, File to) throws TemplateProcessingException {
        if ("build.gradle".equals(to.getName())) {
            lastGradle = to;
        }
        String process = process(templatePath + "/" + from.getPath(), parameters);
        if (process == null) {
            System.out.println("org.nbandroid.netbeans.gradle.v2.project.template.freemarker.ProjectTemplateLoader.instantiate()");
        }
        try {
            FileUtils.writeToFile(to, process);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void mkDir(File at) {
        at.mkdirs();
        System.out.println("mkDir: " + at.getPath());
    }

    @Override
    public void merge(File from, File to) throws TemplateProcessingException {
        System.out.println("org.nbandroid.netbeans.gradle.v2.project.template.freemarker.ProjectTemplateLoader.merge()");
    }

    @Override
    public void addFilesToOpen(File file) {
    }

    @Override
    public void applyPlugin(String plugin) {
        System.out.println("org.nbandroid.netbeans.gradle.v2.project.template.freemarker.ProjectTemplateLoader.applyPlugin()");
    }

    @Override
    public void addClasspath(String mavenUrl) {
        System.out.println("org.nbandroid.netbeans.gradle.v2.project.template.freemarker.ProjectTemplateLoader.addClasspath()");
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
            tmp.add(mavenUrl);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void updateAndSync() {
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
        System.out.println("org.nbandroid.netbeans.gradle.v2.project.template.freemarker.ProjectTemplateLoader.append()");
    }

}
