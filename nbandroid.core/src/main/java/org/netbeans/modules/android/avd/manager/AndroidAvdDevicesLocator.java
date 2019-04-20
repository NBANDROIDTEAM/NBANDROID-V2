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
package org.netbeans.modules.android.avd.manager;

import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import org.netbeans.modules.android.avd.manager.pojo.Devices;

/**
 *
 * @author arsi
 */
public class AndroidAvdDevicesLocator {
    
    private static Devices devicesXml = null;
    public static Devices getDeviceDefs(){
        if (devicesXml==null) {
            //switch to module ClassLoader to avoid Exception
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(AndroidAvdDevicesLocator.class.getClassLoader());
                Unmarshaller unmarshaller = JAXBContext.newInstance(Devices.class).createUnmarshaller();
                InputStream resourceAsStream = AndroidAvdDevicesLocator.class.getClassLoader().getResourceAsStream("org/netbeans/modules/android/avd/manager/device-art.xml");
                devicesXml = (Devices) unmarshaller.unmarshal(resourceAsStream);
            } catch (Exception ex) {
                ex.printStackTrace();
            }finally{
                Thread.currentThread().setContextClassLoader(contextClassLoader);
            }
        }
        return devicesXml;
    }
}
