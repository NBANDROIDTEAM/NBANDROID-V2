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

import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

/**
 * ColorCodesPreviewOptionsPanel. If you would like to add options for your
 * provider, please use the panel extended this panel. (see
 * ColorCodesProvider.getOptionsPanel())
 *
 * @since 0.11.1
 * @author junichi11
 */
public abstract class ColorCodesPreviewOptionsPanel extends JPanel {

    /**
     * Empty panel. Instead, please use
     * ColorCodesPreviewOptionsPanel.createEmptyPanel().
     *
     * @deprecated
     * @since 0.11.1
     */
    public static final ColorCodesPreviewOptionsPanel EMPTY_PANEL = new ColorCodesPreviewOptionsEmptyPanel();
    private static final long serialVersionUID = -6323395294472234402L;

    /**
     * Create an empty panel. It can be used if Options is not needed.
     *
     * @since 0.12.1
     * @return an empty panel
     */
    public static ColorCodesPreviewOptionsPanel createEmptyPanel() {
        return new ColorCodesPreviewOptionsEmptyPanel();
    }

    /**
     * Load settings. This is invoked when the Options panel is opened. e.g. You
     * can set values to components of your panel from stored options.
     *
     * @since 0.11.1
     */
    public abstract void load();

    /**
     * Store settings. This is invoked when the OK button clicked in the Options
     * panel. e.g. You can store components values to your options.
     *
     * @since 0.11.1
     */
    public abstract void store();

    /**
     * Check whether form is consistent and complete.
     *
     * @since 0.11.1
     * @return {@code true} if the form is consistend and complete, otherwise
     * {@code false}
     */
    public abstract boolean valid();

    /**
     * Get the error message.
     *
     * @since 0.11.1
     * @return the error message
     */
    public abstract String getErrorMessage();

    /**
     * Add the ChangeListener.
     *
     * @since 0.11.1
     * @param listener the listener
     */
    public abstract void addChangeListener(ChangeListener listener);

    /**
     * Remove the ChangeListener.
     *
     * @since 0.11.1
     * @param listener the listener
     */
    public abstract void removeChangeListener(ChangeListener listener);

    //~ Nested class
    private static class ColorCodesPreviewOptionsEmptyPanel extends ColorCodesPreviewOptionsPanel {

        public ColorCodesPreviewOptionsEmptyPanel() {
        }
        private static final long serialVersionUID = 3772795247048065205L;

        @Override
        public void load() {
        }

        @Override
        public void store() {
        }

        @Override
        public boolean valid() {
            return true;
        }

        @Override
        public String getErrorMessage() {
            return null;
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
        }
    }
}
