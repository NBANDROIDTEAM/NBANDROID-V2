/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package org.netbeans.modules.android.project.layout;

import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author radim
 */
class RenderingException extends Exception {

  public RenderingException(Throwable t) {
    super(t);
  }

  public RenderingException(String msg) {
    super(msg);
  }

  public RenderingException() {
    super();
  }

}
