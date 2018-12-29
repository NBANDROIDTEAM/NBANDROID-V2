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
package org.nbandroid.netbeans.gradle.v2.layout.tools;

import static com.android.SdkConstants.*;
import java.util.ArrayList;
import java.util.List;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleableAttr;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleableAttrEnum;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleableAttrType;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author arsi
 */
@ServiceProvider(service = ToolsNamespaceProvider.class)
public class DefaultToolsNamespaceProvider extends ToolsNamespaceProvider {

    // Manifest merger attribute names
    public static final String ATTR_NODE = "node";
    public static final String ATTR_STRICT = "strict";
    public static final String ATTR_REMOVE = "remove";
    public static final String ATTR_REPLACE = "replace";
    public static final String ATTR_OVERRIDE_LIBRARY = "overrideLibrary";
    private static final List<AndroidStyleableAttr> attrs = new ArrayList<>();

    public DefaultToolsNamespaceProvider() {
        loadAttrs();
    }

    private void loadAttrs() {

        attrs.add(new AndroidStyleableAttr(AndroidStyleableAttrType.Enum, ATTR_ACTION_BAR_NAV_MODE, escapeHTML("Attribute set on the root element of a layout to configure the navigation mode used by the Action Bar.\n"
                + " Possible values include: \"standard\", \"list\" and \"tabs\" Requires Studio 0.8.0 or later.\n"
                + "\n"
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    android:orientation=\"vertical\"\n"
                + "    android:layout_width=\"match_parent\"\n"
                + "    android:layout_height=\"match_parent\"\n"
                + "    tools:actionBarNavMode=\"tabs\" />"),
                new AndroidStyleableAttrEnum("standard", "", ""),
                new AndroidStyleableAttrEnum("list", "", ""),
                new AndroidStyleableAttrEnum("tabs", "", "")));

        attrs.add(new AndroidStyleableAttr(ATTR_CONTEXT, escapeHTML("This attribute is typically set on the root element in a layout XML file,\n"
                + " and records which activity the layout is associated with (at designtime, since obviously a layout can be used by more than one layout).\n"
                + " This will for example be used by the layout editor to guess a default theme, since themes are defined in the Manifest\n "
                + "and are associated with activities, not layouts. \n"
                + "You can use the same dot prefix as in manifests to just specify the activity class without the full application package name\n"
                + " as a prefix.\n"
                + "\n"
                + "<android.support.v7.widget.GridLayout xmlns:android=\"http://schemas.android.com/apk/res/android\" xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    tools:context=\".MainActivity\" ... >"),
                AndroidStyleableAttrType.String, AndroidStyleableAttrType.Reference));
        attrs.add(new AndroidStyleableAttr(ATTR_IGNORE, escapeHTML("The following attributes help suppress lint warning messages.\n"
                + "tools:ignore\n"
                + "\n"
                + "Intended for: Any element\n"
                + "\n"
                + "Used by: Lint\n"
                + "\n"
                + "This attribute accepts a comma-separated list of lint issue ID's that you'd like the tools to ignore on this element or any of its decendents.\n"
                + "\n"
                + "For example, you can tell the tools to ignore the MissingTranslation error:\n"
                + "\n"
                + "<string name=\"show_all_apps\" tools:ignore=\"MissingTranslation\">All</string>\n"
                + ""),
                AndroidStyleableAttrType.String));
        attrs.add(new AndroidStyleableAttr(ATTR_LISTFOOTER, escapeHTML("Intended for: <AdapterView> (and subclasses like <ListView>)\n"
                + "\n"
                + "Used by: Android Studio layout editor\n"
                + "\n"
                + "These attributes specify which layout to show in the layout preview for a list's items, header, and footer.\n "
                + "Any data fields in the layout are filled with numeric contents such as \"Item 1\" so that the list items are not repetitive.\n"
                + "\n"
                + "For example:\n"
                + "\n"
                + "<ListView xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    android:id=\"@android:id/list\"\n"
                + "    android:layout_width=\"match_parent\"\n"
                + "    android:layout_height=\"match_parent\"\n"
                + "    tools:listitem=\"@layout/sample_list_item\"\n"
                + "    tools:listheader=\"@layout/sample_list_header\"\n"
                + "    tools:listfooter=\"@layout/sample_list_footer\" />\n"
                + "\n"
                + "Note: These attributes don't work for ListView in Android Studio 2.2, but this is fixed in 2.3 (issue 215172)."),
                AndroidStyleableAttrType.Reference));
        attrs.add(new AndroidStyleableAttr(ATTR_LISTHEADER, escapeHTML("Intended for: <AdapterView> (and subclasses like <ListView>)\n"
                + "\n"
                + "Used by: Android Studio layout editor\n"
                + "\n"
                + "These attributes specify which layout to show in the layout preview for a list's items, header, and footer.\n"
                + " Any data fields in the layout are filled with numeric contents such as \"Item 1\" so that the list items are not repetitive.\n"
                + "\n"
                + "For example:\n"
                + "\n"
                + "<ListView xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    android:id=\"@android:id/list\"\n"
                + "    android:layout_width=\"match_parent\"\n"
                + "    android:layout_height=\"match_parent\"\n"
                + "    tools:listitem=\"@layout/sample_list_item\"\n"
                + "    tools:listheader=\"@layout/sample_list_header\"\n"
                + "    tools:listfooter=\"@layout/sample_list_footer\" />\n"
                + "\n"
                + "Note: These attributes don't work for ListView in Android Studio 2.2, but this is fixed in 2.3 (issue 215172)."),
                AndroidStyleableAttrType.Reference));
        attrs.add(new AndroidStyleableAttr(ATTR_LISTITEM, escapeHTML("Intended for: <AdapterView> (and subclasses like <ListView>)\n"
                + "\n"
                + "Used by: Android Studio layout editor\n"
                + "\n"
                + "These attributes specify which layout to show in the layout preview for a list's items, header, and footer.\n"
                + " Any data fields in the layout are filled with numeric contents such as \"Item 1\" so that the list items are not repetitive.\n"
                + "\n"
                + "For example:\n"
                + "\n"
                + "<ListView xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    android:id=\"@android:id/list\"\n"
                + "    android:layout_width=\"match_parent\"\n"
                + "    android:layout_height=\"match_parent\"\n"
                + "    tools:listitem=\"@layout/sample_list_item\"\n"
                + "    tools:listheader=\"@layout/sample_list_header\"\n"
                + "    tools:listfooter=\"@layout/sample_list_footer\" />\n"
                + "\n"
                + "Note: These attributes don't work for ListView in Android Studio 2.2, but this is fixed in 2.3 (issue 215172)."),
                AndroidStyleableAttrType.Reference));
        attrs.add(new AndroidStyleableAttr(ATTR_LAYOUT, escapeHTML("Intended for: <fragment>\n"
                + "\n"
                + "Used by: Android Studio layout editor\n"
                + "\n"
                + "This attribute declares which layout you want the layout preview to draw inside the fragment \n"
                + "(because the layout preview cannot execute the activity code that normally applies the layout).\n"
                + "\n"
                + "For example:\n"
                + "\n"
                + "<fragment android:name=\"com.example.master.ItemListFragment\"\n"
                + "    tools:layout=\"@layout/list_content\" />\n"
                + ""),
                AndroidStyleableAttrType.Reference));
        attrs.add(new AndroidStyleableAttr(ATTR_LOCALE, escapeHTML("Intended for: <resources>\n"
                + "\n"
                + "Used by: Lint, Android Studio editor\n"
                + "\n"
                + "This tells the tools what the default language/locale is for the resources in the given <resources> element\n"
                + " (because the tools otherwise assume English) in order to avoid warnings from the spell checker. \n"
                + "The value must be a valid locale qualifier.\n"
                + "\n"
                + "For example, you can add this to your values/strings.xml file (the default string values)\n"
                + " to indicate that the language used for the default strings is Spanish rather than English:\n"
                + "\n"
                + "<resources xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    tools:locale=\"es\">\n"
                + ""),
                AndroidStyleableAttrType.Locale));
        attrs.add(new AndroidStyleableAttr(ATTR_MENU, escapeHTML("Intended for: Any root <View>\n"
                + "\n"
                + "Used by: Android Studio layout editor\n"
                + "\n"
                + "This attribute specifies which menu the layout preview should show in the app bar.\n"
                + " The value can be one or more menu IDs, separated by commas\n"
                + " (without @menu/ or any such ID prefix and without the .xml extension). For example:\n"
                + "\n"
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    android:orientation=\"vertical\"\n"
                + "    android:layout_width=\"match_parent\"\n"
                + "    android:layout_height=\"match_parent\"\n"
                + "    tools:menu=\"menu1,menu2\" />\n"
                + ""),
                AndroidStyleableAttrType.String));
        attrs.add(new AndroidStyleableAttr(ATTR_MOCKUP, "",
                AndroidStyleableAttrType.String));
        attrs.add(new AndroidStyleableAttr(ATTR_MOCKUP_OPACITY, "",
                AndroidStyleableAttrType.Float));
        attrs.add(new AndroidStyleableAttr(ATTR_MOCKUP_CROP, "",
                AndroidStyleableAttrType.String));
        attrs.add(new AndroidStyleableAttr(AndroidStyleableAttrType.Enum, ATTR_OPEN_DRAWER, escapeHTML("Intended for: <DrawerLayout>\n"
                + "\n"
                + "Used by: Android Studio layout editor\n"
                + "\n"
                + "This attribute allows you to open a DrawerLayout in the Preview pane of the layout editor. \n"
                + "<android.support.v4.widget.DrawerLayout\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    android:id=\"@+id/drawer_layout\"\n"
                + "    android:layout_width=\"match_parent\"\n"
                + "    android:layout_height=\"match_parent\"\n"
                + "    tools:openDrawer=\"start\" />\n"
                + ""),
                new AndroidStyleableAttrEnum("end", "800005", "Push object to the end of its container, not changing its size."),
                new AndroidStyleableAttrEnum("left", "3", "Push object to the left of its container, not changing its size."),
                new AndroidStyleableAttrEnum("right", "5", "Push object to the right of its container, not changing its size."),
                new AndroidStyleableAttrEnum("start", "800003", "Push object to the beginning of its container, not changing its size.")));
        attrs.add(new AndroidStyleableAttr(ATTR_PARENT_TAG, escapeHTML("There is a new parentTag tools attribute (added in Android Studio 2.2)\n"
                + " that you can use to specify the layout type for a merge tag, which will make the layout render correctly\n"
                + " in the layout editor preview.\n"
                + "\n"
                + "So using your example:\n"
                + "\n"
                + "<merge xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    android:layout_width=\"match_parent\"\n"
                + "    android:layout_height=\"match_parent\"\n"
                + "    tools:parentTag=\"LinearLayout\"\n"
                + "    tools:orientation=\"horizontal\">\n"
                + "\n"
                + "    <TextView\n"
                + "        android:layout_width=\"wrap_content\"\n"
                + "        android:layout_height=\"wrap_content\"\n"
                + "        android:text=\"Some text\"\n"
                + "        android:textSize=\"20sp\"/>\n"
                + "\n"
                + "    <TextView\n"
                + "        android:layout_width=\"wrap_content\"\n"
                + "        android:layout_height=\"wrap_content\"\n"
                + "        android:text=\"Some other text\"/>\n"
                + "</merge>\n"
                + "\n"
                + "Note: Both android:layout_width and android:layout_height must be specified in order for the layout to display properly in the editor."),
                AndroidStyleableAttrType.String));
        attrs.add(new AndroidStyleableAttr(ATTR_SHOW_IN, escapeHTML("Intended for: Any root <View> in a layout that's referred to by an <include>\n"
                + "\n"
                + "Used by: Android Studio layout editor\n"
                + "\n"
                + "This attribute allows you to point to a layout that uses this layout as an include, so you can preview (and edit) \n"
                + "this file as it appears while embedded in its parent layout.\n"
                + "\n"
                + "For example:\n"
                + "\n"
                + "<TextView xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    android:text=\"@string/hello_world\"\n"
                + "    android:layout_width=\"wrap_content\"\n"
                + "    android:layout_height=\"wrap_content\"\n"
                + "    tools:showIn=\"@layout/activity_main\" />\n"
                + "\n"
                + "Now the layout preview shows this TextView layout as it appears inside the activity_main layout."),
                AndroidStyleableAttrType.Reference));
        attrs.add(new AndroidStyleableAttr(ATTR_TARGET_API, escapeHTML("Intended for: Any element\n"
                + "\n"
                + "Used by: Lint\n"
                + "\n"
                + "This attribute works the same as the @TargetApi annotation in Java code: it lets you specify the API level\n"
                + " (either as an integer or as a code name) that supports this element.\n"
                + "\n"
                + "This tells the tools that you believe this element (and any children) will be used only on the specified API level or higher.\n"
                + " This stops lint from warning you if that element or its attributes are not available on the API level you specify\n"
                + " as your minSdkVersion.\n"
                + "\n"
                + "For example, you might use this because GridLayout is available only on API level 14 and higher, but you know\n"
                + " this layout is not used for any lower versions:\n"
                + "\n"
                + "<GridLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    tools:targetApi=\"14\" >\n"
                + "\n"
                + "However, you should instead use GridLayout from the support library."),
                AndroidStyleableAttrType.Integer));
        attrs.add(new AndroidStyleableAttr(AndroidStyleableAttrType.Enum, ATTR_NODE, "To apply a merge rule to an entire XML element (to all attributes in a given manifest element and to all its child tags)",
                new AndroidStyleableAttrEnum("merge", "", "Merge all attributes in this tag and all nested elements when there are no conflicts using the merge conflict heuristics. This is the default behavior for elements."),
                new AndroidStyleableAttrEnum("merge-only-attributes", "", "Merge attributes in this tag only; do not merge nested elements."),
                new AndroidStyleableAttrEnum("remove", "", "Remove this element from the merged manifest. Although it seems like you should instead just delete this element, using this is necessary when you discover an element in your merged manifest that you don't need, and it was provided by a lower-priority manifest file that's out of your control (such as an imported library). "),
                new AndroidStyleableAttrEnum("removeAll", "", "Like tools:node=\"remove\", but it removes all elements matching this element type (within the same parent element). "),
                new AndroidStyleableAttrEnum("replace", "", "Replace the lower-priority element completely. That is, if there is a matching element in the lower-priority manifest, ignore it and use this element exactly as it appears in this manifest. "),
                new AndroidStyleableAttrEnum("strict", "", "Generate a build failure any time this element in the lower-priority manifest does not exactly match it in the higher-priority manifest (unless resolved by other merge rule markers). This overrides the merge conflict heuristics. For example, if the lower-priority manifest simply includes an extra attribute, the build fails (whereas the default behavior adds the extra attribtue to the merged manifest). ")));

        attrs.add(new AndroidStyleableAttr(AndroidStyleableAttrType.Enum, ATTR_SHRINK_MODE, escapeHTML("Intended for: <resources>\n"
                + "\n"
                + "Used by: Build tools with resource shrinking\n"
                + "\n"
                + "This attribute allows you to specify whether the build tools should use \"safe mode\" \n"
                + "(play it safe and keep all resources that are explicitly cited and that might be referenced dynamically\n"
                + " with a call to Resources.getIdentifier()) or \"strict mode\" (keep only the resources that are explicitly \n"
                + "cited in code or in other resources).\n"
                + "\n"
                + "The default is to use safe mode (shrinkMode=\"safe\"). To instead use strict mode, \n"
                + "add shrinkMode=\"strict\" to the <resources> tag as shown here:\n"
                + "\n"
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<resources xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    tools:shrinkMode=\"strict\" />\n"
                + "\n"
                + "When you enable strict mode, you may need to use tools:keep to keep resources that were removed\n"
                + " but that you actually want, and use tools:discard to explicitly remove even more resources.\n"
                + "\n"
                + "For more information, see Shrink your resources."),
                new AndroidStyleableAttrEnum("safe", "", ""),
                new AndroidStyleableAttrEnum("strict", "", "")
        ));
        attrs.add(new AndroidStyleableAttr(ATTR_KEEP, escapeHTML("Intended for: <resources>\n"
                + "\n"
                + "Used by: Build tools with resource shrinking\n"
                + "\n"
                + "When using resource shrinking to remove unused resources, this attribute allows you to specify resources \n"
                + "to keep (typically because they are referenced in an indirect way at runtime, \n"
                + "such as by passing a dynamically generated resource name to Resources.getIdentifier()).\n"
                + "\n"
                + "To use, create an XML file in your resources directory (for example, at res/raw/keep.xml) with a <resources>\n"
                + " tag and specify each resource to keep in the tools:keep attribute as a comma-separated list. \n"
                + "You can use the asterisk character as a wild card. For example:\n"
                + "\n"
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<resources xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    tools:keep=\"@layout/used_1,@layout/used_2,@layout/*_3\" />\n"
                + "\n"
                + "For more information, see Shrink your resources."),
                AndroidStyleableAttrType.Reference));
        attrs.add(new AndroidStyleableAttr(ATTR_DISCARD, escapeHTML("Intended for: <resources>\n"
                + "\n"
                + "Used by: Build tools with resource shrinking\n"
                + "\n"
                + "When using resource shrinking to strip out unused resources, this attribute allows you to specify\n"
                + " resources you want to manually discard (typically because the resource is referenced\n"
                + " but in a way that does not affect your app,\n"
                + " or because the Gradle plugin has incorrectly deduced that the resource is referenced).\n"
                + "\n"
                + "To use, create an XML file in your resources directory (for example, at res/raw/keep.xml)\n"
                + " with a <resources> tag and specify each resource to keep in the tools:discard attribute as a comma-separated list.\n"
                + " You can use the asterisk character as a wild card. For example:\n"
                + "\n"
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<resources xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    tools:discard=\"@layout/unused_1\" />\n"
                + "\n"
                + "For more information, see Shrink your resources."),
                AndroidStyleableAttrType.Reference));

    }

    @Override
    public List<AndroidStyleableAttr> getToolsAttrs() {
        return attrs;
    }

    public static final String escapeHTML(String s) {
        StringBuilder sb = new StringBuilder();
        int n = s.length();
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case 'à':
                    sb.append("&agrave;");
                    break;
                case 'À':
                    sb.append("&Agrave;");
                    break;
                case 'â':
                    sb.append("&acirc;");
                    break;
                case 'Â':
                    sb.append("&Acirc;");
                    break;
                case 'ä':
                    sb.append("&auml;");
                    break;
                case 'Ä':
                    sb.append("&Auml;");
                    break;
                case 'å':
                    sb.append("&aring;");
                    break;
                case 'Å':
                    sb.append("&Aring;");
                    break;
                case 'æ':
                    sb.append("&aelig;");
                    break;
                case 'Æ':
                    sb.append("&AElig;");
                    break;
                case 'ç':
                    sb.append("&ccedil;");
                    break;
                case 'Ç':
                    sb.append("&Ccedil;");
                    break;
                case 'é':
                    sb.append("&eacute;");
                    break;
                case 'É':
                    sb.append("&Eacute;");
                    break;
                case 'è':
                    sb.append("&egrave;");
                    break;
                case 'È':
                    sb.append("&Egrave;");
                    break;
                case 'ê':
                    sb.append("&ecirc;");
                    break;
                case 'Ê':
                    sb.append("&Ecirc;");
                    break;
                case 'ë':
                    sb.append("&euml;");
                    break;
                case 'Ë':
                    sb.append("&Euml;");
                    break;
                case 'ï':
                    sb.append("&iuml;");
                    break;
                case 'Ï':
                    sb.append("&Iuml;");
                    break;
                case 'ô':
                    sb.append("&ocirc;");
                    break;
                case 'Ô':
                    sb.append("&Ocirc;");
                    break;
                case 'ö':
                    sb.append("&ouml;");
                    break;
                case 'Ö':
                    sb.append("&Ouml;");
                    break;
                case 'ø':
                    sb.append("&oslash;");
                    break;
                case 'Ø':
                    sb.append("&Oslash;");
                    break;
                case 'ß':
                    sb.append("&szlig;");
                    break;
                case 'ù':
                    sb.append("&ugrave;");
                    break;
                case 'Ù':
                    sb.append("&Ugrave;");
                    break;
                case 'û':
                    sb.append("&ucirc;");
                    break;
                case 'Û':
                    sb.append("&Ucirc;");
                    break;
                case 'ü':
                    sb.append("&uuml;");
                    break;
                case 'Ü':
                    sb.append("&Uuml;");
                    break;
                case '®':
                    sb.append("&reg;");
                    break;
                case '©':
                    sb.append("&copy;");
                    break;
                case '€':
                    sb.append("&euro;");
                    break;
                case '\n':
                    sb.append("<br>");
                    break;
                // be carefull with this one (non-breaking whitee space)
                case ' ':
                    sb.append("&nbsp;");
                    break;

                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

}
