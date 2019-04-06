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
package nbandroid.gradle.tooling;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

/**
 *
 * @author arsi
 */
public class NbAndroidToolingPlugin implements Plugin<Project> {

    private final ToolingModelBuilderRegistry registry;

    @Inject
    public NbAndroidToolingPlugin(ToolingModelBuilderRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void apply(Project target) {
        registry.register(new NbAndroidToolingModelBuilder());
    }

    private static class NbAndroidToolingModelBuilder implements ToolingModelBuilder {

        public NbAndroidToolingModelBuilder() {
        }

        @Override
        public boolean canBuild(String modelName) {
            return AndroidProjectInfo.class.getName().equals(modelName);
        }

        @Override
        public Object buildAll(String modelName, Project project) {
            AndroidProjectInfo info = new AndroidProjectInfoImpl();
            try {
                ((AndroidProjectInfoImpl) info).setProjectPath(project.getProjectDir().getAbsolutePath());
                Map<String, List<TaskInfo>> projectTasks = info.getProjectTasks();
                Map<Project, Set<Task>> tasks = project.getAllTasks(true);
                for (Map.Entry<Project, Set<Task>> entry : tasks.entrySet()) {
                    Set<Task> tasksSet = entry.getValue();
                    tasksSet.stream().forEach(t -> {
                        String group = t.getGroup();
                        if (group == null) {
                            group = "default";
                        }
                        List<TaskInfo> lst = projectTasks.get(group);
                        if (lst == null) {
                            lst = new ArrayList<>();
                            projectTasks.put(group, lst);
                        }
                        TaskInfoImpl infoImpl = new TaskInfoImpl(t.getEnabled(), group, t.getDescription(), t.getName(), t.getPath());
                        if (!lst.contains(infoImpl)) {
                            lst.add(infoImpl);
                        }

                    });
                }
                List<String> defaultTasks = project.getDefaultTasks();
                List<TaskInfo> lst = projectTasks.get("default");
                if (lst == null) {
                    lst = new ArrayList<>();
                    projectTasks.put("default", lst);
                }
                for (String defaultTask : defaultTasks) {
                    TaskInfoImpl infoImpl = new TaskInfoImpl(true, "default", defaultTask, defaultTask, ":" + defaultTask);
                    if (!lst.contains(infoImpl)) {
                        lst.add(infoImpl);
                    }
                }

            } catch (Exception ex) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                pw.println(ex.toString());
                ex.printStackTrace(pw);

                ((AndroidProjectInfoImpl) info).setException(sw.toString());
            }
            return info;
        }
    }

}
