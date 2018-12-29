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

import com.android.manifmerger.*;
import com.android.utils.StdLogger;
import com.android.utils.XmlUtils;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import freemarker.template.TemplateException;
import java.io.*;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;

/**
 * Utility class to support the recipe.xml merge instruction.
 */
public class RecipeMergeUtils {

    private static final String MERGE_ATTR_STRATEGY = "templateMergeStrategy";
    private static final String MERGE_ATTR_STRATEGY_REPLACE = "replace";
    private static final String MERGE_ATTR_STRATEGY_PRESERVE = "preserve";

    /**
     * Finds include ':module_name_1', ':module_name_2',... statements in
     * settings.gradle files
     */
    private static final Pattern INCLUDE_PATTERN = Pattern.compile("(^|\\n)\\s*include +(':[^']+', *)*':[^']+'");

    public static String mergeGradleSettingsFile(@NotNull String source, @NotNull String dest) throws IOException, TemplateException {
        // TODO: Right now this is implemented as a dumb text merge. It would be much better to read it into PSI using IJ's Groovy support.
        // If Gradle build files get first-class PSI support in the future, we will pick that up cheaply. At the moment, Our Gradle-Groovy
        // support requires a project, which we don't necessarily have when instantiating a template.

        StringBuilder contents = new StringBuilder(dest);

        for (String line : Splitter.on('\n').omitEmptyStrings().trimResults().split(source)) {
            if (!line.startsWith("include")) {
                throw new RuntimeException("When merging settings.gradle files, only include directives can be merged.");
            }
            line = line.substring("include".length()).trim();

            Matcher matcher = INCLUDE_PATTERN.matcher(contents);
            if (matcher.find()) {
                contents.insert(matcher.end(), ", " + line);
            } else {
                contents.insert(0, "include " + line + System.lineSeparator());
            }
        }
        return contents.toString();
    }

    /**
     * Merges sourceXml into targetXml/targetFile (targetXml is the contents of
     * targetFile). Returns the resulting xml if it still needs to be written to
     * targetFile, or null if the file has already been/doesn't need to be
     * updated.
     */
    public static String mergeXml(File moduleRoot, String sourceXml, String targetXml, File targetFile) {
        String contents = null;
        Document currentDocument = XmlUtils.parseDocumentSilently(targetXml, true);
            assert currentDocument != null : targetXml + " failed to parse";
            Document fragment = XmlUtils.parseDocumentSilently(sourceXml, true);
            assert fragment != null : sourceXml + " failed to parse";
            MergingReport report = mergeManifest(moduleRoot, targetFile, targetXml, sourceXml);
            if (report != null && report.getResult().isSuccess()) {
                contents = report.getMergedDocument(MergingReport.MergedManifestKind.MERGED);
            } else {
                contents = null;
        }

        return contents;
    }

    /**
     * Merges the given manifest fragment into the given manifest file
     */
    @Nullable
    private static MergingReport mergeManifest(@NotNull File moduleRoot, @NotNull final File targetManifest,
            @NotNull final String targetXml, @NotNull final String mergeText) {
        try {
            boolean isMasterManifest = filesEqual(moduleRoot, targetManifest.getParentFile());
            //noinspection SpellCheckingInspection
            final File tempFile2 = new File(targetManifest.getParentFile(), "nevercreated.xml");
            StdLogger logger = new StdLogger(StdLogger.Level.INFO);
            return ManifestMerger2.newMerger(targetManifest, logger, ManifestMerger2.MergeType.APPLICATION)
                    .withFeatures(ManifestMerger2.Invoker.Feature.EXTRACT_FQCNS,
                            ManifestMerger2.Invoker.Feature.HANDLE_VALUE_CONFLICTS_AUTOMATICALLY,
                            ManifestMerger2.Invoker.Feature.NO_PLACEHOLDER_REPLACEMENT)
                    .addLibraryManifest(tempFile2)
                    .asType(isMasterManifest ? XmlDocument.Type.MAIN : XmlDocument.Type.OVERLAY)
                    .withFileStreamProvider(new ManifestMerger2.FileStreamProvider() {
                        @Override
                        protected InputStream getInputStream(@NotNull File file) throws FileNotFoundException {
                            String text = filesEqual(file, targetManifest) ? targetXml : mergeText;
                            return new ByteArrayInputStream(text.getBytes(Charsets.UTF_8));
                        }
                    })
                    .merge();
        } catch (ManifestMerger2.MergeFailureException e) {
            return null;
        }
    }

    public static boolean filesEqual(@Nullable File file1, @Nullable File file2) {
        // on MacOS java.io.File.equals() is incorrectly case-sensitive
        return Objects.equals(file1, file2);
    }

}
