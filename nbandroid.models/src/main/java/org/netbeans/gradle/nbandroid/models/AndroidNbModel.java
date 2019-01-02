/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.gradle.nbandroid.models;

import java.io.Serializable;

/**
 *
 * @author arsi
 */
public class AndroidNbModel implements Serializable {

    private static final long serialVersionUID = 2L;

    private transient Object lookup = null;


    public AndroidNbModel() {
    }

    public Object getLookup() {
        return lookup;
    }

    public void setLookup(Object lookup) {
        this.lookup = lookup;
    }


}
