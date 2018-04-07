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

/**
 *
 * @author arsi
 */
public enum AndroidStyleableType implements Serializable {
    ToBeDetermined,
    Widget,
    Layout,
    LayoutParams,
    Other;

    public static final AndroidStyleableType decode(String line) {
        switch (line.charAt(0)) {
            case 'L':
                return AndroidStyleableType.Layout;
            case 'P':
                return AndroidStyleableType.LayoutParams;
            case 'W':
                return AndroidStyleableType.Widget;
        }
        return AndroidStyleableType.ToBeDetermined;
    }
}
