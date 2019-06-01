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
package org.nbandroid.netbeans.gradle.v2.color.preview;

import com.junichi11.netbeans.modules.color.codes.preview.spi.ColorCodeFormatter;
import com.junichi11.netbeans.modules.color.codes.preview.spi.ColorCodesProvider;
import com.junichi11.netbeans.modules.color.codes.preview.spi.ColorValue;
import java.awt.Color;
import org.netbeans.api.annotations.common.NonNull;

/**
 *
 * @author arsi
 */
public class ReadOnlyColorValue implements ColorValue {

    private final int line;
    private final int startOffset;
    private final int endOffset;
    private final String value;
    private final ColorCodesProvider codesProvider;
    private final Color color;

    public ReadOnlyColorValue(@NonNull ColorCodesProvider codesProvider, Color color, @NonNull String value, int startOffset, int endOffset, int line) {
        this.value = value;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.line = line;
        this.codesProvider = codesProvider;
        this.color = color;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getStartOffset() {
        return startOffset;
    }

    @Override
    public int getEndOffset() {
        return endOffset;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public Color getColor() {
        return color;
    }


    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public ColorCodeFormatter getFormatter() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
