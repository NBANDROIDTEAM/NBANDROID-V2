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
package org.nbandroid.netbeans.gradle.v2.layout.completion.analyzer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class StyleableClassVisitor
        extends ClassVisitor {

    private final StyleableResultCollector resultCollector;

    private final MethodVisitor methodVisitor;

    public StyleableClassVisitor(MethodVisitor methodVisitor, StyleableResultCollector resultCollector) {
        super(Opcodes.ASM5);
        this.methodVisitor = methodVisitor;
        this.resultCollector = resultCollector;
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature,
            final String superName, final String[] interfaces) {
        resultCollector.setClassName(name);
        resultCollector.setSuperClassName(superName);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
            final String[] exceptions) {
        return methodVisitor;
    }

}
