/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.android.spi;

import javax.swing.JPanel;
import org.netbeans.modules.android.options.AndroidOptionsPanelController;

/**
 *
 * @author arsi
 */
public abstract class AndroidOptionsSubPanel extends JPanel {

    protected final AndroidOptionsPanelController controller;

    public AndroidOptionsSubPanel(AndroidOptionsPanelController controller) {
        this.controller = controller;
    }

    public abstract String getCategory();

    public abstract void load();

    public abstract void store();

    public abstract boolean valid();

    @Override
    public String toString() {
        return getCategory();
    }


}
