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
package nbandroid.gradle.impl.update;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import java.io.IOException;
import java.util.Collections;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author arsi
 */
public class GradleUpdateHandler {

    public static CurrentGradleVersion getCurrentGradleVersion() {
        return readCurrentGradleVersion("https://services.gradle.org/versions/current", 5000, 5000);
    }

    private static CurrentGradleVersion readCurrentGradleVersion(final String url, int connectTimeout, int readTimeout) {
        try {
            ProgressHandle progressHandle = ProgressHandleFactory.createSystemHandle("Loading https://services.gradle.org/versions/current");
            progressHandle.start();
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
            setTimeout(restTemplate, connectTimeout, readTimeout);
            ResponseEntity<CurrentGradleVersion> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(requestHeaders), CurrentGradleVersion.class);
            CurrentGradleVersion currentGradleVersion = response.getBody();
            progressHandle.finish();
            return currentGradleVersion;
        } catch (Exception exception) {
            NotifyDescriptor nd = new NotifyDescriptor.Message("Unable to download current Gradle version info from web. Using default 5.4", NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(nd);
            //fallback to default
            CurrentGradleVersion currentGradleVersion = new CurrentGradleVersion();
            currentGradleVersion.setVersion("5.4");
            currentGradleVersion.setDownloadUrl("https://services.gradle.org/distributions/gradle-5.4-bin.zip");
            
        }
        return null;
    }

    private static void setTimeout(RestTemplate restTemplate, int connectTimeout, int readTimeout) {
        restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory());
        SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory) restTemplate
                .getRequestFactory();
        rf.setReadTimeout(readTimeout);
        rf.setConnectTimeout(connectTimeout);
    }
}
