/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.layout;

import java.io.Serializable;

/**
 *
 * @author arsi
 */
public enum AndroidWidgetType implements Serializable {
    ToBeDetermined,
    Widget,
    Layout,
    LayoutParams,
    Other;

    public static final AndroidWidgetType decode(String line) {
        switch (line.charAt(0)) {
            case 'L':
                return AndroidWidgetType.Layout;
            case 'P':
                return AndroidWidgetType.LayoutParams;
            case 'W':
                return AndroidWidgetType.Widget;
        }
        return AndroidWidgetType.ToBeDetermined;
    }
}
