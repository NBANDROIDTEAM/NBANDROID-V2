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

package org.nbandroid.netbeans.gradle.v2.layout.completion;

import javax.swing.ImageIcon;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleableType;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Default StyleableIconProvider that return icon by styleable type
 *
 * @author arsi
 */
@ServiceProvider(service = StyleableIconProvider.class)
public class DefaultStyleableIconProvider implements StyleableIconProvider {

    @StaticResource
    private static final String LAYOUT_ICON_RES = "org/nbandroid/netbeans/gradle/v2/layout/layout.png";
    public static final ImageIcon LAYOUT_ICON = new ImageIcon(ImageUtilities.loadImage(LAYOUT_ICON_RES));

    @StaticResource
    private static final String WIDGET_ICON_RES = "org/nbandroid/netbeans/gradle/v2/layout/widget.png";
    public static final ImageIcon WIDGET_ICON = new ImageIcon(ImageUtilities.loadImage(WIDGET_ICON_RES));

    @StaticResource
    private static final String WIDGET_ATTR_ICON_RES = "org/nbandroid/netbeans/gradle/v2/layout/widget_attr.png";
    public static final ImageIcon WIDGET_ATTR = new ImageIcon(ImageUtilities.loadImage(WIDGET_ATTR_ICON_RES));

    @StaticResource
    private static final String LAYOUT_ATTR_ICON_RES = "org/nbandroid/netbeans/gradle/v2/layout/layout_attr.png";
    public static final ImageIcon LAYOUT_ATTR = new ImageIcon(ImageUtilities.loadImage(LAYOUT_ATTR_ICON_RES));

    @StaticResource
    private static final String WIDGET_LAYOUT_ATTR_ICON_RES = "org/nbandroid/netbeans/gradle/v2/layout/widget_layout_attr.png";
    public static final ImageIcon WIDGET_LAYOUT_ATTR = new ImageIcon(ImageUtilities.loadImage(WIDGET_LAYOUT_ATTR_ICON_RES));

    @StaticResource
    private static final String TOOLS_ATTR_ICON_RES = "org/nbandroid/netbeans/gradle/v2/layout/tools_attr.png";
    public static final ImageIcon TOOLS_ATTR_ICON = new ImageIcon(ImageUtilities.loadImage(TOOLS_ATTR_ICON_RES));

    @Override
    public ImageIcon getIcon(String fullClassName, AndroidStyleableType androidStyleableType) {
        switch (androidStyleableType) {

            case Widget:
                return WIDGET_ICON;
            case Layout:
                return LAYOUT_ICON;
        }
        return null;
    }

    @Override
    public ImageIcon getWidgetAttrIcon() {
        return WIDGET_ATTR;
    }

    @Override
    public ImageIcon getLayoutAttrIcon() {
        return LAYOUT_ATTR;
    }

    @Override
    public ImageIcon getWidgetLayoutAttrIcon() {
        return WIDGET_LAYOUT_ATTR;
    }

    @Override
    public ImageIcon getToolsAttrIcon() {
        return TOOLS_ATTR_ICON;
    }

}
