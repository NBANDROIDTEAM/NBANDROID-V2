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

import com.android.ide.common.xml.AndroidManifestParser;
import com.android.ide.common.xml.ManifestData;
import com.android.io.StreamException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.xml.parsers.ParserConfigurationException;
import org.nbandroid.netbeans.gradle.core.sdk.DalvikPlatform;
import org.nbandroid.netbeans.gradle.spi.DalvikPlatformResolver;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
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

    public static ManifestData parseProjectManifest(InputStream manifestIS) {
        try {
            return AndroidManifestParser.parse(manifestIS);
        } catch (ParserConfigurationException ex) {
            LOG.log(Level.INFO, "AndroidManifest.xml cannot be parsed", ex);
        } catch (StreamException ex) {
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

    public static DalvikPlatform projectPlatform(Project project) {
        DalvikPlatformResolver dpr = project.getLookup().lookup(DalvikPlatformResolver.class);
        if (dpr != null) {
            DalvikPlatform platform = dpr.findDalvikPlatform();
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
        DalvikPlatformResolver dpr = p != null ? p.getLookup().lookup(DalvikPlatformResolver.class) : null;
        if (dpr == null) {
            return false;
        }
        return dpr.findDalvikPlatform() != null;
    }

    public static ReferenceResolver noReferenceResolver() {
        return new NullRefResolver();
    }
}
