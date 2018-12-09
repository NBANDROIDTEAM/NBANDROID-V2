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
package org.nbandroid.netbeans.gradle.v2.project.template.parameters;

import static android.studio.imports.templates.TemplateMetadata.*;
import com.android.SdkConstants;
import com.android.repository.Revision;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;
import static org.nbandroid.netbeans.gradle.v2.project.template.AndroidProjectTemplatePanelVisualAndroidSettings.PROP_MAX_BUILD_LEVEL;
import org.nbandroid.netbeans.gradle.v2.project.template.AndroidProjectTemplatePanelVisualBasicSettings;
import static org.nbandroid.netbeans.gradle.v2.project.template.AndroidProjectTemplatePanelVisualBasicSettings.PROP_PROJECT_SDK;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidPlatformInfo;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdk;
import org.openide.WizardDescriptor;

/**
 *
 * @author arsi
 */
public class TemplateValueInjector {

    public static void setupNewModule(Map<String, Object> parameters, WizardDescriptor wiz, String platformPropertyName) {
        AndroidSdk androidSdk = (AndroidSdk) wiz.getProperty(PROP_PROJECT_SDK);
        parameters.put(ATTR_SDK_DIR, androidSdk.getSdkPath());
        int maxBuildLevel = (int) wiz.getProperty(PROP_MAX_BUILD_LEVEL);
        AndroidPlatformInfo platformInfo = (AndroidPlatformInfo) wiz.getProperty(platformPropertyName);
        int revision = platformInfo.getAndroidTarget().getRevision();
        Revision revisionBuildTool = platformInfo.getAndroidTarget().getBuildToolInfo().getRevision();
        parameters.put(ATTR_IS_NEW_PROJECT, true); // Android Modules are called Gradle Projects
        parameters.put(ATTR_THEME_EXISTS, true); // New modules always have a theme (unless its a library, but it will have no activity)

        parameters.put(ATTR_MIN_API_LEVEL, platformInfo.getAndroidVersion().getApiLevel());
        parameters.put(ATTR_MIN_API, platformInfo.getAndroidVersion().getApiString());
        parameters.put(ATTR_BUILD_API, maxBuildLevel);
        parameters.put(ATTR_BUILD_API_STRING, String.valueOf(maxBuildLevel));
        parameters.put(ATTR_TARGET_API, platformInfo.getAndroidVersion().getApiLevel());
        parameters.put(ATTR_TARGET_API_STRING, platformInfo.getAndroidVersion().getApiString());
        parameters.put(ATTR_BUILD_API_REVISION, revision);
        parameters.put(ATTR_BUILD_TOOLS_VERSION, revisionBuildTool.toString());

        parameters.put(ATTR_GRADLE_PLUGIN_VERSION, SdkConstants.GRADLE_PLUGIN_LATEST_VERSION);
        parameters.put(ATTR_GRADLE_VERSION, SdkConstants.GRADLE_LATEST_VERSION);
        parameters.put(ATTR_IS_INSTANT_APP, false);
        parameters.put(ATTR_JAVA_VERSION, "1.7");
        parameters.put(ATTR_KOTLIN_VERSION, "1.0.0");
        parameters.put(ATTR_IS_LOW_MEMORY, false);
        parameters.put(ATTR_IS_GRADLE, true);
        parameters.put(ATTR_MAKE_IGNORE, true);
        parameters.put("target.files", new HashSet<>());
        parameters.put("files.to.open", new ArrayList<>());
        parameters.put(ATTR_CREATE_ACTIVITY, true);

    }

    public static void setupModuleRoots(Map<String, Object> parameters, WizardDescriptor wiz, String moduleDirectory) {
        File projectRoot = (File) wiz.getProperty(AndroidProjectTemplatePanelVisualBasicSettings.PROP_PROJECT_DIR);
        File moduleRoot = new File(projectRoot.getPath() + File.separator + moduleDirectory);
        String packageName = (String) wiz.getProperty(AndroidProjectTemplatePanelVisualBasicSettings.PROP_PROJECT_PACKAGE);
        assert moduleRoot != null;

        parameters.put(ATTR_TOP_OUT, projectRoot.getAbsolutePath());
        // Register the resource directories associated with the active source provider
        parameters.put(ATTR_PROJECT_OUT, moduleRoot.getAbsolutePath());

        File srcDir = new File(moduleRoot.getPath() + File.separator + "src" + File.separator + "main" + File.separator + "java");
        parameters.put(ATTR_SRC_DIR, getRelativePath(moduleRoot, srcDir) + File.separator + packageName.replace(".", File.separator));
        parameters.put(ATTR_SRC_OUT, srcDir.getAbsolutePath() + File.separator + packageName.replace(".", File.separator));

        File testDir = new File(moduleRoot.getPath() + File.separator + "src" + File.separator + "main" + File.separator + "test");
        parameters.put(ATTR_TEST_DIR, getRelativePath(moduleRoot, testDir));
        parameters.put(ATTR_TEST_OUT, testDir.getAbsolutePath());

        File resDir = new File(moduleRoot.getPath() + File.separator + "src" + File.separator + "main" + File.separator + "res");
        parameters.put(ATTR_RES_DIR, getRelativePath(moduleRoot, resDir));
        parameters.put(ATTR_RES_OUT, resDir.getPath());

        File manifestDir = new File(moduleRoot.getPath() + File.separator + "src" + File.separator + "main");

        parameters.put(ATTR_MANIFEST_DIR, getRelativePath(moduleRoot, manifestDir));
        parameters.put(ATTR_MANIFEST_OUT, manifestDir.getPath());

        File aidlDir = new File(moduleRoot.getPath() + File.separator + "src" + File.separator + "main" + File.separator + "aidl");

        parameters.put(ATTR_AIDL_DIR, getRelativePath(moduleRoot, aidlDir));
        parameters.put(ATTR_AIDL_OUT, aidlDir.getPath());

        parameters.put(ATTR_PROJECT_LOCATION, moduleRoot.getParent());

        // We're really interested in the directory name on disk, not the module name. These will be different if you give a module the same
        // name as its containing project.
        parameters.put(ATTR_MODULE_NAME, moduleRoot.getName());
        parameters.put(ATTR_PACKAGE_NAME, packageName);
    }

    public static String getRelativePath(final File dir, final File file) {
        Stack<String> stack = new Stack<String>();
        File tempFile = file;
        while (tempFile != null && !tempFile.equals(dir)) {
            stack.push(tempFile.getName());
            tempFile = tempFile.getParentFile();
        }
        assert tempFile != null : file.getAbsolutePath() + "not found in " + dir.getAbsolutePath();//NOI18N
        StringBuilder retval = new StringBuilder();
        while (!stack.isEmpty()) {
            retval.append(stack.pop());
            if (!stack.isEmpty()) {
                retval.append('/');//NOI18N
            }
        }
        return retval.toString();
    }
}
