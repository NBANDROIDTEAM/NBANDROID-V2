/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.project.distributions;

import java.util.List;
import java.util.Locale;
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

    public double getSupportedDistributionForApiLevel(int apiLevel) {
        if (apiLevel <= 0) {
            return 0;
        }
        List<DistributionPOJO> distributions = getDistributions();
        if (distributions == null) {
            return -1;
        }
        double unsupportedSum = 0;
        for (DistributionPOJO d : distributions) {
            if (Integer.parseInt(d.getApiLevel()) >= apiLevel) {
                break;
            }
            unsupportedSum += Double.parseDouble(d.getDistributionPercentage());
        }
        return 1 - unsupportedSum;
    }

    public static String getApiHelpText(int selectedApi, String selectedApiName) {
        float percentage = (float) (AndroidDistributionsProvider.getDefault().getSupportedDistributionForApiLevel(selectedApi) * 100);
        return String.format(Locale.getDefault(), "<html>By targeting API %1$s and later, your app will run on %2$s of the devices<br>that are "
                + "active on the Google Play Store.</html>",
                selectedApiName,
                percentage < 1 ? "&lt; 1%" : String.format(Locale.getDefault(), "approximately <b>%.1f%%</b>", percentage));
    }
}
