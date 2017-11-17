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
package org.nbandroid.netbeans.gradle.v2.gradle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.gradle.impldep.org.apache.commons.io.IOUtils;

/**
 *
 * @author arsi
 */
public class FindRepositoriesVisitor extends CodeVisitorSupport {

    private final List<Repository> repositories = new ArrayList<>();

    public FindRepositoriesVisitor() {
        repositories.add(new AndroidRepository());
    }

    public static final List<Repository> visit(File buildGradle) throws FileNotFoundException, IOException {
        AstBuilder builder = new AstBuilder();
        List<ASTNode> nodes = builder.buildFromString(IOUtils.toString(new FileInputStream(buildGradle), "UTF-8"));
        FindRepositoriesVisitor visitor = new FindRepositoriesVisitor();
        for (ASTNode node : nodes) {
            node.visit(visitor);
        }
        return visitor.repositories;
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        if (!"buildscript".equals(call.getMethodAsString())) {
            if ("repositories".equals(call.getMethodAsString())) {
                call.getArguments().visit(new RepositoriesVisitor());
            }
        }
        super.visitMethodCallExpression(call);
    }

    private class RepositoriesVisitor extends CodeVisitorSupport {

        @Override
        public void visitMethodCallExpression(MethodCallExpression call) {
            if (null != call.getMethodAsString()) {
                switch (call.getMethodAsString()) {
                    case "maven":
                        call.getArguments().visit(new MavenVisitor());
                        break;
                    case "ivy":
                        call.getArguments().visit(new IvyVisitor());
                        break;
                    case "mavenCentral":
                        MavenCentralRepository centralRepository = new MavenCentralRepository();
                        if (!repositories.contains(centralRepository)) {
                            repositories.add(centralRepository);
                        }
                        break;
                    case "jcenter":
                        JCenterRepository centerRepository = new JCenterRepository();
                        if (!repositories.contains(centerRepository)) {
                            repositories.add(centerRepository);
                        }
                        break;
                    default:
                        break;
                }
            }
        }

    }

    private class MavenVisitor extends CodeVisitorSupport {

        @Override
        public void visitMethodCallExpression(MethodCallExpression call) {
            if ("url".equals(call.getMethodAsString())) {
                String url = call.getArguments().getText();
                MavenRepository mavenRepository = new MavenRepository(url);
                if (!repositories.contains(mavenRepository)) {
                    repositories.add(mavenRepository);
                }
            }

        }
    }

    private class IvyVisitor extends CodeVisitorSupport {

        @Override
        public void visitMethodCallExpression(MethodCallExpression call) {
            if ("url".equals(call.getMethodAsString())) {
                String url = call.getArguments().getText();
                IvyRepository ivyRepository = new IvyRepository(url);
                if (!repositories.contains(ivyRepository)) {
                    repositories.add(ivyRepository);
                }
            }

        }
    }

}
