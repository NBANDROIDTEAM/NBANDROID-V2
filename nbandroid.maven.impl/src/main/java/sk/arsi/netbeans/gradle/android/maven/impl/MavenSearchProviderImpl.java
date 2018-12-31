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
package sk.arsi.netbeans.gradle.android.maven.impl;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.QueryField;
import org.netbeans.modules.maven.indexer.api.RepositoryIndexer;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
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
public class MavenSearchProviderImpl implements MavenSearchProvider {

    public static final String ANDROID_REPO = "https://dl.google.com/dl/android/maven2/master-index.xml";
    public static final String MAVEN_CENTRAL = "http://central.maven.org/maven2";

    @Override
    public void searchPackageName(String queryText, String repo, RepoSearchListener listener, long searchId, List<Repository> repositories) {
        Runnable runnable = new Runnable() {
            public void run() {
                List<Repository> reposToLoad = new ArrayList<>(repositories);
                for (Iterator<Repository> iterator = reposToLoad.iterator(); iterator.hasNext();) {
                    Repository repository = iterator.next();
                    if (repository.getType() != RepositoryType.MAVEN && repository.getType() != RepositoryType.MAVEN_CENTRAL) {
                        iterator.remove();
                    }
                }
                if (!reposToLoad.isEmpty()) {
                    List<RepositoryInfo> repositoryInfosToUse = new ArrayList<>();
                    List<RepositoryInfo> repositoryInfos = RepositoryPreferences.getInstance().getRepositoryInfos();
                    for (Iterator<Repository> iterator = reposToLoad.iterator(); iterator.hasNext();) {
                        Repository rp = iterator.next();
                        for (RepositoryInfo repositoryInfo : repositoryInfos) {
                            if (repositoryInfo.getRepositoryUrl() != null && rp.getUrl() != null) {
                                String repositoryUrl1 = repositoryInfo.getRepositoryUrl().toLowerCase();
                                repositoryUrl1 = repositoryUrl1.replace("https://", "");
                                repositoryUrl1 = repositoryUrl1.replace("http://", "");
                                String repositoryUrl2 = rp.getUrl();
                                repositoryUrl2 = repositoryUrl2.replace("https://", "");
                                repositoryUrl2 = repositoryUrl2.replace("http://", "");
                                if (!repositoryUrl1.endsWith("/")) {
                                    repositoryUrl1 += "/";
                                }
                                if (!repositoryUrl2.endsWith("/")) {
                                    repositoryUrl2 += "/";
                                }
                                if (repositoryUrl1.equals(repositoryUrl2)) {
                                    repositoryInfosToUse.add(repositoryInfo);
                                    iterator.remove();
                                }
                            }
                        }
                    }
                    for (Repository rp : reposToLoad) {
                        try {
                            RepositoryInfo info = new RepositoryInfo(rp.extractNameFromUrl(), rp.extractNameFromUrl(), null, rp.getUrl());
                            RepositoryPreferences.getInstance().addOrModifyRepositoryInfo(info);
                            RepositoryIndexer.indexRepo(info);
                            repositoryInfosToUse.add(info);
                        } catch (URISyntaxException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    List<MavenDependencyInfo> tmp = search(queryText, repositoryInfosToUse, listener, searchId);
                    try {
                        listener.searchDone(RepoSearchListener.Type.MAVEN, searchId, partial, tmp);
                    } catch (Exception e) {
                    }
                }
            }
        };
        GoogleSearchProviderImpl.SEARCH_PROCESSOR.execute(runnable);
    }

    private boolean partial = false;

    private List<MavenDependencyInfo> search(String queryText, List<RepositoryInfo> repositoryInfosToUse, RepoSearchListener listener, long searchId) {
        final List<QueryField> fields = new ArrayList<>();
        final List<QueryField> fieldsNonClasses = new ArrayList<>();
        String q = queryText.trim();
        String[] splits = q.split(" "); //NOI118N
        List<String> fStrings = new ArrayList<>();
        fStrings.add(QueryField.FIELD_GROUPID);
        fStrings.add(QueryField.FIELD_ARTIFACTID);
        fStrings.add(QueryField.FIELD_VERSION);
        fStrings.add(QueryField.FIELD_NAME);
        fStrings.add(QueryField.FIELD_DESCRIPTION);
        fStrings.add(QueryField.FIELD_CLASSES);
        for (String curText : splits) {
            for (String fld : fStrings) {
                QueryField f = new QueryField();
                f.setField(fld);
                f.setValue(curText);
                fields.add(f);
                if (!QueryField.FIELD_CLASSES.equals(fld)) {
                    fieldsNonClasses.add(f);
                }
            }
        }
        final RepositoryQueries.Result<NBVersionInfo> queryResult = RepositoryQueries.findResult(fields, repositoryInfosToUse);
        partial = queryResult.isPartial();
        if (partial) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    queryResult.waitForSkipped();
                    List<NBVersionInfo> results = queryResult.getResults();
                    List<MavenDependencyInfo> tmp = makeResults(results);
                    try {
                        listener.searchDone(RepoSearchListener.Type.MAVEN, searchId, false, tmp);
                    } catch (Exception e) {
                    }
                }
            };
            GoogleSearchProviderImpl.SEARCH_PROCESSOR.execute(runnable);
        }
        List<NBVersionInfo> results = queryResult.getResults();
        List<MavenDependencyInfo> tmp = makeResults(results);
        return tmp;
    }

    private List<MavenDependencyInfo> makeResults(List<NBVersionInfo> results) {
        final Map<MavenDependencyInfo, List<NBVersionInfo>> map = new HashMap<>();
        for (NBVersionInfo result : results) {
            MavenDependencyInfo dependencyInfo = new MavenDependencyInfo(MavenDependencyInfo.Type.MAVEN, result.getGroupId(), result.getArtifactId());
            List<NBVersionInfo> list = map.get(dependencyInfo);
            if (list == null) {
                list = new ArrayList<>();
                map.put(dependencyInfo, list);
            }
            list.add(result);
        }
        for (Map.Entry<MavenDependencyInfo, List<NBVersionInfo>> entry : map.entrySet()) {
            MavenDependencyInfo dependencyInfo = entry.getKey();
            List<NBVersionInfo> values = entry.getValue();
            for (NBVersionInfo value : values) {
                dependencyInfo.addVersion(value.getVersion());
            }

        }
        List<MavenDependencyInfo> tmp = new ArrayList<>();
        tmp.addAll(map.keySet());
        return tmp;
    }

}
