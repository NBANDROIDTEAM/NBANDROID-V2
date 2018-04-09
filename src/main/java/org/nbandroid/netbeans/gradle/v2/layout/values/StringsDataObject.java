/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nbandroid.netbeans.gradle.v2.layout.values;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.XMLDataObject;

/**
 *
 * @author arsi
 */
//@MIMEResolver.Registration(
//        resource = "StringsResolver.xml",
//        displayName = "Android strings.xml"
//)
//@DataObject.Registration(
//        mimeType = "text/x-android-strings+xml",
//        iconBase = "org/nbandroid/netbeans/gradle/v2/layout/layout.png",
//        displayName = "strings.xml",
//        position = 300
//)
public class StringsDataObject extends XMLDataObject {

    public StringsDataObject(FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
        super(fo, loader);
        Project owner = FileOwnerQuery.getOwner(fo);
        addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println(evt.getPropertyName() + "=" + evt.getNewValue());
            }
        });
    }


}
