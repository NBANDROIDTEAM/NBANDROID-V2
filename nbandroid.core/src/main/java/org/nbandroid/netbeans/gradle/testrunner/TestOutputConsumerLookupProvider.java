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
package org.nbandroid.netbeans.gradle.testrunner;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.LookupProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Action provider of the Android project.
 */
@LookupProvider.Registration(
        projectTypes = @LookupProvider.Registration.ProjectType(id = "org-netbeans-modules-android-project", position = 400))
public class TestOutputConsumerLookupProvider implements LookupProvider {

    private static final Logger LOG = Logger.getLogger(TestOutputConsumerLookupProvider.class.getName());

    @Override
    public Lookup createAdditionalLookup(Lookup lkp) {
        LOG.log(Level.FINER, "createAdditionalLookup {0}", lkp);
        final Project aPrj = lkp.lookup(Project.class);
        return Lookups.singleton(new TestOutputUIDisplayer(aPrj));
    }
}
