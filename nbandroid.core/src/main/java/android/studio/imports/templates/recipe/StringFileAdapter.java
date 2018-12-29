/*
 * Copyright (C) 2015 The Android Open Source Project
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
package android.studio.imports.templates.recipe;

import com.android.utils.XmlUtils;
import java.io.File;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * JAXB adapter that converts string values to Files.
 */
public final class StringFileAdapter extends XmlAdapter<String, File> {

    @Override
    public File unmarshal(String s) throws Exception {
        String unescapedString = XmlUtils.fromXmlAttributeValue(s);
        return new File(toSystemDependentName(unescapedString, File.separatorChar));
    }

    @Override
    public String marshal(File file) throws Exception {
        throw new UnsupportedOperationException("File -> String marshalling should not be needed");
    }

    public static String toSystemDependentName(String fileName, final char separatorChar) {
        return fileName.replace('/', separatorChar).replace('\\', separatorChar);
    }
}
