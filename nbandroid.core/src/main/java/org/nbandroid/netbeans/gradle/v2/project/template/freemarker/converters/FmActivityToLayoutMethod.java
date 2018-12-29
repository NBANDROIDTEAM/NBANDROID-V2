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
package org.nbandroid.netbeans.gradle.v2.project.template.freemarker.converters;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import java.util.List;
import org.nbandroid.netbeans.gradle.v2.project.template.freemarker.AssetNameConverter;
import org.nbandroid.netbeans.gradle.v2.project.template.freemarker.AssetNameConverter.Type;

/**
 * Method invoked by FreeMarker to convert an Activity class name into a
 * suitable layout name.
 */
public class FmActivityToLayoutMethod implements TemplateMethodModelEx {

    @Override
    public TemplateModel exec(List args) throws TemplateModelException {
        if (args.size() < 1 || args.size() > 2) {
            throw new TemplateModelException("Wrong arguments");
        }

        String activityName = ((TemplateScalarModel) args.get(0)).getAsString();
        if (activityName.isEmpty()) {
            return new SimpleScalar("");
        }

        String layoutName = args.size() > 1 ? ((TemplateScalarModel) args.get(1)).getAsString() : null;
        return new SimpleScalar(new AssetNameConverter(Type.ACTIVITY, activityName).overrideLayoutPrefix(layoutName).getValue(Type.LAYOUT));
    }
}
