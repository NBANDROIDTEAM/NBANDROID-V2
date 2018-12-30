/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.netbeans.gradle.android.bintray;

import sk.arsi.netbeans.gradle.android.maven.RepoSearchListener;

/**
 *
 * @author arsi
 */
public interface BintraySearchProvider {

    public void searchPackageName(String name, String repo, RepoSearchListener listener, long searchId);

}
