/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 /*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nbandroid.netbeans.gradle.api;

import javax.annotation.concurrent.Immutable;

/**
 * A description of a resource reference from R.java file generated by composing
 * project resources.
 */
@Immutable
public class ResourceRef {

    public final boolean samePackage;
    public final String packageName;
    // TODO use ResourceType?
    public final String resourceType;
    public final String resourceName;
    /**
     * A value representing primitive value assigned to resource. Technically it
     * can be an array and thus mutable state of this class.
     */
    public final Object resourceValue;

    public ResourceRef(boolean samePackage, String packageName, String resourceType, String resourceName,
            Object resourceValue) {
        this.samePackage = samePackage;
        this.packageName = packageName;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.resourceValue = resourceValue;
    }

    /**
     * Expression used in XML file to refer to this.
     */
    public String toRefString() {
        return "@" + (samePackage ? "" : packageName + ":")
                + resourceType + "/" + resourceName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ResourceRef other = (ResourceRef) obj;
        if (this.samePackage != other.samePackage) {
            return false;
        }
        if ((this.packageName == null) ? (other.packageName != null) : !this.packageName.equals(other.packageName)) {
            return false;
        }
        if ((this.resourceType == null) ? (other.resourceType != null) : !this.resourceType.equals(other.resourceType)) {
            return false;
        }
        if ((this.resourceName == null) ? (other.resourceName != null) : !this.resourceName.equals(other.resourceName)) {
            return false;
        }
        // value should be derived from other values
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash + (this.samePackage ? 1 : 0);
        hash = 13 * hash + (this.packageName != null ? this.packageName.hashCode() : 0);
        hash = 13 * hash + (this.resourceType != null ? this.resourceType.hashCode() : 0);
        hash = 13 * hash + (this.resourceName != null ? this.resourceName.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "@" + (samePackage ? "" : packageName + ":")
                + resourceType + "/" + resourceName + "=" + resourceValue;
    }
}
