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
package org.nbandroid.netbeans.gradle.v2.adb;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.CountDownLatch;
import org.gradle.internal.impldep.org.apache.commons.lang.StringUtils;
import org.openide.util.Exceptions;

/**
 *
 * @author arsi
 */
public class AdbTools {

    public static class IpRecord {

        final String name;
        final String ip;

        public IpRecord(String name, String ip) {
            this.name = name;
            this.ip = ip;
        }

        public String getName() {
            return name;
        }

        public String getIp() {
            return ip;
        }

    }

    public static boolean openWifiSettings(IDevice device) {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            device.executeShellCommand("am start -a android.intent.action.MAIN -n com.android.settings/.wifi.WifiSettings", new CollectingOutputReceiver(latch));
            latch.await();
        } catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException ex) {
            return false;
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        return true;
    }

    public static List<IpRecord> getDeviceIps(IDevice device, boolean all) {
        CountDownLatch latch = new CountDownLatch(1);
        IpReceiver outputReceiver = new IpReceiver(latch, all);
        try {
            device.executeShellCommand("netcfg", outputReceiver);
        } catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        try {
            latch.await();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (!outputReceiver.isOk()) {
            latch = new CountDownLatch(1);
            IfconfigReceiver ifconfigReceiver = new IfconfigReceiver(latch, all);
            try {
                device.executeShellCommand("ifconfig", ifconfigReceiver);
            } catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            try {
                latch.await();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            return ifconfigReceiver.getIps();
        }
        return outputReceiver.getIps();
    }

    private static class IpReceiver extends MultiLineReceiver {

        private final List<IpRecord> ips = new ArrayList<>();

        private final CountDownLatch mCompletionLatch;

        private boolean ok = true;
        private final boolean all;

        public IpReceiver(CountDownLatch mCompletionLatch, boolean all) {
            this.mCompletionLatch = mCompletionLatch;
            this.all = all;
        }

        public List<IpRecord> getIps() {
            return ips;
        }

        @Override
        public void processNewLines(String[] lines) {
            if (lines.length == 1 && lines[0] != null && lines[0].contains("not found")) {
                ok = false;
                return;
            }
            for (String line : lines) {
                line = StringUtils.normalizeSpace(line);
                StringTokenizer tokenizer = new StringTokenizer(line, " ", false);
                if (tokenizer.countTokens() == 5) {
                    String ifName = tokenizer.nextToken();
                    String state = tokenizer.nextToken();
                    String ip = tokenizer.nextToken();
                    if (all || ("UP".equals(state) && !"lo".equals(ifName) && !"0.0.0.0/0".equals(ip) && !ifName.startsWith("rmnet_"))) {
                        ips.add(new IpRecord(ifName, ip));
                    }
                }
            }

        }

        public boolean isOk() {
            return ok;
        }

        @Override
        public void done() {
            mCompletionLatch.countDown();
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

    }

    private static class IfconfigReceiver extends MultiLineReceiver {

        private final List<IpRecord> ips = new ArrayList<>();

        private final CountDownLatch mCompletionLatch;

        private boolean ok = true;
        private final boolean all;

        public IfconfigReceiver(CountDownLatch mCompletionLatch, boolean all) {
            this.mCompletionLatch = mCompletionLatch;
            this.all = all;
        }

        public List<IpRecord> getIps() {
            return ips;
        }

        @Override
        public void processNewLines(String[] lines) {
            if (lines.length == 1 && lines[0] != null && lines[0].contains("not found")) {
                ok = false;
                return;
            }
            String ifName = null;
            String ip = null;
            String mask = null;
            boolean nextRecord = true;
            for (String line : lines) {
                line = StringUtils.normalizeSpace(line);
                if (line.isEmpty()) {
                    ifName = null;
                    ip = null;
                    mask = null;
                    nextRecord = true;
                } else if (nextRecord) {
                    nextRecord = false;
                    StringTokenizer tokenizer = new StringTokenizer(line, " ", false);
                    if (tokenizer.countTokens() > 1) {
                        ifName = tokenizer.nextToken();
                    }
                } else if (line.contains("inet addr:")) {
                    StringTokenizer tokenizer = new StringTokenizer(line, " ", false);
                    while (tokenizer.hasMoreElements()) {
                        String nextToken = tokenizer.nextToken();
                        if (nextToken.equals("inet")) {
                            while (tokenizer.hasMoreElements()) {
                                nextToken = tokenizer.nextToken();
                                if (nextToken.contains("addr:")) {
                                    ip = nextToken.replace("addr:", "");
                                } else if (nextToken.contains("Mask:")) {
                                    mask = nextToken.replace("Mask:", "");
                                }
                            }
                        }
                    }
                    if (ifName != null && mask != null && (all || (!"lo".equals(ifName) && ip != null && !ifName.startsWith("rmnet_")))) {
                        int cidr = convertNetmaskToCIDR(mask);
                        ip = ip + "/" + cidr;
                        ips.add(new IpRecord(ifName, ip));
                    }
                }
            }

        }

        public boolean isOk() {
            return ok;
        }

        @Override
        public void done() {
            mCompletionLatch.countDown();
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

    }

    public static int convertNetmaskToCIDR(String netmask) {

        byte[] netmaskBytes = new byte[4];
        StringTokenizer stringTokenizer = new StringTokenizer(netmask, ".", false);
        int pos = 0;
        while (stringTokenizer.hasMoreElements()) {
            String nextToken = stringTokenizer.nextToken();
            netmaskBytes[pos] = (byte) Integer.parseInt(nextToken);
            pos++;
        }
        int cidr = 0;
        boolean zero = false;
        for (byte b : netmaskBytes) {
            int mask = 0x80;

            for (int i = 0; i < 8; i++) {
                int result = b & mask;
                if (result == 0) {
                    zero = true;
                } else if (zero) {
                    throw new IllegalArgumentException("Invalid netmask.");
                } else {
                    cidr++;
                }
                mask >>>= 1;
            }
        }
        return cidr;
    }

}
