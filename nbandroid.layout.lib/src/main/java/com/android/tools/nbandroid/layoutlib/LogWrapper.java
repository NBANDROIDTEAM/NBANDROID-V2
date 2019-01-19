/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.nbandroid.layoutlib;

import com.android.utils.ILogger;
import java.util.Arrays;
import java.util.logging.Logger;

public class LogWrapper implements ILogger {

    private static final Logger LOG = Logger.getLogger(LogWrapper.class.getName());

    public LogWrapper() {
    }

  @Override
    public void warning(String warningFormat, Object... args) {
        System.out.println(Arrays.toString(args));
    }

  @Override
    public void info(String msgFormat, Object... args) {
        System.out.println(Arrays.toString(args));
    }

  @Override
    public void verbose(String msgFormat, Object... args) {
        System.out.println(Arrays.toString(args));
  }

  @Override
    public void error(Throwable t, String errorFormat, Object... args) {
        System.out.println(Arrays.toString(args));
    }
}
