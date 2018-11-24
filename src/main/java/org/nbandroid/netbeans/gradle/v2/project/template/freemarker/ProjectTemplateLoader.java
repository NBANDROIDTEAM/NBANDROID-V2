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
import java.util.Enumeration;
import java.util.Map;
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
        return root.getFileObject(path);
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
        FileObject fileObject = root.getFileObject(templatePath + from.getPath());
        if (fileObject != null) {
            if (fileObject.isData()) {
                try {
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

    @Override
    public void instantiate(File from, File to) throws TemplateProcessingException {
        String process = process(templatePath + "/" + from.getPath(), parameters);
        try {
            FileUtils.writeToFile(to, process);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void mkDir(File at) {
        at.mkdirs();
    }

    @Override
    public void merge(File from, File to) throws TemplateProcessingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addFilesToOpen(File file) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void applyPlugin(String plugin) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addClasspath(String mavenUrl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addDependency(String configuration, String mavenUrl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateAndSync() {
    }

    @Override
    public void pushFolder(String folder) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void popFolder() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void append(File from, File to) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
