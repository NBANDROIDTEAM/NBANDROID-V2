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

import java.util.List;
import java.util.Map;
import javax.swing.text.Document;

/**
 * This provides support for showing the colors for the specific color code in
 * the editor side bar. You can add your provider via a ServiceProvider
 * annotation. e.g.
 * <pre>
 * &#64;ServiceProvider(service = ColorCodesProvider.class, position = 1000)
 * public class MyColorCodesProvider implements ColorCodesProvider {
 *     // implementations...
 * }
 * </pre>
 *
 * @author junichi11, arsi
 * @since 0.10.0
 */
public interface ColorCodesProvider {

    /**
     * Get the id for the specific provider.
     *
     * @return the id for the specific provider
     */
    public String getId();

    /**
     * Get the display name.
     *
     * @return the display name
     */
    public String getDisplayName();

    /**
     * Get the description.
     *
     * @return the description
     */
    public String getDescription();

    /**
     * Is this provider enabled.
     *
     * @since 0.10.0
     * @param document the document
     * @return {@code true} if enabled, otherwise {@code false}
     */
    public boolean isProviderEnabled(Document document);

    /**
     * Parse the line then return color values.
     *
     * @since 0.10.0
     * @param document the document
     * @param line the string to parse
     * @param lineNumber line number to store to ColorValue
     * @param variableColorValues save colors of variables to this map
     * @return color values
     */
    public List<ColorValue> getColorValues(Document document, String line, int lineNumber, Map<String, List<ColorValue>> variableColorValues);

    /**
     * Get the start position for parsing lines.
     *
     * @since 0.10.0
     * @param document the document
     * @param currentIndex the line number
     * @return the line number to start parsing
     */
    public int getStartIndex(Document document, int currentIndex);

    /**
     * Get the panel for Options.
     *
     * @since 0.11.1
     * @return the panel for Options
     */
    public ColorCodesPreviewOptionsPanel getOptionsPanel();

    /**
     * Check whether this provider provides generating color code feature. If it
     * is provided, please return ColorCodeGeneratorItems in
     * getColorCodeGeneratorItems().
     *
     * @since 0.12.1
     * @return {@code true} if this provider provides generating color code
     * feature, otherwise {@code false}
     */
    public boolean canGenerateColorCode();

    /**
     * Get items for color code generator.
     *
     * @since 0.12.1
     * @param mimeType the mime type
     * @return ColorCodeGenaratorItems
     */
    public List<ColorCodeGeneratorItem> getColorCodeGeneratorItems(String mimeType);

}
