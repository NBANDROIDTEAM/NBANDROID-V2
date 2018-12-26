/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nbandroid.netbeans.gradle.symlink;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import org.openide.util.Exceptions;

/**
 *
 * @author arsi
 */
public class GradleSymLinkRemover implements Runnable {

    private final File tempDir;

    public GradleSymLinkRemover() {
        String tmpdir = System.getProperty("java.io.tmpdir");
        tempDir = new File(tmpdir);
    }


    @Override
    public void run() {
        File[] listFiles = tempDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("symlink") && name.endsWith("test_link");
            }
        });
        for (File file : listFiles) {
            if (Files.isSymbolicLink(file.toPath())) {
                try {
                    Files.deleteIfExists(file.toPath());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

}
