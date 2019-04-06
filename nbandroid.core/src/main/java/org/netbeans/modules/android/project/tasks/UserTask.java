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
package org.netbeans.modules.android.project.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import nbandroid.gradle.spi.GradleCommandTemplate;

/**
 *
 * @author arsi
 */
public class UserTask {

    private String taskName;
    private final List<String> tasks = new ArrayList<>();
    private final List<String> gradleArguments = new ArrayList<>();
    private final List<String> jvmArguments = new ArrayList<>();

    public GradleCommandTemplate getCommandTemplate() {
        GradleCommandTemplate.Builder builder = new GradleCommandTemplate.Builder(taskName, tasks);
        builder.setArguments(gradleArguments);
        builder.setJvmArguments(jvmArguments);
        return builder.create();
    }

    public UserTask(String taskName) {
        this.taskName = taskName;
    }

    public UserTask() {
    }

    public static UserTask deserializeFromString(String s) {
        StringTokenizer st1 = new StringTokenizer(s, "|||||", false);
        if (st1.countTokens() > 0) {
            UserTask task = new UserTask();
            task.taskName = st1.nextToken();
            StringTokenizer st2 = null;
            if (st1.hasMoreElements()) {
                st2 = new StringTokenizer(st1.nextToken(), ";;;;;", false);
                while (st2.hasMoreElements()) {
                    task.tasks.add(st2.nextToken());
                }
                if (st1.hasMoreElements()) {
                    st2 = new StringTokenizer(st1.nextToken(), ";;;;;", false);
                    while (st2.hasMoreElements()) {
                        task.gradleArguments.add(st2.nextToken());
                    }
                    if (st1.hasMoreElements()) {
                        st2 = new StringTokenizer(st1.nextToken(), ";;;;;", false);
                        while (st2.hasMoreElements()) {
                            task.jvmArguments.add(st2.nextToken());
                        }
                    }
                }

            }

            return task;
        }
        return null;
    }

    public static String listToString(List<UserTask> tasks) {
        if (tasks.isEmpty()) {
            return "";
        }
        String tmp = "";
        for (UserTask task : tasks) {
            tmp += task.serializeToString() + "~~~~~";
        }
        return tmp.substring(0, tmp.length() - 2);
    }

    public static List<UserTask> stringToList(String s) {
        if (s.isEmpty()) {
            return new ArrayList<>();
        }
        List<UserTask> tmp = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(s, "~~~~~", false);
        while (st.hasMoreElements()) {
            UserTask userTask = deserializeFromString(st.nextToken());
            if (userTask != null) {
                tmp.add(userTask);
            }
        }
        return tmp;
    }

    public String serializeToString() {
        String tmp = "";
        tmp += taskName + "|||||";
        for (String task : tasks) {
            tmp += task + ";;;;;";
        }
        tmp += "|||||";
        for (String gradle : gradleArguments) {
            tmp += gradle + ";;;;;";
        }
        tmp += "|||||";
        for (String jvm : jvmArguments) {
            tmp += jvm + ";;;;;";
        }
        return tmp;
    }

    public List<String> getTasks() {
        return tasks;
    }

    public List<String> getGradleArguments() {
        return gradleArguments;
    }

    public List<String> getJvmArguments() {
        return jvmArguments;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    @Override
    public String toString() {
        return taskName; //To change body of generated methods, choose Tools | Templates.
    }

}
