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
package org.nbandroid.netbeans.gradle.v2;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;
import org.netbeans.core.startup.MainLookup;
import org.openide.util.Lookup;

/**
 * A class loader that combines Netbeans SPI and Impl module into one.<br>
 * The Impl classes loaded by this class loader are associated with this class
 * loader, i.e. Class.getClassLoader() points to this class loader.
 *
 * @author ArSi
 */
public class NbOptionalDependencySpiLoader extends ClassLoader {

    private final ClassLoader spiModuleClassLoader;
    private final String implPackage;
    private final String spiPackage;

    /**
     * Add Impl of SPI to NB default lookup if the SPI module is available. To
     * use as optional (weak) dependency add <scope>provided</scope> to SPI
     * dependency in POM. Dont access Impl classes from Impl module.
     *
     * @param spiFullClassName SPI full Class name
     * @param implFullClassName Impl full Class name
     * @param classToExtractImplClassLoader Class from Impl module to get
     * Classloader
     * @return true if SPI is created
     */
    public static final boolean installServiceProvider(String spiFullClassName, String implFullClassName, Class<?> classToExtractImplClassLoader) {
        ClassLoader classLoader = Lookup.getDefault().lookup(ClassLoader.class);
        try {
            Class<?> colorCodesProvider = classLoader.loadClass(spiFullClassName);
            if (colorCodesProvider != null) {
                ClassLoader previewClassLoader = colorCodesProvider.getClassLoader();
                ClassLoader androidClassLoader = classToExtractImplClassLoader.getClassLoader();
                NbOptionalDependencySpiLoader proxyClassLoader = new NbOptionalDependencySpiLoader(androidClassLoader, implFullClassName.substring(0, implFullClassName.lastIndexOf('.')), previewClassLoader, spiFullClassName.substring(0, spiFullClassName.lastIndexOf('.')));
                Class<?> androidColorCodesProvider = proxyClassLoader.findClass(implFullClassName);
                Object newInstance = androidColorCodesProvider.newInstance();
                MainLookup.register(colorCodesProvider.cast(newInstance));
            }
            return true;
        } catch (Exception ex) {
        }
        return false;
    }

    public NbOptionalDependencySpiLoader(ClassLoader implModuleClassLoader, String implPackage, ClassLoader spiModuleClassLoader, String spiPackage) {
        super(implModuleClassLoader);
        this.spiModuleClassLoader = spiModuleClassLoader;
        this.implPackage = implPackage;
        this.spiPackage = spiPackage;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (name.startsWith(implPackage) || name.startsWith(spiPackage)) {
            return findClass(name);

        }
        return super.loadClass(name);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        if (name.startsWith(spiPackage)) {
            return spiModuleClassLoader.loadClass(name);
        }
        String path = name.replace('.', '/') + ".class";
        URL url = findResource(path);
        if (url == null) {
            throw new ClassNotFoundException(name);
        }
        ByteBuffer byteCode;
        try {
            byteCode = loadResource(url);
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
        return defineClass(name, byteCode, null);
    }

    private ByteBuffer loadResource(URL url) throws IOException {
        InputStream stream = null;
        try {
            stream = url.openStream();
            int initialBufferCapacity = Math.min(0x40000, stream.available() + 1);
            if (initialBufferCapacity <= 2) {
                initialBufferCapacity = 0x10000;
            } else {
                initialBufferCapacity = Math.max(initialBufferCapacity, 0x200);
            }
            ByteBuffer buf = ByteBuffer.allocate(initialBufferCapacity);
            while (true) {
                if (!buf.hasRemaining()) {
                    ByteBuffer newBuf = ByteBuffer.allocate(2 * buf.capacity());
                    buf.flip();
                    newBuf.put(buf);
                    buf = newBuf;
                }
                int len = stream.read(buf.array(), buf.position(), buf.remaining());
                if (len <= 0) {
                    break;
                }
                buf.position(buf.position() + len);
            }
            buf.flip();
            return buf;
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    @Override
    protected URL findResource(String name) {
        URL resource = spiModuleClassLoader.getResource(name);
        if (resource == null) {
            return super.getResource(name);
        }
        return resource;
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        Vector<URL> vector = new Vector<>();
        vector.addAll(Collections.list(spiModuleClassLoader.getResources(name)));
        vector.addAll(Collections.list(super.getResources(name)));
        return vector.elements();
    }

}
