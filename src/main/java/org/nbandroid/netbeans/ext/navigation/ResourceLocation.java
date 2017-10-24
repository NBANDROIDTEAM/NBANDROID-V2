package org.nbandroid.netbeans.ext.navigation;

import org.openide.filesystems.FileObject;

/**
 *
 * @author radim
 */
public class ResourceLocation {
  public final FileObject resource;
  public final int line;

  public ResourceLocation(FileObject resource, int line) {
    this.resource = resource;
    this.line = line;
  }

  @Override
  public String toString() {
    return "ResourcePosition{" + "resource=" + resource + ", line=" + line + '}';
  }
    
}
