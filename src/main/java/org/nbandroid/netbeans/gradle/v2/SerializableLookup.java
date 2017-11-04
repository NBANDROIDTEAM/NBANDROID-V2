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
package org.nbandroid.netbeans.gradle.v2;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jtrim.utils.ExceptionHelper;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author arsi
 */
public final class SerializableLookup implements Serializable {

    private static final long serialVersionUID = 1L;

    public final Lookup lookup;

    public SerializableLookup(Lookup lookup) {
        ExceptionHelper.checkNotNullArgument(lookup, "lookup");

        this.lookup = lookup;
    }

    public <T> T lookup(Class<T> clazz) {
        return lookup.lookup(clazz);
    }

    public <T> Collection<? extends T> lookupAll(Class<T> clazz) {
        return lookup.lookupAll(clazz);
    }

    private Object writeReplace() {
        return new SerializedFormat(this);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Use proxy.");
    }

    private static final class SerializedFormat implements Serializable {

        private static final long serialVersionUID = 1L;

        private final Object[] lookupContent;

        public SerializedFormat(SerializableLookup source) {
            this.lookupContent = filterSerializable(source.lookup.lookupAll(Object.class));
        }

        private static Object[] filterSerializable(Collection<?> objects) {
            List<Object> result = new ArrayList<>(objects.size());
            for (Object obj : objects) {
                if (obj instanceof Serializable) {
                    result.add(obj);
                }
            }
            return result.toArray();
        }

        private Object readResolve() throws ObjectStreamException {
            return new SerializableLookup(Lookups.fixed(lookupContent));
        }
    }
}
