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
package sk.arsi.netbeans.gradle.android.maven.impl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.openide.util.lookup.ServiceProvider;
import sk.arsi.netbeans.gradle.android.maven.MavenPomParser;

/**
 *
 * @author arsi
 */
@ServiceProvider(service = MavenPomParser.class)
public class MavenPomParserImpl implements MavenPomParser {

    @Override
    public List<String> parseDependenciesFromPom(File pom) {
        List<String> tmp = new ArrayList<>();
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader(pom));
            List<Dependency> dependencies = model.getDependencies();
            for (int i = 0; i < dependencies.size(); i++) {
                Dependency dep = dependencies.get(i);
                tmp.add(dep.getType() + ":" + dep.getGroupId() + ":" + dep.getArtifactId() + ":" + dep.getVersion());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (XmlPullParserException ex) {
            ex.printStackTrace();
        }
        return tmp;
    }

}
