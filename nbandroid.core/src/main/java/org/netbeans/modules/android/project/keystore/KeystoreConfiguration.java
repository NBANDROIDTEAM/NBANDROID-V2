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
package org.netbeans.modules.android.project.keystore;

import java.io.File;
import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.apk.keystore.options.KeystoreOptionsSubPanel;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.util.NbPreferences;

/**
 *
 * @author arsi
 */
public class KeystoreConfiguration {

    private final Project project;
    private final AuxiliaryProperties auxProps;

    private final String hash;

    private boolean useGlobal;
    private boolean ask;

    private String keystorePath;
    private String keystorePassword;
    private String keyAlias;
    private String keyPassword;
    private boolean apkV1;
    private boolean apkV2;
    private boolean apkRelease;
    private boolean apkDebug;
    private boolean rememberPassword;

    private String globalKeystorePath;
    private String globalKeystorePassword;
    private String globalKeyAlias;
    private String globalKeyPassword;

    public static final String USE_GLOBAL = "USE_GLOBAL";
    public static final String ASK = "ASK";
    public static final String KEY_STORE_PATH = "PROJECT_KEY_STORE_PATH";
    public static final String KEY_STORE_PASSWORD = "_KEY_STORE_PASSWORD";
    public static final String KEY_ALIAS = "PROJECT_KEY_ALIAS";
    public static final String KEY_PASSWORD = "_KEY_PASSWORD";
    public static final String APK_V1 = "_APK_V1";
    public static final String APK_V2 = "_APK_V2";
    public static final String APK_RELEASE = "_APK_RELEASE";
    public static final String APK_DEBUG = "_APK_DEBUG";
    public static final String REMEMBER_PASSWORDS = "_REMEMBER_PASSWORDS";

    public KeystoreConfiguration(Project project) {
        this.project = project;
        auxProps = project.getLookup().lookup(AuxiliaryProperties.class);
        hash = "ANDROID_" + project.getProjectDirectory().getPath().hashCode();
        loadGlobalOptions();
        loadProjectOptions();
    }

    private void loadProjectOptions() {
        char[] keystorePasswd = Keyring.read(hash + KEY_STORE_PASSWORD);
        char[] keyPasswd = Keyring.read(hash + KEY_PASSWORD);
        if (keystorePasswd != null) {
            keystorePassword = new String(keystorePasswd);
        }
        if (keyPasswd != null) {
            keyPassword = new String(keyPasswd);
        }
        keystorePath = auxProps.get(KEY_STORE_PATH, false);
        keyAlias = auxProps.get(KEY_ALIAS, false);
        useGlobal = getBooleanValue(USE_GLOBAL, true);
        ask = getBooleanValue(ASK, true);
        apkV1 = getBooleanValue(APK_V1, true);
        apkV2 = getBooleanValue(APK_V2, true);
        apkRelease = getBooleanValue(APK_RELEASE, true);
        apkDebug = getBooleanValue(APK_DEBUG, false);
        rememberPassword = getBooleanValue(REMEMBER_PASSWORDS, true);
    }

    private boolean getBooleanValue(String key, boolean dflt) {
        String value = auxProps.get(key, false);
        if (value == null) {
            return dflt;
        }
        return Boolean.valueOf(value);

    }

    private void loadGlobalOptions() {
        char[] keystorePasswd = Keyring.read(KeystoreOptionsSubPanel.KEY_STORE_PASSWORD);
        char[] keyPasswd = Keyring.read(KeystoreOptionsSubPanel.KEY_PASSWORD);
        if (keystorePasswd != null) {
            globalKeystorePassword = new String(keystorePasswd);
        }
        if (keyPasswd != null) {
            globalKeyPassword = new String(keyPasswd);
        }
        globalKeystorePath = NbPreferences.forModule(KeystoreOptionsSubPanel.class).get(KeystoreOptionsSubPanel.KEY_STORE_PATH, "");
        globalKeyAlias = NbPreferences.forModule(KeystoreOptionsSubPanel.class).get(KeystoreOptionsSubPanel.KEY_ALIAS, "");

    }

    public CurrentKeystoreConfiguration getCurrentKeystoreConfiguration() {
        loadGlobalOptions();
        loadProjectOptions();
        if (useGlobal) {
            if (isValid(globalKeystorePath, globalKeystorePassword, globalKeyAlias, globalKeyPassword)) {
                return new CurrentKeystoreConfiguration(true, true, useGlobal, ask, globalKeystorePath, globalKeystorePassword, globalKeyAlias, globalKeyPassword, apkV1, apkV2, apkRelease, apkDebug, rememberPassword);
            } else if (isValid(keystorePath, keystorePassword, keyAlias, keyPassword)) {
                return new CurrentKeystoreConfiguration(true, false, useGlobal, ask, keystorePath, keystorePassword, keyAlias, keyPassword, apkV1, apkV2, apkRelease, apkDebug, rememberPassword);
            }
        } else if (isValid(keystorePath, keystorePassword, keyAlias, keyPassword)) {
            return new CurrentKeystoreConfiguration(true, false, useGlobal, ask, keystorePath, keystorePassword, keyAlias, keyPassword, apkV1, apkV2, apkRelease, apkDebug, rememberPassword);
        }
        return new CurrentKeystoreConfiguration(false, false, useGlobal, ask, "", "", "", "", apkV1, apkV2, apkRelease, apkDebug, rememberPassword);
    }

    public CurrentKeystoreConfiguration getLocalKeystoreConfiguration() {
        loadProjectOptions();
        if (isValid(keystorePath, keystorePassword, keyAlias, keyPassword)) {
            return new CurrentKeystoreConfiguration(true, false, useGlobal, ask, keystorePath, keystorePassword, keyAlias, keyPassword, apkV1, apkV2, apkRelease, apkDebug, rememberPassword);
        }
        return new CurrentKeystoreConfiguration(false, false, useGlobal, ask, "", "", "", "", apkV1, apkV2, apkRelease, apkDebug, rememberPassword);
    }

    public CurrentKeystoreConfiguration getGlobalKeystoreConfiguration() {
        loadGlobalOptions();
        if (isValid(globalKeystorePath, globalKeystorePassword, globalKeyAlias, globalKeyPassword)) {
            return new CurrentKeystoreConfiguration(true, true, useGlobal, ask, globalKeystorePath, globalKeystorePassword, globalKeyAlias, globalKeyPassword, apkV1, apkV2, apkRelease, apkDebug, rememberPassword);
        }
        return new CurrentKeystoreConfiguration(false, false, useGlobal, ask, "", "", "", "", apkV1, apkV2, apkRelease, apkDebug, rememberPassword);
    }

    public boolean isValid(String keystorePath, String keystorePassword, String alias, String keyPassword) {
        if (keystorePath != null) {
            File f = new File(keystorePath);
            if (f.exists()) {
                try {
                    KeyStore ks = KeyStore.getInstance("jks");
                    ks.load(new FileInputStream(f), keystorePassword.toCharArray());
                    Key key = ks.getKey(alias, keyPassword.toCharArray());
                    if (key != null) {
                        return true;
                    }
                } catch (Exception ex) {
                }
            }
        }
        return false;
    }

    public void saveToGlobal(CurrentKeystoreConfiguration configuration) {
        if (configuration.isRememberPassword()) {
            Keyring.save(KeystoreOptionsSubPanel.KEY_STORE_PASSWORD, configuration.getKeystorePassword().toCharArray(), "NBANDROID Global Keystore Password");
            Keyring.save(KeystoreOptionsSubPanel.KEY_PASSWORD, configuration.getKeyPassword().toCharArray(), "NBANDROID Project Keystore Key Password");
        } else {
            Keyring.delete(KeystoreOptionsSubPanel.KEY_STORE_PASSWORD);
            Keyring.delete(KeystoreOptionsSubPanel.KEY_PASSWORD);
        }
        NbPreferences.forModule(KeystoreOptionsSubPanel.class).put(KeystoreOptionsSubPanel.KEY_STORE_PATH, configuration.getKeystorePath());
        NbPreferences.forModule(KeystoreOptionsSubPanel.class).put(KeystoreOptionsSubPanel.KEY_ALIAS, configuration.getKeyAlias());
        NbPreferences.forModule(KeystoreOptionsSubPanel.class).putBoolean(KeystoreOptionsSubPanel.REMEMBER_PASSWORDS, configuration.isRememberPassword());
    }

    public void saveToProject(CurrentKeystoreConfiguration configuration) {
        if (configuration.isRememberPassword()) {
            Keyring.save(hash + KEY_STORE_PASSWORD, configuration.getKeystorePassword().toCharArray(), "NBANDROID Project Keystore Password");
            Keyring.save(hash + KEY_PASSWORD, configuration.getKeyPassword().toCharArray(), "NBANDROID Project Keystore Key Password");
        } else {
            Keyring.delete(hash + KEY_STORE_PASSWORD);
            Keyring.delete(hash + KEY_PASSWORD);
        }
        auxProps.put(KEY_STORE_PATH, configuration.getKeystorePath(), false);
        auxProps.put(KEY_ALIAS, configuration.getKeyAlias(), false);
        saveSigningOptions(configuration);
    }

    public void saveSigningOptions(CurrentKeystoreConfiguration configuration) {
        auxProps.put(USE_GLOBAL, "" + configuration.isUseGlobal(), false);
        auxProps.put(ASK, "" + configuration.isAsk(), false);
        auxProps.put(APK_V1, "" + configuration.isApkV1(), false);
        auxProps.put(APK_V2, "" + configuration.isApkV2(), false);
        auxProps.put(APK_RELEASE, "" + configuration.isApkRelease(), false);
        auxProps.put(APK_DEBUG, "" + configuration.isApkDebug(), false);
        auxProps.put(REMEMBER_PASSWORDS, "" + configuration.isRememberPassword(), false);
    }

    public static class CurrentKeystoreConfiguration {

        private final boolean useGlobal;
        private final boolean ask;

        private final String keystorePath;
        private final String keystorePassword;
        private final String keyAlias;
        private final String keyPassword;
        private final boolean apkV1;
        private final boolean apkV2;
        private final boolean apkRelease;
        private final boolean apkDebug;
        private final boolean rememberPassword;
        private final boolean valid;
        private final boolean fromGlobal;

        public CurrentKeystoreConfiguration(boolean valid, boolean fromGlobal, boolean useGlobal, boolean ask, String keystorePath, String keystorePassword, String keyAlias, String keyPassword, boolean apkV1, boolean apkV2, boolean apkRelease, boolean apkDebug, boolean rememberPassword) {
            this.useGlobal = useGlobal;
            this.ask = ask;
            this.keystorePath = keystorePath;
            this.keystorePassword = keystorePassword;
            this.keyAlias = keyAlias;
            this.keyPassword = keyPassword;
            this.apkV1 = apkV1;
            this.apkV2 = apkV2;
            this.apkRelease = apkRelease;
            this.apkDebug = apkDebug;
            this.rememberPassword = rememberPassword;
            this.valid = valid;
            this.fromGlobal = fromGlobal;
        }

        public CurrentKeystoreConfiguration(boolean useGlobal, boolean ask, String keystorePath, String keystorePassword, String keyAlias, String keyPassword, boolean apkV1, boolean apkV2, boolean apkRelease, boolean apkDebug, boolean rememberPassword) {
            this.useGlobal = useGlobal;
            this.ask = ask;
            this.keystorePath = keystorePath;
            this.keystorePassword = keystorePassword;
            this.keyAlias = keyAlias;
            this.keyPassword = keyPassword;
            this.apkV1 = apkV1;
            this.apkV2 = apkV2;
            this.apkRelease = apkRelease;
            this.apkDebug = apkDebug;
            this.rememberPassword = rememberPassword;
            this.valid = true;
            this.fromGlobal = false;
        }

        public boolean isFromGlobal() {
            return fromGlobal;
        }

        public boolean isValid() {
            return valid;
        }

        public boolean isUseGlobal() {
            return useGlobal;
        }

        public boolean isAsk() {
            return ask;
        }

        public String getKeystorePath() {
            return keystorePath;
        }

        public String getKeystorePassword() {
            return keystorePassword;
        }

        public String getKeyAlias() {
            return keyAlias;
        }

        public String getKeyPassword() {
            return keyPassword;
        }

        public boolean isApkV1() {
            return apkV1;
        }

        public boolean isApkV2() {
            return apkV2;
        }

        public boolean isApkRelease() {
            return apkRelease;
        }

        public boolean isApkDebug() {
            return apkDebug;
        }

        public boolean isRememberPassword() {
            return rememberPassword;
        }

    }

}
