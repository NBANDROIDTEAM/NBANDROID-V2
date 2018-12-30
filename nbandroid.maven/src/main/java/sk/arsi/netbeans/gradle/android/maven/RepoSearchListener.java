/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sk.arsi.netbeans.gradle.android.maven;

import java.util.List;

/**
 *
 * @author arsi
 */
public interface RepoSearchListener {

    public void searchDone(long searchId, List<MavenDependencyInfo> dependencyInfos);
}
