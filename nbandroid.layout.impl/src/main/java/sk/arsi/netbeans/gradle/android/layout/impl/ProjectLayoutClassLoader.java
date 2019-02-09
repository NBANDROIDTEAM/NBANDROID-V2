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
package sk.arsi.netbeans.gradle.android.layout.impl;

import com.android.ide.common.rendering.api.ResourceNamespace;
import com.android.resources.ResourceType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

/**
 * Classloader to load project classes and R.txt
 *
 * @author arsi
 */
public class ProjectLayoutClassLoader extends URLClassLoader {

    private final ResourceClassGeneratorConfig classGeneratorConfig;

    public ResourceClassGeneratorConfig getClassGeneratorConfig() {
        return classGeneratorConfig;
    }

    public static ProjectLayoutClassLoader getClassloader(File projectClassesFolder, File projectR, String appPackage, LayoutClassLoader layoutClassLoader) {
        URL urls[];
        try {
            urls = new URL[]{projectClassesFolder.toURI().toURL()};
        } catch (MalformedURLException ex) {
            urls = new URL[0];
        }
        return new ProjectLayoutClassLoader(urls, projectR, appPackage, layoutClassLoader);
    }

    private ProjectLayoutClassLoader(URL urls[], File projectR, String appPackage, LayoutClassLoader layoutClassLoader) {
        super(urls, layoutClassLoader);//pass empty URLs we need to exlude R.classes
        classGeneratorConfig = layoutClassLoader.getClassGeneratorConfig();
        generateRs(projectR, appPackage);
    }


    private void generateRs(File projectR, String appPackage) {
        Map<String, List<File>> packages = new HashMap<>();
        if (projectR.exists() && projectR.isFile()) {
            Map<ResourceType, Map<String, Object>> resourceMap = ResourceClassGenerator.buildFullResourceMap(ResourceNamespace.RES_AUTO, appPackage, projectR, classGeneratorConfig);
            ResourceClassGenerator.generate(appPackage, resourceMap, (className, classBytes) -> defineClass(className, classBytes, 0, classBytes.length));

        }
    }

    private static String toBinaryClassName(String name) {
        return name.replace('.', '/');
    }

    private static String toClassName(String name) {
        return name.replace('/', '.');
    }

    static void generate(Class<?> originalBuildClass, BiConsumer<String, byte[]> defineClass) {
        ClassLoader loader = originalBuildClass.getClassLoader();
        String originalBuildClassName = originalBuildClass.getName();
        String originalBuildBinaryClassName = toBinaryClassName(originalBuildClassName);
        Deque<String> pendingClasses = new LinkedList<>();
        pendingClasses.push(originalBuildClassName);

        Remapper remapper = new Remapper() {
            @Override
            public String map(String typeName) {
                if (typeName.startsWith(originalBuildBinaryClassName)) {
                    return "android/os/Build" + trimStart(typeName, originalBuildBinaryClassName);
                }

                return typeName;
            }
        };

        while (!pendingClasses.isEmpty()) {
            String name = pendingClasses.pop();

            String newName = "android.os.Build" + trimStart(name, originalBuildClassName);
            String binaryName = toBinaryClassName(name);

            try (InputStream is = loader.getResourceAsStream(binaryName + ".class")) {
                ClassWriter writer = new ClassWriter(0);
                ClassReader reader = new ClassReader(is);
                ClassRemapper classRemapper = new ClassRemapper(writer, remapper) {
                    @Override
                    public void visitInnerClass(String name, String outerName, String innerName, int access) {
                        if (outerName.startsWith(binaryName)) {
                            pendingClasses.push(toClassName(name));
                        }
                        super.visitInnerClass(name, outerName, innerName, access);
                    }

                };
                reader.accept(classRemapper, 0);
                defineClass.accept(newName, writer.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String trimStart(String s, String prefix) {
        if (s.startsWith(prefix)) {
            return s.substring(prefix.length());
        }
        return s;
    }

}
