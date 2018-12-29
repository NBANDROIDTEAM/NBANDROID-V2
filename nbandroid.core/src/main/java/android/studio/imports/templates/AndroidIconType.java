/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.studio.imports.templates;

/**
 * Categories of Android icon purposes - each type has its own rules for
 * generating a final look and feel.
 */
public enum AndroidIconType {
    /**
     * Launcher icon to be shown in the application list.
     */
    LAUNCHER("Launcher Icons (Adaptive and Legacy)", "ic_launcher"),
    /**
     * Launcher icon to be shown in the application list.
     */
    LAUNCHER_LEGACY("Launcher Icons (Legacy only)", "ic_launcher"),
    /**
     * Icons shown in the action bar
     */
    ACTIONBAR("Action Bar and Tab Icons", "ic_action_%s"),
    /**
     * Icons shown in a notification message
     */
    NOTIFICATION("Notification Icons", "ic_stat_%s");

    private final String myDisplayName;
    /**
     * Default asset name format, for use in generating a name for the final
     * asset.
     */
    private final String myDefaultNameFormat;

    AndroidIconType(String displayName, String defaultNameFormat) {
        myDisplayName = displayName;
        myDefaultNameFormat = defaultNameFormat;
    }

    public String getDisplayName() {
        return myDisplayName;
    }

    /**
     * Converts a value like 'name' to the icon appropriate version, e.g.
     * 'icon_stat_name' for notification icons.
     */
    public String toOutputName(String baseName) {
        return String.format(myDefaultNameFormat, baseName);
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
