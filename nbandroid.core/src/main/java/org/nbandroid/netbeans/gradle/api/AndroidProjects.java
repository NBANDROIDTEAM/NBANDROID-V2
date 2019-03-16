/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nbandroid.netbeans.gradle.api;

import static com.android.SdkConstants.ANDROID_URI;
import com.android.ide.common.xml.AndroidManifestParser;
import com.android.ide.common.xml.ManifestData;
import com.android.utils.XmlUtils;
import com.android.xml.AndroidManifest;
import static com.android.xml.AndroidManifest.ATTRIBUTE_THEME;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.xml.parsers.ParserConfigurationException;
import org.nbandroid.netbeans.gradle.spi.AndroidPlatformResolver;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidPlatformInfo;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.android.project.api.NbAndroidRootProjectImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class AndroidProjects {

    private static final Logger LOG = Logger.getLogger(AndroidProjects.class.getName());

    private AndroidProjects() {
    }

    @Nullable
    public static ManifestData parseProjectManifest(Project project) {
        if (project == null) {
            return null;
        }
        AndroidManifestSource ams = project.getLookup().lookup(AndroidManifestSource.class);
        FileObject androidManifest = ams != null ? ams.get() : null;
        if (androidManifest == null) {
            LOG.log(Level.WARNING, "No AndroidManifest.xml in {0}", project);
            return null;
        }
        try {
            return parseProjectManifest(androidManifest.getInputStream());
        } catch (FileNotFoundException ex) {
            LOG.log(Level.WARNING, null, ex);
        }
        return null;
    }

    static String getAttributeValue(Element element,
            String namespace,
            String localName) {
        return element.getAttributeNS(namespace, localName);
    }

    public static FileObject findResFolder(Project project) {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] resGroup = sources.getSourceGroups(AndroidConstants.SOURCES_TYPE_ANDROID_RES);
        if (resGroup.length > 0) {
            return resGroup[0].getRootFolder();
        }
        return null;
    }

    public static File findResFolderAsFile(Project project, FileObject layoutFo) {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] resGroup = sources.getSourceGroups(AndroidConstants.SOURCES_TYPE_ANDROID_RES);
        for (SourceGroup sourceGroup : resGroup) {
            FileObject rootFolder = sourceGroup.getRootFolder();
            if (FileUtil.isParentOf(rootFolder, layoutFo)) {
                return FileUtil.toFile(rootFolder);
            }
        }
        if (resGroup.length > 0) {
            return FileUtil.toFile(resGroup[0].getRootFolder());
        }
        return null;
    }

    public static String parseProjectTheme(Project project) {
        if (project == null) {
            return null;
        }
        AndroidManifestSource ams = project.getLookup().lookup(AndroidManifestSource.class);
        FileObject androidManifest = ams != null ? ams.get() : null;
        if (androidManifest == null) {
            LOG.log(Level.WARNING, "No AndroidManifest.xml in {0}", project);
            return null;
        }
        try {
            Document document = XmlUtils.parseDocumentSilently(androidManifest.asText("UTF-8"), true);
            Element root = document.getDocumentElement();
            Node node = root.getFirstChild();
            while (node != null) {
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String nodeName = node.getNodeName();
                    if (AndroidManifest.NODE_APPLICATION.equals(nodeName)) {
                        Element application = (Element) node;
                        String projectThemeTmp = getAttributeValue(application, ANDROID_URI, ATTRIBUTE_THEME);
                        if (projectThemeTmp != null && projectThemeTmp.contains("/")) {
                            projectThemeTmp = projectThemeTmp.substring(projectThemeTmp.lastIndexOf('/') + 1);
                        }
                        return projectThemeTmp;
                    }

                }
                node = node.getNextSibling();
            }
        } catch (FileNotFoundException ex) {
            LOG.log(Level.WARNING, null, ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public static ManifestData parseProjectManifest(InputStream manifestIS) {
        try {
            return AndroidManifestParser.parse(manifestIS);
        } catch (ParserConfigurationException ex) {
            LOG.log(Level.INFO, "AndroidManifest.xml cannot be parsed", ex);
        } catch (IOException ioe) {
            LOG.log(Level.INFO, "AndroidManifest.xml cannot be parsed", ioe);
        } catch (SAXException saxe) {
            LOG.log(Level.INFO, "AndroidManifest.xml cannot be parsed", saxe);
        } finally {
            try {
                manifestIS.close();
            } catch (IOException ex) {
            }
        }

        return null;
    }

    public static AndroidPlatformInfo projectPlatform(Project project) {
        AndroidPlatformResolver dpr = project.getLookup().lookup(AndroidPlatformResolver.class);
        if (dpr != null) {
            AndroidPlatformInfo platform = dpr.findAndroidPlatform(project);
            LOG.log(Level.FINE, "project {0} has DalvikPlatformResolver and resolves to {1}",
                    new Object[]{project, platform});
            if (platform != null) {
                return platform;
            }
        }
        LOG.log(Level.INFO, "could not find platform for {0}", project);
        return null;
    }

    public static boolean isAndroidMavenProject(Project p) {
        AndroidPlatformResolver dpr = p != null ? p.getLookup().lookup(AndroidPlatformResolver.class) : null;
        if (dpr == null) {
            return false;
        }
        return dpr.findAndroidPlatform(p) != null;
    }

    public static ReferenceResolver noReferenceResolver() {
        return new NullRefResolver();
    }

    public static final Project findRootProject(Project current) {
        if (current instanceof NbAndroidRootProjectImpl) {
            return current;
        }
        Project owner = FileOwnerQuery.getOwner(current.getProjectDirectory().getParent());
        if (owner instanceof NbAndroidRootProjectImpl) {
            return owner;
        } else {
            throw new IllegalStateException("Gradle root projec not found!");
        }
    }
}
