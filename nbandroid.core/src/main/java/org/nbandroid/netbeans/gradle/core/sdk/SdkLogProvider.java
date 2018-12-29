/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.nbandroid.netbeans.gradle.core.sdk;

import com.android.utils.ILogger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility to create SDK loggers.
 *
 * @author radim
 */
public class SdkLogProvider {

    private static final Logger LOG = Logger.getLogger(SdkLogProvider.class.getName());

    private SdkLogProvider() {
    }

    /**
     * Creates an instance of {@link ILogger}.
     *
     * @param errorReportOn {@code true} to log errors to standard Java
     * utilities logging API
     * @return log consumer
     */
    public static ILogger createLogger(final boolean errorReportOn) {
        return new AndroidToolsLogger(errorReportOn);
    }

    private static class AndroidToolsLogger implements ILogger {

        private final boolean errorReportOn;

        public AndroidToolsLogger(boolean errorReportOn) {
            this.errorReportOn = errorReportOn;
        }

        @Override
        public void warning(String warningFormat, Object... args) {
            try {
                LOG.log(errorReportOn ? Level.WARNING : Level.FINER, String.format(warningFormat, args));
            } catch (Exception ex) {
                LOG.log(Level.FINE, null, ex);
            }
        }

        @Override
        public void error(Throwable t, String errorFormat, Object... args) {
            try {
                if (errorFormat != null) {
                    LOG.log(errorReportOn ? Level.SEVERE : Level.FINER, String.format(errorFormat, args));
                }
                LOG.log(errorReportOn ? Level.SEVERE : Level.FINER, null, t);
            } catch (Exception ex) {
                LOG.log(Level.FINE, null, ex);
            }
        }

        @Override
        public void info(String string, Object... os) {
            LOG.log(Level.INFO, String.format(string, os));
        }

        @Override
        public void verbose(String string, Object... os) {
            LOG.log(Level.FINE, String.format(string, os));
        }
    }
}
