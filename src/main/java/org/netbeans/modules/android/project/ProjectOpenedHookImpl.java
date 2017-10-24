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

package org.netbeans.modules.android.project;

import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.modules.android.core.sdk.StatsCollector;
import org.netbeans.modules.android.project.queries.ClassPathProviderImpl;
import org.netbeans.spi.project.ui.ProjectOpenedHook;

final class ProjectOpenedHookImpl extends ProjectOpenedHook {

    private final AndroidProject project;
    private final PropertiesHelper propHelper;

    ProjectOpenedHookImpl(AndroidProject project, PropertiesHelper propHelper) {
        this.project = project;
        this.propHelper = propHelper;
    }

    protected @Override void projectOpened() {
        // XXX check for missing build.xml and try to upgrade too
        project.getLookup().lookup(ClassPathProviderImpl.class).register();
        propHelper.updateProperties(DalvikPlatformManager.getDefault());

        StatsCollector.getDefault().incrementCounter("antproject");
    }

    protected @Override void projectClosed() {
        project.getLookup().lookup(ClassPathProviderImpl.class).unregister();
    }

}
