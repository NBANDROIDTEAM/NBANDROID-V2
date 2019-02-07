/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sk.arsi.netbeans.gradle.android.layout.impl;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.utils.ILogger;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.netbeans.api.io.IOProvider;
import org.netbeans.api.io.InputOutput;

/**
 *
 * @author arsi
 */
public class LayoutIO implements ILogger {
    public static InputOutput getDefaultIO() {
        return IOProvider.getDefault().getIO("Android Layout Preview", false);
    }

    public static void logWarning(@NonNull String s, @Nullable Throwable e) {
        InputOutput io = getDefaultIO();
        io.getOut().println("Warning: " + s);
        if (e != null) {
            io.getOut().println(throwableToString(e));
        }
    }

    public static void logError(@NonNull String s, @Nullable Throwable e) {
        InputOutput io = getDefaultIO();
        io.getErr().println("Error: " + s);
        if (e != null) {
            io.getErr().println(throwableToString(e));
        }

    }

    public static void logInfo(@NonNull String s) {
        InputOutput io = getDefaultIO();
        io.getOut().println("Info: " + s);
    }

    public static void logVerbose(@NonNull String s) {
        InputOutput io = getDefaultIO();
        io.getOut().println("Verbose: " + s);
    }

    private static String throwableToString(@NonNull Throwable e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    @Override
    public void error(@Nullable Throwable t, @Nullable String errorFormat, Object... args) {
        if (errorFormat != null) {
            String msg = String.format("Error: " + errorFormat, args);

            logError(msg, t);
        }
    }

    @Override
    public void warning(@NonNull String warningFormat, Object... args) {

        String msg = String.format("Warning: " + warningFormat, args);

        logWarning(msg, null);
    }

    @Override
    public void info(@NonNull String msgFormat, Object... args) {

        String msg = String.format(msgFormat, args);

        logInfo(msg);
    }

    @Override
    public void verbose(@NonNull String msgFormat, Object... args) {

        String msg = String.format(msgFormat, args);

        logVerbose(msg);
    }
}
