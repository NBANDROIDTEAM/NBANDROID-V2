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

package org.netbeans.modules.android.core.sdk;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.ZipFile;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

public final class Util {

  private static Tool toTool(String toolName) {
    for (Tool t : Tool.values()) {
      if (t.getSystemName().equals(toolName)) {
        return t;
      }
    }
    return null;
  }
  private Util () {
  }

  /**
   * A set of executable tools used by IDE.
   * @return enumeration set of tools.
   */
  public static EnumSet<Tool> sdkTools() {
    return EnumSet.of(
        Tool.AGENT, Tool.EMULATOR);
  }

  /**
   * A set of XML resources from SDK used by IDE.
   * @return enum set of tools.
   */
  public static EnumSet<Tool> sdkResources() {
    return EnumSet.of(Tool.ATTRS_MANIFEST, Tool.ATTRS_LAYOUT, Tool.WIDGETS);
  }

    static ClassPath createClassPath(String classpath) {
        StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
        List<PathResourceImplementation> list = 
            new ArrayList<PathResourceImplementation>();
        while (tokenizer.hasMoreTokens()) {
            String item = tokenizer.nextToken();
            File f = FileUtil.normalizeFile(new File(item));            
            URL url = getRootURL (f);
            if (url!=null) {
                list.add(ClassPathSupport.createResource(url));
            }
        }
        return ClassPathSupport.createClassPath(list);
    }

    // XXX this method could probably be removed... use standard FileUtil stuff
    static URL getRootURL  (final File f) {        
        try {
            URL url = f.toURI().toURL();
            if (FileUtil.isArchiveFile(url)) {
                url = FileUtil.getArchiveRoot (url);
            }
            else if (!f.exists()) {
                String surl = url.toExternalForm();
                if (!surl.endsWith("/")) {
                    url = new URL (surl+"/");
                }
            }
            else if (f.isFile()) {
                //Slow but it will be called only in very rare cases:
                //file on the classpath for which isArchiveFile returned false
                try {
                    ZipFile z = new ZipFile (f);
                    z.close();
                    url = FileUtil.getArchiveRoot (url);
                } catch (IOException e) {
                    url = null;
                }
            }
            return url;
        } catch (MalformedURLException e) {
            throw new AssertionError(e);            
        }        
    }


    /**
     * Returns normalized name from display name.
     * The normalized name should be used in the Ant properties and external files.
     * @param displayName
     * @return String
     */
    public static String normalizeName (String displayName) {
        StringBuilder normalizedName = new StringBuilder ();
        for (int i=0; i< displayName.length(); i++) {
            char c = displayName.charAt(i);
            if (Character.isJavaIdentifierPart(c) || c =='-' || c =='.') {
                normalizedName.append(c);
            }
            else {
                normalizedName.append('_');
            }
        }
        return normalizedName.toString();
    }


    static FileObject findTool (String toolName, FileObject installFolder) {
      assert toolName != null;
      Tool t = toTool(toolName);
      boolean isBinary = sdkTools().contains(t);

      for (String searchFolder : t.getFolders()) {
        FileObject toolFO = findToolInFolder(toolName, searchFolder, installFolder, isBinary);
        if (toolFO != null) {
          return toolFO;
        }
        if (installFolder.getParent() == null) {
          continue;
        }
        FileObject sdkRoot = installFolder.getParent().getParent();
        toolFO = findToolInFolder(toolName, searchFolder, sdkRoot, isBinary);
        if (toolFO != null) {
          return toolFO;
        }
      }
      return null;
    }

    /**
     * Looks for a resource in a given folder under search root.
     * If resource has no extension it is assumed to be an executable so {@code .bat}
     * or {@code .exe} is searched in Windows.
     */
    private static FileObject findToolInFolder(
        String resName, String resDir, FileObject searchFolder, boolean isBinary) {
      assert resName != null;
      if (searchFolder == null) {
        return null;
      }
      String path = resDir + "/" + resName;
      FileObject toolFO = null;
      if (Utilities.isWindows() && isBinary) {
        toolFO = searchFolder.getFileObject(path + ".exe");
        if (toolFO == null) {
          toolFO = searchFolder.getFileObject(path + ".bat");
        }
      } else {
        toolFO = searchFolder.getFileObject(path);
      }
      return toolFO;
    }
}
