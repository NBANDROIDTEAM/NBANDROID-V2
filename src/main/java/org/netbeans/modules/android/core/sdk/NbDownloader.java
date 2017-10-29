/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netbeans.modules.android.core.sdk;

import com.android.repository.api.Downloader;
import com.android.repository.api.ProgressIndicator;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import javax.annotation.Nullable;
import org.netbeans.api.progress.ProgressHandle;
/**
 *
 * @author arsi
 */
public class NbDownloader implements Downloader {

    private static final int BUFFER_SIZE = 1024;

    /**
     * Creates a new {@code StudioDownloader}. The current
     * {@link com.intellij.openapi.progress.ProgressIndicator} will be picked up
     * when downloads are run.
     */
    public NbDownloader() {
    }

    @Override
    public InputStream downloadAndStream(URL url, ProgressIndicator indicator)
            throws IOException {
        File file = downloadFully(url, indicator);
        if (file == null) {
            return null;
        }
        file.deleteOnExit();
        return new FileInputStream(file) {
            @Override
            public void close() throws IOException {
                super.close();
                file.delete();
            }
        };
    }

    @Override
    public void downloadFully(URL url, File target, @Nullable String checksum, ProgressIndicator indicator)
            throws IOException {

        if (target.exists() && checksum != null) {
            if (checksum.equals(Downloader.hash(new BufferedInputStream(new FileInputStream(target)), target.length(), indicator))) {
                return;
            }
        }

        // We don't use the settings here explicitly, since HttpRequests picks up the network settings from studio directly.
        indicator.logInfo("Downloading " + url);
        indicator.setText("Downloading...");
        indicator.setSecondaryText(url.toString());
        ProgressHandle handle = ProgressHandle.createHandle("Downloading " + url);
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(3000);
        connection.connect();
        int contentLength = connection.getContentLength();
        handle.start(contentLength);
        OutputStream dest = new FileOutputStream(target);
        InputStream in = connection.getInputStream();

        int count;
        int done = 0;
        byte data[] = new byte[BUFFER_SIZE];
        while ((count = in.read(data, 0, BUFFER_SIZE)) != -1) {
            done += count;
            handle.progress(done);
            dest.write(data, 0, count);
        }
        handle.finish();
        dest.close();
        in.close();
    }

    @Nullable
    @Override
    public File downloadFully(URL url,
            ProgressIndicator indicator) throws IOException {
        // TODO: caching
        String suffix = url.getPath();
        suffix = suffix.substring(suffix.lastIndexOf("/") + 1);
        File tempFile = File.createTempFile("NbDownloader", suffix);
        tempFile.deleteOnExit();
        downloadFully(url, tempFile, null, indicator);
        return tempFile;
    }

}
