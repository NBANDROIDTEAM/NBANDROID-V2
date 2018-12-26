/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.template;

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
        "Templates/Android/MobileTemplate.java",// NOI18N
        "Templates/Android/WearTemplate.java"// NOI18N
};

    private static final String[] APPLICATION_TYPES = new String[]{"java-classes", // NOI18N
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
