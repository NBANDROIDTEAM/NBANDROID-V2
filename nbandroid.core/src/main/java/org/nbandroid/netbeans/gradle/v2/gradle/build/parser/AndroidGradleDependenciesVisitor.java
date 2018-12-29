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
package org.nbandroid.netbeans.gradle.v2.gradle.build.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.gradle.internal.impldep.org.apache.commons.io.IOUtils;

/**
 *
 * @author arsi
 */
public class AndroidGradleDependenciesVisitor extends CodeVisitorSupport {

    private int dependenceStartLineNum = -1;
    private int dependenceEndLineNum = -1;
    private int currentBlockEnd = -1;
    private int currentBlockStart = -1;
    private int currentBlockEndColumn = -1;
    private int currentBlockStartColumn = -1;
    private int columnNum = -1;
    private AndroidGradleDependencies dependencies = null;
    private AndroidGradleDependency currentDependency = null;
    private boolean buildScript = false;
    private BlockStatement rootBlockStatement = null;
    private boolean excludeMode = false;
    private String variableName = null;

    public static AndroidGradleDependenciesVisitor parse(File file) throws IOException {
        AstBuilder builder = new AstBuilder();
        AndroidGradleDependenciesVisitor visitor = new AndroidGradleDependenciesVisitor();
        List<ASTNode> nodes = builder.buildFromString(IOUtils.toString(new FileInputStream(file), "UTF-8"));
        for (ASTNode node : nodes) {
            visitor.setRootBlockStatement((BlockStatement) node);
            node.visit(visitor);
        }
        return visitor;
    }

    protected AndroidGradleDependenciesVisitor() {
    }

    public void setRootBlockStatement(BlockStatement rootBlockStatement) {
        this.rootBlockStatement = rootBlockStatement;
    }

    @Override
    public void visitBlockStatement(BlockStatement block) {
        if (block.equals(rootBlockStatement)) {
            List<Statement> statements = block.getStatements();
            for (int i = 0; i < statements.size(); i++) {
                //workaround to exlude buildscript->dependencies
                buildScript = false;
                Statement st = statements.get(i);
                st.visit(this);
            }
        } else {
            super.visitBlockStatement(block);
        }
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        if (call.getMethodAsString().equals("buildscript")) {
            buildScript = true;
            //exlude
        } else if (call.getMethodAsString().equals("dependencies") && !buildScript) {
            //find dependencies lines
            dependenceStartLineNum = call.getLineNumber();
            dependenceEndLineNum = call.getLastLineNumber();
            dependencies = new AndroidGradleDependencies(call.getLineNumber(), call.getColumnNumber(), call.getLastLineNumber(), call.getLastColumnNumber());
        } else if (call.getLineNumber() > dependenceStartLineNum && call.getLineNumber() < dependenceEndLineNum && call.getLineNumber() > currentBlockEnd) {
            //find dependency block
            currentBlockEnd = call.getLastLineNumber();
            currentBlockStart = call.getLineNumber();
            currentBlockStartColumn = call.getColumnNumber();
            currentBlockEndColumn = call.getLastColumnNumber();
            currentDependency = null;
            androidDependency = null;
        }

        super.visitMethodCallExpression(call);
    }

    @Override
    public void visitVariableExpression(VariableExpression expression) {
        if (expression.getLineNumber() > dependenceStartLineNum && expression.getLineNumber() < dependenceEndLineNum) {
            variableName = expression.getText();
        }
        super.visitVariableExpression(expression); //To change body of generated methods, choose Tools | Templates.
    }

    private boolean waitForGroup = false;
    private boolean waitForModule = false;
    private AndroidGradleDependency.AndroidDependency androidDependency = null;

    @Override
    public void visitMapExpression(MapExpression expression) {
        if (excludeMode) {
            List<MapEntryExpression> mapEntryExpressions = expression.getMapEntryExpressions();
            Map<String, String> tmp = new HashMap<>();
            for (MapEntryExpression mapEntryExpression : mapEntryExpressions) {
                Expression key = mapEntryExpression.getKeyExpression();
                Expression value = mapEntryExpression.getValueExpression();
                if ((key instanceof ConstantExpression) && (value instanceof ConstantExpression)) {
                    String keyText = key.getText();
                    String valueText = value.getText();
                    tmp.put(keyText, valueText);
                }
            }
            if (tmp.containsKey("module")) {
                String group = tmp.get("group");
                String module = tmp.get("module");
                currentDependency.getExclude().add(new AndroidGradleDependency.AndroidDependencyExclude(group, module));
            }
            excludeMode = false;
        } else if (expression.getLineNumber() > dependenceStartLineNum && expression.getLineNumber() < dependenceEndLineNum) {
            if (androidDependency instanceof AndroidGradleDependency.AndroidLocalBinaryTreeDependency) {
                List<MapEntryExpression> mapEntryExpressions = expression.getMapEntryExpressions();
                Map<String, String> tmp = new HashMap<>();
                for (MapEntryExpression mapEntryExpression : mapEntryExpressions) {
                    Expression key = mapEntryExpression.getKeyExpression();
                    Expression value = mapEntryExpression.getValueExpression();
                    if ((key instanceof ConstantExpression) && (value instanceof ConstantExpression)) {
                        String keyText = key.getText();
                        String valueText = value.getText();
                        tmp.put(keyText, valueText);
                    } else if ((key instanceof ConstantExpression) && (value instanceof ListExpression)) {
                        String keyText = key.getText();
                        List<Expression> expressions = ((ListExpression) value).getExpressions();
                        String out = "";
                        for (int i = 0; i < expressions.size(); i++) {
                            Expression exp = expressions.get(i);
                            if (exp instanceof ConstantExpression) {
                                out += "'" + ((ConstantExpression) exp).getText() + "'";
                                if (i < expressions.size() - 1) {
                                    out += ",";
                                }
                            }
                        }
                        tmp.put(keyText, out);
                    }
                }
                ((AndroidGradleDependency.AndroidLocalBinaryTreeDependency) androidDependency).getLocalLibrary().putAll(tmp);
            } else {
                super.visitMapExpression(expression); //To change body of generated methods, choose Tools | Templates.
            }

        } else {
            super.visitMapExpression(expression); //To change body of generated methods, choose Tools | Templates.
        }
    }


    @Override
    public void visitConstantExpression(ConstantExpression expression) {
        if (expression.getLineNumber() > dependenceStartLineNum && expression.getLineNumber() < dependenceEndLineNum) {
            String text = expression.getText();
            if (currentDependency == null) {
                currentDependency = new AndroidGradleDependency(text, currentBlockStart, currentBlockStartColumn, currentBlockEnd, currentBlockEndColumn);
                dependencies.getDependencies().add(currentDependency);
            } else if ("exclude".equals(text)) {
                excludeMode = true;
            } else if (variableName != null) {
                currentDependency.getVariables().put(variableName, text);
                variableName = null;
            } else if (!excludeMode) {
                if (androidDependency == null) {
                    switch (text) {
                        case "project":
                            androidDependency = new AndroidGradleDependency.AndroidLocalLibraryModuleDependency();
                            currentDependency.setAndroidDependency(androidDependency);
                            break;
                        case "fileTree":
                            androidDependency = new AndroidGradleDependency.AndroidLocalBinaryTreeDependency();
                            currentDependency.setAndroidDependency(androidDependency);
                            break;
                        case "files":
                            androidDependency = new AndroidGradleDependency.AndroidLocalBinaryFilesDependency();
                            currentDependency.setAndroidDependency(androidDependency);
                            break;
                        default:
                            androidDependency = new AndroidGradleDependency.AndroidRemoteBinaryDependency(text);
                            currentDependency.setAndroidDependency(androidDependency);
                            break;
                    }
                } else {
                    if (androidDependency instanceof AndroidGradleDependency.AndroidLocalLibraryModuleDependency) {
                        ((AndroidGradleDependency.AndroidLocalLibraryModuleDependency) androidDependency).setLocalLibraryModule(text);
                    } else if (androidDependency instanceof AndroidGradleDependency.AndroidLocalBinaryFilesDependency) {
                        ((AndroidGradleDependency.AndroidLocalBinaryFilesDependency) androidDependency).getLocalFiles().add(text);
                    }
                    System.out.println(text);
                }
            }
        }
        super.visitConstantExpression(expression); //To change body of generated methods, choose Tools | Templates.
    }

    public int getDependenceLineNum() {
        return dependenceStartLineNum;
    }

    public int getColumnNum() {
        return columnNum;
    }

    public AndroidGradleDependencies getDependencies() {
        return dependencies;
    }

}
