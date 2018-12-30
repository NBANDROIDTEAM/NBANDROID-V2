/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sk.arsi.netbeans.gradle.android.google.impl;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author arsi
 */
public class GoogleMasterIndex {

    private final Map<String, String> groups = new HashMap<>();

    public GoogleMasterIndex() {
    }

    public Map<String, String> getGroups() {
        return groups;
    }

}
