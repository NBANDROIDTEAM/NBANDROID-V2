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
import org.nbandroid.netbeans.gradle.v2.layout.values.completion.AndroidValueType;

/**
 *
 * @author arsi
 */
public interface StyleableIconProvider {

    /**
     * Return icon for Styleable type type: AndroidStyleableType.WIDGET_ICON or
     * AndroidStyleableType.LAYOUT_ICON
     * or null if not supported
     *
     * @param fullClassName
     * @param androidStyleableType
     * @return
     */
    public ImageIcon getIcon(String fullClassName, AndroidStyleableType androidStyleableType);

    /**
     * return icon for Widget attribute
     * or null if not supported
     *
     * @return
     */
    public ImageIcon getWidgetAttrIcon();

    /**
     * return icon for Widget Layout attribute (LayoutParams) or null if not
     * supported
     *
     * @return
     */
    public ImageIcon getWidgetLayoutAttrIcon();

    /**
     * return icon for Layout attribute or null if not supported
     *
     * @return
     */
    public ImageIcon getLayoutAttrIcon();

    /**
     * return icon for Tools attribute or null if not supported
     *
     * @return
     */
    public ImageIcon getToolsAttrIcon();

    public ImageIcon getValuesIcon(AndroidValueType type);

}
