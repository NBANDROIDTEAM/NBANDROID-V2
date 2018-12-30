/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sk.arsi.netbeans.gradle.android.google.impl;

import org.openide.util.Lookup;
import org.openide.windows.OnShowing;
import sk.arsi.netbeans.gradle.android.google.GoogleSearchProvider;

/**
 *
 * @author arsi
 */
@OnShowing
public class GoogleOnShowing implements Runnable {

    @Override
    public void run() {
        GoogleSearchProvider provider = Lookup.getDefault().lookup(GoogleSearchProvider.class);
        if (provider instanceof GoogleSearchProviderImpl) {
            new Thread(((GoogleSearchProviderImpl) provider), "Google-index-updater").start();
        }
    }

}
