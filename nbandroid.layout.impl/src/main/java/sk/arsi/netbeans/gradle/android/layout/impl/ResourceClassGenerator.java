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
import com.android.ide.common.xml.AndroidManifestParser;
import com.android.ide.common.xml.ManifestData;
import com.android.resources.ResourceType;
import com.google.android.collect.Maps;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.function.BiConsumer;
import javax.xml.parsers.ParserConfigurationException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.IASTORE;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.NEWARRAY;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.SIPUSH;
import static org.objectweb.asm.Opcodes.T_INT;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.openide.util.Exceptions;
import org.xml.sax.SAXException;

/**
 *
 * @author arsi
 */
public class ResourceClassGenerator {

    public static Map<ResourceType, Map<String, Object>> buildFullResourceMap(ResourceNamespace namespace, String fqcn, List<File> aarList, ResourceClassGeneratorConfig config) {
        final Map<Integer, Integer> originalToGenerated = new HashMap<>();
        final Map<ResourceType, Map<String, Object>> resources = Maps.newHashMap();
        for (File aar : aarList) {
            File rTxt = new File(aar.getPath() + File.separator + "R.txt");
            try {
                List<String> readLines = Files.readLines(rTxt, StandardCharsets.UTF_8);
                for (String line : readLines) {
                    StringTokenizer tok = new StringTokenizer(line, " ", false);
                    if (tok.countTokens() > 3) {
                        switch (tok.nextToken()) {
                            case "int":
                                handleFullLine(namespace, tok, resources, originalToGenerated, config);
                                break;
                            case "int[]":
                                handleFullLineArray(tok, line, resources);
                                break;
                        }
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        //refresh array ids to new IDs
        for (Map.Entry<ResourceType, Map<String, Object>> entry : resources.entrySet()) {
            ResourceType type = entry.getKey();
            Map<String, Object> values = entry.getValue();
            for (Map.Entry<String, Object> entry1 : values.entrySet()) {
                String name = entry1.getKey();
                Object value = entry1.getValue();
                if (value instanceof List) {
                    List<Integer> in = (List<Integer>) value;
                    List<Integer> out = new ArrayList<>();
                    for (Integer val : in) {
                        Integer newVal = originalToGenerated.get(val);
                        if (newVal == null) {
                            out.add(val);
                        } else {
                            out.add(newVal);
                        }
                    }
                    values.replace(name, out);
                }
            }
        }
        return resources;
    }

    private static void handleFullLine(ResourceNamespace namespace, StringTokenizer tok, final Map<ResourceType, Map<String, Object>> resources, Map<Integer, Integer> originalToGenerated, ResourceClassGeneratorConfig config) {

        ResourceType resourceType = ResourceType.getEnum(tok.nextToken());
        String name = tok.nextToken();
        String value = tok.nextToken();
        Map<String, Object> map = resources.get(resourceType);
        if (map == null) {
            map = new HashMap<>();
            resources.put(resourceType, map);
        }
        int id = Integer.decode(value);
        int newId = id;
        int packageId = (id & ResourceClassGeneratorConfig.NAMESPACE_MASK);
        if (packageId > ResourceClassGeneratorConfig.ANDROID_NAMESPACE) {
            newId = config.getOrCreateId(namespace, resourceType, name);
        }
        originalToGenerated.put(id, newId);
        map.put(name, newId);

    }

    private static void handleFullLineArray(StringTokenizer tok, String line, final Map<ResourceType, Map<String, Object>> resources) {
        ResourceType resourceType = ResourceType.getEnum(tok.nextToken());
        String name = tok.nextToken();
        line = line.replace(" ", "");
        line = line.substring(line.indexOf('{') + 1, line.indexOf('}'));
        tok = new StringTokenizer(line, ",", false);
        Map<String, Object> map = resources.get(resourceType);
        if (map == null) {
            map = new HashMap<>();
            resources.put(resourceType, map);
        }
        List<Integer> tmp = new ArrayList<>();
        while (tok.hasMoreElements()) {
            try {
                tmp.add(Integer.decode(tok.nextToken().trim()));
            } catch (NumberFormatException numberFormatException) {
            }
        }
        map.put(name, tmp);

    }

    public static void generate(String packageName, Map<ResourceType, Map<String, Object>> resources, BiConsumer<String, byte[]> defineClass) {
        ClassLoader loader = R.class.getClassLoader();
        String originalBuildClassName = R.class.getName();
        String originalBuildBinaryClassName = toBinaryClassName(originalBuildClassName);
        Deque<String> pendingClasses = new LinkedList<>();
        pendingClasses.push(originalBuildClassName);
        Remapper remapper = new Remapper() {
            @Override
            public String map(String typeName) {
                if (typeName.startsWith(originalBuildBinaryClassName)) {
                    return toBinaryClassName(packageName) + trimStart(typeName, originalBuildBinaryClassName);
                }
                return typeName;
            }
        };
        while (!pendingClasses.isEmpty()) {
            String name = pendingClasses.pop();

            String newName = packageName + trimStart(name, originalBuildClassName);
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
                if (newName.contains("$")) {
                    Map<String, List<Integer>> todoArrays = new HashMap<>();
                    String type = newName.substring(newName.indexOf("$") + 1);
                    ResourceType resourceType = ResourceType.getEnum(type);
                    if (resourceType != null) {
                        Map<String, Object> res = resources.get(resourceType);
                        if (res != null) {
                            for (Map.Entry<String, Object> entry : res.entrySet()) {
                                String fieldName = entry.getKey();
                                Object fieldValue = entry.getValue();
                                if (fieldValue instanceof Integer) {
                                    generateField(writer, getFieldNameByResourceName(fieldName), (int) fieldValue);
                                } else {
                                    generateArrayField(writer, getFieldNameByResourceName(fieldName));
                                    todoArrays.put(getFieldNameByResourceName(fieldName), (List<Integer>) fieldValue);
                                }
                            }
                            if (!todoArrays.isEmpty()) {
                                MethodVisitor mv = writer.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
                                mv.visitCode();
                                for (Map.Entry<String, List<Integer>> entry : todoArrays.entrySet()) {
                                    String fieldName = entry.getKey();
                                    List<Integer> fieldValue = entry.getValue();
                                    generateArrayInitialization(mv, toBinaryClassName(newName), fieldName, fieldValue);
                                }
                                mv.visitInsn(RETURN);
                                mv.visitMaxs(4, 0);
                                mv.visitEnd();
                            }
                        }
                    }
                }
                defineClass.accept(newName, writer.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getFieldNameByResourceName(String styleName) {
        for (int i = 0, n = styleName.length(); i < n; i++) {
            char c = styleName.charAt(i);
            if (c == '.' || c == '-' || c == ':') {
                return styleName.replace('.', '_').replace('-', '_').replace(':', '_');
            }
        }
        return styleName;
    }

    private static void generateArrayInitialization(MethodVisitor mv, String className, String fieldName, List<Integer> values) {
        if (values.isEmpty()) {
            return;
        }
        pushIntValue(mv, values.size());
        mv.visitIntInsn(NEWARRAY, T_INT);
        int idx = 0;
        for (Integer value : values) {
            mv.visitInsn(DUP);
            pushIntValue(mv, idx);
            mv.visitLdcInsn(value);
            mv.visitInsn(IASTORE);
            idx++;
        }
        mv.visitFieldInsn(PUTSTATIC, className, fieldName, "[I");
    }

    private static void pushIntValue(MethodVisitor mv, int value) {
        if (value >= -1 && value <= 5) {
            mv.visitInsn(ICONST_0 + value);
        } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            mv.visitIntInsn(BIPUSH, value);
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            mv.visitIntInsn(SIPUSH, value);
        } else {
            mv.visitLdcInsn(value);
        }
    }

    private static void generateArrayField(ClassWriter cw, String name) {
        int[] aa = {10, 20};
        cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, name, "[I", null, null).visitEnd();
    }

    private static void generateField(ClassWriter cw, String name, int value) {
        cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, name, "I", null, value).visitEnd();
    }

    public static String trimStart(String s, String prefix) {
        if (s.startsWith(prefix)) {
            return s.substring(prefix.length());
        }
        return s;
    }

    private static String toBinaryClassName(String name) {
        return name.replace('.', '/');
    }

    private static String toClassName(String name) {
        return name.replace('/', '.');
    }

    public static ManifestData parseProjectManifest(InputStream manifestIS) {
        try {
            return AndroidManifestParser.parse(manifestIS);
        } catch (ParserConfigurationException ex) {
        } catch (IOException ioe) {
        } catch (SAXException saxe) {
        } finally {
            try {
                manifestIS.close();
            } catch (IOException ex) {
            }
        }

        return null;
    }

    public static ResourceNamespace findAarNamespace(File aarFolder) {
        File manifest = new File(aarFolder.getPath() + File.separator + "AndroidManifest.xml");
        if (manifest.exists() && manifest.isFile()) {
            try {
                ManifestData manifestData = parseProjectManifest(new FileInputStream(manifest));
                if (manifestData != null) {
                    return ResourceNamespace.fromPackageName(manifestData.getPackage());
                }
            } catch (FileNotFoundException ex) {
            }
        }
        return ResourceNamespace.RES_AUTO;
    }

}
