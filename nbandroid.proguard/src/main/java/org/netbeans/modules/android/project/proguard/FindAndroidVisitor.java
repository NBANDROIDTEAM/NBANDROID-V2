/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.android.project.proguard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.ast.expr.MethodCallExpression;

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
