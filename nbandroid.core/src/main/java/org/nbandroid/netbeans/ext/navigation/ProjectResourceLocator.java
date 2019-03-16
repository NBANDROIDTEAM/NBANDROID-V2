package org.nbandroid.netbeans.ext.navigation;

import com.android.resources.ResourceType;
import static com.android.resources.ResourceType.DRAWABLE;
import static com.android.resources.ResourceType.ID;
import static com.android.resources.ResourceType.LAYOUT;
import static com.android.resources.ResourceType.STRING;
import static com.android.resources.ResourceType.STYLEABLE;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.android.project.api.AndroidConstants;
import org.nbandroid.netbeans.gradle.api.ResourceRef;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;

/**
 *
 * @author radim
 */
@Deprecated
public class ProjectResourceLocator implements ResourceLocator {

    private static final Logger LOG = Logger.getLogger(ProjectResourceLocator.class.getName());

    private final Project prj;

    public ProjectResourceLocator(Project prj) {
        this.prj = prj;
    }

    @Override
    public ResourceLocation findResourceLocation(final ResourceRef resRef) {
        if (resRef == null || !isMatchingProject(resRef)) {
            return null;
        }
        SourceGroup[] resSG = ProjectUtils.getSources(prj).getSourceGroups(AndroidConstants.SOURCES_TYPE_ANDROID_RES);
        ResourceType res = ResourceType.getEnum(resRef.resourceType);
        if (res == null) {
            LOG.log(Level.FINE, "Unknown resource type {0}.", resRef.resourceType);
            return null;
        }
        switch (res) {
            case LAYOUT:
                return findResourceFile(resSG, resRef.resourceName, "layout");
            case XML:
                return findResourceFile(resSG, resRef.resourceName, "xml");
            case COLOR:
                ResourceLocation colorPos = findResourcePosition(
                        ResourceType.COLOR, resRef.resourceName,
                        "values");
                if (colorPos == null) {
                    LOG.log(Level.FINE, "Color not found for {0}.", resRef.resourceName);
                    return null;
                }
                return colorPos;
            case DRAWABLE:
                FileObject drawableFile = Iterables.find(
                        Iterables.transform(
                                Arrays.asList(resSG),
                                new Function<SourceGroup, FileObject>() {
                            @Override
                            public FileObject apply(SourceGroup sg) {
                                FileObject imgFile = sg.getRootFolder().getFileObject("drawable/" + resRef.resourceName + ".png");
                                if (imgFile == null) {
                                    imgFile = sg.getRootFolder().getFileObject("drawable/" + resRef.resourceName + ".jpg");
                                }
                                return imgFile;
                            }
                        }),
                        Predicates.notNull(),
                        null);
                if (drawableFile != null) {
                    return new ResourceLocation(drawableFile, -1);
                }
                ResourceLocation drawablePos = findResourcePosition(
                        ResourceType.DRAWABLE, resRef.resourceName,
                        "values");
                if (drawablePos == null) {
                    LOG.log(Level.FINE, "Drawable not found for {0}.", resRef.resourceName);
                    return null;
                }
                return drawablePos;
            case ID:
                // TODO also menu
                ResourceLocation resPos = findResourcePosition(
                        ResourceType.ID, resRef.resourceName,
                        "layout");
                if (resPos == null) {
                    LOG.log(Level.FINE, "ID definition not found for {0}.", resRef.resourceName);
                    return null;
                }
                return resPos;
            case STRING:
                ResourceLocation strPos = findResourcePosition(
                        ResourceType.STRING, resRef.resourceName,
                        "values");
                if (strPos == null) {
                    LOG.log(Level.FINE, "string definition not found for {0}.", resRef.resourceName);
                    return null;
                }
                return strPos;
            case STYLEABLE:
                ResourceLocation styleablePos = findResourcePosition(
                        ResourceType.STYLEABLE, resRef.resourceName,
                        "values");
                if (styleablePos == null) {
                    LOG.log(Level.FINE, "styleable definition not found for {0}.", resRef.resourceName);
                    return null;
                }
                return styleablePos;
        }
        LOG.log(Level.FINE, "Unsupported resource type {0}.", resRef.resourceType);

        return null;
    }

    private ResourceLocation findResourceFile(SourceGroup[] resSG, final String value, final String folderName) {
        FileObject resFile = Iterables.find(
                Iterables.transform(
                        Arrays.asList(resSG),
                        new Function<SourceGroup, FileObject>() {
                    @Override
                    public FileObject apply(SourceGroup sg) {
                        return sg.getRootFolder().getFileObject(folderName + "/" + value + ".xml");
                    }
                }),
                Predicates.notNull(),
                null);
        if (resFile == null) {
            LOG.log(Level.FINE, "Resource file {0} not found for {0}.", value);
            return null;
        }
        return new ResourceLocation(resFile, -1);
    }

    private ResourceLocation findResourcePosition(final ResourceType res, final String value, final String folderName) {
        SourceGroup[] resSG = ProjectUtils.getSources(prj).getSourceGroups(AndroidConstants.SOURCES_TYPE_ANDROID_RES);
        return Iterables.find(
                Iterables.transform(
                        Iterables.concat(Iterables.transform(
                                Arrays.asList(resSG),
                                new Function<SourceGroup, Iterable<FileObject>>() {

                            @Override
                            public Iterable<FileObject> apply(SourceGroup f) {
                                FileObject folder = f.getRootFolder().getFileObject(folderName);
                                return folder != null
                                        ? Lists.newArrayList(folder.getChildren())
                                        : Collections.<FileObject>emptyList();
                            }
                        })),
                        new Function<FileObject, ResourceLocation>() {
                    @Override
                    public ResourceLocation apply(FileObject f) {
                        Map<String, ResourceLocation> idResources = parseResources(f).get(res);
                        return idResources != null ? idResources.get(value) : null;
                    }
                }),
                Predicates.<ResourceLocation>notNull(),
                null);
    }

    private static Map<ResourceType, Map<String, ResourceLocation>> parseResources(final FileObject fo) {
        final Map<ResourceType, Map<String, ResourceLocation>> result
                = new EnumMap<>(ResourceType.class);
        try {
            XMLReader reader = XMLUtil.createXMLReader();

            DefaultHandler2 handler = new DefaultHandler2() {
                private Locator locator;
                Stack<String> elements = new Stack<>();
                String currentStyleableName = null;

                @Override
                public void setDocumentLocator(Locator locator) {
                    this.locator = locator;
                }

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
                    boolean scanStrings = !elements.empty() && "resources".equals(elements.peek()) && "string".equals(qName);
                    boolean scanColor = !elements.empty() && "resources".equals(elements.peek()) && "color".equals(qName);
                    boolean scanDrawable = !elements.empty() && "resources".equals(elements.peek()) && "drawable".equals(qName);
                    boolean scanStyleable = !elements.empty() && "resources".equals(elements.peek()) && "declare-styleable".equals(qName);
                    boolean scanStyleableAttrs = !elements.empty() && "declare-styleable".equals(elements.peek()) && "attr".equals(qName);
                    for (int i = 0; i < attrs.getLength(); i++) {
                        if ("android:id".equals(attrs.getQName(i))) {
                            String id = attrs.getValue(i);
                            if (id != null && id.startsWith("@+id/")) {
                                id = id.substring("@+id/".length());
                                Map<String, ResourceLocation> ids = result.get(ResourceType.ID);
                                if (ids == null) {
                                    ids = Maps.newHashMap();
                                    result.put(ResourceType.ID, ids);
                                }
                                ids.put(id, new ResourceLocation(fo, locator.getLineNumber() - 1));
                            }
                        }
                        if (scanStrings) {
                            checkForRef(ResourceType.STRING, attrs, i);
                        }
                        if (scanColor) {
                            checkForRef(ResourceType.COLOR, attrs, i);
                        }
                        if (scanDrawable) {
                            checkForRef(ResourceType.DRAWABLE, attrs, i);
                        }
                        if (scanStyleable && "name".equals(attrs.getQName(i))) {
                            String id = attrs.getValue(i);
                            if (id != null) {
                                Map<String, ResourceLocation> ids = result.get(ResourceType.STYLEABLE);
                                if (ids == null) {
                                    ids = Maps.newHashMap();
                                    result.put(ResourceType.STYLEABLE, ids);
                                }
                                ids.put(id, new ResourceLocation(fo, locator.getLineNumber() - 1));
                            }
                            currentStyleableName = id;
                        }
                        if (scanStyleableAttrs && "name".equals(attrs.getQName(i))) {
                            String id = currentStyleableName + '_' + attrs.getValue(i);
                            if (id != null) {
                                Map<String, ResourceLocation> ids = result.get(ResourceType.STYLEABLE);
                                if (ids == null) {
                                    ids = Maps.newHashMap();
                                    result.put(ResourceType.STYLEABLE, ids);
                                }
                                ids.put(id, new ResourceLocation(fo, locator.getLineNumber() - 1));
                            }
                            currentStyleableName = id;
                        }
                    }
                    elements.push(qName);
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                    elements.pop();
                }

                private void checkForRef(ResourceType type, Attributes attrs, int i) {
                    if ("name".equals(attrs.getQName(i))) {
                        String id = attrs.getValue(i);
                        if (id != null) {
                            Map<String, ResourceLocation> ids = result.get(type);
                            if (ids == null) {
                                ids = Maps.newHashMap();
                                result.put(type, ids);
                            }
                            ids.put(id, new ResourceLocation(fo, locator.getLineNumber() - 1));
                        }
                    }
                }
            };
            reader.setContentHandler(handler);

            InputStream is = fo.getInputStream();
            reader.parse(new InputSource(is));
            is.close();

        } catch (SAXException | IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return result;
    }

    private boolean isMatchingProject(ResourceRef ref) {
        return ref.samePackage;
    }
}
