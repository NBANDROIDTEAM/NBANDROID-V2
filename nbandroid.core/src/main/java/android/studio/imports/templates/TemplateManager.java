/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package android.studio.imports.templates;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author arsi
 */
public class TemplateManager {

    public static List<Template> findTemplates(String subFolder) {
        List<Template> tmp = new ArrayList<>();
        URL location = TemplateManager.class.getProtectionDomain().getCodeSource().getLocation();
        FileObject fo = URLMapper.findFileObject(location);
        FileObject archiveFile = FileUtil.getArchiveRoot(fo);
        FileObject rootDir = archiveFile.getFileObject("/android/studio/imports/templates/" + subFolder);
        Enumeration<? extends FileObject> children = rootDir.getChildren(true);
        while (children.hasMoreElements()) {
            FileObject nextElement = children.nextElement();
            if ("template.xml".equalsIgnoreCase(nextElement.getNameExt())) {
                Template template = new Template(nextElement);
                TemplateMetadata metadata = template.getMetadata();
                tmp.add(template);
            }
        }
        return tmp;
    }

    public static FileObject getRootFolder() {
        URL location = TemplateManager.class.getProtectionDomain().getCodeSource().getLocation();
        FileObject fo = URLMapper.findFileObject(location);
        FileObject archiveFile = FileUtil.getArchiveRoot(fo);
        return archiveFile.getFileObject("/android/studio/imports/templates/");
    }

    public static String getJarPath(String path) {
        return path.replace("android/studio/imports/templates/", "");
    }

    public static List<Template> findActivityTemplates(String formFactor, int minApiLevel, int buildApiLevel) {
        List<Template> tmp = new ArrayList<>();
        List<Template> templates = findTemplates("activities");
        for (Template template : templates) {
            if (template.getMetadata().getCategory() != null && template.getMetadata().getFormFactor().equals(formFactor)) {
                if (template.getMetadata().getMinBuildApi() <= buildApiLevel && template.getMetadata().getMinSdk() <= minApiLevel) {
                    tmp.add(template);
                }
            }
        }
        Collections.sort(tmp, new Comparator<Template>() {
            @Override
            public int compare(Template t, Template t1) {
                return t.getMetadata().getTitle().compareTo(t1.getMetadata().getTitle());
            }
        });
        return tmp;
    }

    public static Template findProjectTemplate(String name) {
        List<Template> templates = findTemplates("gradle-projects");
        for (Template template : templates) {
            if (name.equals(template.getMetadata().getTitle())) {
                return template;
            }
        }
        return null;
    }

}
