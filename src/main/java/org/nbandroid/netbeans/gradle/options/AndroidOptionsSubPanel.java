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
package org.nbandroid.netbeans.gradle.options;

import javax.swing.JPanel;

/**
 * Holder for one part of Android options panel.
 *
 * @author radim
 */
public abstract class AndroidOptionsSubPanel extends JPanel {

    public abstract boolean valid();

    public abstract void store();

    public abstract void load();
}
