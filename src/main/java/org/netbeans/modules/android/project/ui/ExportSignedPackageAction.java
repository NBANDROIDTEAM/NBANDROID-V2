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
package org.netbeans.modules.android.project.ui;

import com.android.sdklib.internal.build.DebugKeyProvider.IKeyGenOutput;
import com.android.sdklib.internal.build.KeystoreHelper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.apache.tools.ant.module.api.support.AntScriptUtils;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.modules.android.project.spi.AndroidProjectDirectory;
import org.netbeans.modules.android.project.ui.customizer.AndroidProjectProperties;
import org.netbeans.modules.android.project.ui.wizards.ExportPackageWizardIterator;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

public final class ExportSignedPackageAction implements ActionListener {

  private final AndroidProject context;

  public ExportSignedPackageAction(AndroidProject context) {
    this.context = context;
  }

  @Override
  public void actionPerformed(ActionEvent ev) {
    // TODO use context
    PropertyEvaluator props = context.evaluator();
    WizardDescriptor.Iterator iterator = new ExportPackageWizardIterator();
    WizardDescriptor wizardDescriptor = new WizardDescriptor(iterator);
    // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
    // {1} will be replaced by WizardDescriptor.Iterator.name()
    wizardDescriptor.setTitleFormat(new MessageFormat("{0} ({1})"));
    wizardDescriptor.setTitle("Export Signed Application Package");

    String releaseFileName = props.getProperty(AndroidProjectProperties.PROP_RELEASE_FILE);
    if (releaseFileName == null) {
      FileObject buildFO = findBuildXml();
      if (buildFO == null) {
        DialogDisplayer.getDefault().notify(
            new NotifyDescriptor.Message(
                MessageFormat.format("build.xml file is missing in Android project {0}", 
                    context.getLookup().lookup(ProjectInformation.class).getDisplayName()),
            NotifyDescriptor.ERROR_MESSAGE));
        return;
      }
      String appName = AntScriptUtils.getAntScriptName(buildFO);
      File apkFile = new File(
          context.getLookup().lookup(AndroidProjectDirectory.class).get(), "bin" + File.separatorChar + appName + "-release.apk");
      releaseFileName = apkFile.getAbsolutePath();
    }
    wizardDescriptor.putProperty(AndroidProjectProperties.PROP_RELEASE_FILE, 
        releaseFileName);
    wizardDescriptor.putProperty(AndroidProjectProperties.PROP_KEY_STORE, 
        props.getProperty(AndroidProjectProperties.PROP_KEY_STORE));
    wizardDescriptor.putProperty(AndroidProjectProperties.PROP_KEY_STORE_PASSWD,
        props.getProperty(AndroidProjectProperties.PROP_KEY_STORE_PASSWD));
    wizardDescriptor.putProperty(ExportPackageWizardIterator.PROP_USE_EXISTING_KEYSTORE, 
        Boolean.valueOf(props.getProperty(AndroidProjectProperties.PROP_KEY_STORE) != null));

    Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
    dialog.setVisible(true);
    dialog.toFront();
    boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
    if (!cancelled) {
      doExport(wizardDescriptor);
    }
  }

  private void doExport(WizardDescriptor wizardDescriptor) {
    try {
      Properties buildProps = new Properties();
      String keystore = 
          (String) wizardDescriptor.getProperty(AndroidProjectProperties.PROP_KEY_STORE);
      String keystorePasswd = 
          (String) wizardDescriptor.getProperty(AndroidProjectProperties.PROP_KEY_STORE_PASSWD);
      String keyAlias = 
          (String) wizardDescriptor.getProperty(AndroidProjectProperties.PROP_KEY_ALIAS);
      String keyPasswd = 
          (String) wizardDescriptor.getProperty(AndroidProjectProperties.PROP_KEY_ALIAS_PASSWD);
      buildProps.put(AndroidProjectProperties.PROP_KEY_STORE, keystore);
      buildProps.put(AndroidProjectProperties.PROP_KEY_STORE_PASSWD, keystorePasswd);
      buildProps.put(AndroidProjectProperties.PROP_KEY_ALIAS, keyAlias);
      buildProps.put(AndroidProjectProperties.PROP_KEY_ALIAS_PASSWD, keyPasswd);

      Boolean useAlias = (Boolean) wizardDescriptor.getProperty(
          ExportPackageWizardIterator.PROP_USE_EXISTING_ALIAS);
      Boolean useKeystore = (Boolean) wizardDescriptor.getProperty(
          ExportPackageWizardIterator.PROP_USE_EXISTING_KEYSTORE);
      if (!useKeystore || !useAlias) {
        int validity = (Integer) wizardDescriptor.getProperty(
            ExportPackageWizardIterator.PROP_KEY_ALIAS_VALIDITY);
        String dName = (String) wizardDescriptor.getProperty(
            ExportPackageWizardIterator.PROP_KEY_ALIAS_DNAME);

        final List<String> output = Lists.newArrayList();
        boolean createdStore = KeystoreHelper.createNewStore(
            keystore,
            null /*storeType*/,
            keystorePasswd,
            keyAlias,
            keyPasswd,
            dName,
            validity,
            new IKeyGenOutput() {
                @Override
                public void err(String message) {
                    output.add(message);
                }
                @Override
                public void out(String message) {
                    output.add(message);
                }
            });

        if (!createdStore) {
            // keystore creation error!
          DialogDisplayer.getDefault().notify(
              new NotifyDescriptor.Message(Joiner.on('\n').join(output), 
              NotifyDescriptor.ERROR_MESSAGE));
            return;
        }

        // XXX try to load the key just to make sure it is OK?
      }
      // now do the build
      FileObject buildFO = findBuildXml();
      ExecutorTask task = ActionUtils.runTarget(buildFO, new String[] {"release"}, buildProps);
    } catch (Exception ex) {
      Exceptions.printStackTrace(ex);
    }
  }
  private FileObject findBuildXml() {
      return context.getProjectDirectory().getFileObject("build.xml");
  }
}
