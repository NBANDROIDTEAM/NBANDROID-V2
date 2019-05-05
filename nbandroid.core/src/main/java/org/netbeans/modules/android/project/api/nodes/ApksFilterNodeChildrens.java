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
package org.netbeans.modules.android.project.api.nodes;

import com.android.build.OutputFile;
import com.android.build.VariantOutput;
import com.android.builder.model.ProjectBuildOutput;
import com.android.builder.model.VariantBuildOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author arsi
 */
public class ApksFilterNodeChildrens extends Children.Keys<DataObject> implements  LookupListener {

    private final Project p;
    private static final RequestProcessor RP = new RequestProcessor("Refresh APK Nodes", 1);
    final Lookup.Result<ProjectBuildOutput> lookupResult;

    public ApksFilterNodeChildrens(Project p) {
        super(true);
        this.p = p;
        lookupResult = p.getLookup().lookupResult(ProjectBuildOutput.class);
        lookupResult.addLookupListener(WeakListeners.create(LookupListener.class, this, lookupResult));
    }



    @Override
    protected Node[] createNodes(DataObject key) {
        return new Node[]{new FilterNode(key.getNodeDelegate())};
    }


    @Override
    public void resultChanged(LookupEvent le) {
        List<DataObject> dobs = new ArrayList<>();
        Collection<? extends ProjectBuildOutput> allInstances = lookupResult.allInstances();
        if (!allInstances.isEmpty()) {
            ProjectBuildOutput pbo = allInstances.iterator().next();
            Collection<VariantBuildOutput> variantsBuildOutput = pbo.getVariantsBuildOutput();
            for (VariantBuildOutput vbo : variantsBuildOutput) {
                Collection<OutputFile> outputs = vbo.getOutputs();
                for (OutputFile outputFile : outputs) {
                    String outputType = outputFile.getOutputType();
                    if (VariantOutput.MAIN.equals(outputType)) {
                        FileObject fo = FileUtil.toFileObject(outputFile.getOutputFile());
                        if (fo != null) {
                            try {
                                DataObject dob = DataObject.find(fo);
                                if (!dobs.contains(dob)) {
                                    dobs.add(dob);
                                }
                            } catch (DataObjectNotFoundException ex) {
                            }
                        }
                    }
                }
            }
        }
        try {
            FileObject release = FileUtil.createFolder(p.getProjectDirectory(), "release");
            FileObject[] childrens = release.getChildren();
            for (FileObject children : childrens) {
                if ("apk".equals(children.getExt())) {
                    DataObject dob = DataObject.find(children);
                    if (!dobs.contains(dob)) {
                        dobs.add(dob);
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        Collections.sort(dobs, new Comparator<DataObject>() {
            @Override
            public int compare(DataObject o1, DataObject o2) {
                return o1.getPrimaryFile().getName().compareTo(o2.getPrimaryFile().getName());
            }
        });
        Runnable runnable = () -> {
            setKeys(dobs);
        };
        RP.execute(runnable);
    }

}
