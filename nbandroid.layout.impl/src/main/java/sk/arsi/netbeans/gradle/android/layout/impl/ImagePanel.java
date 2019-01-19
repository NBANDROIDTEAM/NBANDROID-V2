/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.netbeans.gradle.android.layout.impl;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author arsi
 */
public class ImagePanel extends JPanel {

    private BufferedImage image = null;

    public ImagePanel() {
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        setPreferredSize(new Dimension(new Dimension(image.getWidth(), image.getHeight())));
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                revalidate();
            }
        };
        SwingUtilities.invokeLater(runnable);
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.drawImage(image, 0, 0, this); // see javadoc for more info on the parameters
        }

    }

}
