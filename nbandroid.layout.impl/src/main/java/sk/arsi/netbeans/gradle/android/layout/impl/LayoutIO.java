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
