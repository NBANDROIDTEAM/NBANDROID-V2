/*
 * Copyright (C) 2015 The Android Open Source Project
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

package org.nbandroid.netbeans.gradle.v2.project.template.freemarker.converters;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import java.util.List;
import java.util.Map;

public class FmGetApplicationThemeMethod implements TemplateMethodModelEx {

    private final Map<String, Object> myParamMap;

    public FmGetApplicationThemeMethod(Map<String, Object> paramMap) {
        myParamMap = paramMap;
    }

    @Override
    public Object exec(List arguments) throws TemplateModelException {
        return null;
    }

}
