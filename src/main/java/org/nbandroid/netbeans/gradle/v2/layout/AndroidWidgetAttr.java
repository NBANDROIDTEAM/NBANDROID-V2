/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.nbandroid.netbeans.gradle.v2.layout;

import java.io.Serializable;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author arsi
 */
public class AndroidWidgetAttr implements Serializable {

    private final String name;
    private final EnumSet<AndroidWidgetAttrType> attrTypes;
    private final AndroidWidgetAttrEnum[] enums;
    private final AndroidWidgetAttrFlag[] flags;
    private final String description;

    public AndroidWidgetAttr(String name, String description, AndroidWidgetAttrType... attrTypes) {
        this.attrTypes = EnumSet.copyOf(Arrays.asList(attrTypes));
        this.enums = null;
        this.flags = null;
        this.description = description;
        this.name = name;
    }

    public AndroidWidgetAttr(String name, String description, List<AndroidWidgetAttrFlag> flags, List<AndroidWidgetAttrEnum> enums, AndroidWidgetAttrType... attrTypes) {
        this.attrTypes = EnumSet.copyOf(Arrays.asList(attrTypes));
        this.enums = enums.toArray(new AndroidWidgetAttrEnum[enums.size()]);;
        this.flags = flags.toArray(new AndroidWidgetAttrFlag[flags.size()]);
        this.description = description;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public AndroidWidgetAttrEnum[] getEnums() {
        if (enums != null) {
            return enums;
        } else {
            return new AndroidWidgetAttrEnum[0];
        }
    }

    public AndroidWidgetAttrFlag[] getFlags() {
        if (flags != null) {
            return flags;
        } else {
            return new AndroidWidgetAttrFlag[0];
        }
    }

    public EnumSet<AndroidWidgetAttrType> getAttrTypes() {
        return attrTypes;
    }

    @Override
    public String toString() {
        return "AndroidWidgetAttr{" + "name=" + name + ", attrTypes=" + attrTypes + ", enums=" + enums + ", flags=" + flags + ", description=" + description + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.name);
        hash = 23 * hash + Objects.hashCode(this.attrTypes);
        hash = 23 * hash + Arrays.deepHashCode(this.enums);
        hash = 23 * hash + Arrays.deepHashCode(this.flags);
        hash = 23 * hash + Objects.hashCode(this.description);
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
        final AndroidWidgetAttr other = (AndroidWidgetAttr) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.attrTypes, other.attrTypes)) {
            return false;
        }
        if (!Arrays.deepEquals(this.enums, other.enums)) {
            return false;
        }
        if (!Arrays.deepEquals(this.flags, other.flags)) {
            return false;
        }
        return true;
    }

}
