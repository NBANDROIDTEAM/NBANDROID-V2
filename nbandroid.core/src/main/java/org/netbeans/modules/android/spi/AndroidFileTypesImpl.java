/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
**/
package org.netbeans.modules.android.spi;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.util.Arrays;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import org.nbandroid.netbeans.gradle.api.AndroidFileTypes;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.android.project.api.AndroidConstants;
import org.netbeans.modules.android.project.query.AndroidClassPathProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provides the Android grammar for any documents whose root elements matches a
 * standard pattern.
 *
 * @author Radim Kubacki
 */
@ServiceProvider(service = AndroidFileTypes.class)
public final class AndroidFileTypesImpl implements AndroidFileTypes {

    private static final Logger LOG = Logger.getLogger(AndroidFileTypesImpl.class.getName());

    public AndroidFileTypesImpl() {
    }

    @Override
    public boolean isLayoutFile(@Nullable final FileObject fo) {
        if (fo == null || !"xml".equals(fo.getExt())) {
            return false;
        }
        Project p = FileOwnerQuery.getOwner(fo);
        if (p.getLookup().lookup(AndroidClassPathProvider.class) == null) {
            return false;
        }
        return Iterables.any(
                Arrays.asList(ProjectUtils.getSources(p).getSourceGroups(AndroidConstants.SOURCES_TYPE_ANDROID_RES)),
                new Predicate<SourceGroup>() {
            @Override
            public boolean apply(SourceGroup t) {
                FileObject folder = fo.getParent();
                if (folder == null) {
                    return false;
                }
                return FileUtil.isParentOf(t.getRootFolder(), fo)
                        && "xml".equals(fo.getExt())
                        && ("layout".equals(folder.getNameExt()) || folder.getNameExt().startsWith("layout-"));
            }
        });
    }
}
