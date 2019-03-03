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

package org.netbeans.modules.android.project.api;

import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;

/**
 *
 * @author arsi
 */
public class PrivilegedTemplatesImpl implements PrivilegedTemplates, RecommendedTemplates {

    private static final String[] PRIVILEGED_NAMES_1 = new String[]{"Templates/Classes/Class.java", // NOI18N
        "Templates/Classes/Package", // NOI18N
        "Templates/Classes/Interface.java", // NOI18N
        "Templates/Android/BroadcastReceiver.java",// NOI18N
        "Templates/Android/Service.java",// NOI18N
        "Templates/Android/MobileTemplate.java",// NOI18N
        "Templates/Android/WearTemplate.java",// NOI18N
        "Templates/Android/TvTemplate.java",// NOI18N
        "Templates/Android/ContentProvider.java",// NOI18N
        "Templates/Android/IntentService.java",// NOI18N
        "Templates/Android/SliceProvider.java",// NOI18N
};

    private static final String[] APPLICATION_TYPES = new String[]{"java-classes", // NOI18N
        "android-files", // NOI18N
        "android-activity", // NOI18N
        "XML", // NOI18N
};

    @Override
    public String[] getPrivilegedTemplates() {
        return PRIVILEGED_NAMES_1;
    }

    @Override
    public String[] getRecommendedTypes() {
        return APPLICATION_TYPES;
    }

}
