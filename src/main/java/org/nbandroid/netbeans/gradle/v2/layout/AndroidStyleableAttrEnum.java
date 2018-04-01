/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nbandroid.netbeans.gradle.v2.layout;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author arsi
 */
public class AndroidStyleableAttrEnum implements Serializable {

    private final String name;
    private final String value;
    private final String comment;

    public AndroidStyleableAttrEnum(String name, String value, String comment) {
        this.name = name;
        this.value = value;
        this.comment = comment;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.name);
        hash = 97 * hash + Objects.hashCode(this.value);
        hash = 97 * hash + Objects.hashCode(this.comment);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AndroidStyleableAttrEnum other = (AndroidStyleableAttrEnum) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        if (!Objects.equals(this.comment, other.comment)) {
            return false;
        }
        return true;
    }


}
