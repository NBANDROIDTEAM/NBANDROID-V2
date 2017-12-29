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
package org.nbandroid.netbeans.gradle.v2.apk;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.apache.commons.compress.archivers.zip.ZipFile;

/**
 *
 * @author arsi
 */
public class ApkUtils {

    private static final long APK_SIG_BLOCK_MAGIC_HI = 0x3234206b636f6c42L;
    private static final long APK_SIG_BLOCK_MAGIC_LO = 0x20676953204b5041L;
    public static final int SIGNED_NOT = 0;
    public static final int SIGNED_V1 = 1;
    public static final int SIGNED_V2 = 2;
    public static final int SIGNED_V1V2 = 3;

    public static int fastSignInfo(File file) {
        int v1 = SIGNED_NOT;
        int v2 = SIGNED_NOT;
        try {
            ZipFile zip = new ZipFile(file);
            if (zip.getEntry("META-INF/CERT.RSA") != null) {
                v1 = SIGNED_V1;
            }
            Field archiveField = ZipFile.class.getDeclaredField("archive");
            archiveField.setAccessible(true);
            final RandomAccessFile archive = (RandomAccessFile) archiveField.get(zip);
            Method positionAtCentralDirectory = ZipFile.class.getDeclaredMethod("positionAtCentralDirectory");
            positionAtCentralDirectory.setAccessible(true);
            positionAtCentralDirectory.invoke(zip);
            long centralDirectoryOffset = archive.getFilePointer();
            archive.seek(centralDirectoryOffset - 24);
            byte[] buffer = new byte[24];
            archive.readFully(buffer);
            zip.close();
            ByteBuffer footer = ByteBuffer.wrap(buffer);
            footer.order(ByteOrder.LITTLE_ENDIAN);
            if ((footer.getLong(8) == APK_SIG_BLOCK_MAGIC_LO)
                    && (footer.getLong(16) == APK_SIG_BLOCK_MAGIC_HI)) {
                v2 = SIGNED_V2;
            }

        } catch (IOException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
        }
        return v1 | v2;
    }

}
