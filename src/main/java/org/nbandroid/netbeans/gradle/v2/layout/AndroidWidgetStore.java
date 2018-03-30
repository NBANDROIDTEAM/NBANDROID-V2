/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author arsi
 */
public class AndroidWidgetStore {

    private static final List<AndroidWidgetAttrEnum> ATTR_ENUMS = new ArrayList<>();
    private static final List<AndroidWidgetAttrFlag> ATTR_FLAGS = new ArrayList<>();
    private static final List<AndroidWidgetAttr> WIDGET_ATTRS = new ArrayList<>();
    private static final ReentrantLock LOCK = new ReentrantLock(true);

    public static AndroidWidgetAttrEnum getOrAddEnum(AndroidWidgetAttrEnum attrEnum) {
        LOCK.lock();
        try {
            if (ATTR_ENUMS.contains(attrEnum)) {
                return ATTR_ENUMS.get(ATTR_ENUMS.indexOf(attrEnum));
            } else {
                ATTR_ENUMS.add(attrEnum);
                return attrEnum;
            }
        } finally {
            LOCK.unlock();
        }
    }

    public static AndroidWidgetAttrFlag getOrAddFlag(AndroidWidgetAttrFlag attrFlag) {
        LOCK.lock();
        try {
            if (ATTR_FLAGS.contains(attrFlag)) {
                return ATTR_FLAGS.get(ATTR_FLAGS.indexOf(attrFlag));
            } else {
                ATTR_FLAGS.add(attrFlag);
                return attrFlag;
            }
        } finally {
            LOCK.unlock();
        }
    }

    public static AndroidWidgetAttr getOrAddAttr(AndroidWidgetAttr attr) {
        LOCK.lock();
        try {
            if (WIDGET_ATTRS.contains(attr)) {
                return WIDGET_ATTRS.get(WIDGET_ATTRS.indexOf(attr));
            } else {
                WIDGET_ATTRS.add(attr);
                return attr;
            }
        } finally {
            LOCK.unlock();
        }
    }

}
