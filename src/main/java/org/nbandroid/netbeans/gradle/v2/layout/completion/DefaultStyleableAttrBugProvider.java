/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.layout.completion;

import java.util.EnumSet;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleableAttrType;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author arsi
 */
@ServiceProvider(service = StyleableAttrBugProvider.class)
public class DefaultStyleableAttrBugProvider implements StyleableAttrBugProvider {

    @Override
    public boolean handleBugs(String name, EnumSet<AndroidStyleableAttrType> attrTypes) {
        if (attrTypes.contains(AndroidStyleableAttrType.Unknown)) {
            switch (name) {
                case "minWidth":
                case "minHeight":
                    attrTypes.remove(AndroidStyleableAttrType.Unknown);
                    attrTypes.add(AndroidStyleableAttrType.Dimension);
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

}
