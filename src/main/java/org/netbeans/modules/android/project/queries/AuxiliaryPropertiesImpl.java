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
package org.netbeans.modules.android.project.queries;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;
import org.openide.util.UserQuestionException;
import org.openide.util.Utilities;
import org.openide.util.WeakSet;

/**
 * AuxiliaryProperties implementation mostly copied from Ant project support.
 */
public class AuxiliaryPropertiesImpl implements AuxiliaryProperties {

  /**
   * Relative path from project directory to the customary shared properties file.
   */
  public static final String PROJECT_PROPERTIES_PATH = "nbandroid/project.properties"; // NOI18N
  /**
   * Relative path from project directory to the customary private properties file.
   */
  public static final String PRIVATE_PROPERTIES_PATH = "nbandroid/private.properties"; // NOI18N

  private final String propertyPrefix = "auxiliary.";

  /**
   * Properties loaded from metadata files on disk.
   * Keys are project-relative paths such as {@link #PROJECT_PROPERTIES_PATH}.
   * Values are loaded property providers.
   */
  private final Map<String, PP> properties = new HashMap<String, PP>();
  private final AndroidProject aProject;

  public AuxiliaryPropertiesImpl(AndroidProject aProject) {
    this.aProject = aProject;
  }

  @Override
  public String get(String key, boolean shared) {
    String location = shared ? PROJECT_PROPERTIES_PATH : PRIVATE_PROPERTIES_PATH;
    EditableProperties props = getProperties(location);

    return props.get(propertyPrefix + key);
  }

  @Override
  public void put(final String key, final String value, final boolean shared) {
    ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {

      @Override
      public Void run() {
        String location = shared ? PROJECT_PROPERTIES_PATH : PRIVATE_PROPERTIES_PATH;
        EditableProperties props = getProperties(location);

        if (value != null) {
          props.put(propertyPrefix + key, value);
        } else {
          props.remove(propertyPrefix + key);
        }

        putProperties(location, props);
        save();
        return null;
      }
    });
  }

  @Override
  public Iterable<String> listKeys(boolean shared) {
    List<String> result = new LinkedList<String>();
    String location = shared ? PROJECT_PROPERTIES_PATH : PRIVATE_PROPERTIES_PATH;
    EditableProperties props = getProperties(location);

    for (String k : props.keySet()) {
      if (k.startsWith(propertyPrefix)) {
        result.add(k.substring(propertyPrefix.length()));
      }
    }

    return result;
  }

  private void save() {
    try {
      PP editableProps = getPP(PRIVATE_PROPERTIES_PATH);
      if (editableProps.loaded) {
        FileLock lock = editableProps.write();
        if (lock != null) {
          lock.releaseLock();
        }
      }
      PP editableProps2 = getPP(PROJECT_PROPERTIES_PATH);
      if (editableProps2.loaded) {
        FileLock lock2 = editableProps2.write();
        if (lock2 != null) {
          lock2.releaseLock();
        }
      }
    } catch (IOException ex) {
      Exceptions.printStackTrace(ex);
    }

  }

  private EditableProperties getProperties(final String path) {
    return ProjectManager.mutex().readAccess(new Mutex.Action<EditableProperties>() {

      @Override
      public EditableProperties run() {
        EditableProperties ep = getPP(path).getEditablePropertiesOrNull();
        if (ep != null) {
          return ep.cloneProperties();
        } else {
          return new EditableProperties(true);
        }
      }
    });
  }

  private boolean putProperties(String path, EditableProperties props) {
    return getPP(path).put(props);
  }

  private PP getPP(String path) {
    PP pp = properties.get(path);
    if (pp == null) {
      pp = new PP(path, aProject.getProjectDirectory());
      properties.put(path, pp);
    }
    return pp;
  }

  private static final class PP implements FileChangeListener {

    private static final RequestProcessor RP = new RequestProcessor("ProjectProperties.PP.RP"); // NOI18N
    // XXX lock any loaded property files while the project is modified, to prevent manual editing,
    // and reload any modified files if the project is unmodified
    private final String path;
    private EditableProperties properties = null;
    private volatile boolean loaded = false;
    private Throwable reloadedStackTrace;
    /** Atomic actions in use to save XML files. */
    private final Set<AtomicAction> saveActions = new WeakSet<AtomicAction>();
    private final FileObject projectDir;

    public PP(String path, FileObject projectDir) {
      this.path = path;
      this.projectDir = projectDir;
      File fl = new File(FileUtil.toFile(dir()), path.replace('/', File.separatorChar));
      FileUtil.addFileChangeListener(this, FileUtil.normalizeFile(fl));
    }

    private FileObject dir() {
      return projectDir;
    }

    public EditableProperties getEditablePropertiesOrNull() {
      if (!loaded) {
        properties = null;
        FileObject fo = dir().getFileObject(path);
        if (fo != null) {
          try {
            EditableProperties p;
            InputStream is = fo.getInputStream();
            try {
              p = new EditableProperties(true);
              p.load(is);
            } finally {
              is.close();
            }
            properties = p;
          } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
          }
        }
        loaded = true;
        reloadedStackTrace = null;
      }
      return properties;
    }

    public boolean put(EditableProperties nue) {
      loaded = true;
      reloadedStackTrace = null;
      boolean modifying = !Utilities.compareObjects(nue, properties);
      if (modifying) {
        if (nue != null) {
          properties = nue.cloneProperties();
        } else {
          properties = null;
        }
      }
      return modifying;
    }

    private void runSaveAA(AtomicAction action) throws IOException {
      synchronized (saveActions) {
        saveActions.add(action);
      }
      dir().getFileSystem().runAtomicAction(action);
    }

    public FileLock write() throws IOException {
      if (!loaded) {
        Logger.getLogger(PP.class.getName()).log(Level.INFO, null,
            new IOException("#167784: changes on disk for " + path + " in " + dir() + " clobbered by in-memory data").initCause(reloadedStackTrace));
        loaded = true;
        reloadedStackTrace = null;
      }
      final FileObject f = dir().getFileObject(path);
      final FileLock[] _lock = new FileLock[1];
      try {
        if (properties != null) {
          // Supposed to create/modify the file.
          // Need to use an atomic action - otherwise listeners will first
          // receive an event that the file has been written to zero length
          // (which for *.properties means no keys), which is wrong.
          runSaveAA(new AtomicAction() {

            public void run() throws IOException {
              final FileObject _f;
              if (f == null) {
                _f = FileUtil.createData(dir(), path);
                assert _f != null : "FU.cD must not return null; called on " + dir() + " + " + path; // #50802
              } else {
                _f = f;
              }
              ByteArrayOutputStream baos = new ByteArrayOutputStream();
              properties.store(baos);
              final byte[] data = baos.toByteArray();
              try {
                _lock[0] = _f.lock(); // released by {@link AntProjectHelper#save}
                OutputStream os = _f.getOutputStream(_lock[0]);
                try {
                  os.write(data);
                } finally {
                  os.close();
                }
              } catch (UserQuestionException uqe) { // #46089
//                helper.needPendingHook();
                UserQuestionHandler.handle(uqe, new UserQuestionHandler.Callback() {

                  public void accepted() {
                    // Try again.
                    try {
                      runSaveAA(new AtomicAction() {

                        public void run() throws IOException {
                          OutputStream os = _f.getOutputStream();
                          try {
                            os.write(data);
                          } finally {
                            os.close();
                          }
//                          helper.maybeCallPendingHook();
                        }
                      });
                    } catch (IOException e) {
                      // Oh well.
                      ErrorManager.getDefault().notify(e);
                      reload();
                    }
                  }

                  public void denied() {
                    reload();
                  }

                  public void error(IOException e) {
                    ErrorManager.getDefault().notify(e);
                    reload();
                  }

                  private void reload() {
//                    helper.cancelPendingHook();
                    // Revert the save.
                    diskChange(null);
                  }
                });
              }
            }
          });
        } else {
          // We are supposed to remove any existing file.
          if (f != null) {
            f.delete();
          }
        }
      } catch (IOException e) {
        if (_lock[0] != null) {
          // Release it now, since no one else will.
          _lock[0].releaseLock();
        }
        throw e;
      }
      return _lock[0];
    }

    private void diskChange(FileEvent fe) {
      boolean writing = false;
      if (fe != null) {
        synchronized (saveActions) {
          for (AtomicAction a : saveActions) {
            if (fe.firedFrom(a)) {
              writing = true;
              break;
            }
          }
        }
      }
      if (!writing) {
        loaded = false;
        reloadedStackTrace = new Throwable("noticed disk change here");
      }
//      if (!writing) {
//        helper.fireExternalChange(path);
//      }
    }

    public void fileFolderCreated(FileEvent fe) {
      diskChange(fe);
    }

    public void fileDataCreated(FileEvent fe) {
      diskChange(fe);
    }

    public void fileChanged(FileEvent fe) {
      diskChange(fe);
    }

    public void fileRenamed(FileRenameEvent fe) {
      diskChange(fe);
    }

    public void fileDeleted(FileEvent fe) {
      diskChange(fe);
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
    }
  }
}
