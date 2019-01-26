/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.netbeans.gradle.android.layout.impl;

import android.os._Original_Build;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

/**
 *
 * @author arsi
 */
public class LayoutClassLoader extends URLClassLoader {

    public LayoutClassLoader(URL[] urls, List<File> aars, ClassLoader parent) {
        super(urls, parent);
        generate(_Original_Build.class, (className, classBytes) -> defineClass(className, classBytes, 0, classBytes.length));
        String out = "/jetty/netbeans9/xxx";
        for (File aar : aars) {
            ResClassGenerator gen = new ResClassGenerator(aar);
            if (gen.isR()) {
                String rootFqcn = gen.getFqcn();
                byte[] rootClass = gen.getRootClass();
                try {
                    Class<?> defineClass = defineClass(rootFqcn, rootClass, 0, rootClass.length);
                } catch (Exception exception) {
                }
                System.out.println("sk.arsi.netbeans.gradle.android.layout.impl.LayoutClassLoader.<init>()");
                Map<String, byte[]> innerClasses = gen.getInnerClasses();
                for (Map.Entry<String, byte[]> entry : innerClasses.entrySet()) {
                    String fqcn = entry.getKey();
                    byte[] bytecode = entry.getValue();
                    try {
                        Class<?> defineClass1 = defineClass(fqcn.replace("/", "."), bytecode, 0, bytecode.length);
                    } catch (Exception exception) {
                    }
                    System.out.println("sk.arsi.netbeans.gradle.android.layout.impl.LayoutClassLoader.<init>()");
                }
                System.out.println("sk.arsi.netbeans.gradle.android.layout.impl.LayoutClassLoader.<init>()");
            }

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
