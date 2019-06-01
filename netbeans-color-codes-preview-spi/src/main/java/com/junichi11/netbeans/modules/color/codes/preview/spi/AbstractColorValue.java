/*
 * Copyright 2019 junichi11.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.junichi11.netbeans.modules.color.codes.preview.spi;

import com.junichi11.netbeans.modules.color.codes.preview.api.OffsetRange;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Abstract ColorValue implementation.
 *
 * @see ColorValue
 * @author junichi11
 */
public abstract class AbstractColorValue implements ColorValue {

    private final int line;
    private final int startOffset;
    private final int endOffset;
    private final String value;

    /**
     * Constructor.
     *
     * @since 0.11.1
     * @param value the color value
     * @param offsetRange the offset range
     * @param line the line number
     */
    public AbstractColorValue(@NonNull String value, OffsetRange offsetRange, int line) {
        this.value = value;
        this.startOffset = offsetRange.getStartOffset();
        this.endOffset = offsetRange.getEndOffset();
        this.line = line;
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

}
