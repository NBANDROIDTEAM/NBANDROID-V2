/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nbandroid.netbeans.gradle.v2.project.template;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 *
 * @author arsi
 */
public class ImageLabel extends JLabel {

    private Image _myimage;

    public ImageLabel(String text) {
        super(text);
    }

    public ImageLabel() {
    }


    public void setIcon(Icon icon) {
        super.setIcon(icon);
        if (icon instanceof ImageIcon) {
            _myimage = ((ImageIcon) icon).getImage();
        }
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(_myimage, 0, 0, this.getWidth(), this.getHeight(), null);
    }
}
