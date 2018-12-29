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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author arsi
 */
public enum AndroidStyleableAttrType implements Serializable {
    Boolean,
    Color,
    Reference,
    Float,
    Dimension,
    Flag,
    Integer,
    String,
    Locale,
    Enum,
    Fraction,
    Unknown;

    public static List<AndroidStyleableAttrType> decode(String txt) {

        List<AndroidStyleableAttrType> tmp = new ArrayList<>();
        if (txt.contains("|")) {
            StringTokenizer tok = new StringTokenizer(txt, "|", false);
            while (tok.hasMoreElements()) {
                tmp.add(decodeSingle(tok.nextToken()));
            }
            return tmp;
        } else {
            tmp.add(decodeSingle(txt));
            return tmp;
        }
    }

    public static AndroidStyleableAttrType decodeSingle(String txt) {
        switch (txt) {
            case "boolean":
                return Boolean;
            case "color":
                return Color;
            case "reference":
                return Reference;
            case "float":
                return Float;
            case "dimension":
                return Dimension;
            case "flag":
                return Flag;
            case "integer":
                return Integer;
            case "string":
                return String;
            case "enum":
                return Enum;
            case "fraction":
                return Fraction;
            case "locale":
                return Locale;
            default:
                return Unknown;
        }
    }

}
