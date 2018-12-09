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
package org.nbandroid.netbeans.gradle.v2.project.template.parameters;

import com.google.common.collect.Lists;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author arsi
 */
@XmlRootElement(name = "globals")
public class Globals {

    @XmlElements({
        @XmlElement(name = "global", type = GlobalInstruction.class)})
    private List<GlobalInstruction> instructions = Lists.newArrayList();

    public List<GlobalInstruction> getInstructions() {
        return instructions;
    }

    public static Globals parse(@NotNull Reader xmlReader) throws JAXBException {
        Globals globals = unmarshal(xmlReader);
        return globals;
    }

    public void addParameters(Map<String, Object> inMap) {
        for (GlobalInstruction instruction : instructions) {
            if (instruction.type != null && !inMap.containsKey(instruction.id)) {
                switch (instruction.type) {
                    case "boolean":
                        inMap.put(instruction.id, Boolean.valueOf(instruction.value));
                        break;
                    case "integer":
                        inMap.put(instruction.id, Integer.valueOf(instruction.value));
                        break;
                    default:
                        inMap.put(instruction.id, instruction.value);
                        break;
                }
            } else if (!inMap.containsKey(instruction.id)) {
                //no type insert as string
                inMap.put(instruction.id, instruction.value);
            }
        }
    }

    private static Globals unmarshal(@NotNull Reader xmlReader) throws JAXBException {
        Unmarshaller unmarshaller = JAXBContext.newInstance(Globals.class).createUnmarshaller();
        unmarshaller.setEventHandler(new ValidationEventHandler() {
            @Override
            public boolean handleEvent(ValidationEvent event) {
                throw new RuntimeException(event.getLinkedException());
            }
        });
        return (Globals) unmarshaller.unmarshal(xmlReader);
    }

    public static final class GlobalInstruction {

        @XmlAttribute(required = true)
        @NotNull
        private String id;

        @XmlAttribute
        @Nullable
        private String type;

        @XmlAttribute
        @Nullable
        private String value;

    }

}
