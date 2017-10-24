package org.netbeans.modules.android.project.layout;

/**
 * Simple container to hold data about theme.
 */
public final class ThemeData implements Comparable<ThemeData> {
  public final String themeName;
  public final boolean isProjectTheme;

  public ThemeData(String themeName, boolean isProjectTheme) {
    this.themeName = themeName;
    this.isProjectTheme = isProjectTheme;
  }

  @Override
  public String toString() {
    return "ThemeData{" + "themeName=" + themeName + ", isProjectTheme=" + isProjectTheme + '}';
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 67 * hash + (this.themeName != null ? this.themeName.hashCode() : 0);
    hash = 67 * hash + (this.isProjectTheme ? 1 : 0);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ThemeData other = (ThemeData) obj;
    if ((this.themeName == null) ? (other.themeName != null) : !this.themeName.equals(other.themeName)) {
      return false;
    }
    if (this.isProjectTheme != other.isProjectTheme) {
      return false;
    }
    return true;
  }

  @Override
  public int compareTo(ThemeData o) {
    if (isProjectTheme) {
      if (!o.isProjectTheme) {
        return -1;
      }
    } else {
      if (o.isProjectTheme) {
        return 1;
      }
    }
    return themeName.compareTo(o.themeName);
  }
}
