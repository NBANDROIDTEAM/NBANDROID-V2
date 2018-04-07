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
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.nbandroid.netbeans.gradle.v2.layout.completion.StyleableAttrBugProvider;
import org.openide.util.Lookup;

/**
 *
 * @author arsi
 */
public class AndroidStyleableAttr implements Serializable {

    private final String name;
    private final EnumSet<AndroidStyleableAttrType> attrTypes;
    private final AndroidStyleableAttrEnum[] enums;
    private final AndroidStyleableAttrFlag[] flags;
    private final String description;

    public AndroidStyleableAttr(String name, String description, AndroidStyleableAttrType... attrTypes) {
        this.attrTypes = EnumSet.copyOf(Arrays.asList(attrTypes));
        this.enums = null;
        this.flags = null;
        this.description = description;
        if (name.contains(":")) {
            this.name = name.substring(name.lastIndexOf(':') + 1);
        } else {
            this.name = name;
        }
        handleBugs();
    }

    public AndroidStyleableAttr(String name, String description, List<AndroidStyleableAttrFlag> flags, List<AndroidStyleableAttrEnum> enums, AndroidStyleableAttrType... attrTypes) {
        this.attrTypes = EnumSet.copyOf(Arrays.asList(attrTypes));
        this.enums = enums.toArray(new AndroidStyleableAttrEnum[enums.size()]);;
        this.flags = flags.toArray(new AndroidStyleableAttrFlag[flags.size()]);
        this.description = description;
        if (name.contains(":")) {
            this.name = name.substring(name.lastIndexOf(':') + 1);
        } else {
            this.name = name;
        }
        handleBugs();
    }

    private void handleBugs() {
        Collection<? extends StyleableAttrBugProvider> bugProviders = Lookup.getDefault().lookupAll(StyleableAttrBugProvider.class);
        Iterator<? extends StyleableAttrBugProvider> iterator = bugProviders.iterator();
        while (iterator.hasNext()) {
            StyleableAttrBugProvider next = iterator.next();
            if (next.handleBugs(name, this.attrTypes)) {
                break;
            }
        }
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public AndroidStyleableAttrEnum[] getEnums() {
        if (enums != null) {
            return enums;
        } else {
            return new AndroidStyleableAttrEnum[0];
        }
    }

    public AndroidStyleableAttrFlag[] getFlags() {
        if (flags != null) {
            return flags;
        } else {
            return new AndroidStyleableAttrFlag[0];
        }
    }

    public EnumSet<AndroidStyleableAttrType> getAttrTypes() {
        return attrTypes;
    }

    @Override
    public String toString() {
        return "AndroidStyleableAttr{" + "name=" + name + ", attrTypes=" + attrTypes + ", enums=" + enums + ", flags=" + flags + ", description=" + description + '}';
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
        final AndroidStyleableAttr other = (AndroidStyleableAttr) obj;
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
