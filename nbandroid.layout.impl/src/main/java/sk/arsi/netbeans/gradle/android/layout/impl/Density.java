/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.netbeans.gradle.android.layout.impl;

/**
 *
 * @author arsi
 */
public enum Density {
    LOW(com.android.resources.Density.LOW),
    MEDIUM(com.android.resources.Density.MEDIUM),
    TV(com.android.resources.Density.TV),
    HIGH(com.android.resources.Density.HIGH),
    XHIGH(com.android.resources.Density.XHIGH),
    XXHIGH(com.android.resources.Density.XXHIGH),
    XXXHIGH(com.android.resources.Density.XXXHIGH),;
    private final com.android.resources.Density density;

    private Density(com.android.resources.Density density) {
        this.density = density;
    }

    public com.android.resources.Density getDensity() {
        return density;
    }

    @Override
    public String toString() {
        return density.getLongDisplayValue();
    }

}
