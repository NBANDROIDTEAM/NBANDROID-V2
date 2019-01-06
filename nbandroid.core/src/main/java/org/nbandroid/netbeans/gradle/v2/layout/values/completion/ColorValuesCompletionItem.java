/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.layout.values.completion;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import static org.nbandroid.netbeans.gradle.v2.layout.values.completion.BasicColorValuesCompletionItem.decodeAlfa;

/**
 *
 * @author arsi
 */
public class ColorValuesCompletionItem extends BasicValuesCompletionItem {

    protected ColorValuesCompletionItem(AndroidValueType type, String name, String value, String comment) {
        super(type, name, value, comment);

    }

    @Override
    protected String getDoc() {
        try {
            Color color = decodeAlfa(value);
        } catch (NumberFormatException numberFormatException) {
            return super.getDoc();
        }
        return super.getDoc(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected ImageIcon getIcon() {
        try {
            Color color = null;
            if (value.length() == 9) {
                color = decodeAlfa(value);
            } else {
                color = decodeAlfa(value.replace("#", "#FF"));
            }
            return createImage(color, new Dimension(16, 16));
        } catch (NumberFormatException numberFormatException) {
        }
        return super.getIcon(); //To change body of generated methods, choose Tools | Templates.
    }

    public Color getColor() {
        Color color = null;
        if (value.length() == 9) {
            color = decodeAlfa(value);
        } else {
            color = decodeAlfa(value.replace("#", "#FF"));
        }
        return color;
    }

    public static ImageIcon createImage(Color c, Dimension size) {
        BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.createGraphics();
        g.setColor(c);
        g.fillRect(0, 0, size.width, size.height);
        return new ImageIcon(img);
    }

}
