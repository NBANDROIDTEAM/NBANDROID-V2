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
package sk.arsi.netbeans.gradle.android.maven;

import java.util.List;
import sk.arsi.netbeans.gradle.android.maven.repository.Repository;

/**
 *
 * @author arsi
 */
public interface MavenSearchProvider {

    /**
     * Search for packages There are currently three implementations Google
     * JCenter Maven indexed repos
     *
     * Use the Lookup.getDefault().lookupAll(BintraySearchProvider.class)
     *
     * @param searchText text to search for
     * @param repo only for jfrog repos, default use jcenter, null rearch cross
     * all repos
     * @param listener listener where is search result delivered
     * @param searchId id of current search, used to identify the Search result
     * @param repositories list of project repositories
     */
    public void searchPackageName(String searchText, String repo, RepoSearchListener listener, long searchId, List<Repository> repositories);
}
