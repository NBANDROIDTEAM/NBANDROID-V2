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
public class FindAndroidVisitor extends CodeVisitorSupport {

    private boolean androidProject = false;
    public FindAndroidVisitor() {
    }

    public static final boolean visit(File buildGradle) throws FileNotFoundException, IOException {
        AstBuilder builder = new AstBuilder();
        List<ASTNode> nodes = builder.buildFromString(IOUtils.toString(new FileInputStream(buildGradle), "UTF-8"));
        FindAndroidVisitor visitor = new FindAndroidVisitor();
        for (ASTNode node : nodes) {
            node.visit(visitor);
        }
        return visitor.androidProject;
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        if (!"buildscript".equals(call.getMethodAsString())) {
            if ("android".equals(call.getMethodAsString())) {
                androidProject = true;
            }
        }
        super.visitMethodCallExpression(call);
    }

}
