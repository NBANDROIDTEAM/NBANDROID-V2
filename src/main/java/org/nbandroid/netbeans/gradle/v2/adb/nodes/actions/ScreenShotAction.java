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
package org.nbandroid.netbeans.gradle.v2.adb.nodes.actions;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.RawImage;
import com.android.ddmlib.TimeoutException;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.nbandroid.netbeans.gradle.v2.adb.nodes.DevicesNode;
import org.nbandroid.netbeans.gradle.v2.adb.nodes.MobileDeviceNode;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author arsi
 */
@ActionID(
        category = "ADB/MobileDevice",
        id = "org.nbandroid.netbeans.gradle.v2.adb.nodes.actions.ScreenShotAction"
)
@ActionRegistration(
        displayName = "", lazy = false
)
@ActionReferences({
    @ActionReference(path = "Android/ADB/MobileDevice", position = 1000),
    @ActionReference(path = "Android/ADB/EmulatorDevice", position = 1000),})
public class ScreenShotAction extends NodeAction {

    private static final String PNG = "png";
    private static final Logger LOG = Logger.getLogger(ScreenShotAction.class.getName());

    @Override
    protected void performAction(Node[] nodes) {
        assert nodes != null;
        assert nodes.length == 1;
        final IDevice device = nodes[0].getLookup().lookup(DevicesNode.MobileDeviceHolder.class).getMasterDevice();
        assert device != null;
        try {
            final RawImage image = device.getScreenshot();
            if (image == null) {
                throw new IOException("No screenshot"); //NOI18N
            }
            int imageType;

            // determine image format
            switch (image.bpp) {
                case 16: {
                    imageType = BufferedImage.TYPE_USHORT_565_RGB;
                    break;
                }

                default: {
                    imageType = BufferedImage.TYPE_INT_ARGB;
                    break;
                }
            }

            final BufferedImage img = new BufferedImage(image.width, image.height, imageType);
            final DataBuffer db = img.getRaster().getDataBuffer();
            for (int y = 0; y < image.height; y++) {
                for (int x = 0; x < image.width; x++) {
                    int color;

                    switch (imageType) {
                        case BufferedImage.TYPE_USHORT_565_RGB: {
                            byte l = image.data[2 * (y * image.width + x)];
                            byte h = image.data[2 * (y * image.width + x) + 1];
                            color = ((h << 8) | (l & 0xff)) & 0xffff;
                            break;
                        }

                        default: {
                            color = image.getARGB((image.bpp / 8) * (y * image.width + x));
                            break;
                        }
                    }

                    db.setElem(y * image.width + x, color);
                }
            }
            final String tmpVal = System.getProperty("java.io.tmpdir");   //NOI18N
            final File tmpFile = FileUtil.normalizeFile(new File(tmpVal));
            final String name = FileUtil.findFreeFileName(FileUtil.toFileObject(tmpFile),
                    device.getSerialNumber(), PNG);
            final File pictureFile = new File(tmpFile, name + '.' + PNG);
            ImageIO.write(img, PNG, pictureFile);
            final FileObject pictureFileObj = FileUtil.toFileObject(pictureFile);
            DataObject dobj = DataObject.find(pictureFileObj);
            OpenCookie oc = dobj.getCookie(OpenCookie.class);
            if (oc != null) {
                oc.open();
            }
        } catch (IOException ioe) {
            LOG.log(Level.INFO, null, ioe);
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(MobileDeviceNode.class, "ERROR_ScreenShot"),
                    NotifyDescriptor.ERROR_MESSAGE));
        } catch (TimeoutException ex) {
            LOG.log(Level.INFO, null, ex);
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(MobileDeviceNode.class, "ERROR_ScreenShot"),
                    NotifyDescriptor.ERROR_MESSAGE));
        } catch (AdbCommandRejectedException ex) {
            LOG.log(Level.INFO, null, ex);
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(MobileDeviceNode.class, "ERROR_ScreenShot"),
                    NotifyDescriptor.ERROR_MESSAGE));
        }
    }

    @Override
    protected boolean enable(Node[] nodes) {
        if (nodes.length != 1) {
            return false;
        }
        return nodes[0].getLookup().lookup(DevicesNode.MobileDeviceHolder.class) != null;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(MobileDeviceNode.class, "TXT_ScreenShotAction");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.nbandroid.netbeans.gradle.v2.adb.nodes.MobileDeviceNode");
    }

}
