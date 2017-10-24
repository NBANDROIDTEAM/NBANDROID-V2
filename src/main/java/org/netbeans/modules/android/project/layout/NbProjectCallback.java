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
 */
package org.netbeans.modules.android.project.layout;

import com.android.ide.common.rendering.LayoutLibrary;
import com.android.ide.common.rendering.api.*;
import com.android.resources.ResourceType;
import com.android.util.Pair;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.android.project.api.AndroidClassPath;
import org.netbeans.modules.android.project.api.ReferenceResolver;
import org.netbeans.modules.android.project.api.ResourceRef;

/**
 *
 * @author radim
 */
public class NbProjectCallback implements IProjectCallback {
  private static final Logger LOG = Logger.getLogger(NbProjectCallback.class.getName());

  // TODO can remove ReferenceResolver from params and get it from project lookup
  public static NbProjectCallback create(LayoutLibrary layoutLib, AndroidClassPath cpProvider, ReferenceResolver refResolver) {
    List<URL> cpUrls = Lists.newArrayList();
    for (ClassPath.Entry en: cpProvider.getClassPath(ClassPath.EXECUTE).entries()) {
      cpUrls.add(en.getURL());
    }
    LOG.log(Level.FINE, "classpath for layout rendering {0}", cpUrls);
    URLClassLoader clzLoader = new URLClassLoader(cpUrls.toArray(new URL[0]), layoutLib.getClassLoader());
    return new NbProjectCallback(clzLoader, refResolver);
  }
  
  private Map<ResourceType, Map<String, Integer>> mIdMap = new EnumMap<>(ResourceType.class);
  private Map<Integer, Pair<ResourceType, String>> mReverseIdMap = new HashMap<>();
  private final ClassLoader projectClzLoader;

  private NbProjectCallback(ClassLoader clzLoader, ReferenceResolver refResolver) {
    for (ResourceRef ref : refResolver.getReferences()) {
      ResourceType resType = ResourceType.getEnum(ref.resourceType);
      if (resType == null) {
        LOG.log(Level.WARNING, "unknown resource type {0}", ref);
        continue;
      }
      if (!(ref.resourceValue instanceof Integer)) {
        // there are some arrays
        continue;
      }
      Map<String, Integer> idValue = mIdMap.get(resType);
      if (idValue == null) {
        idValue = Maps.newHashMap();
        mIdMap.put(resType, idValue);
      }
      Integer value = (Integer) ref.resourceValue;
      idValue.put(ref.resourceName, value);
      mReverseIdMap.put(value, Pair.of(resType, ref.resourceName));
    }
    projectClzLoader = clzLoader;
  }

  @Override
  public Object loadView(String name, Class[] constructorSignature, Object[] constructorArgs)
      throws ClassNotFoundException, Exception {
    LOG.log(Level.FINE, "loadView({0}, {1}, {2})", 
        new Object[] {name, Arrays.toString(constructorSignature), Arrays.toString(constructorArgs)});
    if (projectClzLoader != null) {
      try {
        Class<?> clz = projectClzLoader.loadClass(name);
        LOG.log(Level.FINER, "loaded {0}", name);
        Constructor<?> ctor = clz.getConstructor(constructorSignature);
        ctor.setAccessible(true);
        Object view = ctor.newInstance(constructorArgs);
        LOG.log(Level.FINER, "instance created {0}", view);
        return view;
      } catch (ClassNotFoundException ex) {
        LOG.log(Level.FINE, null, ex);
        throw ex;
      } catch (RuntimeException ex) {
        LOG.log(Level.FINE, null, ex);
        throw new ClassNotFoundException(name, ex);
      }
    }
    throw new ClassNotFoundException(name);
  }

  @Override
  public String getNamespace() {
    LOG.finer("getNamespace ignored");
    // no custom class == no custom attribute, this is not needed.
    return null;
  }

  @Override
  public Pair<ResourceType, String> resolveResourceId(int id) {
    LOG.log(Level.FINE, "resolveResourceId({0})", id);
    return mReverseIdMap.get(id);
  }

  @Override
  public String resolveResourceId(int[] ints) {
    LOG.log(Level.FINE, "resolveResourceId {0} ignored", Arrays.toString(ints));
    // this is needed only when custom views have custom styleable
    return null;
  }

  @Override
  public Integer getResourceId(ResourceType type, String name) {
    LOG.log(Level.FINE, "getResourceId({0}, {1})", new Object[] {type, name});
    // since we don't have access to compiled id, generate one on the fly.
    Map<String, Integer> typeMap = mIdMap.get(type);
    if (typeMap == null) {
      typeMap = new HashMap<>();
      mIdMap.put(type, typeMap);
    }

    Integer value = typeMap.get(name);
    if (value == null) {
      LOG.log(Level.FINE, "need to assing new id to resource ({0}, {1})", new Object[]{type, name});
      
      value = typeMap.size() + 1;
      typeMap.put(name, value);
      mReverseIdMap.put(value, Pair.of(type, name));
    }

    return value;
  }

  @Override
  public ILayoutPullParser getParser(String layoutName) {
    LOG.log(Level.FINE, "ignored getParser {0}", layoutName);
    // don't support custom parser for included files.
    return null;
  }

  @Override
  public ILayoutPullParser getParser(ResourceValue layoutResource) {
    LOG.log(Level.FINE, "ignored getParser {0}", layoutResource);
    // don't support custom parser for included files.
    return null;
  }

  @Override
  public Object getAdapterItemValue(ResourceReference adapterView, Object adapterCookie,
            ResourceReference itemRef,
            int fullPosition, int positionPerType,
            int fullParentPosition, int parentPositionPerType,
            ResourceReference viewRef, ViewAttribute viewAttribute, Object defaultValue) {
    LOG.fine("ignored getAdapterItemValue");
    return null;
  }

  @Override
  public AdapterBinding getAdapterBinding(ResourceReference adapterViewRef, Object adapterCookie,
            Object viewObject) {
    LOG.fine("ignored getAdapterBinding");
    return null;
  }

  @Override
  public ActionBarCallback getActionBarCallback() {
    return new ActionBarHandler();
  }

}
