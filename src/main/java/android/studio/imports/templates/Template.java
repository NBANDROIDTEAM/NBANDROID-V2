/*
 * Copyright (C) 2013 The Android Open Source Project
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
 * Handler which manages instantiating FreeMarker templates, copying resources
 * and merging into existing files
 */
public class Template {

    public static final String TEMPLATE_XML_NAME = "template.xml";
    // Various tags and attributes used in template.xml
    public static final String TAG_EXECUTE = "execute";
    public static final String TAG_GLOBALS = "globals";
    public static final String TAG_GLOBAL = "global";
    public static final String TAG_PARAMETER = "parameter";
    public static final String TAG_THUMB = "thumb";
    public static final String TAG_THUMBS = "thumbs";
    public static final String TAG_DEPENDENCY = "dependency";
    public static final String TAG_ICONS = "icons";
    public static final String ATTR_FORMAT = "format";
    public static final String ATTR_VALUE = "value";
    public static final String ATTR_DEFAULT = "default";
    public static final String ATTR_SUGGEST = "suggest";
    public static final String ATTR_ID = "id";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_DESCRIPTION = "description";
    public static final String ATTR_TYPE = "type";
    public static final String ATTR_HELP = "help";
    public static final String ATTR_FILE = "file";
    public static final String ATTR_CONSTRAINTS = "constraints";
    public static final String ATTR_VISIBILITY = "visibility";
    public static final String ATTR_ENABLED = "enabled";
    public static final String CATEGORY_ACTIVITIES = "activities";
    public static final String CATEGORY_PROJECTS = "gradle-projects";
    public static final String CATEGORY_OTHER = "other";
    public static final String CATEGORY_APPLICATION = "Application";

    /**
     * Highest supported format; templates with a higher number will be skipped
     * <p/>
     * <ul>
     * <li> 1: Initial format, supported by ADT 20 and up.
     * <li> 2: ADT 21 and up. Boolean variables that have a default value and
     * are not edited by the user would end up as strings in ADT 20; now they
     * are always proper Booleans. Templates which rely on this should specify
     * format >= 2.
     * <li> 3: The wizard infrastructure passes the {@code isNewProject} boolean
     * variable to indicate whether a wizard is created as part of a new blank
     * project
     * <li> 4: Constraint type app_package ({@link Constraint#APP_PACKAGE}),
     * provides srcDir, resDir and manifestDir variables for locations of files
     * <li> 5: All files are relative to the template instead of using an
     * implicit "root" folder.
     * </ul>
     */
    static final int CURRENT_FORMAT = 5;

    /**
     * Templates from this version and up use relative (from this template) path
     * names. Recipe files from older versions uses an implicit "root" folder.
     */
    static final int RELATIVE_FILES_FORMAT = 5;

    private static final int MAX_WARNINGS = 10;
    private static final String GOOGLE_GLASS_PATH_19 = "/addon-google_gdk-google-19/";

}
