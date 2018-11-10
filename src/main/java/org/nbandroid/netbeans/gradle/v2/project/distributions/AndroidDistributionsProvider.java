/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nbandroid.netbeans.gradle.v2.project.distributions;

import java.util.List;
import org.openide.util.Lookup;

/**
 *
 * @author arsi
 */
public abstract class AndroidDistributionsProvider {

    public static AndroidDistributionsProvider getDefault() {
        return Lookup.getDefault().lookup(AndroidDistributionsProvider.class);
    }

    public abstract List<DistributionPOJO> getDistributions();
}
