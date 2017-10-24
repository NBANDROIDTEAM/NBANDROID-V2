package org.netbeans.modules.android.project.ui.layout;

import com.google.common.base.Objects;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

public class ImagePanel extends JPanel {
  private static final Logger LOG = Logger.getLogger(ImagePanel.class.getName());
  
  private BufferedImage image;
  
  private boolean zoomToFit = true;

  public void setImage(BufferedImage image) {
    BufferedImage old = this.image;
    this.image = image;
    if (!Objects.equal(old, image)) {
      revalidate();
      updateSize();
      repaint();
    }
  }
  
  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (image == null) {
      return;
    }
    Dimension paintedDimension = getScaledDimension(getParent().getSize(), new Dimension(image.getWidth(), image.getHeight()));
    final Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.drawImage(image, 0, 0, paintedDimension.width, paintedDimension.height, 0, 0, image.getWidth(), image.getHeight(), null);
  }

  public void updateSize() {
    if (image == null || getParent() == null) {
      setSize(0, 0);
      return;
    }
    Dimension parentSize = getParent().getSize();
    setSize(getScaledDimension(parentSize, new Dimension(image.getWidth(), image.getHeight())));
//        Math.min(image.getWidth(), parentSize.width), 
//        Math.min(image.getHeight(), parentSize.height));
    LOG.log(Level.FINER, "setSize {0}, parent {1}", new Object[]{getSize(), parentSize});
  }
  
  private Dimension getScaledDimension(Dimension compSize, Dimension imageSize) {
    if (zoomToFit) {
      if (imageSize.getWidth() == 0 || imageSize.getHeight() == 0) {
        return new Dimension(imageSize);
      }
      if (imageSize.getWidth() <= compSize.getWidth() && imageSize.getHeight() <= compSize.getHeight()) {
        return new Dimension(imageSize);
      }
      double scaleWidth = compSize.getWidth() / imageSize.getWidth();
      double scaleHeight = compSize.getHeight() / imageSize.getHeight();
      double scaleFactor = Math.min(scaleWidth, scaleHeight);
      Dimension result = new Dimension((int) (imageSize.width * scaleFactor), (int) (imageSize.height * scaleFactor));
      LOG.log(Level.FINER, "scaled image {0}, parent {1}, result", new Object[]{imageSize, compSize, result});
      return result;
    }
    return new Dimension(imageSize);
  }
}
