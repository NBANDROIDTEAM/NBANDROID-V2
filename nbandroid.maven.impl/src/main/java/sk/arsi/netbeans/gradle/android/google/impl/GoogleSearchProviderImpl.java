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
package sk.arsi.netbeans.gradle.android.google.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.lucene.util.NamedThreadFactory;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import sk.arsi.netbeans.gradle.android.maven.MavenDependencyInfo;
import sk.arsi.netbeans.gradle.android.maven.MavenSearchProvider;
import sk.arsi.netbeans.gradle.android.maven.RepoSearchListener;
import sk.arsi.netbeans.gradle.android.maven.repository.Repository;
import sk.arsi.netbeans.gradle.android.maven.repository.RepositoryType;

/**
 *
 * @author arsi
 */
@ServiceProviders({
    @ServiceProvider(service = MavenSearchProvider.class),
    @ServiceProvider(service = GoogleSearchProviderImpl.class)})

public class GoogleSearchProviderImpl implements MavenSearchProvider, Runnable {

    private static final String BASE_URL = "https://dl.google.com/dl/android/maven2/";
    private static final String MASTER_INDEX = "master-index.xml";
    private static final String GROUP_INDEX = "group-index.xml";
    private final List<MavenDependencyInfo> googleIndex = new ArrayList<>();
    public final String NBANDROID_FOLDER = "nbandroid/";
    private final File cacheGoogleIndex = Places.getCacheSubfile(NBANDROID_FOLDER + "google.index");
    public static final ScheduledThreadPoolExecutor POOL_EXECUTOR = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("Android-index-download"));
    public static final RequestProcessor SEARCH_PROCESSOR = new RequestProcessor("nbandroid-index-search-processor", 12);
    public GoogleSearchProviderImpl() {
    }

    @Override
    public void searchPackageName(String searchText, String repo, RepoSearchListener listener, long searchId, List<Repository> repositories) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                List<MavenDependencyInfo> collected = googleIndex.parallelStream().filter(info -> info.getGradleLine().toLowerCase().contains(searchText)).collect(Collectors.toList());
                try {
                    listener.searchDone(RepoSearchListener.Type.GOOGLE, searchId, false, collected);
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                }
            }
        };
        boolean enabled = false;
        for (Repository repository : repositories) {
            if (repository.getType() == RepositoryType.ANDROID || repository.getUrl().contains(".google.com/")) {
                enabled = true;
                break;
            }
        }
        if (enabled) {
            try {
                listener.searchDone(RepoSearchListener.Type.GOOGLE, searchId, true, new ArrayList<>());
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
            GoogleSearchProviderImpl.SEARCH_PROCESSOR.execute(runnable);
        } else {
            try {
                listener.searchDone(RepoSearchListener.Type.GOOGLE, searchId, false, new ArrayList<>());
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    private List<MavenDependencyInfo> downloadGoogleIndex(int connectTimeout, int readTimeout) {
        ProgressHandle handle = ProgressHandle.createHandle("Goodle index download");
        List<MavenDependencyInfo> tmp = new ArrayList<>();
        handle.start();
        GoogleMasterIndex googleMasterIndex = readGoogleMasterIndex(connectTimeout, readTimeout);
        if (googleMasterIndex != null) {
            handle.switchToDeterminate(googleMasterIndex.getGroups().size());
            int step = 0;
            Map<String, String> groups = googleMasterIndex.getGroups();
            for (Map.Entry<String, String> entry : groups.entrySet()) {
                final String group = entry.getKey();
                final String url = entry.getValue();
                GoogleGroupIndex googleGroupIndex = readGoogleGroupIndex(group, url, connectTimeout, readTimeout);
                tmp.addAll(googleGroupIndex.getArtifacts());
                handle.progress(step++);
            }
        }
        handle.finish();
        return tmp;
    }

    private GoogleGroupIndex readGoogleGroupIndex(final String group, final String url, int connectTimeout, int readTimeout) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
            setTimeout(restTemplate, connectTimeout, readTimeout);
            ObjectMapper mapper = Jackson2ObjectMapperBuilder.xml().build();
            mapper = mapper.addHandler(new DeserializationProblemHandler() {
                @Override
                public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser p, JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName) throws IOException {
                    if (beanOrClass instanceof GoogleGroupIndex) {
                        if ("versions".equals(propertyName)) {
                            if (((GoogleGroupIndex) beanOrClass).lastArtifactId != null) {
                                ((GoogleGroupIndex) beanOrClass).downloaded.put(((GoogleGroupIndex) beanOrClass).lastArtifactId, p.getText());
                                ((GoogleGroupIndex) beanOrClass).lastArtifactId = null;
                            }
                        } else {
                            ((GoogleGroupIndex) beanOrClass).lastArtifactId = propertyName;
                        }
                        return true;
                    }
                    return false;
                }

            });
            restTemplate.getMessageConverters().clear();
            restTemplate.getMessageConverters().add(new MappingJackson2XmlHttpMessageConverter(mapper));
            ResponseEntity<GoogleGroupIndex> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(requestHeaders), GoogleGroupIndex.class);
            GoogleGroupIndex groupIndex = response.getBody();
            groupIndex.group = group;
            groupIndex.url = url;
            return groupIndex.build();
        } catch (Exception exception) {
            Exceptions.printStackTrace(exception);
        }
        return null;
    }

    private GoogleMasterIndex readGoogleMasterIndex(int connectTimeout, int readTimeout) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
            setTimeout(restTemplate, connectTimeout, readTimeout);
            ObjectMapper mapper = Jackson2ObjectMapperBuilder.xml().build();
            mapper = mapper.addHandler(new DeserializationProblemHandler() {
                @Override
                public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser p, JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName) throws IOException {
                    if (beanOrClass instanceof GoogleMasterIndex) {
                        ((GoogleMasterIndex) beanOrClass).getGroups().put(propertyName, BASE_URL + propertyName.replace(".", "/") + "/" + GROUP_INDEX);
                        return true;
                    }
                    return false;
                }

            });
            restTemplate.getMessageConverters().clear();
            restTemplate.getMessageConverters().add(new MappingJackson2XmlHttpMessageConverter(mapper));
            ResponseEntity<GoogleMasterIndex> response = restTemplate.exchange(BASE_URL + MASTER_INDEX, HttpMethod.GET, new HttpEntity<>(requestHeaders), GoogleMasterIndex.class);
            return response.getBody();
        } catch (Exception exception) {
            Exceptions.printStackTrace(exception);
        }
        return null;
    }

    private void setTimeout(RestTemplate restTemplate, int connectTimeout, int readTimeout) {
        restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory());
        SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory) restTemplate
                .getRequestFactory();
        rf.setReadTimeout(readTimeout);
        rf.setConnectTimeout(connectTimeout);
    }

    protected void start() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                List<MavenDependencyInfo> tmp = downloadGoogleIndex(2000, 120000);
                if (!tmp.isEmpty()) {
                    googleIndex.clear();
                    googleIndex.addAll(tmp);
                    try (ObjectOutputStream oStream = new ObjectOutputStream(new FileOutputStream(cacheGoogleIndex))) {
                        oStream.writeObject(tmp);
                        oStream.flush();
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    POOL_EXECUTOR.schedule(this, 10, TimeUnit.MINUTES);
                }
            }
        };
        if (cacheGoogleIndex.exists() && cacheGoogleIndex.isFile()) {
            try (ObjectInputStream objStream = new ObjectInputStream(new FileInputStream(cacheGoogleIndex))) {
                List<MavenDependencyInfo> tmp = (List<MavenDependencyInfo>) objStream.readObject();
                if (!tmp.isEmpty()) {
                    googleIndex.clear();
                    googleIndex.addAll(tmp);
                }
            } catch (Exception ex) {
            }
        }

        POOL_EXECUTOR.schedule(runnable, 2, TimeUnit.MINUTES);
    }

    @Override
    public void run() {
        start();
    }

}
