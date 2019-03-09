/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package nbandroid.gradle.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class GradleCommandTemplate {

    public static final class Builder {

        private final String displayName;
        private final List<String> tasks;
        private List<String> arguments;
        private List<String> jvmArguments;

        public Builder(GradleCommandTemplate command) {
            this.displayName = command.getDisplayName();
            this.tasks = command.getTasks();
            this.arguments = command.getArguments();
            this.jvmArguments = command.getJvmArguments();
        }

        public Builder(
                String displayName,
                List<String> tasks) {

            this.displayName = displayName;
            this.tasks = copyNullSafeList(tasks);
            this.arguments = Collections.emptyList();
            this.jvmArguments = Collections.emptyList();

            if (this.tasks.isEmpty()) {
                throw new IllegalArgumentException("Must have at least a single task specified.");
            }
        }

        public void setArguments(List<String> arguments) {
            this.arguments = copyNullSafeList(arguments);
        }

        public void setJvmArguments(List<String> jvmArguments) {
            this.jvmArguments = copyNullSafeList(jvmArguments);
        }

        public GradleCommandTemplate create() {
            return new GradleCommandTemplate(this);
        }
    }

    private final String displayName;
    private final List<String> tasks;
    private final List<String> arguments;
    private final List<String> jvmArguments;

    private GradleCommandTemplate(Builder builder) {
        this.displayName = builder.displayName;
        this.tasks = builder.tasks;
        this.arguments = builder.arguments;
        this.jvmArguments = builder.jvmArguments;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSafeDisplayName() {
        return displayName.isEmpty() ? tasks.toString() : displayName;
    }

    public List<String> getTasks() {
        return tasks;
    }

    public String[] getTasksArray() {
        return tasks.toArray(new String[tasks.size()]);
    }

    public List<String> getArguments() {
        return arguments;
    }

    public String[] getArgumentsArray() {
        return arguments.toArray(new String[arguments.size()]);
    }

    public List<String> getJvmArguments() {
        return jvmArguments;
    }

    public String[] getJvmArgumentsArray() {
        return jvmArguments.toArray(new String[jvmArguments.size()]);
    }

    public static <E> List<E> copyNullSafeList(Collection<? extends E> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(copyNullSafeMutableList(list));
    }

    public static <E> ArrayList<E> copyNullSafeMutableList(Collection<? extends E> list) {
        if (list == null) {
            throw new NullPointerException("list");
        }

        ArrayList<E> result = new ArrayList<E>(list);
        for (E element : result) {
            if (element == null) {
                throw new NullPointerException("element");
            }
        }
        return result;
    }
}
