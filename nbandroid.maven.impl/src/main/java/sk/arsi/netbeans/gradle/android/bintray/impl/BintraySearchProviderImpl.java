/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import sk.arsi.netbeans.gradle.android.bintray.BintraySearchProvider;
import sk.arsi.netbeans.gradle.android.google.impl.GoogleSearchProviderImpl;
import sk.arsi.netbeans.gradle.android.maven.MavenDependencyInfo;
import sk.arsi.netbeans.gradle.android.maven.RepoSearchListener;

/**
 *
 * @author arsi
 */
@ServiceProvider(service = BintraySearchProvider.class)
public class BintraySearchProviderImpl implements BintraySearchProvider {

    //https://api.bintray.com/search/packages/maven?g=org.actframework&repo=jcenter
    //https://api.bintray.com/search/packages?name=org.knowhowlab.osgi&repo=jcenter
    public static final String BINTRAY_SEARCH = "https://api.bintray.com/search/packages?name={name}&repo={repo}";

    @Override
    public void searchPackageName(String name, String repo, RepoSearchListener listener, long searchId) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    HttpHeaders requestHeaders = new HttpHeaders();
                    requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                    RestTemplate restTemplate = new RestTemplate();
                    setTimeout(restTemplate, 2000, 120000);
                    restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                    ResponseEntity<BintraySearchResult[]> response = restTemplate.exchange(BINTRAY_SEARCH, HttpMethod.GET, new HttpEntity<>(requestHeaders), BintraySearchResult[].class, name, repo);
                    BintraySearchResult[] results = response.getBody();
                    List<MavenDependencyInfo> tmp = new ArrayList<>();
                    if (results != null) {
                        for (BintraySearchResult result : results) {
                            tmp.addAll(result.build());
                        }
                    }
                    List<MavenDependencyInfo> collect = tmp.parallelStream().filter(info -> info.getGradleLine().toLowerCase().contains(name)).collect(Collectors.toList());
                    try {
                        listener.searchDone(searchId, collect);
                    } catch (Exception e) {
                        Exceptions.printStackTrace(e);
                    }
                } catch (Exception exception) {
                    try {
                        listener.searchDone(searchId, new ArrayList<>());
                    } catch (Exception e) {
                        Exceptions.printStackTrace(e);
                    }
                    Exceptions.printStackTrace(exception);
                }
            }
        };
        GoogleSearchProviderImpl.POOL_EXECUTOR.execute(runnable);
    }

    private void setTimeout(RestTemplate restTemplate, int connectTimeout, int readTimeout) {
        restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory());
        SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory) restTemplate
                .getRequestFactory();
        rf.setReadTimeout(readTimeout);
        rf.setConnectTimeout(connectTimeout);
    }

}
