/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.core.sdk;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.repository.api.ProgressIndicatorAdapter;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.nbandroid.netbeans.gradle.AndroidIO;
import org.openide.windows.InputOutput;

/**
 * ProgressIndicatorAdapter for Android tools APLI
 *
 * @author arsi
 */
public class NbOutputWindowProgressIndicator extends ProgressIndicatorAdapter {

    private static final InputOutput io = AndroidIO.getDefaultIO();

    @Override
    public void logWarning(@NonNull String s, @Nullable Throwable e) {
        io.getOut().println("Warning: " + s);
        if (e != null) {
            io.getOut().println(throwableToString(e));
        }
    }

    @Override
    public void logError(@NonNull String s, @Nullable Throwable e) {
        io.getErr().println("Error: " + s);
        if (e != null) {
            io.getErr().println(throwableToString(e));
        }

    }

    @Override
    public void logInfo(@NonNull String s) {
        io.getOut().println("Info: " + s);
    }

    private static String throwableToString(@NonNull Throwable e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
