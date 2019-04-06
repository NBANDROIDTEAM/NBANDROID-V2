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

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.AuxiliaryProperties;

/**
 *
 * @author arsi
 */
public class RunActivityConfiguration implements MainActivityConfiguration {

    private final Project project;
    private final AuxiliaryProperties auxProps;

    private boolean useFromManifest;
    private boolean askBeforeLaunch;
    private String mainActivity;

    public static final String USE_FROM_MANIFEST = "USE_FROM_MANIFEST";
    public static final String ASK_BEFORE_LAUNCH = "ASK_BEFORE_LAUNCH";
    public static final String MAIN_ACTIVITY = "MAIN_ACTIVITY";

    public RunActivityConfiguration(Project project) {
        this.project = project;
        auxProps = project.getLookup().lookup(AuxiliaryProperties.class);
        String tmp = auxProps.get(USE_FROM_MANIFEST, false);
        if (tmp != null) {
            try {
                useFromManifest = Boolean.valueOf(tmp);
            } catch (Exception e) {
            }
        } else {
            useFromManifest = true;
        }
        tmp = auxProps.get(ASK_BEFORE_LAUNCH, false);
        if (tmp != null) {
            try {
                askBeforeLaunch = Boolean.valueOf(tmp);
            } catch (Exception e) {
            }
        } else {
            askBeforeLaunch = false;
        }
        mainActivity = auxProps.get(MAIN_ACTIVITY, false);
    }

    @Override
    public void setUseFromManifest(boolean useFromManifest) {
        this.useFromManifest = useFromManifest;
        auxProps.put(USE_FROM_MANIFEST, Boolean.toString(useFromManifest), false);
    }

    @Override
    public void setAskBeforeLaunch(boolean askBeforeLaunch) {
        this.askBeforeLaunch = askBeforeLaunch;
        auxProps.put(ASK_BEFORE_LAUNCH, Boolean.toString(askBeforeLaunch), false);
    }

    @Override
    public void setMainActivity(String mainActivity) {
        this.mainActivity = mainActivity;
        auxProps.put(MAIN_ACTIVITY, mainActivity, false);
    }

    @Override
    public boolean isUseFromManifest() {
        return useFromManifest;
    }

    @Override
    public boolean isAskBeforeLaunch() {
        return askBeforeLaunch;
    }

    @Override
    public String getMainActivity() {
        return mainActivity;
    }


}
