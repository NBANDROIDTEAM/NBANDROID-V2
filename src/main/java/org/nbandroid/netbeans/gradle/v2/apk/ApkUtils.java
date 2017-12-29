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

import com.android.utils.Pair;
import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import javax.security.auth.x500.X500Principal;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

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

    //  AndroidBuilder.signApk(in, new DefaultSigningConfig(TOOL_TIP_TEXT_KEY), out);
    public static void test() {
    }

    private static String encodeDN(DN dn) {
        StringBuilder builder = new StringBuilder();
        buildDNpair(builder, "CN", dn.firstAndLastName);
        buildDNpair(builder, "OU", dn.organizationUnit);
        buildDNpair(builder, "O", dn.organization);
        buildDNpair(builder, "L", dn.city);
        buildDNpair(builder, "ST", dn.stateOrProvince);
        buildDNpair(builder, "C", dn.countryCode);
        return builder.toString();
    }

    private static void buildDNpair(StringBuilder builder, String prefix, String text) {
        if (text != null) {
            String value = text.trim();
            if (value.length() > 0) {
                if (builder.length() > 0) {
                    builder.append(",");
                }
                builder.append(prefix);
                builder.append('=');
                builder.append(value);
            }
        }
    }

    public static boolean createNewStore(String storeType, File storeFile, char[] storePassword, DN dn) {
        if (storeType == null) {
            storeType = "jks";
        }
        try {
            KeyStore ks = KeyStore.getInstance(storeType);
            ks.load(null, null);
            Pair<PrivateKey, X509Certificate> generated = generateKeyAndCertificate("RSA", "SHA1withRSA", dn.validityYears, encodeDN(dn));
            ks.setKeyEntry(dn.alias, generated.getFirst(), dn.password, new Certificate[]{generated.getSecond()});
            FileOutputStream fos = new FileOutputStream(storeFile);
            boolean threw = true;
            try {
                ks.store(fos, storePassword);
                threw = false;
            } finally {
                Closeables.close(fos, threw);
            }
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | OperatorCreationException e) {
            return false;
        }
        return true;
    }

    public static boolean addNewKey(KeyStore ks, File storeFile, char[] storePassword, DN dn) {
        try {
            Pair<PrivateKey, X509Certificate> generated = generateKeyAndCertificate("RSA", "SHA1withRSA", dn.validityYears, encodeDN(dn));
            ks.setKeyEntry(dn.alias, generated.getFirst(), dn.password, new Certificate[]{generated.getSecond()});
            FileOutputStream fos = new FileOutputStream(storeFile);
            boolean threw = true;
            try {
                ks.store(fos, storePassword);
                threw = false;
            } finally {
                Closeables.close(fos, threw);
            }
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | OperatorCreationException e) {
            return false;
        }
        return true;
    }

    private static Pair<PrivateKey, X509Certificate> generateKeyAndCertificate(String asymmetric, String sign, int validityYears, String dn) throws NoSuchAlgorithmException, OperatorCreationException, CertificateException {
        Preconditions.checkArgument(validityYears > 0, "validityYears <= 0");
        KeyPair keyPair = KeyPairGenerator.getInstance(asymmetric).generateKeyPair();
        Date notBefore = new Date(System.currentTimeMillis());
        Date notAfter = new Date(System.currentTimeMillis() + validityYears * 31536000000l);
        X500Name issuer = new X500Name(new X500Principal(dn).getName());
        SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());
        X509v1CertificateBuilder builder = new X509v1CertificateBuilder(issuer, BigInteger.ONE, notBefore, notAfter, issuer, publicKeyInfo);
        ContentSigner signer = new JcaContentSignerBuilder(sign).setProvider(new BouncyCastleProvider()).build(keyPair.getPrivate());
        X509CertificateHolder holder = builder.build(signer);
        JcaX509CertificateConverter converter = new JcaX509CertificateConverter().setProvider(new BouncyCastleProvider());
        X509Certificate certificate = converter.getCertificate(holder);
        return Pair.of(keyPair.getPrivate(), certificate);
    }

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

    public static class DN {

        private String alias;
        private char[] password;
        private int validityYears;
        private String firstAndLastName;
        private String organizationUnit;
        private String organization;
        private String city;
        private String stateOrProvince;
        private String countryCode;

        public DN() {
        }

        public DN(String alias, char[] password, int validityYears, String firstAndLastName, String organizationUnit, String organization, String city, String stateOrProvince, String countryCode) {
            this.alias = alias;
            this.password = password;
            this.validityYears = validityYears;
            this.firstAndLastName = firstAndLastName;
            this.organizationUnit = organizationUnit;
            this.organization = organization;
            this.city = city;
            this.stateOrProvince = stateOrProvince;
            this.countryCode = countryCode;
        }

        public String getFirstAndLastName() {
            return firstAndLastName;
        }

        public void setFirstAndLastName(String firstAndLastName) {
            this.firstAndLastName = firstAndLastName;
        }

        public String getOrganizationUnit() {
            return organizationUnit;
        }

        public void setOrganizationUnit(String organizationUnit) {
            this.organizationUnit = organizationUnit;
        }

        public String getOrganization() {
            return organization;
        }

        public void setOrganization(String organization) {
            this.organization = organization;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getStateOrProvince() {
            return stateOrProvince;
        }

        public void setStateOrProvince(String stateOrProvince) {
            this.stateOrProvince = stateOrProvince;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public char[] getPassword() {
            return password;
        }

        public void setPassword(char[] password) {
            this.password = password;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }

        public int getValidityYears() {
            return validityYears;
        }

        public void setValidityYears(int validityYears) {
            this.validityYears = validityYears;
        }

    }

}
