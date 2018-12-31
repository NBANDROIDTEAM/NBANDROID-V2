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
package sk.arsi.netbeans.gradle.android.bintray.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import sk.arsi.netbeans.gradle.android.google.impl.GoogleSearchProviderImpl;
import sk.arsi.netbeans.gradle.android.maven.MavenDependencyInfo;
import sk.arsi.netbeans.gradle.android.maven.MavenSearchProvider;
import sk.arsi.netbeans.gradle.android.maven.RepoSearchListener;
import sk.arsi.netbeans.gradle.android.maven.repository.Repository;
import sk.arsi.netbeans.gradle.android.maven.repository.RepositoryType;

/**
 *
 * @author arsi
 */
@ServiceProvider(service = MavenSearchProvider.class)
public class BintraySearchProviderImpl implements MavenSearchProvider {

    //https://api.bintray.com/search/packages/maven?g=org.actframework&repo=jcenter
    //https://api.bintray.com/search/packages?name=org.knowhowlab.osgi&repo=jcenter
    public static final String BINTRAY_SEARCH = "https://api.bintray.com/search/packages?name={name}&repo={repo}";
    public static final String BINTRAY_SEARCH_NO_REPO = "https://api.bintray.com/search/packages?name={name}";

    @Override
    public void searchPackageName(String searchText, String repo, RepoSearchListener listener, long searchId, List<Repository> repositories) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    HttpHeaders requestHeaders = new HttpHeaders();
                    requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                    RestTemplate restTemplate = new RestTemplate();
                    setTimeout(restTemplate, 2000, 120000);
                    restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                    ResponseEntity<BintraySearchResult[]> response = null;
                    if (repo != null) {
                        response = restTemplate.exchange(BINTRAY_SEARCH, HttpMethod.GET, new HttpEntity<>(requestHeaders), BintraySearchResult[].class, searchText, repo);
                    } else {
                        response = restTemplate.exchange(BINTRAY_SEARCH_NO_REPO, HttpMethod.GET, new HttpEntity<>(requestHeaders), BintraySearchResult[].class, searchText);
                    }
                    BintraySearchResult[] results = response.getBody();
                    List<MavenDependencyInfo> tmp = new ArrayList<>();
                    if (results != null) {
                        for (BintraySearchResult result : results) {
                            tmp.addAll(result.build());
                        }
                    }
                    List<MavenDependencyInfo> collect = tmp.parallelStream().filter(info -> info.getGradleLine().toLowerCase().contains(searchText)).collect(Collectors.toList());
                    try {
                        listener.searchDone(RepoSearchListener.Type.JCENTER, searchId, false, collect);
                    } catch (Exception e) {
                        Exceptions.printStackTrace(e);
                    }
                } catch (Exception exception) {
                    try {
                        listener.searchDone(RepoSearchListener.Type.JCENTER, searchId, false, new ArrayList<>());
                    } catch (Exception e) {
                        Exceptions.printStackTrace(e);
                    }
                    Exceptions.printStackTrace(exception);
                }
            }
        };
        boolean enabled = false;
        for (Repository repository : repositories) {
            if (repository.getType() == RepositoryType.JCENTER || repository.getUrl().contains(".bintray.com/")) {
                enabled = true;
                break;
            }
        }
        //long lasting operation, first call partial
        if (enabled) {
            try {
                listener.searchDone(RepoSearchListener.Type.JCENTER, searchId, true, new ArrayList<>());
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
            GoogleSearchProviderImpl.SEARCH_PROCESSOR.execute(runnable);
        } else {
            try {
                listener.searchDone(RepoSearchListener.Type.JCENTER, searchId, false, new ArrayList<>());
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }

    }

    private void setTimeout(RestTemplate restTemplate, int connectTimeout, int readTimeout) {
        restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory());
        SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory) restTemplate
                .getRequestFactory();
        rf.setReadTimeout(readTimeout);
        rf.setConnectTimeout(connectTimeout);
    }

}
