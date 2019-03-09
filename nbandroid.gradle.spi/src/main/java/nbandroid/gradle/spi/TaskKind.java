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

/**
 *
 * @author arsi
 */
public enum TaskKind {
    /**
     * Defines the most common command type. It is recommended for build, clean,
     * test, etc.
     */
    BUILD,
    /**
     * Defines a command which compiles and executes the project. It is
     * recommended for commands: run and run file.
     */
    RUN,
    /**
     * Defines a command which will allow the user to debug a code. It is
     * recommended for all kinds of debug commands.
     */
    DEBUG,
    /**
     * Defines a command which will list the executed Gradle tasks in the
     * caption of the output window. Note that two different commands with this
     * type may use separate output window.
     * <P>
     * This type is not recommended for built-in commands because it might cause
     * may output windows to be opened which is not user friendly.
     */
    OTHER
}
