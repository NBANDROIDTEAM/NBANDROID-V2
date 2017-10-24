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

import org.openide.util.NbBundle;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Misnamed storage of information for the android project wizard.
 */
public class FoldersListSettings {
    private static final FoldersListSettings INSTANCE = new FoldersListSettings();
    private static final String NEW_APP_COUNT = "newApplicationCount";  //NOI18N

    public static FoldersListSettings getDefault () {
        return INSTANCE;
    }

    private static Preferences getPreferences() {
        return NbPreferences.forModule(FoldersListSettings.class);
    }

    public String displayName() {
        return NbBundle.getMessage(FoldersListSettings.class, "TXT_J2SEProjectFolderList");
    }

    public int getNewApplicationCount () {
        return getPreferences().getInt(NEW_APP_COUNT, 0);
    }

    public void setNewApplicationCount (int count) {
        getPreferences().putInt(NEW_APP_COUNT, count);
    }

}
