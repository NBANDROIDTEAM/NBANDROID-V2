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

import java.io.IOException;
import java.io.InputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class StyleableClassFileVisitor {

    public static StyleableResultCollector visitClass(String className, InputStream in) {
        StyleableResultCollector resultCollector = new StyleableResultCollector();
        try {
            ClassReader reader = new ClassReader(in);
            MethodVisitor mv = new StyleableMethodVisitor(resultCollector);
            ClassVisitor classVisitor = new StyleableClassVisitor(mv, resultCollector);
            reader.accept(classVisitor, 0);
        } catch (IOException exception) {
            exception.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            // some bug inside ASM causes an IOB exception. Log it and move on?
            // this happens when the class isn't valid.
            System.out.println("Unable to process: " + className);
        }
        return resultCollector;
    }
}
