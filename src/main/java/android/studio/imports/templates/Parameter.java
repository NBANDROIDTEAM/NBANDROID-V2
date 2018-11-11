/*
 * Copyright (C) 2013 The Android Open Source Project
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
package android.studio.imports.templates;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.io.File;
import java.util.*;
import org.openide.util.Exceptions;

/**
 * Parameter represents an external input to a template. It consists of an ID used to refer to it within the template,
 * human-readable information to be displayed in the UI, and type and validation specifications that can be used in the UI to assist in
 * data entry.
 */
public final class Parameter {
  private static final String URI_AUTHORITY_REGEX = "[a-zA-Z][a-zA-Z0-9-_.]*(:\\d+)?";

  public enum Type {
    STRING,
    BOOLEAN,
    ENUM,
    SEPARATOR;

    public static Type get(@NotNull String name) {
      name = name.toUpperCase(Locale.US);
      try {
        return valueOf(name);
      }
      catch (IllegalArgumentException e) {
          Exceptions.printStackTrace(e);
      }

      return STRING;
    }
  }

  /**
   * Constraints that can be applied to a parameter which helps the UI add a
   * validator etc for user input. These are typically combined into a set
   * of constraints via an EnumSet.
   */
  public enum Constraint {
    /**
     * This value must be unique. This constraint usually only makes sense
     * when other constraints are specified, such as {@link #LAYOUT}, which
     * means that the parameter should designate a name that does not
     * represent an existing layout resource name
     */
    UNIQUE,

    /**
     * This value must already exist. This constraint usually only makes sense
     * when other constraints are specified, such as {@link #LAYOUT}, which
     * means that the parameter should designate a name that already exists as
     * a resource name.
     */
    EXISTS,

    /** The associated value must not be empty */
    NONEMPTY,

    /** The associated value is allowed to be empty */
    EMPTY,

    /** The associated value should represent a fully qualified activity class name */
    ACTIVITY,

    /** The associated value should represent an API level */
    APILEVEL,

    /** The associated value should represent a valid class name */
    CLASS,

    /** The associated value should represent a valid package name */
    PACKAGE,

    /** The associated value should represent a valid Android application package name */
    APP_PACKAGE,

    /** The associated value should represent a valid Module name */
    MODULE,

    /** The associated value should represent a valid layout resource name */
    LAYOUT,

    /** The associated value should represent a valid drawable resource name */
    DRAWABLE,

    /** The associated value should represent a valid values file name */
    VALUES,

    /** The associated value should represent a valid id resource name */
    ID,

    /** The associated value should represent a valid source directory name */
    SOURCE_SET_FOLDER,

    /** The associated value should represent a valid string resource name */
    STRING,

    /** */
    URI_AUTHORITY;

    public static Constraint get(@NotNull String name) {
      name = name.toUpperCase(Locale.US);
      try {
        return valueOf(name);
      }
      catch (IllegalArgumentException e) {
          Exceptions.printStackTrace(e);
      }

      return NONEMPTY;
    }
  }

  public static final EnumSet<Constraint> TYPE_CONSTRAINTS = EnumSet
    .of(Constraint.ACTIVITY, Constraint.APILEVEL, Constraint.CLASS, Constraint.PACKAGE, Constraint.APP_PACKAGE, Constraint.MODULE,
        Constraint.LAYOUT, Constraint.DRAWABLE, Constraint.ID, Constraint.SOURCE_SET_FOLDER, Constraint.STRING, Constraint.URI_AUTHORITY);

  /** The template defining the parameter */
  public final TemplateMetadata template;

  /** The type of parameter */
  @NotNull
  public final Type type;

  /** The unique id of the parameter (not displayed to the user) */
  @Nullable
  public final String id;

  /** The display name for this parameter */
  @Nullable
  public final String name;

  /**
   * The initial value for this parameter (see also {@link #suggest} for more
   * dynamic defaults
   */
  @Nullable
  public final String initial;

  /**
   * A template expression using other template parameters for producing a
   * default value based on other edited parameters, if possible.
   */
  @Nullable
  public final String suggest;

  /**
   * A template expression using other template parameters for dynamically changing
   * the visibility of this parameter to the user.
   */
  @Nullable
  public final String visibility;

  /**
   * A template expression using other template parameters for dynamically changing
   * whether this parameter is enabled for the user.
   */
  @Nullable
  public final String enabled;

  /** Help for the parameter, if any */
  @Nullable
  public final String help;

  /** The element defining this parameter */
  @NotNull
  public final Element element;

  /** The constraints applicable for this parameter */
  @NotNull
  public final EnumSet<Constraint> constraints;

  Parameter(@NotNull TemplateMetadata template, @NotNull Element parameter) {
    this.template = template;
    element = parameter;

      String typeName = parameter.getAttribute(Template.ATTR_TYPE);
      assert typeName != null && !typeName.isEmpty() : Template.ATTR_TYPE;
    type = Type.get(typeName);

      id = parameter.getAttribute(Template.ATTR_ID);
      initial = parameter.getAttribute(Template.ATTR_DEFAULT);
      suggest = parameter.getAttribute(Template.ATTR_SUGGEST);
      visibility = parameter.getAttribute(Template.ATTR_VISIBILITY);
      enabled = parameter.getAttribute(Template.ATTR_ENABLED);
      name = parameter.getAttribute(Template.ATTR_NAME);
      help = parameter.getAttribute(Template.ATTR_HELP);
      String constraintString = parameter.getAttribute(Template.ATTR_CONSTRAINTS);
    if (constraintString != null && !constraintString.isEmpty()) {
      List<Constraint> constraintsList = Lists.newArrayListWithExpectedSize(1);
      for (String s : Splitter.on('|').omitEmptyStrings().split(constraintString)) {
        constraintsList.add(Constraint.get(s));
      }
      constraints = EnumSet.copyOf(constraintsList);
    } else {
      constraints = EnumSet.noneOf(Constraint.class);
    }
  }

    @NotNull
  private static String getNameWithoutExtensions(@NotNull File f) {
    if (f.getName().indexOf('.') == -1) {
      return f.getName();
    } else {
      return f.getName().substring(0, f.getName().indexOf('.'));
    }
  }


  public boolean isRelated(Parameter p) {
    Set<Parameter.Constraint> types = Sets.intersection(Sets.intersection(p.constraints, constraints), TYPE_CONSTRAINTS);
    return !types.isEmpty();
  }

  @Override
  public String toString() {
    return "(parameter id: " + id + ")";
  }
}