/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.layout.values.completion;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Window;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.nbandroid.netbeans.gradle.v2.layout.completion.RankingProvider;
import static org.nbandroid.netbeans.gradle.v2.layout.values.completion.ColorValuesCompletionItem.createImage;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.openide.util.Exceptions;

/**
 *
 * @author arsi
 */
public class BasicColorValuesCompletionItem extends BasicValuesCompletionItem implements BasicValuesCompletionItem.LinkResolver {

    private Document document = null;
    int caretOffset;

    public void setDocument(Document document, int caretOffset) {
        this.document = document;
        this.caretOffset = caretOffset;
    }


    public static String getHTMLColorString(Color color) {
        String red = Integer.toHexString(color.getRed());
        String green = Integer.toHexString(color.getGreen());
        String blue = Integer.toHexString(color.getBlue());
        String alfa = Integer.toHexString(color.getAlpha());

        return ("#"
                + (alfa.length() == 1 ? "0" + alfa : alfa)
                + (red.length() == 1 ? "0" + red : red)
                + (green.length() == 1 ? "0" + green : green)
                + (blue.length() == 1 ? "0" + blue : blue)).toUpperCase();
    }

    public static Color decodeAlfa(String nm) throws NumberFormatException {
        Long i = Long.decode(nm);
        return new Color((int) ((i >> 16) & 0xFF), (int) ((i >> 8) & 0xFF), (int) (i & 0xFF), (int) ((i >> 24) & 0xFF));
    }

    public BasicColorValuesCompletionItem(Color color, String name) {
        super(AndroidValueType.COLOR, name, getHTMLColorString(color), "<a href=\"picker\">Color Picker</a>");
        completionText = getHTMLColorString(color);
        linkResolver = this;

    }

    @Override
    protected ImageIcon getIcon() {
        try {
            Color color = decodeAlfa(value);
            return createImage(color, new Dimension(16, 16));
        } catch (NumberFormatException numberFormatException) {
        }
        return super.getIcon(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CompletionDocumentation resolveLink(String link) {
        if ("picker".equals(link)) {
            Component component = findComponentUnderMouse();
            if (component != null) {
                Color color = JColorChooser.showDialog(component, "Color Picker", decodeAlfa(value));
                if (color != null) {
                    try {
                        BaseDocument document = (BaseDocument) this.document;
                        int startPosition = caretOffset - 1;
                        while ('\"' != (document.getChars(startPosition, 1)[0])) {
                            startPosition--;
                        }
                        startPosition++;
                        document.replace(startPosition, caretOffset - startPosition, getHTMLColorString(color), null);
                        Completion.get().hideAll();
                        RankingProvider.inserted(completionText.hashCode());
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);

                    }
                }
            }
        }
        return null;
    }

    public static Component findComponentUnderMouse() {
        Window window = findWindow();
        Point location = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(location, window);
        return SwingUtilities.getDeepestComponentAt(window, location.x, location.y);
    }

    private static Window findWindow() {
        for (Window window : Window.getWindows()) {
            if (window.getMousePosition(true) != null) {
                return window;
            }
        }

        return null;
    }


}
