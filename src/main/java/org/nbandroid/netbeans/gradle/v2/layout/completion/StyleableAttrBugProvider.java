/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.layout.completion;

import java.util.EnumSet;
import org.nbandroid.netbeans.gradle.v2.layout.AndroidStyleableAttrType;

/**
 *
 * @author arsi
 */
public interface StyleableAttrBugProvider {

    public boolean handleBugs(String name, EnumSet<AndroidStyleableAttrType> attrTypes);

}
