/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.netbeans.gradle.android.layout.impl;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.xml.parsers.ParserConfigurationException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.IASTORE;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.NEWARRAY;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.SIPUSH;
import static org.objectweb.asm.Opcodes.T_INT;
import static org.objectweb.asm.Opcodes.V1_6;
import org.objectweb.asm.Type;
import org.openide.util.Exceptions;
import org.xml.sax.SAXException;

/**
 *
 * @author arsi
 */
public class ResClassGenerator {

    private final Map<ResourceType, Map<String, Object>> mResources = Maps.newHashMap();
    private String fqcn;
    private byte[] rootClass;
    private Map<String, byte[]> innerClasses = new HashMap<>();

    public byte[] getRootClass() {
        return rootClass;
    }

    public Map<String, byte[]> getInnerClasses() {
        return innerClasses;
    }

    public String getFqcn() {
        return fqcn;
    }


    public ResClassGenerator(File aar) {
        File rTxt = new File(aar.getPath() + File.separator + "R.txt");
        if (rTxt.exists() && rTxt.isFile()) {
            try {
                List<String> readLines = Files.readLines(rTxt, StandardCharsets.UTF_8);
                for (String line : readLines) {
                    StringTokenizer tok = new StringTokenizer(line, " ", false);
                    if (tok.countTokens() > 3) {
                        switch (tok.nextToken()) {
                            case "int":
                                handle(tok);
                                break;
                            case "int[]":
                                handleArray(tok, line);
                                break;
                        }
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        File manifest = new File(aar.getPath() + File.separator + "AndroidManifest.xml");
        if (manifest.exists() && manifest.isFile()) {
            try {
                ManifestData manifestData = parseProjectManifest(new FileInputStream(manifest));
                fqcn = manifestData.getPackage() + ".R";
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        generate();
    }

    private void handle(StringTokenizer tok) {

        ResourceType resourceType = ResourceType.getEnum(tok.nextToken());
        String name = tok.nextToken();
        String value = tok.nextToken();
        Map<String, Object> map = mResources.get(resourceType);
        if (map == null) {
            map = new HashMap<>();
            mResources.put(resourceType, map);
        }
        int id = Integer.decode(value);
        map.put(name, id);

    }

    private void handleArray(StringTokenizer tok, String line) {
        ResourceType resourceType = ResourceType.getEnum(tok.nextToken());
        String name = tok.nextToken();
        line = line.replace(" ", "");
        line = line.substring(line.indexOf('{') + 1, line.indexOf('}'));
        tok = new StringTokenizer(line, ",", false);
        Map<String, Object> map = mResources.get(resourceType);
        if (map == null) {
            map = new HashMap<>();
            mResources.put(resourceType, map);
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

    private void generate() {
        String className = fqcn.replace('.', '/');
        ClassWriter cw = new ClassWriter(0);
        cw.visit(V1_6, ACC_PUBLIC + ACC_FINAL + ACC_SUPER, className, null, Type.getInternalName(Object.class), null);
        for (Map.Entry<ResourceType, Map<String, Object>> entry : mResources.entrySet()) {
            ResourceType t = entry.getKey();
            Map<String, Object> values = entry.getValue();
            cw.visitInnerClass(className + "$" + t.getName(), className, t.getName(), ACC_PUBLIC + ACC_FINAL + ACC_STATIC);
        }
        generateConstructor(cw);
        cw.visitEnd();
        rootClass = cw.toByteArray();
        for (Map.Entry<ResourceType, Map<String, Object>> entry : mResources.entrySet()) {
            ResourceType resourceType = entry.getKey();
            Map<String, Object> values = entry.getValue();
            cw = new ClassWriter(0);
            cw.visit(V1_6, ACC_PUBLIC + ACC_FINAL + ACC_SUPER, className, null, Type.getInternalName(Object.class), null);
            if (resourceType == ResourceType.STYLEABLE || resourceType == ResourceType.DECLARE_STYLEABLE) {
                resourceType = ResourceType.STYLEABLE;
                cw.visitInnerClass(className + "$" + resourceType.getName(), className, resourceType.getName(), ACC_PUBLIC + ACC_FINAL + ACC_STATIC);
                for (Map.Entry<String, Object> entry1 : values.entrySet()) {
                    String name = entry1.getKey();
                    Object value = entry1.getValue();
                    if (value instanceof Integer) {
                        generateField(cw, name, (int) value);
                    } else {
                        MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
                        mv.visitCode();
                        generateArrayInitialization(mv, className, name, (List<Integer>) value);
                        mv.visitInsn(RETURN);
                        mv.visitMaxs(4, 0);
                        mv.visitEnd();
                    }
                }
                generateConstructor(cw);
                cw.visitEnd();
                innerClasses.put(className + "$" + resourceType.getName(), cw.toByteArray());
            } else {
                cw.visitInnerClass(className + "$" + resourceType.getName(), className, resourceType.getName(), ACC_PUBLIC + ACC_FINAL + ACC_STATIC);
                for (Map.Entry<String, Object> entry1 : values.entrySet()) {
                    String name = entry1.getKey();
                    Object value = entry1.getValue();
                    if (value instanceof Integer) {
                        generateField(cw, name, (int) value);
                    }
                }
                cw.visitEnd();
                generateConstructor(cw);
                innerClasses.put(className + "$" + resourceType.getName(), cw.toByteArray());
            }

        }

    }

    private static void generateField(ClassWriter cw, String name, int value) {
        cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, name, "I", null, value).visitEnd();
    }

    private static void generateArrayField(ClassWriter cw, String name) {
        cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, name, "[I", null, null).visitEnd();
    }

    private static void generateConstructor(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
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

}
