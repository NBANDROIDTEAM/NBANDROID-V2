/*
 * Copyright 2019 junichi11.
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
package com.junichi11.netbeans.modules.color.codes.preview.spi;

import java.awt.Color;

/**
 * Color value object for the color code.
 *
 * @author junichi11
 * @since 0.10.0
 */
public interface ColorValue {

    /**
     * Get the color.
     *
     * @since 0.10.0
     * @return the color
     */
    Color getColor();

    /**
     * Get the start offset of the color from the top of the line.
     *
     * @since 0.10.0
     * @return the start offset
     */
    int getStartOffset();

    /**
     * Get the end offset of the color from the top of the line.
     *
     * @since 0.10.0
     * @return the end offset
     */
    int getEndOffset();

    /**
     * Get the line number.
     *
     * @since 0.10.0
     * @return the line number
     */
    int getLine();

    /**
     * Get the color value.
     *
     * @since 0.10.0
     * @return the color value
     */
    String getValue();

    /**
     * Check whether the color is editable.
     *
     * @since 0.10.0
     * @return {@code true} if it is editable, otherwise {@code false}
     */
    boolean isEditable();

    /**
     * Get the formatter.
     *
     * @since 0.10.0
     * @return the formatter
     */
    ColorCodeFormatter getFormatter();
}
