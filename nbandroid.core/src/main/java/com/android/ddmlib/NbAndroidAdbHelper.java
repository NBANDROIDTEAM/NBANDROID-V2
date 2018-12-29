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
package com.android.ddmlib;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 *
 * @author arsi
 */
public class NbAndroidAdbHelper {

    private static final String SELECT_DEVICE = "host:transport:sn";
    private static final String SWITCH_TO_TCP = "tcpip:port";
    private static final String SWITCH_TO_USB = "usb:";
    private static final String CONNECT_TCP = "host:connect:ip:port";
    private static final String CONNECT_TCP_ALL = "host:connect:";

    private static SocketChannel openAdbConnection() {
        try {
            SocketChannel adbChannel = SocketChannel.open(AndroidDebugBridge.getSocketAddress());
            adbChannel.socket().setTcpNoDelay(true);
            return adbChannel;
        } catch (IOException e) {
            return null;
        }
    }

    public static boolean switchToEthernet(String deviceSerial, String ip, int port) {
        SocketChannel channel = openAdbConnection();
        try {
            if (send(channel, SELECT_DEVICE.replace("sn", deviceSerial))) {
                return send(channel, SWITCH_TO_TCP.replace("port", "" + port));
            }
        } finally {
            try {
                channel.close();
            } catch (IOException ex) {
            }
        }
        return false;
    }

    public static boolean switchToUSB(String deviceSerial) {
        SocketChannel channel = openAdbConnection();
        try {
            if (send(channel, SELECT_DEVICE.replace("sn", deviceSerial))) {
                return send(channel, SWITCH_TO_USB);
            }
        } finally {
            try {
                channel.close();
            } catch (IOException ex) {
            }
        }
        return false;
    }

    public static boolean connectEthernet(String deviceSerial, String ip, int port) {
        SocketChannel channel = openAdbConnection();
        try {
            return send(channel, CONNECT_TCP.replace("ip", ip).replace("port", "" + port));
        } finally {
            try {
                channel.close();
            } catch (IOException ex) {
            }
        }

    }

    public static boolean connectEthernet(String ipPort) {
        SocketChannel channel = openAdbConnection();
        try {
            return send(channel, CONNECT_TCP_ALL + ipPort);
        } finally {
            try {
                channel.close();
            } catch (IOException ex) {
            }
        }

    }


    public static String sendAndReceive(SocketChannel mAdbConnection, String message) {
        if (mAdbConnection != null) {
            byte[] request = AdbHelper.formAdbRequest(message);
            try {
                AdbHelper.write(mAdbConnection, request);
                AdbHelper.AdbResponse resp = AdbHelper.readAdbResponse(mAdbConnection, true);
                if (resp.okay) {
                    return resp.message;
                }

            } catch (Exception e) {
            }
        }
        return null;
    }

    public static boolean send(SocketChannel mAdbConnection, String message) {
        if (mAdbConnection != null) {
            byte[] request = AdbHelper.formAdbRequest(message);
            try {
                AdbHelper.write(mAdbConnection, request);
                AdbHelper.AdbResponse resp = AdbHelper.readAdbResponse(mAdbConnection, false);
                return resp.okay;

            } catch (Exception e) {
            }
        }
        return false;
    }
}
