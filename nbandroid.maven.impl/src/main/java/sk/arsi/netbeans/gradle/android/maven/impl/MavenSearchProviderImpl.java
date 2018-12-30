/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sk.arsi.netbeans.gradle.android.maven.impl;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.maven.indexer.api.RepositoryIndexer;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import sk.arsi.netbeans.gradle.android.maven.MavenSearchProvider;

/**
 *
 * @author arsi
 */
@ServiceProvider(service = MavenSearchProvider.class)
public class MavenSearchProviderImpl implements MavenSearchProvider {

    public static final String ANDROID_REPO = "https://dl.google.com/dl/android/maven2/master-index.xml";
    public static final String MAVEN_CENTRAL = "http://central.maven.org/maven2";


    @Override
    public void getLoadedContexts() {
        RepositoryInfo GOOGLE = null;
        try {
            GOOGLE = new RepositoryInfo("Google", "Google", null, MAVEN_CENTRAL);
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        }
        RepositoryPreferences.getInstance().addOrModifyRepositoryInfo(GOOGLE);
        List<RepositoryInfo> loadedContexts = RepositoryQueries.getLoadedContexts();
        RepositoryIndexer.indexRepo(GOOGLE);
        List<RepositoryInfo> tmp = new ArrayList<>();
        tmp.add(GOOGLE);
        RepositoryQueries.Result<String> artifactsResult = RepositoryQueries.getArtifactsResult("com.android.support", tmp);
        List<String> results = artifactsResult.getResults();
        System.out.println("sk.arsi.netbeans.gradle.android.maven.impl.MavenProviderImpl.getLoadedContexts()");
    }
}
