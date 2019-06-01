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
package com.junichi11.netbeans.modules.color.codes.preview.api;

/**
 * Represent the offset range.
 *
 * @author junichi11
 */
public final class OffsetRange {

    private final int startOffset;
    private final int endOffset;

    /**
     * Constructor.
     *
     * @since 0.11.1
     * @param startOffset the start offset
     * @param endOffset the end offset
     */
    public OffsetRange(int startOffset, int endOffset) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        if (startOffset > endOffset) {
            throw new IllegalArgumentException("The end offset must be greater than or equal to the start offset (start offset: " // NOI18N
                    + startOffset + ", end offset: " + endOffset + ")"); // NOI18N
        }
    }

    /**
     * Get start offset.
     *
     * @since 0.11.1
     * @return the start offset
     */
    public int getStartOffset() {
        return startOffset;
    }

    /**
     * Get end offset.
     *
     * @since 0.11.1
     * @return the end offset
     */
    public int getEndOffset() {
        return endOffset;
    }

    @Override
    public String toString() {
        return "start offset: " + startOffset + ", end offset: " + endOffset; // NOI18N
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OffsetRange other = (OffsetRange) obj;
        if (this.startOffset != other.startOffset) {
            return false;
        }
        return this.endOffset == other.endOffset;
    }

}
