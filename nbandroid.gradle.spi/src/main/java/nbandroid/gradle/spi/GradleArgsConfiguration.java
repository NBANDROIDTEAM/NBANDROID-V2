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
package nbandroid.gradle.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.AuxiliaryProperties;

/**
 *
 * @author arsi
 */
public class GradleArgsConfiguration {

    private final Project project;
    private final AuxiliaryProperties auxProps;

    private static final String PREFERENCE_GRADLE_ARGUMENTS = "PREFERENCE_GRADLE_ARGUMENTS";

    private String custom;

    public GradleArgsConfiguration(Project project) {
        this.project = project;
        auxProps = project.getLookup().lookup(AuxiliaryProperties.class);
        custom = auxProps.get(PREFERENCE_GRADLE_ARGUMENTS, false);
    }

    public String getCustom() {
        return custom;
    }

    public void setCustom(String custom) {
        this.custom = custom;
        auxProps.put(PREFERENCE_GRADLE_ARGUMENTS, custom, false);
    }

    public String[] getJvmArguments() {
        List<String> arguments = new ArrayList<>();
        if (custom != null && !custom.isEmpty()) {
            String tmp = custom.replace("\n\r", ";").replace("\r\n", ";").replace("\r", ";").replace("\n", ";");
            StringTokenizer tok = new StringTokenizer(tmp, ";", false);
            while (tok.hasMoreElements()) {
                arguments.add(tok.nextToken().trim());
            }
        }
        return arguments.toArray(new String[arguments.size()]);
    }

}
