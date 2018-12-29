/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nbandroid.netbeans.gradle.v2.project.distributions;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.codehaus.jackson.map.ObjectMapper;
import org.openide.modules.OnStart;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author arsi
 */
@ServiceProvider(service = AndroidDistributionsProvider.class)
@OnStart
public class AndroidDistributionsProviderImpl extends AndroidDistributionsProvider implements Runnable {

    private static final String STATS_URL = "https://dl.google.com/android/studio/metadata/distributions.json";
    private List<DistributionPOJO> pojos = new ArrayList<>();
    private static final ScheduledExecutorService POOL = Executors.newScheduledThreadPool(1);

    public AndroidDistributionsProviderImpl() {

    }

    @Override
    public List<DistributionPOJO> getDistributions() {
        return pojos;
    }

    @Override
    public void run() {
        ObjectMapper mapper = new ObjectMapper(); // just need one
        try {
            // Got a Java class that data maps to nicely? If so:
            DistributionPOJO graph[] = mapper.readValue(new URL(STATS_URL), DistributionPOJO[].class);
            pojos = new ArrayList<>(Arrays.asList(graph));
            return;
            // Or: if no class (and don't need one), just map to Map.class:
        } catch (Exception ex) {
        }
        POOL.schedule(this, 5, TimeUnit.MINUTES);
    }

}
