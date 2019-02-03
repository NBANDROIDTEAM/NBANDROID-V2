/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.netbeans.gradle.android.layout.impl;

import android.os._Original_Build;
import com.android.ide.common.rendering.api.ResourceNamespace;
import com.android.ide.common.xml.ManifestData;
import com.android.resources.ResourceType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
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
import org.openide.util.Exceptions;

/**
 *
 * @author arsi
 */
public class LayoutClassLoader extends URLClassLoader {

    private final ResourceClassGeneratorConfig classGeneratorConfig;
    private final ResourceNamespace appNamespace;

    public ResourceClassGeneratorConfig getClassGeneratorConfig() {
        return classGeneratorConfig;
    }

    public LayoutClassLoader(URL[] urls, List<File> aars, ClassLoader parent, ResourceNamespace appNamespace) {
        super(urls, parent);
        classGeneratorConfig = new ResourceClassGeneratorConfig(appNamespace);
        this.appNamespace = ResourceNamespace.RES_AUTO;
        generate(_Original_Build.class, (className, classBytes) -> defineClass(className, classBytes, 0, classBytes.length));
        generateRs(aars);
    }

    private void generateRs(List<File> aars) {
        Map<String, List<File>> packages = new HashMap<>();
        //sort aars by package, it can only one R class for single package
        for (File aar : aars) {
            File rTxt = new File(aar.getPath() + File.separator + "R.txt");
            if (rTxt.exists() && rTxt.isFile()) {
                File manifest = new File(aar.getPath() + File.separator + "AndroidManifest.xml");
                if (manifest.exists() && manifest.isFile()) {
                    try {
                        ManifestData manifestData = ResourceClassGenerator.parseProjectManifest(new FileInputStream(manifest));
                        if (manifestData != null) {
                            String fqcn = manifestData.getPackage() + ".R";
                            List<File> tmp = packages.get(fqcn);
                            if (tmp == null) {
                                tmp = new ArrayList<>();
                                packages.put(fqcn, tmp);
                            }
                            tmp.add(aar);
                        }
                    } catch (FileNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
//        //geterate R.class
        for (Map.Entry<String, List<File>> entry : packages.entrySet()) {
            String fqcn = entry.getKey();
            List<File> aarList = entry.getValue();
            Map<ResourceType, Map<String, Object>> resourceMap = ResourceClassGenerator.buildFullResourceMap(ResourceNamespace.RES_AUTO, fqcn, aarList, classGeneratorConfig);
            ResourceClassGenerator.generate(fqcn, resourceMap, (className, classBytes) -> defineClass(className, classBytes, 0, classBytes.length));
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
